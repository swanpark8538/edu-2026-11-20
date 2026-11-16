package egovframework.example.board.web;

import static org.assertj.core.api.Assertions.assertThat;
import static org.hamcrest.Matchers.containsString;
import static org.hamcrest.Matchers.not;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.multipart;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.header;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.model;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import java.util.stream.Stream;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;

@SpringBootTest
@AutoConfigureMockMvc
class BoardControllerTest {

    @TempDir
    static Path uploadDir;

    @DynamicPropertySource
    static void properties(DynamicPropertyRegistry registry) {
        registry.add("Globals.fileStorePath", () -> uploadDir.toString());
    }

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private JdbcTemplate jdbcTemplate;

    private static MockMultipartFile file(String name, String body) {
        return new MockMultipartFile("files", name, "application/octet-stream", body.getBytes(StandardCharsets.UTF_8));
    }

    private String register(String title, String writer, MockMultipartFile... files) throws Exception {
        var request = multipart("/board/register.do");
        for (MockMultipartFile f : files) {
            request.file(f);
        }
        MvcResult result = mockMvc.perform(request
                        .param("title", title)
                        .param("content", "내용 " + title)
                        .param("writer", writer)
                        .with(csrf()))
                .andExpect(status().is3xxRedirection())
                .andReturn();
        String location = result.getResponse().getRedirectedUrl();
        assertThat(location).contains("/board/update.do").contains("boardId=BOARD_");
        return location.replaceAll(".*boardId=(BOARD_\\d{14}).*", "$1");
    }

    private String atchFileIdOf(String boardId) {
        return jdbcTemplate.queryForObject("SELECT ATCH_FILE_ID FROM BOARD WHERE BOARD_ID = ?", String.class, boardId);
    }

    private long storedFileCount() throws Exception {
        try (Stream<Path> files = Files.list(uploadDir)) {
            return files.count();
        }
    }

    @Test
    void listPageUsesKrdsAndSecurityHeaders() throws Exception {
        mockMvc.perform(get("/board/list.do"))
                .andExpect(status().isOk())
                .andExpect(content().string(containsString("/krds/css/component/output.css")))
                .andExpect(content().string(containsString("krds-table-wrap")))
                .andExpect(content().string(containsString("krds-pagination")))
                .andExpect(header().string("X-Frame-Options", "DENY"))
                .andExpect(header().string("X-Content-Type-Options", "nosniff"))
                .andExpect(header().exists("Content-Security-Policy"));
    }

    @Test
    void rootRedirectsToList() throws Exception {
        mockMvc.perform(get("/"))
                .andExpect(status().is3xxRedirection());
    }

    @Test
    void postWithoutCsrfTokenIsRejected() throws Exception {
        mockMvc.perform(post("/board/register.do").param("title", "t").param("writer", "w"))
                .andExpect(status().isForbidden());
    }

    @Test
    void registerViewDownloadAndAppendFilesKeepsSameGroup() throws Exception {
        String boardId = register("첨부 테스트", "홍길동", file("보고서.txt", "hello"), file("data.csv", "a,b"));
        String atchFileId = atchFileIdOf(boardId);
        assertThat(atchFileId).startsWith("FILE_").hasSize(20);

        mockMvc.perform(get("/board/update.do").param("boardId", boardId))
                .andExpect(status().isOk())
                .andExpect(content().string(containsString("보고서.txt")))
                .andExpect(content().string(containsString("data.csv")))
                .andExpect(content().string(containsString("/cmm/fms/fileDown.do?atchFileId=" + atchFileId + "&amp;fileSn=1")));

        mockMvc.perform(get("/cmm/fms/fileDown.do").param("atchFileId", atchFileId).param("fileSn", "1"))
                .andExpect(status().isOk())
                .andExpect(header().string("Content-Disposition", containsString("attachment")))
                .andExpect(header().string("Content-Disposition", containsString("UTF-8''%EB%B3%B4%EA%B3%A0%EC%84%9C.txt")))
                .andExpect(content().string("hello"));

        mockMvc.perform(multipart("/board/update.do").file(file("추가.pdf", "pdf"))
                        .param("boardId", boardId)
                        .param("title", "첨부 테스트 수정")
                        .param("content", "수정 내용")
                        .param("writer", "홍길동")
                        .param("atchFileId", "FILE_999999999999999")
                        .with(csrf()))
                .andExpect(status().is3xxRedirection());

        assertThat(atchFileIdOf(boardId)).isEqualTo(atchFileId);
        List<Integer> fileSns = jdbcTemplate.queryForList(
                "SELECT FILE_SN FROM ATTACH_FILE_DETAIL WHERE ATCH_FILE_ID = ? ORDER BY FILE_SN", Integer.class, atchFileId);
        assertThat(fileSns).containsExactly(1, 2, 3);
        assertThat(jdbcTemplate.queryForObject("SELECT TITLE FROM BOARD WHERE BOARD_ID = ?", String.class, boardId))
                .isEqualTo("첨부 테스트 수정");
    }

    @Test
    void updateWithoutExistingFilesCreatesGroup() throws Exception {
        String boardId = register("첨부 없음", "작성자");
        assertThat(atchFileIdOf(boardId)).isNull();

        mockMvc.perform(multipart("/board/update.do").file(file("a.txt", "a"))
                        .param("boardId", boardId).param("title", "첨부 없음").param("writer", "작성자")
                        .with(csrf()))
                .andExpect(status().is3xxRedirection());

        assertThat(atchFileIdOf(boardId)).startsWith("FILE_");
    }

    @Test
    void disallowedExtensionIsRejectedWithoutSavingAnything() throws Exception {
        Integer before = jdbcTemplate.queryForObject("SELECT COUNT(*) FROM BOARD", Integer.class);
        long storedBefore = storedFileCount();

        mockMvc.perform(multipart("/board/register.do").file(file("shell.jsp", "<% %>"))
                        .param("title", "악성").param("writer", "공격자").with(csrf()))
                .andExpect(status().isOk())
                .andExpect(content().string(containsString("허용되지 않는 파일 형식입니다. (jsp)")));

        assertThat(jdbcTemplate.queryForObject("SELECT COUNT(*) FROM BOARD", Integer.class)).isEqualTo(before);
        assertThat(storedFileCount()).isEqualTo(storedBefore);
    }

    @Test
    void validationErrorsAreShown() throws Exception {
        mockMvc.perform(multipart("/board/register.do").param("title", " ").param("writer", "").with(csrf()))
                .andExpect(status().isOk())
                .andExpect(model().attributeHasFieldErrors("boardVO", "title", "writer"))
                .andExpect(content().string(containsString("제목을 입력해 주세요.")));
    }

    @Test
    void searchByWriterWithPagingOfTen() throws Exception {
        for (int i = 1; i <= 12; i++) {
            register("페이징 글 " + i, "페이징작성자");
        }
        mockMvc.perform(get("/board/list.do").param("searchCondition", "2").param("searchKeyword", "페이징작성자"))
                .andExpect(status().isOk())
                .andExpect(model().attribute("totCnt", 12))
                .andExpect(model().attribute("resultList", org.hamcrest.Matchers.hasSize(10)))
                .andExpect(content().string(containsString("pageIndex=2")));

        mockMvc.perform(get("/board/list.do").param("searchCondition", "2").param("searchKeyword", "페이징작성자")
                        .param("pageIndex", "2"))
                .andExpect(model().attribute("resultList", org.hamcrest.Matchers.hasSize(2)));

        mockMvc.perform(get("/board/list.do").param("searchCondition", "0").param("searchKeyword", "페이징 글 12"))
                .andExpect(model().attribute("totCnt", 1));

        mockMvc.perform(get("/board/list.do").param("searchCondition", "1").param("searchKeyword", "내용 페이징 글 1"))
                .andExpect(model().attribute("totCnt", 4));

        mockMvc.perform(get("/board/list.do").param("searchCondition", "0").param("searchKeyword", "%"))
                .andExpect(model().attribute("totCnt", 0));
    }

    @Test
    void outputIsHtmlEscaped() throws Exception {
        register("<script>alert(1)</script>", "xss");
        mockMvc.perform(get("/board/list.do").param("searchCondition", "2").param("searchKeyword", "xss"))
                .andExpect(content().string(containsString("&lt;script&gt;alert(1)&lt;/script&gt;")))
                .andExpect(content().string(not(containsString("<script>alert(1)</script>"))));
    }

    @Test
    void deleteSingleFileAndBoardRemovesStoredFiles() throws Exception {
        String boardId = register("삭제 테스트", "삭제자", file("one.txt", "1"), file("two.txt", "2"));
        String atchFileId = atchFileIdOf(boardId);
        long storedBefore = storedFileCount();

        mockMvc.perform(post("/board/deleteFile.do").param("boardId", boardId).param("fileSn", "1").with(csrf()))
                .andExpect(status().is3xxRedirection());
        assertThat(storedFileCount()).isEqualTo(storedBefore - 1);
        mockMvc.perform(get("/cmm/fms/fileDown.do").param("atchFileId", atchFileId).param("fileSn", "1"))
                .andExpect(status().isNotFound());

        mockMvc.perform(post("/board/delete.do").param("boardId", boardId).with(csrf()))
                .andExpect(status().is3xxRedirection());
        assertThat(storedFileCount()).isEqualTo(storedBefore - 2);
        assertThat(jdbcTemplate.queryForObject("SELECT COUNT(*) FROM BOARD WHERE BOARD_ID = ?", Integer.class, boardId))
                .isZero();
        assertThat(jdbcTemplate.queryForObject("SELECT COUNT(*) FROM ATTACH_FILE_DETAIL WHERE ATCH_FILE_ID = ?",
                Integer.class, atchFileId)).isZero();

        mockMvc.perform(get("/board/update.do").param("boardId", boardId))
                .andExpect(status().isBadRequest())
                .andExpect(content().string(containsString("게시물을 찾을 수 없습니다.")));
    }

    @Test
    void downloadRejectsUnknownOrMalformedKeys() throws Exception {
        mockMvc.perform(get("/cmm/fms/fileDown.do").param("atchFileId", "../../etc/passwd").param("fileSn", "1"))
                .andExpect(status().isNotFound());
        mockMvc.perform(get("/cmm/fms/fileDown.do").param("atchFileId", "FILE_1").param("fileSn", "abc"))
                .andExpect(status().isBadRequest());
    }

}
