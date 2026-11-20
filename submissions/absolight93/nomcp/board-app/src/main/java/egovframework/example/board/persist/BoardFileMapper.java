package egovframework.example.board.persist;

import java.util.List;

import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import egovframework.example.board.service.vo.BoardFileVO;

/**
 * 게시물 첨부파일 데이터 접근 Mapper (resources/mapper/BoardFileMapper.xml).
 */
@Mapper
public interface BoardFileMapper {

    /** 첨부파일 등록 */
    int insertBoardFile(BoardFileVO boardFileVO);

    /** 게시물별 첨부파일 목록 조회 */
    List<BoardFileVO> selectBoardFileList(@Param("boardId") String boardId);

    /** 첨부파일 상세 조회 */
    BoardFileVO selectBoardFile(@Param("atchfileId") String atchfileId);

    /** 첨부파일 삭제 */
    int deleteBoardFile(@Param("atchfileId") String atchfileId);

    /** 게시물 삭제 시 첨부파일 전체 삭제 */
    int deleteBoardFileByBoardId(@Param("boardId") String boardId);

}
