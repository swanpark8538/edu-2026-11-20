package egovframework.example.board.persist;

import java.util.List;

import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import egovframework.example.board.service.vo.BoardSearchVO;
import egovframework.example.board.service.vo.BoardVO;

/**
 * 게시물 데이터 접근 Mapper (resources/mapper/BoardMapper.xml).
 */
@Mapper
public interface BoardMapper {

    /** 목록 건수 조회 */
    int countBoardList(BoardSearchVO searchVO);

    /** 목록 조회 (검색 + 페이징) */
    List<BoardVO> selectBoardList(BoardSearchVO searchVO);

    /** 게시물 상세 조회 */
    BoardVO selectBoard(@Param("boardId") String boardId);

    /** 게시물 등록 */
    int insertBoard(BoardVO boardVO);

    /** 게시물 수정 */
    int updateBoard(BoardVO boardVO);

    /** 게시물 삭제 */
    int deleteBoard(@Param("boardId") String boardId);

    /** 조회 수 증가 */
    int updateBoardReadcnt(@Param("boardId") String boardId);

}
