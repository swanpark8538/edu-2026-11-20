package egovframework.board.web;

import static org.hamcrest.Matchers.containsString;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.multipart;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.header;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.redirectedUrl;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.util.List;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.HttpHeaders;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.test.web.servlet.MockMvc;

import egovframework.board.service.BoardFileVO;
import egovframework.board.service.BoardService;
import egovframework.board.service.BoardVO;

@SpringBootTest(properties = "board.file.store-path=${java.io.tmpdir}/board-app-upload-test")
@AutoConfigureMockMvc
class BoardControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private BoardService boardService;

    @Test
    void listShowsSeedPosts() throws Exception {
        mockMvc.perform(get("/"))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/boardList.do"));

        mockMvc.perform(get("/boardList.do"))
                .andExpect(status().isOk())
                .andExpect(content().string(containsString("표준프레임워크 게시판 안내")));
    }

    @Test
    void rejectsEmptyTitle() throws Exception {
        mockMvc.perform(multipart("/addBoard.do")
                        .param("title", "")
                        .param("writer", "")
                        .param("content", ""))
                .andExpect(status().isOk())
                .andExpect(content().string(containsString("제목은 필수입니다.")));
    }

    @Test
    void writesBoardWithAttachmentThenDeletesIt() throws Exception {
        byte[] payload = "첨부내용".getBytes(StandardCharsets.UTF_8);
        mockMvc.perform(multipart("/addBoard.do")
                        .file(new MockMultipartFile("files", "메모.txt", "text/plain", payload))
                        .param("title", "첨부 테스트")
                        .param("writer", "테스터")
                        .param("content", "본문"))
                .andExpect(status().is3xxRedirection());

        BoardVO condition = new BoardVO();
        condition.setSearchCondition("title");
        condition.setSearchKeyword("첨부 테스트");
        condition.setFirstIndex(0);
        condition.setRecordCountPerPage(10);
        List<BoardVO> found = boardService.selectBoardList(condition);
        assertEquals(1, found.size());
        String boardId = found.get(0).getBoardId();

        mockMvc.perform(get("/boardDetail.do").param("boardId", boardId))
                .andExpect(status().isOk())
                .andExpect(content().string(containsString("메모.txt")))
                .andExpect(content().string(containsString("본문")));

        List<BoardFileVO> files = boardService.selectFileList(boardId);
        assertEquals(1, files.size());
        mockMvc.perform(get("/boardFileDownload.do").param("fileId", files.get(0).getFileId()))
                .andExpect(status().isOk())
                .andExpect(header().exists(HttpHeaders.CONTENT_DISPOSITION))
                .andExpect(content().bytes(payload));

        mockMvc.perform(multipart("/updateBoard.do")
                        .file(new MockMultipartFile("files", "두번째.txt", "text/plain", "second".getBytes(StandardCharsets.UTF_8)))
                        .param("boardId", boardId)
                        .param("title", "첨부 테스트")
                        .param("writer", "테스터")
                        .param("content", "수정본"))
                .andExpect(redirectedUrl("/boardDetail.do?boardId=" + boardId));
        assertEquals(2, boardService.selectFileList(boardId).size());

        mockMvc.perform(post("/deleteBoardFile.do")
                        .param("fileId", files.get(0).getFileId())
                        .param("boardId", boardId))
                .andExpect(redirectedUrl("/boardUpdate.do?boardId=" + boardId));
        assertFalse(Files.exists(boardService.resolveStoredFile(files.get(0))));
        assertEquals(1, boardService.selectFileList(boardId).size());

        mockMvc.perform(post("/deleteBoard.do").param("boardId", boardId))
                .andExpect(redirectedUrl("/boardList.do"));
        assertEquals(0, boardService.selectBoardList(condition).size());
    }
}
