package egovframework.example.board.service.impl;

import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.util.Locale;
import java.util.UUID;

import org.egovframe.rte.fdl.filehandling.EgovFileUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;

import egovframework.example.board.service.vo.BoardFileVO;

/**
 * 첨부파일 저장소.
 *
 * <p>eGovFrame {@code EgovFileUtil}(FDL-FILEHANDLING) 을 활용해 확장자를 추출하고
 * 파일을 삭제하며, 실제 저장 파일명은 UUID 로 생성해 중복/경로 조작을 방지한다.</p>
 */
@Component
public class EgovBoardFileStore {

    private static final Logger LOGGER = LoggerFactory.getLogger(EgovBoardFileStore.class);

    private final Path baseDir;

    public EgovBoardFileStore(@Value("${egov.board.attach-path:./upload-files}") String attachPath)
            throws IOException {
        this.baseDir = Paths.get(attachPath).toAbsolutePath().normalize();
        Files.createDirectories(this.baseDir);
        LOGGER.info("첨부파일 저장 경로 : {}", this.baseDir);
    }

    public Path getBaseDir() {
        return baseDir;
    }

    /**
     * 저장 파일명 생성 (원본 확장자 유지, 이름은 UUID)
     */
    public String makeStoredFileName(String originalFileName) {
        String uuid = UUID.randomUUID().toString().replace("-", "");
        String extsn = EgovFileUtil.getFileExtension(originalFileName);
        if (StringUtils.hasText(extsn)) {
            return uuid + "." + extsn.toLowerCase(Locale.KOREA);
        }
        return uuid;
    }

    /**
     * 업로드된 파일을 저장 경로에 기록한다.
     */
    public Path store(String storedFileName, InputStream inputStream) throws IOException {
        Path target = resolveSafely(storedFileName);
        Files.createDirectories(target.getParent());
        Files.copy(inputStream, target, StandardCopyOption.REPLACE_EXISTING);
        return target;
    }

    /**
     * 저장된 파일의 경로를 반환한다.
     */
    public Path locate(BoardFileVO boardFileVO) throws IOException {
        Path target = resolveSafely(boardFileVO.getFileStreNm());
        if (!EgovFileUtil.isExistsFile(target.toString())) {
            throw new IOException("저장된 첨부파일을 찾을 수 없습니다. : " + boardFileVO.getFileStreNm());
        }
        return target;
    }

    /**
     * 저장된 파일을 삭제한다. 파일이 없으면 무시한다.
     */
    public void remove(BoardFileVO boardFileVO) throws IOException {
        Path target = resolveSafely(boardFileVO.getFileStreNm());
        if (EgovFileUtil.isExistsFile(target.toString())) {
            EgovFileUtil.delete(target.toFile());
            LOGGER.debug("첨부파일 삭제 : {}", target);
        }
    }

    /**
     * 저장 경로 바깥으로 나가지 않도록 경로를 검증한다. (경로 조작 방지)
     */
    private Path resolveSafely(String fileName) throws IOException {
        String cleanName = StringUtils.getFilename(fileName);
        if (!StringUtils.hasText(cleanName) || cleanName.contains("..")) {
            throw new IOException("올바르지 않은 파일명입니다. : " + fileName);
        }
        Path target = baseDir.resolve(cleanName).normalize();
        if (!target.startsWith(baseDir)) {
            throw new IOException("첨부파일 저장 경로를 벗어날 수 없습니다. : " + fileName);
        }
        return target;
    }

}
