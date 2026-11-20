package egov.board.service.impl;

import egov.board.persistence.AtcFileDAO;
import egov.board.persistence.BoardDAO;
import egov.board.service.BoardService;
import egov.board.service.FileStorageService;
import egov.board.service.vo.AtcFileVO;
import egov.board.service.vo.BoardListResultVO;
import egov.board.service.vo.BoardSearchVO;
import egov.board.service.vo.BoardVO;
import org.egovframe.rte.fdl.cmmn.EgovAbstractServiceImpl;
import org.egovframe.rte.fdl.cmmn.exception.EgovBizException;
import org.egovframe.rte.fdl.cmmn.exception.FdlException;
import org.egovframe.rte.fdl.idgnr.EgovIdGnrService;
import org.egovframe.rte.ptl.mvc.tags.ui.pagination.PaginationInfo;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

/**
 * 게시판 업무 서비스 구현체.
 *
 * <p>표준프레임워크 공통기반(fdl-cmmn) 의 {@link EgovAbstractServiceImpl} 을 상속받아
 * {@code egovLogger} 로그와 예외 처리 기반을 사용하고,
 * 게시물·첨부파일 ID 는 공통기반 ID 발번(fdl-idgnr) 서비스로 발급한다.</p>
 */
@Service
public class BoardServiceImpl extends EgovAbstractServiceImpl implements BoardService {

    private final BoardDAO boardDAO;
    private final AtcFileDAO atcFileDAO;
    private final FileStorageService fileStorageService;
    private final EgovIdGnrService boardIdGnrService;
    private final EgovIdGnrService atcFileIdGnrService;

    public BoardServiceImpl(BoardDAO boardDAO,
                            AtcFileDAO atcFileDAO,
                            FileStorageService fileStorageService,
                            @Qualifier("boardIdGnrService") EgovIdGnrService boardIdGnrService,
                            @Qualifier("atcFileIdGnrService") EgovIdGnrService atcFileIdGnrService) {
        this.boardDAO = boardDAO;
        this.atcFileDAO = atcFileDAO;
        this.fileStorageService = fileStorageService;
        this.boardIdGnrService = boardIdGnrService;
        this.atcFileIdGnrService = atcFileIdGnrService;
    }

    @Override
    public BoardListResultVO selectBoardList(BoardSearchVO searchVO) throws EgovBizException {
        try {
            searchVO.normalizeSearchCond();

            int totalRecordCount = boardDAO.countBoardList(searchVO);

            // 표준프레임워크 PaginationInfo 로 페이지 번호·조회 범위를 계산한다.
            PaginationInfo paginationInfo = new PaginationInfo();
            paginationInfo.setCurrentPageNo(searchVO.getCurrentPageNo());
            paginationInfo.setRecordCountPerPage(searchVO.getRecordCountPerPage());
            paginationInfo.setPageSize(searchVO.getPageSize());
            paginationInfo.setTotalRecordCount(totalRecordCount);

            searchVO.setFirstRecordIndex(paginationInfo.getFirstRecordIndex());
            searchVO.setLastRecordIndex(paginationInfo.getLastRecordIndex());

            BoardListResultVO resultVO = new BoardListResultVO();
            resultVO.setBoardList(boardDAO.selectBoardList(searchVO));
            resultVO.setPaginationInfo(paginationInfo);

            egovLogger.debug("게시물 목록 조회 : total={}, currentPageNo={}", totalRecordCount,
                    paginationInfo.getCurrentPageNo());
            return resultVO;
        } catch (Exception e) {
            throw new EgovBizException("게시물 목록을 조회하지 못했습니다.", e);
        }
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public BoardVO selectBoard(String boardId, boolean increaseViewCnt) throws EgovBizException {
        try {
            BoardVO searchVO = new BoardVO();
            searchVO.setBoardId(boardId);

            BoardVO boardVO = boardDAO.selectBoard(searchVO);
            if (boardVO == null) {
                throw new EgovBizException("게시물을 찾을 수 없습니다. : " + boardId);
            }
            if (increaseViewCnt) {
                boardDAO.updateBoardViewCnt(searchVO);
                boardVO.setViewCnt(boardVO.getViewCnt() + 1);
            }
            List<AtcFileVO> atcFileList = atcFileDAO.selectAtcFileList(boardId);
            boardVO.setAtcFileList(atcFileList);
            boardVO.setAtcFileNo(atcFileList.size());

            return boardVO;
        } catch (EgovBizException e) {
            throw e;
        } catch (Exception e) {
            throw new EgovBizException("게시물을 조회하지 못했습니다.", e);
        }
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public String insertBoard(BoardVO boardVO, List<MultipartFile> atcFiles) throws EgovBizException {
        try {
            boardVO.setBoardId(boardIdGnrService.getNextStringId());
            boardDAO.insertBoard(boardVO);
            saveAtcFileList(boardVO.getBoardId(), boardVO.getWriter(), atcFiles);

            egovLogger.info("게시물 등록 : {}", boardVO.getBoardId());
            return boardVO.getBoardId();
        } catch (FdlException e) {
            throw new EgovBizException("게시물 번호를 발급하지 못했습니다.", e);
        } catch (IllegalArgumentException e) {
            throw new EgovBizException(e.getMessage(), e);
        } catch (Exception e) {
            throw new EgovBizException("게시물을 등록하지 못했습니다.", e);
        }
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void updateBoard(BoardVO boardVO, List<MultipartFile> atcFiles, List<String> deleteAtcFileIdList)
            throws EgovBizException {
        try {
            BoardVO searchVO = new BoardVO();
            searchVO.setBoardId(boardVO.getBoardId());
            if (boardDAO.selectBoard(searchVO) == null) {
                throw new EgovBizException("수정할 게시물을 찾을 수 없습니다. : " + boardVO.getBoardId());
            }

            // 삭제할 첨부파일 (URL 로 ID 가 조작될 수 있어 해당 게시물의 첨부인지 확인한다)
            if (deleteAtcFileIdList != null) {
                for (String atcFileId : deleteAtcFileIdList) {
                    AtcFileVO atcFileVO = atcFileDAO.selectAtcFile(atcFileId);
                    if (atcFileVO == null || !boardVO.getBoardId().equals(atcFileVO.getBoardId())) {
                        continue;
                    }
                    atcFileDAO.deleteAtcFile(atcFileId);
                    fileStorageService.remove(atcFileVO);
                }
            }

            boardDAO.updateBoard(boardVO);
            saveAtcFileList(boardVO.getBoardId(), boardVO.getWriter(), atcFiles);

            egovLogger.info("게시물 수정 : {}", boardVO.getBoardId());
        } catch (IllegalArgumentException e) {
            throw new EgovBizException(e.getMessage(), e);
        } catch (Exception e) {
            throw new EgovBizException("게시물을 수정하지 못했습니다.", e);
        }
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void deleteBoard(String boardId) throws EgovBizException {
        try {
            List<AtcFileVO> atcFileList = atcFileDAO.selectAtcFileList(boardId);

            BoardVO boardVO = new BoardVO();
            boardVO.setBoardId(boardId);
            if (boardDAO.deleteBoard(boardVO) == 0) {
                throw new EgovBizException("삭제할 게시물을 찾을 수 없습니다. : " + boardId);
            }
            atcFileDAO.deleteAtcFileListOfBoard(boardId);
            fileStorageService.removeAll(atcFileList);

            egovLogger.info("게시물 삭제 : {}", boardId);
        } catch (EgovBizException e) {
            throw e;
        } catch (Exception e) {
            throw new EgovBizException("게시물을 삭제하지 못했습니다.", e);
        }
    }

    @Override
    public AtcFileVO selectAtcFile(String atcFileId) throws EgovBizException {
        try {
            AtcFileVO atcFileVO = atcFileDAO.selectAtcFile(atcFileId);
            if (atcFileVO == null) {
                throw new EgovBizException("첨부파일을 찾을 수 없습니다. : " + atcFileId);
            }
            return atcFileVO;
        } catch (EgovBizException e) {
            throw e;
        } catch (Exception e) {
            throw new EgovBizException("첨부파일을 조회하지 못했습니다.", e);
        }
    }

    /** 업로드 파일을 저장 루트에 저장하고 첨부파일 정보를 DB 에 등록한다. */
    private void saveAtcFileList(String boardId, String regId, List<MultipartFile> atcFiles) {
        if (atcFiles == null || atcFiles.isEmpty()) {
            return;
        }
        for (MultipartFile atcFile : atcFiles) {
            if (atcFile == null || atcFile.isEmpty()) {
                continue;
            }
            AtcFileVO atcFileVO = fileStorageService.store(atcFile, regId);
            atcFileVO.setAtcFileId(nextAtcFileId());
            atcFileVO.setBoardId(boardId);
            atcFileDAO.insertAtcFile(atcFileVO);
        }
    }

    /** 첨부파일 ID 발급 */
    private String nextAtcFileId() {
        try {
            return atcFileIdGnrService.getNextStringId();
        } catch (FdlException e) {
            throw new IllegalStateException("첨부파일 번호를 발급하지 못했습니다.", e);
        }
    }
}

