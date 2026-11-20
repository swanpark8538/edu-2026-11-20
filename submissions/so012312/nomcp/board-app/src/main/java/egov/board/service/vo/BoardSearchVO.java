package egov.board.service.vo;

import java.io.Serializable;

/**
 * 게시판 목록 조회 조건(검색어·페이지)을 담는 값 객체.
 *
 * <p>페이징 계산은 표준프레임워크 화면기반의
 * {@code org.egovframe.rte.ptl.mvc.tags.ui.pagination.PaginationInfo} 가 담당하고,
 * 그 결과가 {@code recordCountPerPage}, {@code firstRecordIndex} 에 채워져 SQL 에 전달된다.</p>
 */
public class BoardSearchVO implements Serializable {

    private static final long serialVersionUID = 1L;

    /** 검색 조건 (제목 TITLE / 내용 CONTENT / 글쓴이 WRITER / 제목+내용 TITLE_CONTENT) */
    private String searchCond = "TITLE";

    /** 검색어 */
    private String searchWord;

    /** 현재 페이지 번호 (1부터 시작) */
    private int currentPageNo = 1;

    /** 한 페이지에 표시할 게시물의 수 */
    private int recordCountPerPage = 10;

    /** 하단 페이지 번호 목록에 표시할 페이지 수 */
    private int pageSize = 10;

    /** 조회 시작 레코드 인덱스 (PaginationInfo 에서 계산되어 세팅됨) */
    private int firstRecordIndex;

    /** 조회 종료 레코드 인덱스 (PaginationInfo 에서 계산되어 세팅됨) */
    private int lastRecordIndex;

    public String getSearchCond() {
        return searchCond;
    }

    public void setSearchCond(String searchCond) {
        this.searchCond = searchCond;
    }

    public String getSearchWord() {
        return searchWord;
    }

    public void setSearchWord(String searchWord) {
        this.searchWord = searchWord;
    }

    public int getCurrentPageNo() {
        return currentPageNo;
    }

    public void setCurrentPageNo(int currentPageNo) {
        this.currentPageNo = currentPageNo < 1 ? 1 : currentPageNo;
    }

    public int getRecordCountPerPage() {
        return recordCountPerPage;
    }

    public void setRecordCountPerPage(int recordCountPerPage) {
        this.recordCountPerPage = recordCountPerPage < 1 ? 10 : recordCountPerPage;
    }

    public int getPageSize() {
        return pageSize;
    }

    public void setPageSize(int pageSize) {
        this.pageSize = pageSize < 1 ? 10 : pageSize;
    }

    public int getFirstRecordIndex() {
        return firstRecordIndex;
    }

    public void setFirstRecordIndex(int firstRecordIndex) {
        this.firstRecordIndex = firstRecordIndex;
    }

    public int getLastRecordIndex() {
        return lastRecordIndex;
    }

    public void setLastRecordIndex(int lastRecordIndex) {
        this.lastRecordIndex = lastRecordIndex;
    }

    /** 검색어가 있는지 여부 (목록 SQL 의 동적 조건에서 사용) */
    public boolean isHasSearchWord() {
        return searchWord != null && !searchWord.trim().isEmpty();
    }

    /** 허용된 검색 조건인지 검증하고 허용 값만 남긴다 (동적 SQL 에 임의 값이 들어가지 않도록 방지) */
    public void normalizeSearchCond() {
        String cond = searchCond == null ? "" : searchCond.trim().toUpperCase();
        switch (cond) {
            case "TITLE":
            case "CONTENT":
            case "WRITER":
            case "TITLE_CONTENT":
                this.searchCond = cond;
                break;
            default:
                this.searchCond = "TITLE";
                break;
        }
    }

    @Override
    public String toString() {
        return "BoardSearchVO{searchCond='" + searchCond + "', searchWord='" + searchWord
                + "', currentPageNo=" + currentPageNo + "}";
    }
}
