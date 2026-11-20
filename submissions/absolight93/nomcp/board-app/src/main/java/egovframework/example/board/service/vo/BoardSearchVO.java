package egovframework.example.board.service.vo;

/**
 * 게시판 목록 검색/페이징 파라미터 VO.
 */
public class BoardSearchVO {

    /** 검색 조건 (TITLE | CONTENT | WRITER | TITLE_CONTENT) */
    private String searchCon;

    /** 검색어 */
    private String searchWd;

    /** 시작 레코드 인덱스 (0부터) */
    private int firstRecordIndex;

    /** 페이지당 레코드 수 */
    private int recordCountPerPage = 10;

    public String getSearchCon() {
        return searchCon;
    }

    public void setSearchCon(String searchCon) {
        this.searchCon = searchCon;
    }

    public String getSearchWd() {
        return searchWd;
    }

    public void setSearchWd(String searchWd) {
        this.searchWd = searchWd;
    }

    public int getFirstRecordIndex() {
        return firstRecordIndex;
    }

    public void setFirstRecordIndex(int firstRecordIndex) {
        this.firstRecordIndex = firstRecordIndex;
    }

    public int getRecordCountPerPage() {
        return recordCountPerPage;
    }

    public void setRecordCountPerPage(int recordCountPerPage) {
        this.recordCountPerPage = recordCountPerPage;
    }

    @Override
    public String toString() {
        return "BoardSearchVO{searchCon=" + searchCon + ", searchWd=" + searchWd + ", firstRecordIndex="
                + firstRecordIndex + ", recordCountPerPage=" + recordCountPerPage + "}";
    }

}
