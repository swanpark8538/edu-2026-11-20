package egovframework.example.cmmn.file.service;

import java.nio.file.Path;
import java.util.List;

import org.springframework.web.multipart.MultipartFile;

/**
 * 첨부파일 관리 서비스.
 */
public interface EgovFileMngService {

    List<FileVO> selectFileList(String atchFileId) throws Exception;

    int selectFileCount(String atchFileId) throws Exception;

    /** 게시물에 연결된 첨부파일만 내려받기 대상으로 조회한다. 없으면 null. */
    FileVO selectDownloadFile(String atchFileId, int fileSn) throws Exception;

    /**
     * 업로드 파일을 검증·저장하고 첨부 묶음에 추가한다.
     *
     * @param atchFileId 기존 첨부 묶음 ID (없으면 null 로 넘겨 새 묶음을 만든다)
     * @return 첨부 묶음 ID. 저장할 파일이 없고 기존 묶음도 없으면 null
     */
    String saveFiles(String atchFileId, List<MultipartFile> files) throws Exception;

    void deleteFile(String atchFileId, int fileSn) throws Exception;

    void deleteAllFiles(String atchFileId) throws Exception;

    Path resolveStoredPath(FileVO fileVO) throws Exception;

}
