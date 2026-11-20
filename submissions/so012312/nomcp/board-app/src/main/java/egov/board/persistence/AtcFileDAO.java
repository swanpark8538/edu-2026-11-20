package egov.board.persistence;

import egov.board.service.vo.AtcFileVO;
import org.apache.ibatis.session.SqlSessionFactory;
import org.egovframe.rte.psl.dataaccess.EgovAbstractMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Repository;

import java.util.List;

/**
 * 게시물 첨부파일 데이터 접근 객체.
 *
 * <p>SQL 은 {@code resources/egovframework/sqlmap/AtcFileMapper.xml} 에 정의한다.</p>
 */
@Repository
public class AtcFileDAO extends EgovAbstractMapper {

    /** AtcFileMapper.xml 의 namespace */
    private static final String NAMESPACE = "AtcFileMapper.";

    @Autowired
    @Override
    public void setSqlSessionFactory(SqlSessionFactory sqlSessionFactory) {
        super.setSqlSessionFactory(sqlSessionFactory);
    }

    /** 첨부파일 등록 */
    public int insertAtcFile(AtcFileVO atcFileVO) {
        return insert(NAMESPACE + "insertAtcFile", atcFileVO);
    }

    /** 게시물에 딸린 첨부파일 목록 조회 */
    public List<AtcFileVO> selectAtcFileList(String boardId) {
        return selectList(NAMESPACE + "selectAtcFileList", boardId);
    }

    /** 첨부파일 한 건 조회 */
    public AtcFileVO selectAtcFile(String atcFileId) {
        return selectOne(NAMESPACE + "selectAtcFile", atcFileId);
    }

    /** 첨부파일 한 건 삭제 */
    public int deleteAtcFile(String atcFileId) {
        return delete(NAMESPACE + "deleteAtcFile", atcFileId);
    }

    /** 게시물에 딸린 첨부파일 전부 삭제 */
    public int deleteAtcFileListOfBoard(String boardId) {
        return delete(NAMESPACE + "deleteAtcFileListOfBoard", boardId);
    }

    /** 첨부파일 개수 갱신용 집계 */
    public int countAtcFileOfBoard(String boardId) {
        Integer count = selectOne(NAMESPACE + "countAtcFileOfBoard", boardId);
        return count == null ? 0 : count;
    }
}
