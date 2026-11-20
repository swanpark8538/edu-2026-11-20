package egov.board.service.vo;

import java.io.Serializable;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

/**
 * 게시물 첨부파일 데이터(ATC_FILE 테이블)를 담는 값 객체.
 *
 * <p>{@code filePath} 는 첨부파일 저장 루트(board.file.upload-dir)에 대한 상대 경로로 저장한다.</p>
 */
public class AtcFileVO implements Serializable {

    private static final long serialVersionUID = 1L;

    private static final DateTimeFormatter VIEW_FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm");

    /** 첨부파일 ID */
    private String atcFileId;

    /** 게시물 ID */
    private String boardId;

    /** 원본 파일명 (업로드 시 사용자가 지정했던 파일명) */
    private String fileNm;

    /** 저장 파일명 (서버에 저장된 파일명, UUID + 확장자) */
    private String storeFileNm;

    /** 저장 상대 경로 (예: 20261120/0a1b2c....png) */
    private String filePath;

    /** 파일 크기 (byte) */
    private long fileSize;

    /** 파일 확장자 (소문자, 점 제외) */
    private String fileExtsn;

    /** 등록자 */
    private String regId;

    /** 등록일 */
    private LocalDateTime regDt;

    public String getAtcFileId() {
        return atcFileId;
    }

    public void setAtcFileId(String atcFileId) {
        this.atcFileId = atcFileId;
    }

    public String getBoardId() {
        return boardId;
    }

    public void setBoardId(String boardId) {
        this.boardId = boardId;
    }

    public String getFileNm() {
        return fileNm;
    }

    public void setFileNm(String fileNm) {
        this.fileNm = fileNm;
    }

    public String getStoreFileNm() {
        return storeFileNm;
    }

    public void setStoreFileNm(String storeFileNm) {
        this.storeFileNm = storeFileNm;
    }

    public String getFilePath() {
        return filePath;
    }

    public void setFilePath(String filePath) {
        this.filePath = filePath;
    }

    public long getFileSize() {
        return fileSize;
    }

    public void setFileSize(long fileSize) {
        this.fileSize = fileSize;
    }

    public String getFileExtsn() {
        return fileExtsn;
    }

    public void setFileExtsn(String fileExtsn) {
        this.fileExtsn = fileExtsn;
    }

    public String getRegId() {
        return regId;
    }

    public void setRegId(String regId) {
        this.regId = regId;
    }

    public LocalDateTime getRegDt() {
        return regDt;
    }

    public void setRegDt(LocalDateTime regDt) {
        this.regDt = regDt;
    }

    /** 화면 표시용 등록일 (yyyy-MM-dd HH:mm) */
    public String getRegDtForView() {
        return regDt == null ? "" : regDt.format(VIEW_FORMATTER);
    }

    /** 화면 표시용 파일 크기 (예: 1.2 MB) */
    public String getFileSizeForView() {
        if (fileSize <= 0) {
            return "0 B";
        }
        if (fileSize < 1024L) {
            return fileSize + " B";
        }
        if (fileSize < 1024L * 1024L) {
            return String.format("%.1f KB", fileSize / 1024.0);
        }
        if (fileSize < 1024L * 1024L * 1024L) {
            return String.format("%.1f MB", fileSize / (1024.0 * 1024.0));
        }
        return String.format("%.1f GB", fileSize / (1024.0 * 1024.0 * 1024.0));
    }

    @Override
    public String toString() {
        return "AtcFileVO{atcFileId='" + atcFileId + "', fileNm='" + fileNm + "', fileSize=" + fileSize + "}";
    }
}
