package egov.board.persistence;

import egov.board.service.vo.BoardSearchVO;
import egov.board.service.vo.BoardVO;
import org.apache.ibatis.session.SqlSessionFactory;
import org.egovframe.rte.psl.dataaccess.EgovAbstractMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Repository;

import java.util.List;

/**
 * 게시판 데이터 접근 객체.
 *
 * <p>표준프레임워크 데이터접근(ptl) 구성요소인 {@link EgovAbstractMapper}(MyBatis
 * {@code SqlSessionDaoSupport} 상위 클래스) 를 상속받아
 * SQL 매핑 ID 로 CRUD 를 수행한다. SQL 은 {@code resources/egovframework/sqlmap/BoardMapper.xml} 에 정의한다.</p>
 */
@Repository
public class BoardDAO extends EgovAbstractMapper {

    /** BoardMapper.xml 의 namespace */
    private static final String NAMESPACE = "BoardMapper.";

    /**
     * MyBatis 자동 설정이 만든 SqlSessionFactory 를 주입받는다.
     * (EgovAbstractMapper 의 상위 클래스가 SqlSessionTemplate 을 생성하도록 한다)
     */
    @Autowired
    @Override
    public void setSqlSessionFactory(SqlSessionFactory sqlSessionFactory) {
        super.setSqlSessionFactory(sqlSessionFactory);
    }

    /** 목록 건수 조회 */
    public int countBoardList(BoardSearchVO searchVO) {
        Integer count = selectOne(NAMESPACE + "countBoardList", searchVO);
        return count == null ? 0 : count;
    }

    /** 게시물 목록 조회 (검색·페이징 적용) */
    public List<BoardVO> selectBoardList(BoardSearchVO searchVO) {
        return selectList(NAMESPACE + "selectBoardList", searchVO);
    }

    /** 게시물 한 건 조회 */
    public BoardVO selectBoard(BoardVO boardVO) {
        return selectOne(NAMESPACE + "selectBoard", boardVO);
    }

    /** 게시물 등록 */
    public int insertBoard(BoardVO boardVO) {
        return insert(NAMESPACE + "insertBoard", boardVO);
    }

    /** 게시물 수정 */
    public int updateBoard(BoardVO boardVO) {
        return update(NAMESPACE + "updateBoard", boardVO);
    }

    /** 게시물 삭제 */
    public int deleteBoard(BoardVO boardVO) {
        return delete(NAMESPACE + "deleteBoard", boardVO);
    }

    /** 조회수 증가 */
    public int updateBoardViewCnt(BoardVO boardVO) {
        return update(NAMESPACE + "updateBoardViewCnt", boardVO);
    }
}
