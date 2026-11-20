package egovframework.board.service.impl;

import java.util.List;

import org.egovframe.rte.psl.dataaccess.mapper.EgovMapper;

import egovframework.board.service.BoardFileVO;

@EgovMapper
public interface BoardFileMapper {

    List<BoardFileVO> selectBoardFileList(String boardId);

    BoardFileVO selectBoardFile(String fileId);

    int selectNextFileSn(String boardId);

    int insertBoardFile(BoardFileVO fileVO);

    int deleteBoardFile(String fileId);

    int deleteBoardFileByBoardId(String boardId);
}
