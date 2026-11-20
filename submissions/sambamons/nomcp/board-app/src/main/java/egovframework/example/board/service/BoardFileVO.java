package egovframework.example.board.service;

import java.time.LocalDateTime;

/**
 * 게시물 첨부파일 (BOARD_FILE)
 */
public class BoardFileVO {

    private String fileId;

    private String boardId;

    /** 원본 파일명 */
    private String orignlFileNm;

    /** 저장 파일명 */
    private String streFileNm;

    private long fileSize;

    private String contentType;

    private LocalDateTime regDt;

    public String getFileId() { return fileId; }
    public void setFileId(String fileId) { this.fileId = fileId; }
    public String getBoardId() { return boardId; }
    public void setBoardId(String boardId) { this.boardId = boardId; }
    public String getOrignlFileNm() { return orignlFileNm; }
    public void setOrignlFileNm(String orignlFileNm) { this.orignlFileNm = orignlFileNm; }
    public String getStreFileNm() { return streFileNm; }
    public void setStreFileNm(String streFileNm) { this.streFileNm = streFileNm; }
    public long getFileSize() { return fileSize; }
    public void setFileSize(long fileSize) { this.fileSize = fileSize; }
    public String getContentType() { return contentType; }
    public void setContentType(String contentType) { this.contentType = contentType; }
    public LocalDateTime getRegDt() { return regDt; }
    public void setRegDt(LocalDateTime regDt) { this.regDt = regDt; }
}
