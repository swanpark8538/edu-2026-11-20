package egovframework.example.board.service.impl;

import java.util.List;

import org.egovframe.rte.psl.dataaccess.mapper.EgovMapper;

import egovframework.example.board.service.BoardDefaultVO;
import egovframework.example.board.service.BoardFileVO;
import egovframework.example.board.service.BoardVO;

@EgovMapper("boardMapper")
public interface BoardMapper {

    List<BoardVO> selectBoardList(BoardDefaultVO searchVO);

    int selectBoardListTotCnt(BoardDefaultVO searchVO);

    BoardVO selectBoard(String boardId);

    void insertBoard(BoardVO vo);

    int updateBoard(BoardVO vo);

    int deleteBoard(String boardId);

    List<BoardFileVO> selectFileList(String boardId);

    BoardFileVO selectFile(String fileId);

    void insertFile(BoardFileVO vo);

    void deleteFile(String fileId);

    void deleteFilesByBoardId(String boardId);
}
