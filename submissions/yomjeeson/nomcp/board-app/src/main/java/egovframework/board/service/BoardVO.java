package egovframework.board.service;

import java.io.Serializable;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.Size;

/** 게시물 VO (검색·페이징 조건 포함) */
public class BoardVO implements Serializable {

    private static final long serialVersionUID = 1L;

    private String boardId;

    @NotBlank(message = "제목을 입력하세요.")
    @Size(max = 200, message = "제목은 200자 이내로 입력하세요.")
    private String title;

    @Size(max = 4000, message = "내용은 4000자 이내로 입력하세요.")
    private String content;

    @NotBlank(message = "작성자를 입력하세요.")
    @Size(max = 50, message = "작성자는 50자 이내로 입력하세요.")
    private String writer;

    private LocalDateTime regDt;
    private int fileCnt;
    private List<BoardFileVO> files = new ArrayList<>();

    /* 검색·페이징 */
    private String searchCondition = "";
    private String searchKeyword = "";
    private int pageIndex = 1;
    private int firstIndex = 0;
    private int recordCountPerPage = 10;

    public String getBoardId() { return boardId; }
    public void setBoardId(String boardId) { this.boardId = boardId; }
    public String getTitle() { return title; }
    public void setTitle(String title) { this.title = title; }
    public String getContent() { return content; }
    public void setContent(String content) { this.content = content; }
    public String getWriter() { return writer; }
    public void setWriter(String writer) { this.writer = writer; }
    public LocalDateTime getRegDt() { return regDt; }
    public void setRegDt(LocalDateTime regDt) { this.regDt = regDt; }
    public int getFileCnt() { return fileCnt; }
    public void setFileCnt(int fileCnt) { this.fileCnt = fileCnt; }
    public List<BoardFileVO> getFiles() { return files; }
    public void setFiles(List<BoardFileVO> files) { this.files = files; }
    public String getSearchCondition() { return searchCondition; }
    public void setSearchCondition(String searchCondition) { this.searchCondition = searchCondition; }
    public String getSearchKeyword() { return searchKeyword; }
    public void setSearchKeyword(String searchKeyword) { this.searchKeyword = searchKeyword; }
    public int getPageIndex() { return pageIndex; }
    public void setPageIndex(int pageIndex) { this.pageIndex = Math.max(1, pageIndex); }
    public int getFirstIndex() { return firstIndex; }
    public void setFirstIndex(int firstIndex) { this.firstIndex = firstIndex; }
    public int getRecordCountPerPage() { return recordCountPerPage; }
    public void setRecordCountPerPage(int recordCountPerPage) { this.recordCountPerPage = recordCountPerPage; }
}
