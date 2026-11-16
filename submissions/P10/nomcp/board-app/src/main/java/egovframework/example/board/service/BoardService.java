package egovframework.example.board.service;

import java.io.IOException;
import java.io.UncheckedIOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;
import java.util.UUID;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.FileSystemResource;
import org.springframework.core.io.Resource;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

@Service
public class BoardService {

    private final BoardMapper mapper;
    private final Path uploadDir;

    public BoardService(BoardMapper mapper, @Value("${board.upload-dir}") String uploadDir) throws IOException {
        this.mapper = mapper;
        this.uploadDir = Paths.get(uploadDir).toAbsolutePath().normalize();
        Files.createDirectories(this.uploadDir);
    }

    public List<BoardVO> list() {
        return mapper.selectList();
    }

    public BoardVO get(String boardId) {
        BoardVO vo = mapper.select(boardId);
        if (vo != null) {
            vo.setFiles(mapper.selectFiles(boardId));
        }
        return vo;
    }

    @Transactional
    public String create(BoardVO vo, List<MultipartFile> files) {
        vo.setBoardId(mapper.nextId());
        mapper.insert(vo);
        saveFiles(vo.getBoardId(), files);
        return vo.getBoardId();
    }

    @Transactional
    public void update(BoardVO vo, List<MultipartFile> files, List<String> deleteFileIds) {
        mapper.update(vo);
        if (deleteFileIds != null) {
            for (String fileId : deleteFileIds) {
                BoardFileVO f = mapper.selectFile(fileId);
                if (f != null && f.getBoardId().equals(vo.getBoardId())) {
                    mapper.deleteFile(fileId);
                    deletePhysical(f);
                }
            }
        }
        saveFiles(vo.getBoardId(), files);
    }

    @Transactional
    public void delete(String boardId) {
        List<BoardFileVO> files = mapper.selectFiles(boardId);
        mapper.delete(boardId);
        files.forEach(this::deletePhysical);
    }

    public BoardFileVO getFile(String fileId) {
        return mapper.selectFile(fileId);
    }

    public Resource loadFile(BoardFileVO f) {
        return new FileSystemResource(uploadDir.resolve(f.getStoredName()));
    }

    private void saveFiles(String boardId, List<MultipartFile> files) {
        if (files == null) {
            return;
        }
        for (MultipartFile mf : files) {
            if (mf == null || mf.isEmpty()) {
                continue;
            }
            BoardFileVO f = new BoardFileVO();
            f.setFileId(UUID.randomUUID().toString());
            f.setBoardId(boardId);
            String orig = Paths.get(String.valueOf(mf.getOriginalFilename())).getFileName().toString();
            f.setOrigName(orig);
            f.setStoredName(UUID.randomUUID().toString());
            f.setFileSize(mf.getSize());
            try {
                mf.transferTo(uploadDir.resolve(f.getStoredName()));
            } catch (IOException e) {
                throw new UncheckedIOException(e);
            }
            mapper.insertFile(f);
        }
    }

    private void deletePhysical(BoardFileVO f) {
        try {
            Files.deleteIfExists(uploadDir.resolve(f.getStoredName()));
        } catch (IOException ignored) {
            // 물리 파일 삭제 실패는 무시
        }
    }
}
