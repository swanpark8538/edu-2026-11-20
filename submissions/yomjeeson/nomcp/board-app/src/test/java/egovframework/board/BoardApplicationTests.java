package egovframework.board;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.multipart;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.header;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.test.web.servlet.MockMvc;

import egovframework.board.service.BoardFileVO;
import egovframework.board.service.BoardService;
import egovframework.board.service.BoardVO;

@SpringBootTest(properties = "board.upload-dir=target/test-upload")
@AutoConfigureMockMvc
class BoardApplicationTests {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private BoardService boardService;

    @Test
    void createViewDownloadUpdateDelete() throws Exception {
        MockMultipartFile file = new MockMultipartFile(
                "uploadFiles", "보고서.txt", "text/plain", "hello".getBytes(StandardCharsets.UTF_8));

        // 등록
        mockMvc.perform(multipart("/board/new").file(file)
                        .param("title", "첫 글").param("writer", "홍길동").param("content", "내용입니다"))
                .andExpect(status().is3xxRedirection());

        BoardVO search = new BoardVO();
        List<BoardVO> list = boardService.selectBoardList(search);
        assertThat(list).hasSize(1);
        String boardId = list.get(0).getBoardId();
        assertThat(boardId).startsWith("BOARD_").hasSize(20);

        BoardVO board = boardService.selectBoard(boardId);
        assertThat(board.getFiles()).hasSize(1);
        BoardFileVO saved = board.getFiles().get(0);
        Path stored = boardService.resolveFilePath(saved);
        assertThat(Files.exists(stored)).isTrue();

        // 목록·상세 화면
        mockMvc.perform(get("/board/list").param("searchKeyword", "첫"))
                .andExpect(status().isOk())
                .andExpect(content().string(org.hamcrest.Matchers.containsString("첫 글")));
        mockMvc.perform(get("/board/view/" + boardId)).andExpect(status().isOk());

        // 다운로드
        mockMvc.perform(get("/board/file/" + saved.getFileId()))
                .andExpect(status().isOk())
                .andExpect(header().string("Content-Disposition", org.hamcrest.Matchers.containsString("attachment")))
                .andExpect(content().bytes("hello".getBytes(StandardCharsets.UTF_8)));

        // 수정: 기존 파일 삭제 + 새 파일 추가
        MockMultipartFile file2 = new MockMultipartFile(
                "uploadFiles", "b.txt", "text/plain", "world".getBytes(StandardCharsets.UTF_8));
        mockMvc.perform(multipart("/board/edit/" + boardId).file(file2)
                        .param("title", "수정된 글").param("writer", "홍길동")
                        .param("deleteFileIds", String.valueOf(saved.getFileId())))
                .andExpect(status().is3xxRedirection());
        board = boardService.selectBoard(boardId);
        assertThat(board.getTitle()).isEqualTo("수정된 글");
        assertThat(board.getFiles()).extracting(BoardFileVO::getOriginalName).containsExactly("b.txt");
        assertThat(Files.exists(stored)).isFalse();

        // 검증 실패 (제목 누락)
        mockMvc.perform(multipart("/board/new").param("title", "").param("writer", "x"))
                .andExpect(status().isOk())
                .andExpect(content().string(org.hamcrest.Matchers.containsString("제목을 입력하세요.")));

        // 삭제
        Path stored2 = boardService.resolveFilePath(board.getFiles().get(0));
        mockMvc.perform(post("/board/delete/" + boardId)).andExpect(status().is3xxRedirection());
        assertThat(boardService.selectBoard(boardId)).isNull();
        assertThat(Files.exists(stored2)).isFalse();
        mockMvc.perform(get("/board/view/" + boardId)).andExpect(status().isNotFound());
    }
}
