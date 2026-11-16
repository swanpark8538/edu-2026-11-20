package egovframework.example.board;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;

import java.util.List;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.web.multipart.MultipartFile;

import egovframework.example.board.service.BoardService;
import egovframework.example.board.service.BoardVO;

@SpringBootTest
class BoardApplicationTests {

    @Autowired
    BoardService service;

    @Test
    void crudWithAttachment() {
        BoardVO vo = new BoardVO();
        vo.setTitle("제목");
        vo.setContent("내용");
        vo.setWriter("작성자");
        List<MultipartFile> files = List.of(new MockMultipartFile("files", "a.txt", "text/plain", "hello".getBytes()));

        String id = service.create(vo, files);
        BoardVO got = service.get(id);
        assertEquals("제목", got.getTitle());
        assertEquals(1, got.getFiles().size());
        assertEquals("a.txt", got.getFiles().get(0).getOrigName());
        assertEquals(5, got.getFiles().get(0).getFileSize());

        service.delete(id);
        assertNull(service.get(id));
        assertNotNull(service.list());
    }
}
