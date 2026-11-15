package egovframework.example.board.service;

import java.io.Serializable;

/**
 * 게시판 검색 조건 및 페이징 VO.
 * searchCondition: 0 제목, 1 내용, 2 작성자
 */
public class BoardDefaultVO implements Serializable {

    private static final long serialVersionUID = 1L;

    public static final int MAX_KEYWORD_LENGTH = 100;

    private String searchCondition = "0";

    private String searchKeyword = "";

    private int pageIndex = 1;

    private int pageUnit = 10;

    private int pageSize = 10;

    private int firstIndex = 0;

    private int lastIndex = 1;

    private int recordCountPerPage = 10;

    /** 허용 범위를 벗어난 검색 조건과 페이지 번호를 기본값으로 바로잡는다. */
    public void normalize() {
        if (!"0".equals(searchCondition) && !"1".equals(searchCondition) && !"2".equals(searchCondition)) {
            searchCondition = "0";
        }
        searchKeyword = searchKeyword == null ? "" : searchKeyword.strip();
        if (searchKeyword.length() > MAX_KEYWORD_LENGTH) {
            searchKeyword = searchKeyword.substring(0, MAX_KEYWORD_LENGTH);
        }
        if (pageIndex < 1) {
            pageIndex = 1;
        }
    }

    /** LIKE 검색용 검색어. ESCAPE '!' 와 함께 사용한다. */
    public String getSearchKeywordLike() {
        if (searchKeyword == null) {
            return "";
        }
        return searchKeyword.replace("!", "!!").replace("%", "!%").replace("_", "!_");
    }

    public String getSearchCondition() {
        return searchCondition;
    }

    public void setSearchCondition(String searchCondition) {
        this.searchCondition = searchCondition;
    }

    public String getSearchKeyword() {
        return searchKeyword;
    }

    public void setSearchKeyword(String searchKeyword) {
        this.searchKeyword = searchKeyword;
    }

    public int getPageIndex() {
        return pageIndex;
    }

    public void setPageIndex(int pageIndex) {
        this.pageIndex = pageIndex;
    }

    public int getPageUnit() {
        return pageUnit;
    }

    public void setPageUnit(int pageUnit) {
        this.pageUnit = pageUnit;
    }

    public int getPageSize() {
        return pageSize;
    }

    public void setPageSize(int pageSize) {
        this.pageSize = pageSize;
    }

    public int getFirstIndex() {
        return firstIndex;
    }

    public void setFirstIndex(int firstIndex) {
        this.firstIndex = firstIndex;
    }

    public int getLastIndex() {
        return lastIndex;
    }

    public void setLastIndex(int lastIndex) {
        this.lastIndex = lastIndex;
    }

    public int getRecordCountPerPage() {
        return recordCountPerPage;
    }

    public void setRecordCountPerPage(int recordCountPerPage) {
        this.recordCountPerPage = recordCountPerPage;
    }

}
