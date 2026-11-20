package egovframework.boardapp.board;

import java.util.List;

import org.apache.ibatis.annotations.Mapper;

@Mapper
public interface BoardMapper {
    List<BoardVO> selectBoardList();
    BoardVO selectBoard(String boardId);
    BoardVO selectPrevBoard(String boardId);
    BoardVO selectNextBoard(String boardId);
    String nextBoardId();
    void insertBoard(BoardVO board);
    int updateBoard(BoardVO board);
    int deleteBoard(String boardId);

    List<BoardFileVO> selectFileList(String boardId);
    BoardFileVO selectFile(String fileId);
    String nextFileId();
    void insertFile(BoardFileVO file);
    int deleteFile(String fileId);
}
