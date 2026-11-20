package egov.board.config;

import org.springframework.boot.context.properties.ConfigurationProperties;

import java.util.Arrays;
import java.util.LinkedHashSet;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * 첨부파일 저장 설정 (application.properties 의 {@code board.file.*}).
 */
@ConfigurationProperties(prefix = "board.file")
public class FileStorageProperties {

    /** 첨부파일을 저장할 루트 디렉터리 */
    private String uploadDir = "data/upload";

    /** 업로드를 허용할 확장자 (소문자, 점 제외, 쉼표 구분) */
    private String allowedExtensions = "jpg,jpeg,gif,png,pdf,txt,hwp,doc,xls,ppt,docx,xlsx,pptx,zip";

    /** 첨부파일 하나의 최대 크기 (byte) */
    private long maxSize = 10L * 1024L * 1024L;

    public String getUploadDir() {
        return uploadDir;
    }

    public void setUploadDir(String uploadDir) {
        this.uploadDir = uploadDir;
    }

    public String getAllowedExtensions() {
        return allowedExtensions;
    }

    public void setAllowedExtensions(String allowedExtensions) {
        this.allowedExtensions = allowedExtensions;
    }

    public long getMaxSize() {
        return maxSize;
    }

    public void setMaxSize(long maxSize) {
        this.maxSize = maxSize;
    }

    /** 허용 확장자 집합 (소문자) */
    public Set<String> allowedExtensionSet() {
        return Arrays.stream(allowedExtensions == null ? new String[0] : allowedExtensions.split(","))
                .map(String::trim)
                .map(ext -> ext.toLowerCase())
                .filter(ext -> !ext.isEmpty())
                .collect(Collectors.toCollection(LinkedHashSet::new));
    }
}
