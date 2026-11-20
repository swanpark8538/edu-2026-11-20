package egovframework.board.web;

import java.nio.file.Path;
import java.util.List;

import org.egovframe.rte.fdl.property.EgovPropertyService;
import org.egovframe.rte.ptl.mvc.tags.ui.pagination.PaginationInfo;
import org.springframework.core.io.FileSystemResource;
import org.springframework.core.io.Resource;
import org.springframework.http.ContentDisposition;
import org.springframework.http.HttpHeaders;
import org.springframework.http.InvalidMediaTypeException;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.util.StringUtils;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import egovframework.board.service.BoardFileVO;
import egovframework.board.service.BoardService;
import egovframework.board.service.BoardVO;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;

@Controller
@RequiredArgsConstructor
public class BoardController {

    private static final int MAX_PAGE_VALUE = 100;

    private final BoardService boardService;
    private final EgovPropertyService propertiesService;

    @GetMapping("/")
    public String index() {
        return "redirect:/boardList.do";
    }

    @GetMapping("/boardList.do")
    public String selectBoardList(@ModelAttribute BoardVO boardVO, Model model) {
        if (boardVO.getPageIndex() < 1) {
            boardVO.setPageIndex(1);
        }
        boardVO.setPageUnit(resolvePageValue(boardVO.getPageUnit(), "pageUnit"));
        boardVO.setPageSize(resolvePageValue(boardVO.getPageSize(), "pageSize"));

        PaginationInfo paginationInfo = new PaginationInfo();
        paginationInfo.setCurrentPageNo(boardVO.getPageIndex());
        paginationInfo.setRecordCountPerPage(boardVO.getPageUnit());
        paginationInfo.setPageSize(boardVO.getPageSize());

        boardVO.setFirstIndex(paginationInfo.getFirstRecordIndex());
        boardVO.setRecordCountPerPage(paginationInfo.getRecordCountPerPage());

        model.addAttribute("resultList", boardService.selectBoardList(boardVO));
        paginationInfo.setTotalRecordCount(boardService.selectBoardListTotCnt(boardVO));
        model.addAttribute("paginationInfo", paginationInfo);
        return "board/list";
    }

    @GetMapping("/boardRegister.do")
    public String registerView(Model model) {
        model.addAttribute("boardVO", new BoardVO());
        model.addAttribute("fileList", List.of());
        return "board/form";
    }

    @PostMapping("/addBoard.do")
    public String addBoard(@Valid @ModelAttribute BoardVO boardVO,
                           BindingResult bindingResult,
                           @RequestParam(value = "files", required = false) List<MultipartFile> files,
                           Model model,
                           RedirectAttributes redirectAttributes) {
        if (bindingResult.hasErrors()) {
            model.addAttribute("fileList", List.of());
            return "board/form";
        }
        String boardId = boardService.insertBoard(boardVO, files);
        redirectAttributes.addFlashAttribute("message", "게시글이 등록되었습니다.");
        return "redirect:/boardDetail.do?boardId=" + boardId;
    }

    @GetMapping("/boardDetail.do")
    public String selectBoard(@RequestParam String boardId, Model model) {
        BoardVO condition = new BoardVO();
        condition.setBoardId(boardId);
        BoardVO boardVO = boardService.selectBoard(condition);
        model.addAttribute("boardVO", boardVO);
        model.addAttribute("fileList", boardService.selectFileList(boardId));
        return "board/detail";
    }

    @GetMapping("/boardUpdate.do")
    public String updateView(@RequestParam String boardId, Model model) {
        BoardVO condition = new BoardVO();
        condition.setBoardId(boardId);
        model.addAttribute("boardVO", boardService.selectBoard(condition));
        model.addAttribute("fileList", boardService.selectFileList(boardId));
        return "board/form";
    }

    @PostMapping("/updateBoard.do")
    public String updateBoard(@Valid @ModelAttribute BoardVO boardVO,
                              BindingResult bindingResult,
                              @RequestParam(value = "files", required = false) List<MultipartFile> files,
                              Model model,
                              RedirectAttributes redirectAttributes) {
        if (bindingResult.hasErrors()) {
            model.addAttribute("fileList", boardService.selectFileList(boardVO.getBoardId()));
            return "board/form";
        }
        boardService.updateBoard(boardVO, files);
        redirectAttributes.addFlashAttribute("message", "게시글이 수정되었습니다.");
        return "redirect:/boardDetail.do?boardId=" + boardVO.getBoardId();
    }

    @PostMapping("/deleteBoard.do")
    public String deleteBoard(@RequestParam String boardId, RedirectAttributes redirectAttributes) {
        BoardVO boardVO = new BoardVO();
        boardVO.setBoardId(boardId);
        boardService.deleteBoard(boardVO);
        redirectAttributes.addFlashAttribute("message", "게시글이 삭제되었습니다.");
        return "redirect:/boardList.do";
    }

    @GetMapping("/boardFileDownload.do")
    public ResponseEntity<Resource> download(@RequestParam String fileId) {
        BoardFileVO file = boardService.selectFile(fileId);
        Path path = boardService.resolveStoredFile(file);
        ContentDisposition disposition = ContentDisposition.attachment()
                .filename(file.getOrgFileNm(), java.nio.charset.StandardCharsets.UTF_8)
                .build();
        return ResponseEntity.ok()
                .contentType(mediaType(file.getContentType()))
                .header(HttpHeaders.CONTENT_DISPOSITION, disposition.toString())
                .body(new FileSystemResource(path));
    }

    @PostMapping("/deleteBoardFile.do")
    public String deleteFile(@RequestParam String fileId,
                             @RequestParam String boardId,
                             RedirectAttributes redirectAttributes) {
        boardService.deleteFile(fileId);
        redirectAttributes.addFlashAttribute("message", "첨부파일을 삭제했습니다.");
        return "redirect:/boardUpdate.do?boardId=" + boardId;
    }

    private int resolvePageValue(Integer value, String property) {
        int configured = Math.max(1, Math.min(propertiesService.getInt(property), MAX_PAGE_VALUE));
        if (value != null && value > 0 && value <= MAX_PAGE_VALUE) {
            return value;
        }
        return configured;
    }

    private MediaType mediaType(String contentType) {
        if (!StringUtils.hasText(contentType)) {
            return MediaType.APPLICATION_OCTET_STREAM;
        }
        try {
            return MediaType.parseMediaType(contentType);
        } catch (InvalidMediaTypeException ex) {
            return MediaType.APPLICATION_OCTET_STREAM;
        }
    }
}
