package egovframework.board.web;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;

import javax.validation.Valid;

import org.egovframe.rte.ptl.mvc.tags.ui.pagination.PaginationInfo;
import org.springframework.core.io.PathResource;
import org.springframework.core.io.Resource;
import org.springframework.http.ContentDisposition;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.server.ResponseStatusException;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import egovframework.board.service.BoardFileVO;
import egovframework.board.service.BoardService;
import egovframework.board.service.BoardVO;

@Controller
@RequestMapping("/board")
public class BoardController {

    private final BoardService boardService;

    public BoardController(BoardService boardService) {
        this.boardService = boardService;
    }

    /** 목록 (검색·페이징) */
    @GetMapping("/list")
    public String list(@ModelAttribute("searchVO") BoardVO searchVO, Model model) {
        PaginationInfo paginationInfo = new PaginationInfo();
        paginationInfo.setCurrentPageNo(searchVO.getPageIndex());
        paginationInfo.setRecordCountPerPage(searchVO.getRecordCountPerPage());
        paginationInfo.setPageSize(10);

        int totCnt = boardService.selectBoardListTotCnt(searchVO);
        paginationInfo.setTotalRecordCount(totCnt);
        searchVO.setFirstIndex(paginationInfo.getFirstRecordIndex());

        model.addAttribute("resultList", boardService.selectBoardList(searchVO));
        model.addAttribute("totCnt", totCnt);
        model.addAttribute("paginationInfo", paginationInfo);
        return "board/list";
    }

    /** 상세 */
    @GetMapping("/view/{boardId}")
    public String view(@PathVariable String boardId, Model model) {
        model.addAttribute("board", findOr404(boardId));
        return "board/view";
    }

    /** 등록 화면 */
    @GetMapping("/new")
    public String createForm(Model model) {
        model.addAttribute("board", new BoardVO());
        return "board/form";
    }

    /** 등록 */
    @PostMapping("/new")
    public String create(@Valid @ModelAttribute("board") BoardVO board, BindingResult bindingResult,
                         @RequestParam(value = "uploadFiles", required = false) List<MultipartFile> uploadFiles,
                         RedirectAttributes redirectAttributes) throws Exception {
        if (bindingResult.hasErrors()) {
            return "board/form";
        }
        String boardId = boardService.insertBoard(board, uploadFiles);
        redirectAttributes.addFlashAttribute("message", "게시물이 등록되었습니다.");
        return "redirect:/board/view/" + boardId;
    }

    /** 수정 화면 */
    @GetMapping("/edit/{boardId}")
    public String editForm(@PathVariable String boardId, Model model) {
        model.addAttribute("board", findOr404(boardId));
        return "board/form";
    }

    /** 수정 */
    @PostMapping("/edit/{boardId}")
    public String update(@PathVariable String boardId,
                         @Valid @ModelAttribute("board") BoardVO board, BindingResult bindingResult,
                         @RequestParam(value = "uploadFiles", required = false) List<MultipartFile> uploadFiles,
                         @RequestParam(value = "deleteFileIds", required = false) List<Long> deleteFileIds,
                         RedirectAttributes redirectAttributes) throws Exception {
        board.setBoardId(boardId);
        if (bindingResult.hasErrors()) {
            board.setFiles(findOr404(boardId).getFiles());
            return "board/form";
        }
        findOr404(boardId);
        boardService.updateBoard(board, uploadFiles, deleteFileIds);
        redirectAttributes.addFlashAttribute("message", "게시물이 수정되었습니다.");
        return "redirect:/board/view/" + boardId;
    }

    /** 삭제 */
    @PostMapping("/delete/{boardId}")
    public String delete(@PathVariable String boardId, RedirectAttributes redirectAttributes) throws Exception {
        findOr404(boardId);
        boardService.deleteBoard(boardId);
        redirectAttributes.addFlashAttribute("message", "게시물이 삭제되었습니다.");
        return "redirect:/board/list";
    }

    /** 첨부파일 다운로드 */
    @GetMapping("/file/{fileId}")
    public ResponseEntity<Resource> download(@PathVariable Long fileId) throws IOException {
        BoardFileVO file = boardService.selectFile(fileId);
        if (file == null) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "파일이 없습니다.");
        }
        Path path = boardService.resolveFilePath(file);
        if (!Files.isReadable(path)) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "파일이 없습니다.");
        }
        ContentDisposition disposition = ContentDisposition.attachment()
                .filename(file.getOriginalName(), StandardCharsets.UTF_8)
                .build();
        return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_DISPOSITION, disposition.toString())
                .contentType(MediaType.APPLICATION_OCTET_STREAM)
                .contentLength(Files.size(path))
                .body(new PathResource(path));
    }

    private BoardVO findOr404(String boardId) {
        BoardVO board = boardService.selectBoard(boardId);
        if (board == null) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "게시물이 없습니다.");
        }
        return board;
    }
}
