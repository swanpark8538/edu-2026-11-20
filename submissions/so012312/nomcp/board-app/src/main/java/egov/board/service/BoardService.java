package egov.board.service;

import egov.board.service.vo.AtcFileVO;
import egov.board.service.vo.BoardListResultVO;
import egov.board.service.vo.BoardSearchVO;
import egov.board.service.vo.BoardVO;
import org.egovframe.rte.fdl.cmmn.exception.EgovBizException;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

/**
 * 게시판 업무 서비스.
 */
public interface BoardService {

    /**
     * 검색 조건에 맞는 게시물 목록과 페이징 정보를 조회한다.
     *
     * @param searchVO 검색·페이지 조건
     * @return 목록 + PaginationInfo
     * @throws EgovBizException 조회 실패
     */
    BoardListResultVO selectBoardList(BoardSearchVO searchVO) throws EgovBizException;

    /**
     * 게시물 한 건을 첨부파일 목록과 함께 조회한다.
     *
     * @param boardId           게시물 ID
     * @param increaseViewCnt   true 이면 조회수를 증가시킨다 (세션에서 미열람일 때만 true 로 호출)
     * @return 게시물 상세
     * @throws EgovBizException 게시물이 없거나 조회 실패
     */
    BoardVO selectBoard(String boardId, boolean increaseViewCnt) throws EgovBizException;

    /**
     * 게시물을 등록하고 첨부파일을 저장한다.
     *
     * @param boardVO   게시물 정보 (제목·내용·글쓴이)
     * @param atcFiles  업로드 파일 (없으면 null 또는 빈 배열)
     * @return 등록한 게시물 ID
     * @throws EgovBizException 등록 실패
     */
    String insertBoard(BoardVO boardVO, List<MultipartFile> atcFiles) throws EgovBizException;

    /**
     * 게시물을 수정하고 첨부파일을 추가·삭제한다.
     *
     * @param boardVO             게시물 정보 (boardId 필수)
     * @param atcFiles            새로 업로드할 파일
     * @param deleteAtcFileIdList 삭제할 첨부파일 ID
     * @throws EgovBizException 수정 실패
     */
    void updateBoard(BoardVO boardVO, List<MultipartFile> atcFiles, List<String> deleteAtcFileIdList)
            throws EgovBizException;

    /**
     * 게시물을 삭제하고 딸린 첨부파일도 디스크에서 제거한다.
     *
     * @param boardId 게시물 ID
     * @throws EgovBizException 삭제 실패
     */
    void deleteBoard(String boardId) throws EgovBizException;

    /**
     * 첨부파일 한 건을 조회한다. (다운로드용)
     *
     * @param atcFileId 첨부파일 ID
     * @return 첨부파일 정보
     * @throws EgovBizException 첨부파일이 없거나 조회 실패
     */
    AtcFileVO selectAtcFile(String atcFileId) throws EgovBizException;
}
