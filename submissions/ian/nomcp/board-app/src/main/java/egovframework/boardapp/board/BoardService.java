package egovframework.boardapp.board;

import java.io.IOException;
import java.io.UncheckedIOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.util.List;
import java.util.UUID;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

@Service
@Transactional
public class BoardService {

    private final BoardMapper mapper;
    private final Path uploadDir;

    public BoardService(BoardMapper mapper, @Value("${board.upload-dir}") String uploadDir) {
        this.mapper = mapper;
        this.uploadDir = Paths.get(uploadDir).toAbsolutePath().normalize();
    }

    @Transactional(readOnly = true)
    public List<BoardVO> list() {
        return mapper.selectBoardList();
    }

    @Transactional(readOnly = true)
    public BoardVO get(String boardId) {
        BoardVO board = mapper.selectBoard(boardId);
        if (board != null) {
            board.setFiles(mapper.selectFileList(boardId));
        }
        return board;
    }

    @Transactional(readOnly = true)
    public BoardVO getPrev(String boardId) {
        return mapper.selectPrevBoard(boardId);
    }

    @Transactional(readOnly = true)
    public BoardVO getNext(String boardId) {
        return mapper.selectNextBoard(boardId);
    }

    public String create(BoardVO board, List<MultipartFile> files) {
        board.setBoardId(mapper.nextBoardId());
        mapper.insertBoard(board);
        saveFiles(board.getBoardId(), files);
        return board.getBoardId();
    }

    public void update(BoardVO board, List<MultipartFile> files, List<String> deleteFileIds) {
        mapper.updateBoard(board);
        if (deleteFileIds != null) {
            for (String fileId : deleteFileIds) {
                BoardFileVO f = mapper.selectFile(fileId);
                // 다른 게시물의 파일을 지우지 못하도록 소속 확인
                if (f != null && f.getBoardId().equals(board.getBoardId())) {
                    mapper.deleteFile(fileId);
                    deleteQuietly(f.getStoredName());
                }
            }
        }
        saveFiles(board.getBoardId(), files);
    }

    public void delete(String boardId) {
        List<BoardFileVO> files = mapper.selectFileList(boardId);
        mapper.deleteBoard(boardId); // BOARD_FILE 은 ON DELETE CASCADE
        files.forEach(f -> deleteQuietly(f.getStoredName()));
    }

    @Transactional(readOnly = true)
    public BoardFileVO getFile(String fileId) {
        return mapper.selectFile(fileId);
    }

    public Path resolve(BoardFileVO file) {
        return uploadDir.resolve(file.getStoredName()).normalize();
    }

    private void saveFiles(String boardId, List<MultipartFile> files) {
        if (files == null) {
            return;
        }
        try {
            Files.createDirectories(uploadDir);
            for (MultipartFile mf : files) {
                if (mf == null || mf.isEmpty()) {
                    continue;
                }
                String stored = UUID.randomUUID().toString();
                Files.copy(mf.getInputStream(), uploadDir.resolve(stored), StandardCopyOption.REPLACE_EXISTING);

                BoardFileVO f = new BoardFileVO();
                f.setFileId(mapper.nextFileId());
                f.setBoardId(boardId);
                f.setOrigName(cleanName(mf.getOriginalFilename()));
                f.setStoredName(stored);
                f.setFileSize(mf.getSize());
                f.setContentType(mf.getContentType());
                mapper.insertFile(f);
            }
        } catch (IOException e) {
            throw new UncheckedIOException(e);
        }
    }

    private String cleanName(String name) {
        if (name == null || name.isBlank()) {
            return "file";
        }
        String n = Paths.get(name.replace('\\', '/')).getFileName().toString();
        return n.length() > 255 ? n.substring(n.length() - 255) : n;
    }

    private void deleteQuietly(String storedName) {
        try {
            Files.deleteIfExists(uploadDir.resolve(storedName));
        } catch (IOException ignored) {
            // 파일 삭제 실패는 DB 처리에 영향을 주지 않는다
        }
    }
}
