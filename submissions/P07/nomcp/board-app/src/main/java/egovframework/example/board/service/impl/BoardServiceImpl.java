package egovframework.example.board.service.impl;

import java.util.List;

import org.egovframe.rte.fdl.cmmn.EgovAbstractServiceImpl;
import org.egovframe.rte.fdl.idgnr.EgovIdGnrService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import egovframework.example.board.service.BoardDefaultVO;
import egovframework.example.board.service.BoardService;
import egovframework.example.board.service.BoardVO;
import egovframework.example.cmmn.file.service.EgovFileMngService;
import jakarta.annotation.Resource;

@Service("boardService")
public class BoardServiceImpl extends EgovAbstractServiceImpl implements BoardService {

    @Resource(name = "boardDAO")
    private BoardDAO boardDAO;

    @Resource(name = "boardIdGnrService")
    private EgovIdGnrService boardIdGnrService;

    @Resource(name = "egovFileMngService")
    private EgovFileMngService fileMngService;

    @Override
    public List<BoardVO> selectBoardList(BoardDefaultVO searchVO) throws Exception {
        return boardDAO.selectBoardList(searchVO);
    }

    @Override
    public int selectBoardListTotCnt(BoardDefaultVO searchVO) throws Exception {
        return boardDAO.selectBoardListTotCnt(searchVO);
    }

    @Override
    public BoardVO selectBoard(String boardId) throws Exception {
        BoardVO boardVO = boardId == null ? null : boardDAO.selectBoard(boardId);
        if (boardVO == null) {
            throw processException("fail.board.notFound");
        }
        return boardVO;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public String insertBoard(BoardVO boardVO, List<MultipartFile> files) throws Exception {
        String boardId = boardIdGnrService.getNextStringId();
        boardVO.setBoardId(boardId);
        boardVO.setAtchFileId(fileMngService.saveFiles(null, files));
        boardDAO.insertBoard(boardVO);
        return boardId;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void updateBoard(BoardVO boardVO, List<MultipartFile> files) throws Exception {
        BoardVO stored = selectBoard(boardVO.getBoardId());
        boardVO.setAtchFileId(fileMngService.saveFiles(stored.getAtchFileId(), files));
        boardDAO.updateBoard(boardVO);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void deleteBoard(String boardId) throws Exception {
        BoardVO stored = selectBoard(boardId);
        boardDAO.deleteBoard(boardId);
        fileMngService.deleteAllFiles(stored.getAtchFileId());
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void deleteBoardFile(String boardId, int fileSn) throws Exception {
        BoardVO stored = selectBoard(boardId);
        if (stored.getAtchFileId() == null) {
            throw processException("fail.file.notFound");
        }
        fileMngService.deleteFile(stored.getAtchFileId(), fileSn);
    }

}
