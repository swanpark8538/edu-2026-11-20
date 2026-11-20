package egovframework.example.board.service.vo;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

/**
 * 게시물 정보를 담는 VO (BOARD 테이블 대응).
 */
public class BoardVO {

    /** 게시물 ID */
    private String boardId;

    /** 제목 */
    @NotBlank(message = "제목은 필수입니다.")
    @Size(max = 200, message = "제목은 200자 이하여야 합니다.")
    private String title;

    /** 내용 */
    @Size(max = 4000, message = "내용은 4000자 이하여야 합니다.")
    private String content;

    /** 작성자 */
    @NotBlank(message = "작성자는 필수입니다.")
    @Size(max = 50, message = "작성자는 50자 이하여야 합니다.")
    private String writer;

    /** 조회 수 */
    private int readcnt;

    /** 등록 일시 */
    private Date createDt;

    /** 수정 일시 */
    private Date modifyDt;

    /** 첨부 파일 수 (목록 노출용) */
    private int atchfileCount;

    /** 첨부 파일 목록 */
    private List<BoardFileVO> boardFileList = new ArrayList<>();

    public String getBoardId() {
        return boardId;
    }

    public void setBoardId(String boardId) {
        this.boardId = boardId;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getContent() {
        return content;
    }

    public void setContent(String content) {
        this.content = content;
    }

    public String getWriter() {
        return writer;
    }

    public void setWriter(String writer) {
        this.writer = writer;
    }

    public int getReadcnt() {
        return readcnt;
    }

    public void setReadcnt(int readcnt) {
        this.readcnt = readcnt;
    }

    public Date getCreateDt() {
        return createDt;
    }

    public void setCreateDt(Date createDt) {
        this.createDt = createDt;
    }

    public Date getModifyDt() {
        return modifyDt;
    }

    public void setModifyDt(Date modifyDt) {
        this.modifyDt = modifyDt;
    }

    public int getAtchfileCount() {
        return atchfileCount;
    }

    public void setAtchfileCount(int atchfileCount) {
        this.atchfileCount = atchfileCount;
    }

    public List<BoardFileVO> getBoardFileList() {
        return boardFileList;
    }

    public void setBoardFileList(List<BoardFileVO> boardFileList) {
        this.boardFileList = boardFileList;
    }

    @Override
    public String toString() {
        return "BoardVO{boardId=" + boardId + ", title=" + title + ", writer=" + writer + ", readcnt=" + readcnt + "}";
    }

}
