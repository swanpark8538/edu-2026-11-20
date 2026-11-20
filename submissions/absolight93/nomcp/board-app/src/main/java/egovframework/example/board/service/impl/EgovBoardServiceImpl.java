package egovframework.example.board.service.impl;

import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Path;
import java.util.List;
import java.util.Objects;

import org.egovframe.rte.fdl.cmmn.EgovAbstractServiceImpl;
import org.egovframe.rte.fdl.cmmn.exception.EgovBizException;
import org.egovframe.rte.fdl.filehandling.EgovFileUtil;
import org.egovframe.rte.fdl.idgnr.EgovIdGnrService;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;
import org.springframework.web.multipart.MultipartFile;

import egovframework.example.board.persist.BoardFileMapper;
import egovframework.example.board.persist.BoardMapper;
import egovframework.example.board.service.EgovBoardService;
import egovframework.example.board.service.vo.BoardFileVO;
import egovframework.example.board.service.vo.BoardSearchVO;
import egovframework.example.board.service.vo.BoardVO;

/**
 * 게시판 업무를 처리하는 서비스 구현.
 *
 * <p>전자정부 공통컴포넌트 {@link EgovAbstractServiceImpl} 을 상속받아
 * 표준 예외 처리({@link EgovBizException})와 이력 처리(leaveaTrace)를 사용한다.</p>
 */
@Service
public class EgovBoardServiceImpl extends EgovAbstractServiceImpl implements EgovBoardService {

    private final BoardMapper boardMapper;

    private final BoardFileMapper boardFileMapper;

    private final EgovBoardFileStore egovBoardFileStore;

    private final EgovIdGnrService egovBoardIdGnrService;

    private final EgovIdGnrService egovAtchfileIdGnrService;

    public EgovBoardServiceImpl(BoardMapper boardMapper, BoardFileMapper boardFileMapper,
            EgovBoardFileStore egovBoardFileStore,
            @Qualifier("egovBoardIdGnrService") EgovIdGnrService egovBoardIdGnrService,
            @Qualifier("egovAtchfileIdGnrService") EgovIdGnrService egovAtchfileIdGnrService) {
        this.boardMapper = boardMapper;
        this.boardFileMapper = boardFileMapper;
        this.egovBoardFileStore = egovBoardFileStore;
        this.egovBoardIdGnrService = egovBoardIdGnrService;
        this.egovAtchfileIdGnrService = egovAtchfileIdGnrService;
    }

    /**
     * 목록 건수 조회
     */
    @Override
    public int countBoardList(BoardSearchVO searchVO) throws Exception {
        return boardMapper.countBoardList(searchVO);
    }

    /**
     * 목록 조회
     */
    @Override
    public List<BoardVO> selectBoardList(BoardSearchVO searchVO) throws Exception {
        leaveaTrace("info.board.selectBoardList");
        return boardMapper.selectBoardList(searchVO);
    }

    /**
     * 상세 조회 (조회 수 증가 후 반환)
     */
    @Override
    @Transactional
    public BoardVO readBoard(String boardId) throws Exception {
        boardMapper.updateBoardReadcnt(boardId);
        return selectBoard(boardId);
    }

    /**
     * 단건 조회 (첨부파일 포함, 조회 수 변동 없음)
     */
    @Override
    public BoardVO selectBoard(String boardId) throws Exception {
        BoardVO boardVO = boardMapper.selectBoard(boardId);
        if (boardVO == null) {
            throw (EgovBizException) processException("error.board.notFound", new String[] { boardId });
        }
        boardVO.setBoardFileList(boardFileMapper.selectBoardFileList(boardId));
        return boardVO;
    }

    /**
     * 게시물 등록 + 첨부파일 저장
     */
    @Override
    @Transactional
    public String insertBoard(BoardVO boardVO, List<MultipartFile> saveFiles) throws Exception {
        boardVO.setBoardId(egovBoardIdGnrService.getNextStringId());

        try {
            boardMapper.insertBoard(boardVO);
        } catch (Exception e) {
            throw (EgovBizException) processException("error.board.failInsert", new String[] {}, e);
        }

        saveBoardFileList(boardVO.getBoardId(), saveFiles);
        return boardVO.getBoardId();
    }

    /**
     * 게시물 수정 + 첨부파일 추가/삭제
     */
    @Override
    @Transactional
    public void updateBoard(BoardVO boardVO, List<MultipartFile> saveFiles, List<String> deleteAtchfileIds)
            throws Exception {
        BoardVO before = boardMapper.selectBoard(boardVO.getBoardId());
        if (before == null) {
            throw (EgovBizException) processException("error.board.notFound",
                    new String[] { boardVO.getBoardId() });
        }

        try {
            // 작성자는 원본 값을 유지한다.
            boardVO.setWriter(before.getWriter());
            boardMapper.updateBoard(boardVO);

            if (deleteAtchfileIds != null) {
                for (String atchfileId : deleteAtchfileIds) {
                    if (StringUtils.hasText(atchfileId)) {
                        deleteBoardFile(atchfileId);
                    }
                }
            }
            saveBoardFileList(boardVO.getBoardId(), saveFiles);
        } catch (EgovBizException e) {
            throw e;
        } catch (Exception e) {
            throw (EgovBizException) processException("error.board.failUpdate", new String[] {}, e);
        }
    }

    /**
     * 게시물 삭제 (첨부파일까지 삭제)
     */
    @Override
    @Transactional
    public void deleteBoard(String boardId) throws Exception {
        List<BoardFileVO> boardFileList = boardFileMapper.selectBoardFileList(boardId);

        try {
            boardFileMapper.deleteBoardFileByBoardId(boardId);
            if (boardMapper.deleteBoard(boardId) == 0) {
                throw (EgovBizException) processException("error.board.notFound", new String[] { boardId });
            }
        } catch (EgovBizException e) {
            throw e;
        } catch (Exception e) {
            throw (EgovBizException) processException("error.board.failDelete", new String[] {}, e);
        }

        // DB 반영 후 실제 파일 정리 (실패해도 게시물 삭제를 되돌리지 않는다)
        for (BoardFileVO boardFileVO : boardFileList) {
            try {
                egovBoardFileStore.remove(boardFileVO);
            } catch (IOException e) {
                egovLogger.warn("첨부파일 물리 삭제 실패 : {}", boardFileVO.getFileStreNm(), e);
            }
        }
    }

    /**
     * 게시물별 첨부파일 목록 조회
     */
    @Override
    public List<BoardFileVO> selectBoardFileList(String boardId) throws Exception {
        return boardFileMapper.selectBoardFileList(boardId);
    }

    /**
     * 첨부파일 상세 조회
     */
    @Override
    public BoardFileVO selectBoardFile(String atchfileId) throws Exception {
        BoardFileVO boardFileVO = boardFileMapper.selectBoardFile(atchfileId);
        if (boardFileVO == null) {
            throw (EgovBizException) processException("error.board.fileNotFound", new String[] { atchfileId });
        }
        return boardFileVO;
    }

    /**
     * 첨부파일 단건 삭제 (DB + 실제 파일)
     */
    @Override
    @Transactional
    public void deleteBoardFile(String atchfileId) throws Exception {
        BoardFileVO boardFileVO = selectBoardFile(atchfileId);
        boardFileMapper.deleteBoardFile(atchfileId);
        try {
            egovBoardFileStore.remove(boardFileVO);
        } catch (IOException e) {
            throw (EgovBizException) processException("error.board.failFileDelete",
                    new String[] { boardFileVO.getFileOrglnm() }, e);
        }
    }

    /**
     * 첨부파일 저장 경로 확인
     */
    @Override
    public Path getStoredFilePath(BoardFileVO boardFileVO) throws Exception {
        return egovBoardFileStore.locate(boardFileVO);
    }

    /**
     * 업로드 파일을 디스크에 저장하고 BOARD_FILE 에 등록한다.
     */
    private void saveBoardFileList(String boardId, List<MultipartFile> saveFiles) throws Exception {
        if (saveFiles == null || saveFiles.isEmpty()) {
            return;
        }

        for (MultipartFile saveFile : saveFiles) {
            if (saveFile == null || saveFile.isEmpty()) {
                continue;
            }

            String fileOrglnm = StringUtils
                    .cleanPath(Objects.requireNonNullElse(saveFile.getOriginalFilename(), "unknown.dat"));
            String fileStreNm = egovBoardFileStore.makeStoredFileName(fileOrglnm);

            BoardFileVO boardFileVO = new BoardFileVO();
            boardFileVO.setAtchfileId(egovAtchfileIdGnrService.getNextStringId());
            boardFileVO.setBoardId(boardId);
            boardFileVO.setFileOrglnm(fileOrglnm);
            boardFileVO.setFileStreNm(fileStreNm);
            boardFileVO.setFileExtsn(EgovFileUtil.getFileExtension(fileOrglnm));
            boardFileVO.setFileSize(saveFile.getSize());

            try (InputStream inputStream = saveFile.getInputStream()) {
                Path storedPath = egovBoardFileStore.store(fileStreNm, inputStream);
                boardFileVO.setFileStrePath(storedPath.getParent().toString());
                boardFileMapper.insertBoardFile(boardFileVO);
            } catch (IOException e) {
                throw (EgovBizException) processException("error.board.failAttachSave",
                        new String[] { fileOrglnm }, e);
            }
        }
    }

}
