package egovframework.example.cmmn.file.service.impl;

import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.util.UUID;
import java.util.regex.Pattern;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.web.multipart.MultipartFile;

/**
 * Globals.fileStorePath 아래에 첨부파일을 저장·삭제한다.
 * 저장 파일명은 추측할 수 없는 난수(UUID)이며 확장자를 붙이지 않아 서버에서 실행되지 않는다.
 */
@Component("egovFileStorage")
public class EgovFileStorage {

    private static final Logger LOGGER = LoggerFactory.getLogger(EgovFileStorage.class);

    private static final Pattern STORED_NAME_PATTERN = Pattern.compile("^[0-9a-f]{32}$");

    private final Path storeRoot;

    public EgovFileStorage(@Value("${Globals.fileStorePath}") String fileStorePath) throws IOException {
        this.storeRoot = Paths.get(fileStorePath).toAbsolutePath().normalize();
        Files.createDirectories(storeRoot);
        LOGGER.info("첨부파일 저장 경로: {}", storeRoot);
    }

    public String store(MultipartFile file) throws IOException {
        String storedName = UUID.randomUUID().toString().replace("-", "");
        Path target = resolve(storedName);
        try (InputStream in = file.getInputStream()) {
            Files.copy(in, target, StandardCopyOption.REPLACE_EXISTING);
        }
        return storedName;
    }

    public void delete(String storedName) {
        try {
            Files.deleteIfExists(resolve(storedName));
        } catch (IOException | IllegalArgumentException e) {
            LOGGER.warn("첨부파일 삭제 실패: {}", storedName);
        }
    }

    /** 저장 파일명이 규칙에 맞고 저장 경로 밖을 가리키지 않을 때만 경로를 돌려준다. */
    public Path resolve(String storedName) {
        if (storedName == null || !STORED_NAME_PATTERN.matcher(storedName).matches()) {
            throw new IllegalArgumentException("invalid stored file name");
        }
        Path path = storeRoot.resolve(storedName).normalize();
        if (!path.startsWith(storeRoot)) {
            throw new IllegalArgumentException("invalid stored file path");
        }
        return path;
    }

}
