package egovframework.board.service.impl;

import java.util.List;

import org.apache.ibatis.annotations.Mapper;

import egovframework.board.service.BoardFileVO;
import egovframework.board.service.BoardSearchVO;
import egovframework.board.service.BoardVO;

@Mapper
public interface BoardMapper {

	List<BoardVO> selectBoardList(BoardSearchVO searchVO);

	int selectBoardListCount(BoardSearchVO searchVO);

	BoardVO selectBoard(String boardId);

	int insertBoard(BoardVO board);

	int updateBoard(BoardVO board);

	int updateViewCount(String boardId);

	int deleteBoard(String boardId);

	List<BoardFileVO> selectFileList(String boardId);

	BoardFileVO selectFile(String fileId);

	int insertFile(BoardFileVO file);

	int deleteFile(String fileId);
}
