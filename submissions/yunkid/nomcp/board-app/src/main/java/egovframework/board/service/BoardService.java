package egovframework.board.service;

import java.nio.file.Path;
import java.util.List;

import org.springframework.web.multipart.MultipartFile;

public interface BoardService {

    List<BoardVO> selectBoardList(BoardVO boardVO);

    int selectBoardListTotCnt(BoardVO boardVO);

    BoardVO selectBoard(BoardVO boardVO);

    String insertBoard(BoardVO boardVO, List<MultipartFile> files);

    void updateBoard(BoardVO boardVO, List<MultipartFile> files);

    void deleteBoard(BoardVO boardVO);

    List<BoardFileVO> selectFileList(String boardId);

    BoardFileVO selectFile(String fileId);

    Path resolveStoredFile(BoardFileVO fileVO);

    void deleteFile(String fileId);
}
