package egovframework.example.board.service;

import java.util.List;

import org.springframework.core.io.Resource;
import org.springframework.web.multipart.MultipartFile;

/**
 * 게시판 서비스
 */
public interface BoardService {

    List<BoardVO> selectBoardList(BoardDefaultVO searchVO) throws Exception;

    int selectBoardListTotCnt(BoardDefaultVO searchVO) throws Exception;

    /** 게시물 상세 (첨부파일 포함) */
    BoardVO selectBoard(String boardId) throws Exception;

    /** 게시물 등록 후 생성된 BOARD_ID 를 돌려준다. */
    String insertBoard(BoardVO vo, List<MultipartFile> uploadFiles) throws Exception;

    void updateBoard(BoardVO vo, List<MultipartFile> uploadFiles, List<String> deleteFileIds) throws Exception;

    void deleteBoard(String boardId) throws Exception;

    BoardFileVO selectFile(String fileId) throws Exception;

    Resource loadFile(BoardFileVO file) throws Exception;
}
