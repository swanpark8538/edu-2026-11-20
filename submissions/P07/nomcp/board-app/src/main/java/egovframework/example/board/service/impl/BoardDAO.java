package egovframework.example.board.service.impl;

import java.util.List;

import org.egovframe.rte.psl.dataaccess.EgovAbstractMapper;
import org.springframework.stereotype.Repository;

import egovframework.example.board.service.BoardDefaultVO;
import egovframework.example.board.service.BoardVO;

@Repository("boardDAO")
public class BoardDAO extends EgovAbstractMapper {

    public List<BoardVO> selectBoardList(BoardDefaultVO searchVO) {
        return selectList("boardDAO.selectBoardList", searchVO);
    }

    public int selectBoardListTotCnt(BoardDefaultVO searchVO) {
        return selectOne("boardDAO.selectBoardListTotCnt", searchVO);
    }

    public BoardVO selectBoard(String boardId) {
        return selectOne("boardDAO.selectBoard", boardId);
    }

    public void insertBoard(BoardVO boardVO) {
        insert("boardDAO.insertBoard", boardVO);
    }

    public int updateBoard(BoardVO boardVO) {
        return update("boardDAO.updateBoard", boardVO);
    }

    public int deleteBoard(String boardId) {
        return delete("boardDAO.deleteBoard", boardId);
    }

}
