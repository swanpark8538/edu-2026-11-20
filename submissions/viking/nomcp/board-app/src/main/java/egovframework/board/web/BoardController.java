package egovframework.board.web;

import java.nio.charset.StandardCharsets;
import java.util.List;

import org.springframework.core.io.Resource;
import org.springframework.http.ContentDisposition;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import egovframework.board.service.BoardFileVO;
import egovframework.board.service.BoardSearchVO;
import egovframework.board.service.BoardService;
import egovframework.board.service.BoardVO;

@Controller
public class BoardController {

	private final BoardService boardService;

	public BoardController(BoardService boardService) {
		this.boardService = boardService;
	}

	@GetMapping("/")
	public String index() {
		return "redirect:/board/list";
	}

	@GetMapping("/board/list")
	public String list(@ModelAttribute("searchVO") BoardSearchVO searchVO, Model model) {
		int totalCount = boardService.selectBoardListCount(searchVO);
		int totalPages = Math.max(1, (int) Math.ceil((double) totalCount / searchVO.getPageSize()));
		if (searchVO.getPage() > totalPages) {
			searchVO.setPage(totalPages);
		}
		model.addAttribute("boards", boardService.selectBoardList(searchVO));
		model.addAttribute("totalCount", totalCount);
		model.addAttribute("totalPages", totalPages);
		return "board/list";
	}

	@GetMapping("/board/new")
	public String createForm(Model model) {
		model.addAttribute("board", new BoardVO());
		return "board/form";
	}

	@PostMapping("/board")
	public String create(@Validated @ModelAttribute("board") BoardVO board, BindingResult result,
			@RequestParam(value = "files", required = false) List<MultipartFile> files,
			RedirectAttributes ra) {
		if (result.hasErrors()) {
			return "board/form";
		}
		String boardId = boardService.insertBoard(board, files);
		ra.addFlashAttribute("message", "게시물을 등록했습니다.");
		return "redirect:/board/" + boardId;
	}

	@GetMapping("/board/{boardId}")
	public String view(@PathVariable String boardId, Model model) {
		model.addAttribute("board", boardService.viewBoard(boardId));
		return "board/view";
	}

	@GetMapping("/board/{boardId}/edit")
	public String editForm(@PathVariable String boardId, Model model) {
		model.addAttribute("board", boardService.selectBoard(boardId));
		return "board/form";
	}

	@PostMapping("/board/{boardId}")
	public String update(@PathVariable String boardId,
			@Validated @ModelAttribute("board") BoardVO board, BindingResult result,
			@RequestParam(value = "files", required = false) List<MultipartFile> files,
			@RequestParam(value = "deleteFileIds", required = false) List<String> deleteFileIds,
			Model model, RedirectAttributes ra) {
		board.setBoardId(boardId);
		if (result.hasErrors()) {
			board.setAttachments(boardService.selectBoard(boardId).getAttachments());
			return "board/form";
		}
		boardService.updateBoard(board, files, deleteFileIds);
		ra.addFlashAttribute("message", "게시물을 수정했습니다.");
		return "redirect:/board/" + boardId;
	}

	@PostMapping("/board/{boardId}/delete")
	public String delete(@PathVariable String boardId, RedirectAttributes ra) {
		boardService.deleteBoard(boardId);
		ra.addFlashAttribute("message", "게시물을 삭제했습니다.");
		return "redirect:/board/list";
	}

	@GetMapping("/board/file/{fileId}")
	public ResponseEntity<Resource> download(@PathVariable String fileId) {
		BoardFileVO file = boardService.selectFile(fileId);
		if (file == null) {
			return ResponseEntity.notFound().build();
		}
		Resource resource = boardService.loadFile(file);
		if (!resource.exists()) {
			return ResponseEntity.notFound().build();
		}
		MediaType mediaType = MediaType.APPLICATION_OCTET_STREAM;
		if (file.getContentType() != null) {
			try {
				mediaType = MediaType.parseMediaType(file.getContentType());
			} catch (IllegalArgumentException ignored) {
				// 잘못된 Content-Type 이면 octet-stream 사용
			}
		}
		return ResponseEntity.ok()
				.contentType(mediaType)
				.header(HttpHeaders.CONTENT_DISPOSITION, ContentDisposition.attachment()
						.filename(file.getOrigName(), StandardCharsets.UTF_8).build().toString())
				.body(resource);
	}
}
