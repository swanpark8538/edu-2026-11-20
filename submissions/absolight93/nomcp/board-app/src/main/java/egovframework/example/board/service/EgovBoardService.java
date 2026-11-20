package egovframework.example.board.service;

import java.util.List;

import org.springframework.web.multipart.MultipartFile;

import egovframework.example.board.service.vo.BoardFileVO;
import egovframework.example.board.service.vo.BoardSearchVO;
import egovframework.example.board.service.vo.BoardVO;

/**
 * 게시판 업무 서비스 인터페이스.
 */
public interface EgovBoardService {

    /** 목록 건수 조회 */
    int countBoardList(BoardSearchVO searchVO) throws Exception;

    /** 목록 조회 */
    List<BoardVO> selectBoardList(BoardSearchVO searchVO) throws Exception;

    /**
     * 게시물 단건 조회 (첨부파일 포함, 조회 수 변동 없음)
     */
    BoardVO selectBoard(String boardId) throws Exception;

    /**
     * 게시물 상세 조회.
     * 조회 시 조회 수를 증가시킨 뒤 가장 최근 값을 반환한다.
     */
    BoardVO readBoard(String boardId) throws Exception;

    /** 게시물 등록 (첨부파일 포함) */
    String insertBoard(BoardVO boardVO, List<MultipartFile> saveFiles) throws Exception;

    /** 게시물 수정 (첨부파일 추가/삭제 포함) */
    void updateBoard(BoardVO boardVO, List<MultipartFile> saveFiles, List<String> deleteAtchfileIds) throws Exception;

    /** 게시물 삭제 (첨부파일까지 삭제) */
    void deleteBoard(String boardId) throws Exception;

    /** 게시물별 첨부파일 목록 조회 */
    List<BoardFileVO> selectBoardFileList(String boardId) throws Exception;

    /** 첨부파일 상세 조회 */
    BoardFileVO selectBoardFile(String atchfileId) throws Exception;

    /** 첨부파일 단건 삭제 */
    void deleteBoardFile(String atchfileId) throws Exception;

    /** 첨부파일의 실제 저장 경로 확인 */
    java.nio.file.Path getStoredFilePath(BoardFileVO boardFileVO) throws Exception;

}
