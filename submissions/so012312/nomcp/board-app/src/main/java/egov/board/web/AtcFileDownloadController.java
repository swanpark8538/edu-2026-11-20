package egov.board.web;

import egov.board.service.BoardService;
import egov.board.service.FileStorageService;
import egov.board.service.vo.AtcFileVO;
import org.egovframe.rte.fdl.cmmn.exception.EgovBizException;
import org.springframework.core.io.Resource;
import org.springframework.http.ContentDisposition;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.http.MediaTypeFactory;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;

import java.nio.charset.StandardCharsets;

/**
 * 게시물 첨부파일 다운로드 컨트롤러.
 *
 * <p>클라이언트에는 파일명을 전달하지 않고 첨부파일 ID 만 전달받아
 * DB 에 저장된 메타데이터로 파일을 읽으므로 경로 조차기(path traversal) 에 안전하다.</p>
 */
@Controller
@RequestMapping("/board/file")
public class AtcFileDownloadController {

    private final BoardService boardService;
    private final FileStorageService fileStorageService;

    public AtcFileDownloadController(BoardService boardService, FileStorageService fileStorageService) {
        this.boardService = boardService;
        this.fileStorageService = fileStorageService;
    }

    /** 첨부파일 다운로드 */
    @GetMapping("/download")
    public ResponseEntity<Resource> download(@RequestParam("atcFileId") String atcFileId) throws EgovBizException {
        AtcFileVO atcFileVO = boardService.selectAtcFile(atcFileId);
        Resource resource = fileStorageService.loadResource(atcFileVO);

        MediaType mediaType = MediaTypeFactory.getMediaType(atcFileVO.getFileNm())
                .orElse(MediaType.APPLICATION_OCTET_STREAM);
        ContentDisposition contentDisposition = ContentDisposition.attachment()
                .filename(atcFileVO.getFileNm(), StandardCharsets.UTF_8)
                .build();

        return ResponseEntity.ok()
                .contentType(mediaType)
                .header(HttpHeaders.CONTENT_DISPOSITION, contentDisposition.toString())
                .body(resource);
    }
}
