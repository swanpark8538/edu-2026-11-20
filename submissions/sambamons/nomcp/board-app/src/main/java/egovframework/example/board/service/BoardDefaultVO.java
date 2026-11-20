package egovframework.example.board.service;

/**
 * 게시판 목록 검색/페이징 조건
 */
public class BoardDefaultVO {

    /** 검색조건 (TITLE, WRITER, CONTENT) */
    private String searchCondition = "TITLE";

    /** 검색어 */
    private String searchKeyword = "";

    /** 현재 페이지 */
    private int pageIndex = 1;

    /** 페이지당 게시물 수 */
    private int pageUnit = 10;

    /** 페이지 목록 크기 */
    private int pageSize = 10;

    private int firstIndex = 0;

    private int recordCountPerPage = 10;

    public String getSearchCondition() { return searchCondition; }
    public void setSearchCondition(String searchCondition) { this.searchCondition = searchCondition; }
    public String getSearchKeyword() { return searchKeyword; }
    public void setSearchKeyword(String searchKeyword) { this.searchKeyword = searchKeyword; }
    public int getPageIndex() { return pageIndex; }
    public void setPageIndex(int pageIndex) { this.pageIndex = Math.max(pageIndex, 1); }
    public int getPageUnit() { return pageUnit; }
    public void setPageUnit(int pageUnit) { this.pageUnit = pageUnit; }
    public int getPageSize() { return pageSize; }
    public void setPageSize(int pageSize) { this.pageSize = pageSize; }
    public int getFirstIndex() { return firstIndex; }
    public void setFirstIndex(int firstIndex) { this.firstIndex = firstIndex; }
    public int getRecordCountPerPage() { return recordCountPerPage; }
    public void setRecordCountPerPage(int recordCountPerPage) { this.recordCountPerPage = recordCountPerPage; }
}
