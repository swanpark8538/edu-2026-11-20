package egovframework.boardapp;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.multipart;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.header;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;

import egovframework.boardapp.board.BoardService;
import egovframework.boardapp.board.BoardVO;

@SpringBootTest
@AutoConfigureMockMvc
@TestPropertySource(properties = "board.upload-dir=target/test-uploads")
class BoardAppTests {

    @Autowired MockMvc mvc;
    @Autowired BoardService service;

    @Test
    void seedRowIsListed() throws Exception {
        mvc.perform(get("/boards")).andExpect(status().isOk())
                .andExpect(content().string(org.hamcrest.Matchers.containsString("첫 번째 게시물")));
    }

    @Test
    void createWithAttachmentThenDownload() throws Exception {
        MockMultipartFile file = new MockMultipartFile("uploadFiles", "hello.txt", "text/plain", "hello".getBytes());
        MvcResult r = mvc.perform(multipart("/boards").file(file)
                        .param("title", "제목").param("writer", "tester").param("content", "내용"))
                .andExpect(status().is3xxRedirection()).andReturn();
        String id = r.getResponse().getRedirectedUrl().substring("/boards/".length());

        BoardVO board = service.get(id);
        assertThat(board.getFiles()).hasSize(1);
        String fileId = board.getFiles().get(0).getFileId();

        mvc.perform(get("/files/" + fileId)).andExpect(status().isOk())
                .andExpect(header().string("Content-Disposition", org.hamcrest.Matchers.containsString("hello.txt")))
                .andExpect(content().string("hello"));
    }

    @Test
    void prevAndNextNavigation() throws Exception {
        // 시드 3건(BRD...01~03): 가운데 글의 이전/다음은 각각 01, 03
        assertThat(service.getPrev("BRD0000000002").getBoardId()).isEqualTo("BRD0000000001");
        assertThat(service.getNext("BRD0000000002").getBoardId()).isEqualTo("BRD0000000003");
        assertThat(service.getPrev("BRD0000000001")).isNull();
        mvc.perform(get("/boards/BRD0000000002")).andExpect(status().isOk())
                .andExpect(content().string(org.hamcrest.Matchers.containsString("/boards/BRD0000000001")))
                .andExpect(content().string(org.hamcrest.Matchers.containsString("/boards/BRD0000000003")));
    }

    @Test
    void validationRejectsBlankTitle() throws Exception {
        mvc.perform(multipart("/boards").param("title", "").param("writer", "w"))
                .andExpect(status().isOk());
    }

    @Test
    void updateAndDelete() throws Exception {
        mvc.perform(multipart("/boards").param("title", "t").param("writer", "w")).andReturn();
        String id = service.list().get(0).getBoardId();
        mvc.perform(multipart("/boards/" + id).param("title", "수정됨").param("writer", "w"))
                .andExpect(status().is3xxRedirection());
        assertThat(service.get(id).getTitle()).isEqualTo("수정됨");
        mvc.perform(multipart("/boards/" + id + "/delete")).andExpect(status().is3xxRedirection());
        assertThat(service.get(id)).isNull();
    }
}
