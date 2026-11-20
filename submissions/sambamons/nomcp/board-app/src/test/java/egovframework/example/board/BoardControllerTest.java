package egovframework.example.board;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.multipart;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.model;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.springframework.test.web.servlet.MockMvc;

import egovframework.example.board.service.BoardFileVO;
import egovframework.example.board.service.BoardService;
import egovframework.example.board.service.BoardVO;

@SpringBootTest
@AutoConfigureMockMvc
class BoardControllerTest {

    @TempDir
    static Path uploadDir;

    @DynamicPropertySource
    static void props(DynamicPropertyRegistry registry) {
        registry.add("board.upload-dir", uploadDir::toString);
    }

    @Autowired
    MockMvc mockMvc;

    @Autowired
    BoardService boardService;

    @Test
    void listShowsSampleData() throws Exception {
        mockMvc.perform(get("/board/list.do"))
                .andExpect(status().isOk())
                .andExpect(content().string(org.hamcrest.Matchers.containsString("BOARD_00000000000001")));
    }

    @Test
    void addWithAttachmentThenDownloadEditDelete() throws Exception {
        MockMultipartFile file = new MockMultipartFile("uploadFiles", "보고서.txt", "text/plain",
                "hello".getBytes(StandardCharsets.UTF_8));

        String location = mockMvc.perform(multipart("/board/add.do").file(file)
                        .param("title", "첨부 테스트").param("writer", "홍길동").param("content", "본문"))
                .andExpect(status().is3xxRedirection())
                .andReturn().getResponse().getRedirectedUrl();
        String boardId = location.split("/")[2];
        assertThat(boardId).startsWith("BOARD_").hasSize(20);

        BoardVO saved = boardService.selectBoard(boardId);
        assertThat(saved.getFiles()).hasSize(1);
        BoardFileVO savedFile = saved.getFiles().get(0);
        assertThat(savedFile.getFileId()).startsWith("FILE_").hasSize(20);
        assertThat(Files.exists(uploadDir.resolve(savedFile.getStreFileNm()))).isTrue();

        mockMvc.perform(get("/board/file/" + savedFile.getFileId() + "/download.do"))
                .andExpect(status().isOk())
                .andExpect(content().bytes("hello".getBytes(StandardCharsets.UTF_8)));

        // 기존 파일 삭제 + 새 파일 추가
        MockMultipartFile file2 = new MockMultipartFile("uploadFiles", "b.txt", "text/plain", "b".getBytes());
        mockMvc.perform(multipart("/board/" + boardId + "/edit.do").file(file2)
                        .param("title", "수정됨").param("writer", "홍길동")
                        .param("deleteFileIds", savedFile.getFileId()))
                .andExpect(status().is3xxRedirection());
        BoardVO edited = boardService.selectBoard(boardId);
        assertThat(edited.getTitle()).isEqualTo("수정됨");
        assertThat(edited.getFiles()).extracting(BoardFileVO::getOrignlFileNm).isEqualTo(List.of("b.txt"));
        assertThat(Files.exists(uploadDir.resolve(savedFile.getStreFileNm()))).isFalse();

        mockMvc.perform(post("/board/" + boardId + "/delete.do"))
                .andExpect(status().is3xxRedirection());
        assertThat(Files.exists(uploadDir.resolve(edited.getFiles().get(0).getStreFileNm()))).isFalse();
        mockMvc.perform(get("/board/" + boardId + "/detail.do"))
                .andExpect(status().isNotFound())
                .andExpect(model().attribute("errorMessage", "해당 데이터가 없습니다."));
    }

    @Test
    void validationFailsWithoutTitle() throws Exception {
        mockMvc.perform(post("/board/add.do").param("writer", "홍길동"))
                .andExpect(status().isOk())
                .andExpect(model().attributeHasFieldErrors("board", "title"));
    }
}
