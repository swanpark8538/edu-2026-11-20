package egovframework.example.board.service;

import java.util.List;

import org.springframework.web.multipart.MultipartFile;

/**
 * 게시판 서비스.
 */
public interface BoardService {

    List<BoardVO> selectBoardList(BoardDefaultVO searchVO) throws Exception;

    int selectBoardListTotCnt(BoardDefaultVO searchVO) throws Exception;

    /** 게시물을 조회한다. 없으면 업무 예외(fail.board.notFound)를 던진다. */
    BoardVO selectBoard(String boardId) throws Exception;

    String insertBoard(BoardVO boardVO, List<MultipartFile> files) throws Exception;

    /** 게시물 내용을 수정하고 새 첨부파일은 기존 첨부 묶음에 추가한다. */
    void updateBoard(BoardVO boardVO, List<MultipartFile> files) throws Exception;

    void deleteBoard(String boardId) throws Exception;

    void deleteBoardFile(String boardId, int fileSn) throws Exception;

}
