package egovframework.example.board.service.vo;

import java.util.Date;

/**
 * 게시물 첨부파일 정보를 담는 VO (BOARD_FILE 테이블 대응).
 */
public class BoardFileVO {

    /** 첨부파일 ID */
    private String atchfileId;

    /** 게시물 ID */
    private String boardId;

    /** 원본 파일명 */
    private String fileOrglnm;

    /** 저장 파일명 (UUID 기반) */
    private String fileStreNm;

    /** 파일 확장자 */
    private String fileExtsn;

    /** 파일 크기 (byte) */
    private long fileSize;

    /** 파일 저장 경로 */
    private String fileStrePath;

    /** 등록 일시 */
    private Date createDt;

    public String getAtchfileId() {
        return atchfileId;
    }

    public void setAtchfileId(String atchfileId) {
        this.atchfileId = atchfileId;
    }

    public String getBoardId() {
        return boardId;
    }

    public void setBoardId(String boardId) {
        this.boardId = boardId;
    }

    public String getFileOrglnm() {
        return fileOrglnm;
    }

    public void setFileOrglnm(String fileOrglnm) {
        this.fileOrglnm = fileOrglnm;
    }

    public String getFileStreNm() {
        return fileStreNm;
    }

    public void setFileStreNm(String fileStreNm) {
        this.fileStreNm = fileStreNm;
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

    public String getFileStrePath() {
        return fileStrePath;
    }

    public void setFileStrePath(String fileStrePath) {
        this.fileStrePath = fileStrePath;
    }

    public Date getCreateDt() {
        return createDt;
    }

    public void setCreateDt(Date createDt) {
        this.createDt = createDt;
    }

    @Override
    public String toString() {
        return "BoardFileVO{atchfileId=" + atchfileId + ", boardId=" + boardId + ", fileOrglnm=" + fileOrglnm + "}";
    }

}
