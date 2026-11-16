package egovframework.example.board.service;

import java.util.List;

import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

@Mapper
public interface BoardMapper {
    String nextId();
    List<BoardVO> selectList();
    BoardVO select(@Param("boardId") String boardId);
    int insert(BoardVO vo);
    int update(BoardVO vo);
    int delete(@Param("boardId") String boardId);

    List<BoardFileVO> selectFiles(@Param("boardId") String boardId);
    BoardFileVO selectFile(@Param("fileId") String fileId);
    int insertFile(BoardFileVO vo);
    int deleteFile(@Param("fileId") String fileId);
}
