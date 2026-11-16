package egovframework.example.cmmn.file.web;

import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;

import org.springframework.core.io.FileSystemResource;
import org.springframework.core.io.Resource;
import org.springframework.http.ContentDisposition;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;

import egovframework.example.cmmn.file.service.EgovFileMngService;
import egovframework.example.cmmn.file.service.FileVO;
import egovframework.example.cmmn.web.EgovResourceNotFoundException;

/**
 * 첨부파일 내려받기. 게시물에 연결된 파일만 내려받을 수 있다.
 */
@Controller
public class EgovFileDownloadController {

    @jakarta.annotation.Resource(name = "egovFileMngService")
    private EgovFileMngService fileMngService;

    @GetMapping("/cmm/fms/fileDown.do")
    public ResponseEntity<Resource> downloadFile(@RequestParam("atchFileId") String atchFileId,
            @RequestParam("fileSn") int fileSn) throws Exception {
        FileVO fileVO = fileMngService.selectDownloadFile(atchFileId, fileSn);
        if (fileVO == null) {
            throw new EgovResourceNotFoundException();
        }
        Path path = fileMngService.resolveStoredPath(fileVO);
        if (!Files.isRegularFile(path)) {
            throw new EgovResourceNotFoundException();
        }

        ContentDisposition disposition = ContentDisposition.attachment()
                .filename(fileVO.getOrignlFileNm(), StandardCharsets.UTF_8)
                .build();
        return ResponseEntity.ok()
                .contentType(MediaType.APPLICATION_OCTET_STREAM)
                .contentLength(Files.size(path))
                .header(HttpHeaders.CONTENT_DISPOSITION, disposition.toString())
                .header("X-Content-Type-Options", "nosniff")
                .body(new FileSystemResource(path));
    }

}
