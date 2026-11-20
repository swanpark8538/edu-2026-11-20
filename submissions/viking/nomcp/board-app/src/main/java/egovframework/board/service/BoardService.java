package egovframework.board.service;

import java.util.List;

import org.springframework.core.io.Resource;
import org.springframework.web.multipart.MultipartFile;

public interface BoardService {

	List<BoardVO> selectBoardList(BoardSearchVO searchVO);

	int selectBoardListCount(BoardSearchVO searchVO);

	/** 조회수를 올리지 않고 게시물 + 첨부파일 조회 (수정 화면용) */
	BoardVO selectBoard(String boardId);

	/** 조회수를 1 올리고 게시물 + 첨부파일 조회 (상세 화면용) */
	BoardVO viewBoard(String boardId);

	String insertBoard(BoardVO board, List<MultipartFile> files);

	void updateBoard(BoardVO board, List<MultipartFile> files, List<String> deleteFileIds);

	void deleteBoard(String boardId);

	BoardFileVO selectFile(String fileId);

	Resource loadFile(BoardFileVO file);
}
