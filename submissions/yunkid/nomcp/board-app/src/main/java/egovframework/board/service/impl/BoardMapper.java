package egovframework.board.service.impl;

import java.util.List;

import org.egovframe.rte.psl.dataaccess.mapper.EgovMapper;

import egovframework.board.service.BoardVO;

@EgovMapper
public interface BoardMapper {

    List<BoardVO> selectBoardList(BoardVO boardVO);

    int selectBoardListTotCnt(BoardVO boardVO);

    BoardVO selectBoard(BoardVO boardVO);

    int insertBoard(BoardVO boardVO);

    int updateBoard(BoardVO boardVO);

    int deleteBoard(BoardVO boardVO);
}
