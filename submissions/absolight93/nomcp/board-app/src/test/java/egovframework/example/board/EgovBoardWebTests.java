package egovframework.example.board;

import static org.hamcrest.Matchers.containsString;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.multipart;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.forwardedUrl;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.header;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.redirectedUrlPattern;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.view;

import java.nio.charset.StandardCharsets;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;

/**
 * 게시판 웹 계층(MVC) 통합 테스트.
 */
@SpringBootTest
@AutoConfigureMockMvc
class EgovBoardWebTests {

    @Autowired
    private MockMvc mockMvc;

    @Test
    void 루트요청은_목록으로_이동한다() throws Exception {
        mockMvc.perform(get("/"))
                .andExpect(status().is3xxRedirection())
                .andExpect(header().string("Location", "/board/list"));
    }

    @Test
    void 목록에_샘플데이터가_노출된다() throws Exception {
        mockMvc.perform(get("/board/list"))
                .andExpect(status().isOk())
                .andExpect(view().name("board/list"))
                .andExpect(content().string(containsString("eGovFrame 5.0 Boot 게시판")));
    }

    @Test
    void 첨부파일을_포함해_등록하고_다운로드한다() throws Exception {
        MockMultipartFile file = new MockMultipartFile("files", "다운로드테스트.txt", "text/plain",
                "mock mvc attachment".getBytes(StandardCharsets.UTF_8));

        MvcResult result = mockMvc.perform(multipart("/board/create")
                        .file(file)
                        .param("title", "웹 계층 등록 테스트")
                        .param("content", "본문")
                        .param("writer", "weber"))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrlPattern("/board/detail?boardId=*"))
                .andReturn();

        String location = result.getResponse().getRedirectedUrl();
        String boardId = location.substring(location.lastIndexOf('=') + 1);

        mockMvc.perform(get("/board/detail").param("boardId", boardId))
                .andExpect(status().isOk())
                .andExpect(content().string(containsString("다운로드테스트.txt")));

        String atchfileId = mockMvc.perform(get("/board/detail").param("boardId", boardId))
                .andReturn().getResponse().getContentAsString()
                .replaceAll("[\\s\\S]*?atchfileId=(\\d{10})[\\s\\S]*", "$1");

        mockMvc.perform(get("/board/file/download").param("atchfileId", atchfileId))
                .andExpect(status().isOk())
                .andExpect(header().string("Content-Disposition", containsString("attachment")))
                .andExpect(content().string("mock mvc attachment"));

        mockMvc.perform(post("/board/delete").param("boardId", boardId))
                .andExpect(status().is3xxRedirection());
    }

    @Test
    void 필수값이_빠지면_작성화면으로_돌아간다() throws Exception {
        mockMvc.perform(multipart("/board/create")
                        .param("title", "")
                        .param("content", "본문만")
                        .param("writer", ""))
                .andExpect(status().isOk())
                .andExpect(view().name("board/write"))
                .andExpect(content().string(containsString("제목은 필수입니다")));
    }

    @Test
    없는_게시물은_표준예외메시지로_응답한다() throws Exception {
        mockMvc.perform(get("/board/detail").param("boardId", "9999999999"))
                .andExpect(status().isOk())
                .andExpect(view().name("board/error"))
                .andExpect(forwardedUrl(null))
                .andExpect(content().string(containsString("error.board.notFound")));
    }

    @Test
    존재하지_않는_경로는_404로_응답한다() throws Exception {
        mockMvc.perform(get("/board/unknown-page"))
                .andExpect(status().isNotFound());
    }

}
