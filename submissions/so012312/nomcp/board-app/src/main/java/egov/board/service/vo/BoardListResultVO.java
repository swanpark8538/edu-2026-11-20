package egov.board.service.vo;

import org.egovframe.rte.ptl.mvc.tags.ui.pagination.PaginationInfo;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

/**
 * 게시판 목록 조회 결과(목록 + 페이징 정보) 를 담는 값 객체.
 *
 * <p>페이징 정보는 표준프레임워크 화면기반(ptl-mvc) 의 {@link PaginationInfo} 를 그대로 사용한다.</p>
 */
public class BoardListResultVO implements Serializable {

    private static final long serialVersionUID = 1L;

    private List<BoardVO> boardList = new ArrayList<>();

    private PaginationInfo paginationInfo = new PaginationInfo();

    public List<BoardVO> getBoardList() {
        return boardList;
    }

    public void setBoardList(List<BoardVO> boardList) {
        this.boardList = boardList == null ? new ArrayList<>() : boardList;
    }

    public PaginationInfo getPaginationInfo() {
        return paginationInfo;
    }

    public void setPaginationInfo(PaginationInfo paginationInfo) {
        this.paginationInfo = paginationInfo == null ? new PaginationInfo() : paginationInfo;
    }
}
