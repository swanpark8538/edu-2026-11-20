package egovframework.example.board.web;

import java.nio.charset.StandardCharsets;
import java.util.List;

import org.egovframe.rte.ptl.mvc.tags.ui.pagination.PaginationInfo;
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

import egovframework.example.board.service.BoardDefaultVO;
import egovframework.example.board.service.BoardFileVO;
import egovframework.example.board.service.BoardService;
import egovframework.example.board.service.BoardVO;

/**
 * 게시판 컨트롤러
 */
@Controller
public class BoardController {

    private final BoardService boardService;

    public BoardController(BoardService boardService) {
        this.boardService = boardService;
    }

    @GetMapping("/")
    public String index() {
        return "redirect:/board/list.do";
    }

    @GetMapping("/board/list.do")
    public String list(@ModelAttribute("searchVO") BoardDefaultVO searchVO, Model model) throws Exception {
        PaginationInfo paginationInfo = new PaginationInfo();
        paginationInfo.setCurrentPageNo(searchVO.getPageIndex());
        paginationInfo.setRecordCountPerPage(searchVO.getPageUnit());
        paginationInfo.setPageSize(searchVO.getPageSize());

        searchVO.setFirstIndex(paginationInfo.getFirstRecordIndex());
        searchVO.setRecordCountPerPage(paginationInfo.getRecordCountPerPage());

        int totCnt = boardService.selectBoardListTotCnt(searchVO);
        paginationInfo.setTotalRecordCount(totCnt);

        model.addAttribute("resultList", boardService.selectBoardList(searchVO));
        model.addAttribute("totCnt", totCnt);
        model.addAttribute("paginationInfo", paginationInfo);
        return "board/list";
    }

    @GetMapping("/board/{boardId}/detail.do")
    public String detail(@PathVariable String boardId, Model model) throws Exception {
        model.addAttribute("board", boardService.selectBoard(boardId));
        return "board/detail";
    }

    @GetMapping("/board/add.do")
    public String addForm(Model model) {
        model.addAttribute("board", new BoardVO());
        return "board/form";
    }

    @PostMapping("/board/add.do")
    public String add(@Validated @ModelAttribute("board") BoardVO board, BindingResult bindingResult,
            @RequestParam(value = "uploadFiles", required = false) List<MultipartFile> uploadFiles,
            RedirectAttributes redirectAttributes) throws Exception {
        if (bindingResult.hasErrors()) {
            return "board/form";
        }
        String boardId = boardService.insertBoard(board, uploadFiles);
        redirectAttributes.addFlashAttribute("message", "등록되었습니다.");
        return "redirect:/board/" + boardId + "/detail.do";
    }

    @GetMapping("/board/{boardId}/edit.do")
    public String editForm(@PathVariable String boardId, Model model) throws Exception {
        model.addAttribute("board", boardService.selectBoard(boardId));
        return "board/form";
    }

    @PostMapping("/board/{boardId}/edit.do")
    public String edit(@PathVariable String boardId,
            @Validated @ModelAttribute("board") BoardVO board, BindingResult bindingResult,
            @RequestParam(value = "uploadFiles", required = false) List<MultipartFile> uploadFiles,
            @RequestParam(value = "deleteFileIds", required = false) List<String> deleteFileIds,
            RedirectAttributes redirectAttributes) throws Exception {
        board.setBoardId(boardId);
        if (bindingResult.hasErrors()) {
            board.setFiles(boardService.selectBoard(boardId).getFiles());
            return "board/form";
        }
        boardService.updateBoard(board, uploadFiles, deleteFileIds);
        redirectAttributes.addFlashAttribute("message", "수정되었습니다.");
        return "redirect:/board/" + boardId + "/detail.do";
    }

    @PostMapping("/board/{boardId}/delete.do")
    public String delete(@PathVariable String boardId, RedirectAttributes redirectAttributes) throws Exception {
        boardService.deleteBoard(boardId);
        redirectAttributes.addFlashAttribute("message", "삭제되었습니다.");
        return "redirect:/board/list.do";
    }

    @GetMapping("/board/file/{fileId}/download.do")
    public ResponseEntity<Resource> download(@PathVariable String fileId) throws Exception {
        BoardFileVO file = boardService.selectFile(fileId);
        Resource resource = boardService.loadFile(file);

        ContentDisposition disposition = ContentDisposition.attachment()
                .filename(file.getOrignlFileNm(), StandardCharsets.UTF_8)
                .build();
        return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_DISPOSITION, disposition.toString())
                .contentType(MediaType.APPLICATION_OCTET_STREAM)
                .contentLength(file.getFileSize())
                .body(resource);
    }
}
