package egovframework.board.service.impl;

import java.io.IOException;
import java.io.UncheckedIOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;

import org.egovframe.rte.fdl.cmmn.EgovAbstractServiceImpl;
import org.egovframe.rte.fdl.cmmn.exception.BaseRuntimeException;
import org.egovframe.rte.fdl.cmmn.exception.FdlException;
import org.egovframe.rte.fdl.idgnr.EgovIdGnrService;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import egovframework.board.service.BoardFileVO;
import egovframework.board.service.BoardService;
import egovframework.board.service.BoardVO;
import jakarta.annotation.PostConstruct;
import lombok.extern.slf4j.Slf4j;

@Service
@Slf4j
public class BoardServiceImpl extends EgovAbstractServiceImpl implements BoardService {

    private static final int MAX_FILES = 5;

    private final BoardMapper boardMapper;
    private final BoardFileMapper boardFileMapper;
    private final EgovIdGnrService boardIdGnrService;
    private final EgovIdGnrService fileIdGnrService;
    private final Path storeRoot;

    public BoardServiceImpl(BoardMapper boardMapper,
                            BoardFileMapper boardFileMapper,
                            @Qualifier("boardIdGnrService") EgovIdGnrService boardIdGnrService,
                            @Qualifier("fileIdGnrService") EgovIdGnrService fileIdGnrService,
                            @Value("${board.file.store-path}") String storePath) {
        this.boardMapper = boardMapper;
        this.boardFileMapper = boardFileMapper;
        this.boardIdGnrService = boardIdGnrService;
        this.fileIdGnrService = fileIdGnrService;
        this.storeRoot = Path.of(storePath).toAbsolutePath().normalize();
    }

    @PostConstruct
    void initStore() throws IOException {
        Files.createDirectories(storeRoot);
    }

    @Override
    public List<BoardVO> selectBoardList(BoardVO boardVO) {
        return boardMapper.selectBoardList(boardVO);
    }

    @Override
    public int selectBoardListTotCnt(BoardVO boardVO) {
        return boardMapper.selectBoardListTotCnt(boardVO);
    }

    @Override
    public BoardVO selectBoard(BoardVO boardVO) {
        BoardVO result = boardMapper.selectBoard(boardVO);
        if (result == null) {
            throw new BaseRuntimeException(processException("info.nodata.msg"));
        }
        return result;
    }

    @Override
    public String insertBoard(BoardVO boardVO, List<MultipartFile> files) {
        boardVO.setBoardId(nextId(boardIdGnrService));
        boardMapper.insertBoard(boardVO);
        saveFiles(boardVO.getBoardId(), files);
        return boardVO.getBoardId();
    }

    @Override
    public void updateBoard(BoardVO boardVO, List<MultipartFile> files) {
        BoardVO stored = selectBoard(boardVO);
        boardVO.setBoardId(stored.getBoardId());
        boardMapper.updateBoard(boardVO);
        saveFiles(boardVO.getBoardId(), files);
    }

    @Override
    public void deleteBoard(BoardVO boardVO) {
        BoardVO stored = selectBoard(boardVO);
        List<BoardFileVO> files = boardFileMapper.selectBoardFileList(stored.getBoardId());
        boardFileMapper.deleteBoardFileByBoardId(stored.getBoardId());
        boardMapper.deleteBoard(stored);
        for (BoardFileVO file : files) {
            deleteQuietly(resolveStoredFile(file));
        }
        deleteQuietly(storeRoot.resolve(stored.getBoardId()));
    }

    @Override
    public List<BoardFileVO> selectFileList(String boardId) {
        return boardFileMapper.selectBoardFileList(boardId);
    }

    @Override
    public BoardFileVO selectFile(String fileId) {
        BoardFileVO file = boardFileMapper.selectBoardFile(fileId);
        if (file == null) {
            throw new BaseRuntimeException(processException("info.nodata.msg"));
        }
        return file;
    }

    @Override
    public Path resolveStoredFile(BoardFileVO fileVO) {
        Path directory = Path.of(fileVO.getFilePath()).toAbsolutePath().normalize();
        Path target = directory.resolve(fileVO.getSaveFileNm()).normalize();
        if (!directory.startsWith(storeRoot) || !target.startsWith(directory)) {
            throw new BaseRuntimeException(processException("info.nodata.msg"));
        }
        return target;
    }

    @Override
    public void deleteFile(String fileId) {
        BoardFileVO file = selectFile(fileId);
        boardFileMapper.deleteBoardFile(fileId);
        deleteQuietly(resolveStoredFile(file));
    }

    private void saveFiles(String boardId, List<MultipartFile> files) {
        List<MultipartFile> accepted = acceptedFiles(files);
        if (accepted.isEmpty()) {
            return;
        }
        Path directory = storeRoot.resolve(boardId).normalize();
        try {
            Files.createDirectories(directory);
        } catch (IOException e) {
            throw new UncheckedIOException(e);
        }

        int fileSn = boardFileMapper.selectNextFileSn(boardId);
        List<Path> stored = new ArrayList<>();
        try {
            for (MultipartFile file : accepted) {
                String fileId = nextId(fileIdGnrService);
                String originalName = sanitizeFileName(file.getOriginalFilename());
                String saveName = fileId + "_" + originalName;
                Path target = directory.resolve(saveName).normalize();
                if (!target.startsWith(directory)) {
                    throw new BaseRuntimeException(processException("info.nodata.msg"));
                }
                file.transferTo(target);
                stored.add(target);

                BoardFileVO fileVO = new BoardFileVO();
                fileVO.setFileId(fileId);
                fileVO.setBoardId(boardId);
                fileVO.setFileSn(fileSn++);
                fileVO.setOrgFileNm(originalName);
                fileVO.setSaveFileNm(saveName);
                fileVO.setFilePath(directory.toString());
                fileVO.setFileSize(file.getSize());
                fileVO.setContentType(file.getContentType());
                boardFileMapper.insertBoardFile(fileVO);
            }
        } catch (RuntimeException | IOException e) {
            for (Path path : stored) {
                deleteQuietly(path);
            }
            if (e instanceof RuntimeException runtimeException) {
                throw runtimeException;
            }
            throw new UncheckedIOException((IOException) e);
        }
    }

    private List<MultipartFile> acceptedFiles(List<MultipartFile> files) {
        List<MultipartFile> accepted = new ArrayList<>();
        if (files == null) {
            return accepted;
        }
        for (MultipartFile file : files) {
            if (file == null || file.isEmpty()) {
                continue;
            }
            accepted.add(file);
        }
        if (accepted.size() > MAX_FILES) {
            throw new BaseRuntimeException("첨부파일은 한 번에 " + MAX_FILES + "개까지 등록할 수 있습니다.");
        }
        return accepted;
    }

    private String sanitizeFileName(String original) {
        String name = (original == null || original.isBlank()) ? "file" : original;
        name = Path.of(name).getFileName().toString().replaceAll("[\\\\/:*?\"<>|]", "_");
        if (name.isBlank() || ".".equals(name) || "..".equals(name)) {
            return "file";
        }
        return name.length() > 120 ? name.substring(name.length() - 120) : name;
    }

    private String nextId(EgovIdGnrService idGnrService) {
        try {
            return idGnrService.getNextStringId();
        } catch (FdlException e) {
            throw new BaseRuntimeException(e);
        }
    }

    private void deleteQuietly(Path path) {
        try {
            Files.deleteIfExists(path);
        } catch (IOException e) {
            log.warn("file delete failed: {}", path, e);
        }
    }
}
