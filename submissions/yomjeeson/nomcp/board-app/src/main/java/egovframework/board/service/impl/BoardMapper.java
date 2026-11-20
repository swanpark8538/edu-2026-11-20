package egovframework.board.service.impl;

import java.util.List;

import org.apache.ibatis.annotations.Mapper;

import egovframework.board.service.BoardFileVO;
import egovframework.board.service.BoardVO;

@Mapper
public interface BoardMapper {

    List<BoardVO> selectBoardList(BoardVO searchVO);

    int selectBoardListTotCnt(BoardVO searchVO);

    BoardVO selectBoard(String boardId);

    int insertBoard(BoardVO boardVO);

    int updateBoard(BoardVO boardVO);

    int deleteBoard(String boardId);

    List<BoardFileVO> selectFileList(String boardId);

    BoardFileVO selectFile(Long fileId);

    int insertFile(BoardFileVO fileVO);

    int deleteFile(Long fileId);

    int deleteFilesByBoard(String boardId);
}
