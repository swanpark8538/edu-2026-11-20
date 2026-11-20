package egovframework.board.service.impl;

import java.io.IOException;
import java.io.UncheckedIOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;
import java.util.UUID;

import org.egovframe.rte.fdl.idgnr.EgovIdGnrService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.PathResource;
import org.springframework.core.io.Resource;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;
import org.springframework.web.multipart.MultipartFile;

import egovframework.board.service.BoardFileVO;
import egovframework.board.service.BoardSearchVO;
import egovframework.board.service.BoardService;
import egovframework.board.service.BoardVO;

@Service("boardService")
@Transactional(rollbackFor = Exception.class)
public class BoardServiceImpl implements BoardService {

	private static final Logger log = LoggerFactory.getLogger(BoardServiceImpl.class);

	private final BoardMapper boardMapper;
	private final EgovIdGnrService boardIdGnrService;
	private final EgovIdGnrService fileIdGnrService;
	private final Path uploadDir;

	public BoardServiceImpl(BoardMapper boardMapper,
			@Qualifier("boardIdGnrService") EgovIdGnrService boardIdGnrService,
			@Qualifier("fileIdGnrService") EgovIdGnrService fileIdGnrService,
			@Value("${board.upload-dir:./uploads}") String uploadDir) {
		this.boardMapper = boardMapper;
		this.boardIdGnrService = boardIdGnrService;
		this.fileIdGnrService = fileIdGnrService;
		this.uploadDir = Paths.get(uploadDir).toAbsolutePath().normalize();
	}

	@Override
	@Transactional(readOnly = true)
	public List<BoardVO> selectBoardList(BoardSearchVO searchVO) {
		return boardMapper.selectBoardList(searchVO);
	}

	@Override
	@Transactional(readOnly = true)
	public int selectBoardListCount(BoardSearchVO searchVO) {
		return boardMapper.selectBoardListCount(searchVO);
	}

	@Override
	@Transactional(readOnly = true)
	public BoardVO selectBoard(String boardId) {
		return findBoard(boardId);
	}

	@Override
	public BoardVO viewBoard(String boardId) {
		boardMapper.updateViewCount(boardId);
		return findBoard(boardId);
	}

	private BoardVO findBoard(String boardId) {
		BoardVO board = boardMapper.selectBoard(boardId);
		if (board == null) {
			throw new IllegalArgumentException("존재하지 않는 게시물입니다: " + boardId);
		}
		board.setAttachments(boardMapper.selectFileList(boardId));
		return board;
	}

	@Override
	public String insertBoard(BoardVO board, List<MultipartFile> files) {
		try {
			board.setBoardId(boardIdGnrService.getNextStringId());
		} catch (Exception e) {
			throw new IllegalStateException("게시물 ID 생성 실패", e);
		}
		boardMapper.insertBoard(board);
		saveFiles(board.getBoardId(), files);
		return board.getBoardId();
	}

	@Override
	public void updateBoard(BoardVO board, List<MultipartFile> files, List<String> deleteFileIds) {
		findBoard(board.getBoardId());
		boardMapper.updateBoard(board);
		if (deleteFileIds != null) {
			for (String fileId : deleteFileIds) {
				BoardFileVO file = boardMapper.selectFile(fileId);
				// 다른 게시물의 파일을 지우지 못하도록 소속 확인
				if (file != null && file.getBoardId().equals(board.getBoardId())) {
					boardMapper.deleteFile(fileId);
					deletePhysical(file);
				}
			}
		}
		saveFiles(board.getBoardId(), files);
	}

	@Override
	public void deleteBoard(String boardId) {
		List<BoardFileVO> files = boardMapper.selectFileList(boardId);
		boardMapper.deleteBoard(boardId); // BOARD_FILE 은 ON DELETE CASCADE
		files.forEach(this::deletePhysical);
	}

	@Override
	@Transactional(readOnly = true)
	public BoardFileVO selectFile(String fileId) {
		return boardMapper.selectFile(fileId);
	}

	@Override
	public Resource loadFile(BoardFileVO file) {
		return new PathResource(uploadDir.resolve(file.getStoredName()));
	}

	private void saveFiles(String boardId, List<MultipartFile> files) {
		if (files == null) {
			return;
		}
		for (MultipartFile multipart : files) {
			if (multipart == null || multipart.isEmpty()) {
				continue;
			}
			try {
				Files.createDirectories(uploadDir);
				String storedName = UUID.randomUUID().toString();
				multipart.transferTo(uploadDir.resolve(storedName));

				BoardFileVO file = new BoardFileVO();
				file.setFileId(fileIdGnrService.getNextStringId());
				file.setBoardId(boardId);
				file.setOrigName(cleanFileName(multipart.getOriginalFilename()));
				file.setStoredName(storedName);
				file.setFileSize(multipart.getSize());
				file.setContentType(multipart.getContentType());
				boardMapper.insertFile(file);
			} catch (IOException e) {
				throw new UncheckedIOException(e);
			} catch (Exception e) {
				throw new IllegalStateException("첨부파일 저장 실패", e);
			}
		}
	}

	private String cleanFileName(String name) {
		String cleaned = StringUtils.getFilename(StringUtils.cleanPath(name == null ? "" : name));
		return StringUtils.hasText(cleaned) ? cleaned : "file";
	}

	private void deletePhysical(BoardFileVO file) {
		try {
			Files.deleteIfExists(uploadDir.resolve(file.getStoredName()));
		} catch (IOException e) {
			log.warn("첨부파일 삭제 실패: {}", file.getStoredName(), e);
		}
	}
}
