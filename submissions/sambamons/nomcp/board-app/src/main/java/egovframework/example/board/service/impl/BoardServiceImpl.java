package egovframework.example.board.service.impl;

import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;

import org.egovframe.rte.fdl.cmmn.EgovAbstractServiceImpl;
import org.egovframe.rte.fdl.idgnr.EgovIdGnrService;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.core.io.Resource;
import org.springframework.core.io.UrlResource;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.transaction.support.TransactionSynchronization;
import org.springframework.transaction.support.TransactionSynchronizationManager;
import org.springframework.util.StringUtils;
import org.springframework.web.multipart.MultipartFile;

import egovframework.example.board.service.BoardDefaultVO;
import egovframework.example.board.service.BoardFileVO;
import egovframework.example.board.service.BoardService;
import egovframework.example.board.service.BoardVO;

/**
 * 게시판 서비스 구현
 */
@Service("boardService")
public class BoardServiceImpl extends EgovAbstractServiceImpl implements BoardService {

    private final BoardMapper boardMapper;

    private final EgovIdGnrService boardIdGnrService;

    private final EgovIdGnrService fileIdGnrService;

    private final BoardFileStore fileStore;

    public BoardServiceImpl(BoardMapper boardMapper,
            @Qualifier("boardIdGnrService") EgovIdGnrService boardIdGnrService,
            @Qualifier("fileIdGnrService") EgovIdGnrService fileIdGnrService,
            BoardFileStore fileStore) {
        this.boardMapper = boardMapper;
        this.boardIdGnrService = boardIdGnrService;
        this.fileIdGnrService = fileIdGnrService;
        this.fileStore = fileStore;
    }

    @Override
    public List<BoardVO> selectBoardList(BoardDefaultVO searchVO) throws Exception {
        return boardMapper.selectBoardList(searchVO);
    }

    @Override
    public int selectBoardListTotCnt(BoardDefaultVO searchVO) throws Exception {
        return boardMapper.selectBoardListTotCnt(searchVO);
    }

    @Override
    public BoardVO selectBoard(String boardId) throws Exception {
        BoardVO vo = boardMapper.selectBoard(boardId);
        if (vo == null) {
            throw processException("info.nodata.msg");
        }
        vo.setFiles(boardMapper.selectFileList(boardId));
        return vo;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public String insertBoard(BoardVO vo, List<MultipartFile> uploadFiles) throws Exception {
        String boardId = boardIdGnrService.getNextStringId();
        vo.setBoardId(boardId);
        boardMapper.insertBoard(vo);
        insertFiles(boardId, uploadFiles);
        return boardId;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void updateBoard(BoardVO vo, List<MultipartFile> uploadFiles, List<String> deleteFileIds) throws Exception {
        if (boardMapper.updateBoard(vo) == 0) {
            throw processException("info.nodata.msg");
        }
        if (deleteFileIds != null) {
            for (String fileId : deleteFileIds) {
                BoardFileVO file = boardMapper.selectFile(fileId);
                // 다른 게시물의 파일 ID 가 넘어오면 무시한다.
                if (file != null && file.getBoardId().equals(vo.getBoardId())) {
                    boardMapper.deleteFile(fileId);
                    deletePhysicalFileAfterCommit(file.getStreFileNm());
                }
            }
        }
        insertFiles(vo.getBoardId(), uploadFiles);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void deleteBoard(String boardId) throws Exception {
        List<BoardFileVO> files = boardMapper.selectFileList(boardId);
        boardMapper.deleteFilesByBoardId(boardId);
        if (boardMapper.deleteBoard(boardId) == 0) {
            throw processException("info.nodata.msg");
        }
        for (BoardFileVO file : files) {
            deletePhysicalFileAfterCommit(file.getStreFileNm());
        }
    }

    @Override
    public BoardFileVO selectFile(String fileId) throws Exception {
        BoardFileVO file = boardMapper.selectFile(fileId);
        if (file == null) {
            throw processException("info.nodata.msg");
        }
        return file;
    }

    @Override
    public Resource loadFile(BoardFileVO file) throws Exception {
        Path path = fileStore.resolve(file.getStreFileNm());
        if (!Files.isReadable(path)) {
            throw processException("info.nodata.msg");
        }
        return new UrlResource(path.toUri());
    }

    /** 업로드 파일을 디스크에 저장하고 BOARD_FILE 에 등록한다. 트랜잭션이 롤백되면 저장한 파일도 지운다. */
    private void insertFiles(String boardId, List<MultipartFile> uploadFiles) throws Exception {
        if (uploadFiles == null) {
            return;
        }
        List<String> stored = new ArrayList<>();
        registerRollbackCleanup(stored);
        for (MultipartFile uploadFile : uploadFiles) {
            if (uploadFile == null || uploadFile.isEmpty()) {
                continue;
            }
            String streFileNm = fileStore.store(uploadFile);
            stored.add(streFileNm);

            BoardFileVO file = new BoardFileVO();
            file.setFileId(fileIdGnrService.getNextStringId());
            file.setBoardId(boardId);
            file.setOrignlFileNm(StringUtils.getFilename(StringUtils.cleanPath(
                    uploadFile.getOriginalFilename() == null ? "file" : uploadFile.getOriginalFilename())));
            file.setStreFileNm(streFileNm);
            file.setFileSize(uploadFile.getSize());
            file.setContentType(uploadFile.getContentType());
            boardMapper.insertFile(file);
        }
    }

    private void registerRollbackCleanup(List<String> stored) {
        TransactionSynchronizationManager.registerSynchronization(new TransactionSynchronization() {
            @Override
            public void afterCompletion(int status) {
                if (status != STATUS_COMMITTED) {
                    stored.forEach(fileStore::delete);
                }
            }
        });
    }

    private void deletePhysicalFileAfterCommit(String streFileNm) {
        TransactionSynchronizationManager.registerSynchronization(new TransactionSynchronization() {
            @Override
            public void afterCommit() {
                fileStore.delete(streFileNm);
            }
        });
    }
}
