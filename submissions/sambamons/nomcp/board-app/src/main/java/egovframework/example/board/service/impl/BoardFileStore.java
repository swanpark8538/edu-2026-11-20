package egovframework.example.board.service.impl;

import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.util.UUID;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.web.multipart.MultipartFile;

/**
 * 첨부파일을 디스크에 저장/삭제한다. 저장 파일명은 UUID 로 만들어 원본 파일명을 경로에 쓰지 않는다.
 */
@Component
public class BoardFileStore {

    private static final Logger LOGGER = LoggerFactory.getLogger(BoardFileStore.class);

    private final Path rootDir;

    public BoardFileStore(@Value("${board.upload-dir}") String uploadDir) throws IOException {
        this.rootDir = Paths.get(uploadDir).toAbsolutePath().normalize();
        Files.createDirectories(rootDir);
    }

    /** 파일을 저장하고 저장 파일명을 돌려준다. */
    public String store(MultipartFile file) throws IOException {
        String streFileNm = UUID.randomUUID().toString().replace("-", "");
        try (InputStream in = file.getInputStream()) {
            Files.copy(in, resolve(streFileNm), StandardCopyOption.REPLACE_EXISTING);
        }
        return streFileNm;
    }

    public Path resolve(String streFileNm) {
        Path path = rootDir.resolve(streFileNm).normalize();
        if (!path.startsWith(rootDir)) {
            throw new IllegalArgumentException("잘못된 파일 경로입니다.");
        }
        return path;
    }

    public void delete(String streFileNm) {
        try {
            Files.deleteIfExists(resolve(streFileNm));
        } catch (IOException e) {
            LOGGER.warn("첨부파일 삭제 실패: {}", streFileNm, e);
        }
    }
}
