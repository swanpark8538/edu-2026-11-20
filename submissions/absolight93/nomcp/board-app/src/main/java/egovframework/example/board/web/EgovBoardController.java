package egovframework.example.board.web;

import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.nio.file.Path;
import java.util.List;

import org.egovframe.rte.ptl.mvc.tags.ui.pagination.PaginationInfo;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.FileSystemResource;
import org.springframework.core.io.Resource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.MediaTypeFactory;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import egovframework.example.board.service.EgovBoardService;
import egovframework.example.board.service.vo.BoardFileVO;
import egovframework.example.board.service.vo.BoardSearchVO;
import egovframework.example.board.service.vo.BoardVO;
import jakarta.validation.Valid;

/**
 * 게시판 화면 컨트롤러.
 *
 * <p>목록 페이징에 eGovFrame {@link PaginationInfo} 를 사용한다.</p>
 */
@Controller
@RequestMapping("/board")
public class EgovBoardController {

    private static final Logger LOGGER = LoggerFactory.getLogger(EgovBoardController.class);

    private final EgovBoardService egovBoardService;

    /** 페이지당 레코드 수 */
    private final int recordCountPerPage;

    /** 화면당 페이지 수 */
    private final int pageSize;

    public EgovBoardController(EgovBoardService egovBoardService,
            @Value("${egov.board.record-count-per-page:10}") int recordCountPerPage,
            @Value("${egov.board.page-size:10}") int pageSize) {
        this.egovBoardService = egovBoardService;
        this.recordCountPerPage = recordCountPerPage;
        this.pageSize = pageSize;
    }

    /**
     * 게시판 목록 (검색/페이징)
     */
    @GetMapping("/list")
    public String selectBoardList(@ModelAttribute("boardSearchVO") BoardSearchVO boardSearchVO,
            @RequestParam(name = "pageIndex", defaultValue = "1") int pageIndex, Model model) throws Exception {

        PaginationInfo paginationInfo = new PaginationInfo();
        paginationInfo.setCurrentPageNo(pageIndex);
        paginationInfo.setRecordCountPerPage(recordCountPerPage);
        paginationInfo.setPageSize(pageSize);
        paginationInfo.setTotalRecordCount(egovBoardService.countBoardList(boardSearchVO));

        boardSearchVO.setFirstRecordIndex(paginationInfo.getFirstRecordIndex());
        boardSearchVO.setRecordCountPerPage(paginationInfo.getRecordCountPerPage());

        model.addAttribute("boardList", egovBoardService.selectBoardList(boardSearchVO));
        model.addAttribute("paginationInfo", paginationInfo);
        return "board/list";
    }

    /**
     * 게시물 상세 (조회 수 증가)
     */
    @GetMapping("/detail")
    public String readBoard(@RequestParam("boardId") String boardId, Model model) throws Exception {
        model.addAttribute("board", egovBoardService.readBoard(boardId));
        return "board/detail";
    }

    /**
     * 등록 화면
     */
    @GetMapping("/write")
    public String openWriteForm(Model model) {
        if (!model.containsAttribute("boardVO")) {
            model.addAttribute("boardVO", new BoardVO());
        }
        return "board/write";
    }

    /**
     * 게시물 등록
     */
    @PostMapping("/create")
    public String createBoard(@Valid @ModelAttribute("boardVO") BoardVO boardVO, BindingResult bindingResult,
            @RequestParam(name = "files", required = false) List<MultipartFile> files,
            RedirectAttributes redirectAttributes) throws Exception {

        if (bindingResult.hasErrors()) {
            return "board/write";
        }

        String boardId = egovBoardService.insertBoard(boardVO, files);
        LOGGER.debug("게시물 등록 : boardId={}", boardId);

        redirectAttributes.addFlashAttribute("resultMsg", "게시물이 등록되었습니다.");
        return "redirect:/board/detail?boardId=" + boardId;
    }

    /**
     * 수정 화면 (조회 수 변동 없음)
     */
    @GetMapping("/edit")
    public String openEditForm(@RequestParam("boardId") String boardId, Model model) throws Exception {
        if (!model.containsAttribute("boardVO")) {
            model.addAttribute("boardVO", egovBoardService.selectBoard(boardId));
        }
        return "board/edit";
    }

    /**
     * 게시물 수정
     */
    @PostMapping("/update")
    public String updateBoard(@Valid @ModelAttribute("boardVO") BoardVO boardVO, BindingResult bindingResult,
            @RequestParam(name = "files", required = false) List<MultipartFile> files,
            @RequestParam(name = "deleteAtchfileIds", required = false) List<String> deleteAtchfileIds,
            RedirectAttributes redirectAttributes) throws Exception {

        if (bindingResult.hasErrors()) {
            boardVO.setBoardFileList(egovBoardService.selectBoardFileList(boardVO.getBoardId()));
            return "board/edit";
        }

        egovBoardService.updateBoard(boardVO, files, deleteAtchfileIds);

        redirectAttributes.addFlashAttribute("resultMsg", "게시물이 수정되었습니다.");
        return "redirect:/board/detail?boardId=" + boardVO.getBoardId();
    }

    /**
     * 게시물 삭제
     */
    @PostMapping("/delete")
    public String deleteBoard(@RequestParam("boardId") String boardId, RedirectAttributes redirectAttributes)
            throws Exception {
        egovBoardService.deleteBoard(boardId);

        redirectAttributes.addFlashAttribute("resultMsg", "게시물이 삭제되었습니다.");
        return "redirect:/board/list";
    }

    /**
     * 첨부파일 다운로드
     */
    @GetMapping("/file/download")
    public ResponseEntity<Resource> downloadBoardFile(@RequestParam("atchfileId") String atchfileId) throws Exception {
        BoardFileVO boardFileVO = egovBoardService.selectBoardFile(atchfileId);
        Path storedPath = egovBoardService.getStoredFilePath(boardFileVO);
        Resource resource = new FileSystemResource(storedPath);

        String downloadName = URLEncoder.encode(boardFileVO.getFileOrglnm(), StandardCharsets.UTF_8)
                .replaceAll("\\+", "%20");
        MediaType mediaType = MediaTypeFactory.getMediaType(boardFileVO.getFileOrglnm())
                .orElse(MediaType.APPLICATION_OCTET_STREAM);

        return ResponseEntity.ok()
                .contentType(mediaType)
                .contentLength(boardFileVO.getFileSize())
                .header(HttpHeaders.CONTENT_DISPOSITION,
                        "attachment; filename=\"" + downloadName + "\"; filename*=UTF-8''" + downloadName)
                .header(HttpHeaders.CACHE_CONTROL, "no-cache, no-store, must-revalidate")
                .body(resource);
    }

    /**
     * 첨부파일 단건 삭제
     */
    @PostMapping("/file/delete")
    public String deleteBoardFile(@RequestParam("atchfileId") String atchfileId,
            @RequestParam("boardId") String boardId, RedirectAttributes redirectAttributes) throws Exception {
        egovBoardService.deleteBoardFile(atchfileId);

        redirectAttributes.addFlashAttribute("resultMsg", "첨부파일이 삭제되었습니다.");
        return "redirect:/board/detail?boardId=" + boardId;
    }

}
