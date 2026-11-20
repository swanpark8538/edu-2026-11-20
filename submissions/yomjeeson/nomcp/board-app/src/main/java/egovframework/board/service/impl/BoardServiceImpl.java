package egovframework.board.service.impl;

import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import javax.annotation.PostConstruct;

import org.egovframe.rte.fdl.cmmn.EgovAbstractServiceImpl;
import org.egovframe.rte.fdl.idgnr.EgovIdGnrService;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.transaction.support.TransactionSynchronization;
import org.springframework.transaction.support.TransactionSynchronizationManager;
import org.springframework.util.StringUtils;
import org.springframework.web.multipart.MultipartFile;

import egovframework.board.service.BoardFileVO;
import egovframework.board.service.BoardService;
import egovframework.board.service.BoardVO;

@Service("boardService")
public class BoardServiceImpl extends EgovAbstractServiceImpl implements BoardService {

    private final BoardMapper boardMapper;
    private final EgovIdGnrService boardIdGnrService;
    private final Path uploadDir;

    public BoardServiceImpl(BoardMapper boardMapper,
                            @Qualifier("boardIdGnrService") EgovIdGnrService boardIdGnrService,
                            @Value("${board.upload-dir}") String uploadDir) {
        this.boardMapper = boardMapper;
        this.boardIdGnrService = boardIdGnrService;
        this.uploadDir = Paths.get(uploadDir).toAbsolutePath().normalize();
    }

    @PostConstruct
    void init() throws IOException {
        Files.createDirectories(uploadDir);
        egovLogger.info("첨부파일 저장 경로: {}", uploadDir);
    }

    @Override
    public List<BoardVO> selectBoardList(BoardVO searchVO) {
        return boardMapper.selectBoardList(searchVO);
    }

    @Override
    public int selectBoardListTotCnt(BoardVO searchVO) {
        return boardMapper.selectBoardListTotCnt(searchVO);
    }

    @Override
    public BoardVO selectBoard(String boardId) {
        BoardVO board = boardMapper.selectBoard(boardId);
        if (board != null) {
            board.setFiles(boardMapper.selectFileList(boardId));
        }
        return board;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public String insertBoard(BoardVO boardVO, List<MultipartFile> files) throws Exception {
        String boardId = boardIdGnrService.getNextStringId();
        boardVO.setBoardId(boardId);
        boardMapper.insertBoard(boardVO);
        saveFiles(boardId, files);
        return boardId;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void updateBoard(BoardVO boardVO, List<MultipartFile> files, List<Long> deleteFileIds) throws Exception {
        if (boardMapper.updateBoard(boardVO) == 0) {
            throw new IllegalArgumentException("게시물이 존재하지 않습니다: " + boardVO.getBoardId());
        }
        if (deleteFileIds != null) {
            for (Long fileId : deleteFileIds) {
                BoardFileVO file = boardMapper.selectFile(fileId);
                // 다른 게시물의 파일은 삭제하지 않는다
                if (file != null && file.getBoardId().equals(boardVO.getBoardId())) {
                    boardMapper.deleteFile(fileId);
                    deleteAfterCommit(file);
                }
            }
        }
        saveFiles(boardVO.getBoardId(), files);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void deleteBoard(String boardId) throws Exception {
        List<BoardFileVO> files = boardMapper.selectFileList(boardId);
        boardMapper.deleteFilesByBoard(boardId);
        if (boardMapper.deleteBoard(boardId) == 0) {
            throw new IllegalArgumentException("게시물이 존재하지 않습니다: " + boardId);
        }
        files.forEach(this::deleteAfterCommit);
    }

    @Override
    public BoardFileVO selectFile(Long fileId) {
        return boardMapper.selectFile(fileId);
    }

    @Override
    public Path resolveFilePath(BoardFileVO fileVO) {
        Path path = uploadDir.resolve(fileVO.getStoredName()).normalize();
        if (!path.startsWith(uploadDir)) {
            throw new IllegalStateException("잘못된 파일 경로입니다.");
        }
        return path;
    }

    private void saveFiles(String boardId, List<MultipartFile> files) throws IOException {
        if (files == null) {
            return;
        }
        List<Path> saved = new ArrayList<>();
        try {
            for (MultipartFile mf : files) {
                if (mf == null || mf.isEmpty()) {
                    continue;
                }
                String originalName = StringUtils.cleanPath(
                        mf.getOriginalFilename() == null ? "file" : mf.getOriginalFilename());
                // 경로가 포함된 경우 파일명만 사용
                originalName = originalName.substring(originalName.lastIndexOf('/') + 1);
                String ext = StringUtils.getFilenameExtension(originalName);
                String storedName = UUID.randomUUID().toString().replace("-", "")
                        + (ext == null ? "" : "." + ext);
                Path target = uploadDir.resolve(storedName);
                try (InputStream in = mf.getInputStream()) {
                    Files.copy(in, target, StandardCopyOption.REPLACE_EXISTING);
                }
                saved.add(target);

                BoardFileVO fileVO = new BoardFileVO();
                fileVO.setBoardId(boardId);
                fileVO.setOriginalName(originalName);
                fileVO.setStoredName(storedName);
                fileVO.setFileSize(mf.getSize());
                fileVO.setContentType(mf.getContentType());
                boardMapper.insertFile(fileVO);
            }
        } catch (IOException | RuntimeException e) {
            // DB 는 롤백되므로 디스크에 저장한 파일도 정리한다
            for (Path p : saved) {
                Files.deleteIfExists(p);
            }
            throw e;
        }
    }

    /** 트랜잭션 커밋 후 실제 파일 삭제 (롤백되면 파일 유지) */
    private void deleteAfterCommit(BoardFileVO file) {
        Path path = resolveFilePath(file);
        if (TransactionSynchronizationManager.isSynchronizationActive()) {
            TransactionSynchronizationManager.registerSynchronization(new TransactionSynchronization() {
                @Override
                public void afterCommit() {
                    deleteQuietly(path);
                }
            });
        } else {
            deleteQuietly(path);
        }
    }

    private void deleteQuietly(Path path) {
        try {
            Files.deleteIfExists(path);
        } catch (IOException e) {
            egovLogger.warn("첨부파일 삭제 실패: {}", path, e);
        }
    }
}
