package egov.board.service;

import egov.board.config.FileStorageProperties;
import egov.board.service.vo.AtcFileVO;
import org.egovframe.rte.fdl.cmmn.EgovAbstractServiceImpl;
import org.egovframe.rte.fdl.filehandling.EgovFileUtil;
import org.springframework.core.io.FileSystemResource;
import org.springframework.core.io.Resource;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.io.UncheckedIOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Locale;
import java.util.Set;
import java.util.UUID;

/**
 * 첨부파일을 디스크에 저장·삭제·조회하는 파일 저장 서비스.
 *
 * <p>저장 경로는 {@code board.file.upload-dir/yyyyMMdd/UUID.확장자} 형태이고,
 * DB 에는 {@code yyyyMMdd/UUID.확장자} 상대 경로만 저장하여 서버 경로가 노출되지 않도록 한다.
 * 파일 처리에는 표준프레임워크 파일입출력(fdl-filehandling) 의 {@link EgovFileUtil} 을 사용한다.</p>
 */
@Service
public class FileStorageService extends EgovAbstractServiceImpl {

    private static final DateTimeFormatter DIR_FORMATTER = DateTimeFormatter.ofPattern("yyyyMMdd");

    private final FileStorageProperties properties;

    public FileStorageService(FileStorageProperties properties) {
        this.properties = properties;
    }

    /** 첨부파일 저장 루트 디렉터리 (절대 경로) */
    public Path getUploadRoot() {
        return Paths.get(properties.getUploadDir()).toAbsolutePath().normalize();
    }

    /**
     * 업로드 파일을 검증한 뒤 저장하고, 첨부파일 정보(DB 에 저장할 값) 를 돌려준다.
     *
     * @param uploadFile 업로드된 파일
     * @param regId      등록자
     * @return 첨부파일 정보 (atcFileId·boardId 는 호출처에서 채운다)
     */
    public AtcFileVO store(MultipartFile uploadFile, String regId) {
        String originalName = safeFileName(uploadFile.getOriginalFilename());
        String extension = extensionOf(originalName);
        validate(uploadFile, originalName, extension);

        String dateDir = LocalDate.now().format(DIR_FORMATTER);
        String storeName = UUID.randomUUID().toString().replace("-", "")
                + (extension.isEmpty() ? "" : "." + extension);

        Path target = getUploadRoot().resolve(dateDir).resolve(storeName);
        try {
            Files.createDirectories(target.getParent());
            uploadFile.transferTo(target);
        } catch (IOException e) {
            egovLogger.error("첨부파일 저장 실패 : {}", target, e);
            throw new UncheckedIOException("첨부파일을 저장하지 못했습니다.", e);
        }

        AtcFileVO atcFileVO = new AtcFileVO();
        atcFileVO.setFileNm(originalName);
        atcFileVO.setStoreFileNm(storeName);
        atcFileVO.setFilePath(dateDir + "/" + storeName);
        atcFileVO.setFileSize(uploadFile.getSize());
        atcFileVO.setFileExtsn(extension);
        atcFileVO.setRegId(regId);
        egovLogger.debug("첨부파일 저장 : {} ({})", target, uploadFile.getSize());
        return atcFileVO;
    }

    /** 첨부파일 리소스를 읽어온다. 파일이 없으면 예외를 던진다. */
    public Resource loadResource(AtcFileVO atcFileVO) {
        Path target = resolveStoredPath(atcFileVO.getFilePath());
        if (!EgovFileUtil.isExistsFile(target.toString())) {
            throw new IllegalStateException("첨부파일을 찾을 수 없습니다. : " + atcFileVO.getFileNm());
        }
        return new FileSystemResource(target);
    }

    /** 첨부파일 하나를 디스크에서 삭제한다. */
    public void remove(AtcFileVO atcFileVO) {
        if (atcFileVO == null || atcFileVO.getFilePath() == null) {
            return;
        }
        Path target = resolveStoredPath(atcFileVO.getFilePath());
        if (!EgovFileUtil.isExistsFile(target.toString())) {
            egovLogger.warn("삭제할 첨부파일이 없습니다. : {}", target);
            return;
        }
        try {
            EgovFileUtil.delete(target.toFile());
            egovLogger.debug("첨부파일 삭제 : {}", target);
        } catch (IOException e) {
            egovLogger.error("첨부파일 삭제 실패 : {}", target, e);
        }
    }

    /** 첨부파일 목록을 디스크에서 삭제한다. */
    public void removeAll(List<AtcFileVO> atcFileList) {
        if (atcFileList == null) {
            return;
        }
        for (AtcFileVO atcFileVO : atcFileList) {
            remove(atcFileVO);
        }
    }

    /** 저장 루트 밖의 경로가 오면 거부한다. (경로 조차기 방지) */
    private Path resolveStoredPath(String relativePath) {
        Path root = getUploadRoot();
        Path target = root.resolve(relativePath).normalize();
        if (!target.startsWith(root)) {
            throw new IllegalArgumentException("허용되지 않는 첨부파일 경로입니다.");
        }
        return target;
    }

    /** 클라이언트가 보내온 파일명에서 디렉터리 요소를 걷어내고 이름만 남긴다. */
    private String safeFileName(String originalFilename) {
        String cleaned = StringUtils.cleanPath(originalFilename == null ? "" : originalFilename);
        String name = Paths.get(cleaned).getFileName().toString();
        return name.isEmpty() ? "no-name" : name;
    }

    /** 표준프레임워크 {@link EgovFileUtil#getFileExtension(String)} 으로 확장자를 구해 소문자화한다. */
    private String extensionOf(String fileName) {
        String extension = EgovFileUtil.getFileExtension(fileName);
        if (extension == null) {
            return "";
        }
        return extension.replace(".", "").trim().toLowerCase(Locale.ROOT);
    }

    /** 빈 파일 여부, 크기, 허용 확장자 검증 */
    private void validate(MultipartFile uploadFile, String originalName, String extension) {
        if (uploadFile.isEmpty()) {
            throw new IllegalArgumentException("첨부파일이 비어 있습니다. : " + originalName);
        }
        if (uploadFile.getSize() > properties.getMaxSize()) {
            throw new IllegalArgumentException("첨부파일은 " + (properties.getMaxSize() / 1024 / 1024)
                    + "MB 이하여야 합니다. : " + originalName);
        }
        Set<String> allowed = properties.allowedExtensionSet();
        if (!allowed.isEmpty() && !allowed.contains(extension)) {
            throw new IllegalArgumentException("허용되지 않는 확장자의 첨부파일입니다. : " + originalName);
        }
    }
}

