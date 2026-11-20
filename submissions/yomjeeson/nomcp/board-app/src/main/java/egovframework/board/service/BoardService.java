package egovframework.board.service;

import java.nio.file.Path;
import java.util.List;

import org.springframework.web.multipart.MultipartFile;

/** 게시판 서비스 */
public interface BoardService {

    List<BoardVO> selectBoardList(BoardVO searchVO);

    int selectBoardListTotCnt(BoardVO searchVO);

    /** 게시물 상세 (첨부파일 목록 포함). 없으면 null */
    BoardVO selectBoard(String boardId);

    /** 등록 후 생성된 BOARD_ID 반환 */
    String insertBoard(BoardVO boardVO, List<MultipartFile> files) throws Exception;

    void updateBoard(BoardVO boardVO, List<MultipartFile> files, List<Long> deleteFileIds) throws Exception;

    void deleteBoard(String boardId) throws Exception;

    BoardFileVO selectFile(Long fileId);

    /** 저장된 첨부파일의 실제 경로 */
    Path resolveFilePath(BoardFileVO fileVO);
}
