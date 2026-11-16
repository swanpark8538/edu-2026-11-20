package egovframework.example.cmmn.file.service;

import java.io.Serializable;
import java.time.LocalDateTime;

/**
 * 첨부파일 VO.
 */
public class FileVO implements Serializable {

    private static final long serialVersionUID = 1L;

    private String atchFileId;

    private int fileSn;

    private String streFileNm;

    private String orignlFileNm;

    private String fileExtsn;

    private long fileSize;

    private LocalDateTime creatDt;

    public String getAtchFileId() {
        return atchFileId;
    }

    public void setAtchFileId(String atchFileId) {
        this.atchFileId = atchFileId;
    }

    public int getFileSn() {
        return fileSn;
    }

    public void setFileSn(int fileSn) {
        this.fileSn = fileSn;
    }

    public String getStreFileNm() {
        return streFileNm;
    }

    public void setStreFileNm(String streFileNm) {
        this.streFileNm = streFileNm;
    }

    public String getOrignlFileNm() {
        return orignlFileNm;
    }

    public void setOrignlFileNm(String orignlFileNm) {
        this.orignlFileNm = orignlFileNm;
    }

    public String getFileExtsn() {
        return fileExtsn;
    }

    public void setFileExtsn(String fileExtsn) {
        this.fileExtsn = fileExtsn;
    }

    public long getFileSize() {
        return fileSize;
    }

    public void setFileSize(long fileSize) {
        this.fileSize = fileSize;
    }

    public LocalDateTime getCreatDt() {
        return creatDt;
    }

    public void setCreatDt(LocalDateTime creatDt) {
        this.creatDt = creatDt;
    }

    /** 화면 표시용 파일 크기 (예: 17KB) */
    public String getFileSizeText() {
        if (fileSize < 1024) {
            return fileSize + "B";
        }
        if (fileSize < 1024L * 1024) {
            return Math.round(fileSize / 1024.0) + "KB";
        }
        return String.format("%.1fMB", fileSize / (1024.0 * 1024));
    }

}
