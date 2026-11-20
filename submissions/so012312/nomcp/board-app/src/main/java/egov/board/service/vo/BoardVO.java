package egov.board.service.vo;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

import java.io.Serializable;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;

/**
 * 게시물 데이터(BOARD 테이블)를 담는 값 객체.
 */
public class BoardVO implements Serializable {

    private static final long serialVersionUID = 1L;

    private static final DateTimeFormatter VIEW_FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm");

    /** 게시물 ID (eGovFrame ID 발번 서비스로 채번, 20자리) */
    private String boardId;

    /** 제목 */
    @NotBlank(message = "제목을 입력해 주세요.")
    @Size(max = 200, message = "제목은 200자 이하여야 합니다.")
    private String title;

    /** 내용 */
    @Size(max = 4000, message = "내용은 4,000자 이하여야 합니다.")
    private String content;

    /** 글쓴이 */
    @NotBlank(message = "글쓴이를 입력해 주세요.")
    @Size(max = 50, message = "글쓴이는 50자 이하여야 합니다.")
    private String writer;

    /** 조회수 */
    private int viewCnt;

    /** 등록일 */
    private LocalDateTime regDt;

    /** 수정일 */
    private LocalDateTime modDt;

    /** 목록 조회 시 표시할 첨부파일 수 */
    private int atcFileNo;

    /** 첨부파일 목록 (조회·등록 시 사용) */
    private List<AtcFileVO> atcFileList = new ArrayList<>();

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

    public int getViewCnt() {
        return viewCnt;
    }

    public void setViewCnt(int viewCnt) {
        this.viewCnt = viewCnt;
    }

    public LocalDateTime getRegDt() {
        return regDt;
    }

    public void setRegDt(LocalDateTime regDt) {
        this.regDt = regDt;
    }

    public LocalDateTime getModDt() {
        return modDt;
    }

    public void setModDt(LocalDateTime modDt) {
        this.modDt = modDt;
    }

    public int getAtcFileNo() {
        return atcFileNo;
    }

    public void setAtcFileNo(int atcFileNo) {
        this.atcFileNo = atcFileNo;
    }

    public List<AtcFileVO> getAtcFileList() {
        return atcFileList;
    }

    public void setAtcFileList(List<AtcFileVO> atcFileList) {
        this.atcFileList = atcFileList == null ? new ArrayList<>() : atcFileList;
    }

    /** 목록·화면 표시용 등록일 (yyyy-MM-dd HH:mm) */
    public String getRegDtForView() {
        return regDt == null ? "" : regDt.format(VIEW_FORMATTER);
    }

    /** 목록·화면 표시용 수정일 (yyyy-MM-dd HH:mm) */
    public String getModDtForView() {
        return modDt == null ? "" : modDt.format(VIEW_FORMATTER);
    }

    @Override
    public String toString() {
        return "BoardVO{boardId='" + boardId + "', title='" + title + "', writer='" + writer + "'}";
    }
}
