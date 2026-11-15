package egovframework.example.board.web;

import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import org.egovframe.rte.fdl.cmmn.exception.EgovBizException;
import org.egovframe.rte.fdl.property.EgovPropertyService;
import org.egovframe.rte.ptl.mvc.tags.ui.pagination.PaginationInfo;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.MessageSource;
import org.springframework.context.i18n.LocaleContextHolder;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.util.unit.DataSize;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.WebDataBinder;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.InitBinder;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;
import org.springframework.web.util.HtmlUtils;

import egovframework.example.board.service.BoardDefaultVO;
import egovframework.example.board.service.BoardService;
import egovframework.example.board.service.BoardVO;
import egovframework.example.cmmn.file.service.EgovFileMngService;
import egovframework.example.cmmn.web.KrdsPaginationRenderer;
import jakarta.annotation.Resource;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;

/**
 * 게시판 목록 / 등록 / 조회·수정 / 삭제.
 */
@Controller
public class BoardController {

    private static final String LIST_VIEW = "board/boardList";
    private static final String REGISTER_VIEW = "board/boardRegister";
    private static final String UPDATE_VIEW = "board/boardUpdate";

    @Resource(name = "boardService")
    private BoardService boardService;

    @Resource(name = "egovFileMngService")
    private EgovFileMngService fileMngService;

    @Resource(name = "propertiesService")
    private EgovPropertyService propertiesService;

    @Resource(name = "krdsPaginationRenderer")
    private KrdsPaginationRenderer paginationRenderer;

    @Resource(name = "messageSource")
    private MessageSource messageSource;

    @Value("${Globals.fileUpload.extensions}")
    private String fileExtensions;

    @Value("${Globals.fileUpload.maxCount:10}")
    private int fileMaxCount;

    @Value("${spring.servlet.multipart.max-file-size:10MB}")
    private DataSize fileMaxSize;

    /** 화면의 파일 선택 단계에서 미리 안내·검사할 업로드 정책 (최종 검사는 서버에서 한다) */
    @ModelAttribute("filePolicy")
    public Map<String, Object> filePolicy() {
        String accept = Arrays.stream(fileExtensions.split(","))
                .map(String::strip)
                .filter(ext -> !ext.isEmpty())
                .map(ext -> "." + ext)
                .collect(Collectors.joining(","));
        return Map.of(
                "accept", accept,
                "maxCount", fileMaxCount,
                "maxSize", fileMaxSize.toBytes(),
                "maxSizeText", fileMaxSize.toMegabytes() + "MB");
    }

    /** 클라이언트가 보낸 값으로 첨부 묶음 ID·등록일 등을 덮어쓰지 못하도록 바인딩 필드를 제한한다. */
    @InitBinder("boardVO")
    public void initBoardBinder(WebDataBinder binder) {
        binder.setAllowedFields("boardId", "title", "content", "writer", "searchCondition", "searchKeyword", "pageIndex");
    }

    @InitBinder("searchVO")
    public void initSearchBinder(WebDataBinder binder) {
        binder.setAllowedFields("searchCondition", "searchKeyword", "pageIndex");
    }

    @GetMapping("/")
    public String index() {
        return "redirect:/board/list.do";
    }

    @GetMapping("/board/list.do")
    public String selectBoardList(@ModelAttribute("searchVO") BoardDefaultVO searchVO,
            HttpServletRequest request, Model model) throws Exception {
        searchVO.normalize();
        searchVO.setPageUnit(propertiesService.getInt("pageUnit"));
        searchVO.setPageSize(propertiesService.getInt("pageSize"));

        PaginationInfo paginationInfo = new PaginationInfo();
        paginationInfo.setCurrentPageNo(searchVO.getPageIndex());
        paginationInfo.setRecordCountPerPage(searchVO.getPageUnit());
        paginationInfo.setPageSize(searchVO.getPageSize());

        int totCnt = boardService.selectBoardListTotCnt(searchVO);
        paginationInfo.setTotalRecordCount(totCnt);
        if (searchVO.getPageIndex() > paginationInfo.getTotalPageCount()) {
            searchVO.setPageIndex(Math.max(paginationInfo.getTotalPageCount(), 1));
            paginationInfo.setCurrentPageNo(searchVO.getPageIndex());
        }

        searchVO.setFirstIndex(paginationInfo.getFirstRecordIndex());
        searchVO.setLastIndex(paginationInfo.getLastRecordIndex());
        searchVO.setRecordCountPerPage(paginationInfo.getRecordCountPerPage());

        List<BoardVO> resultList = boardService.selectBoardList(searchVO);

        model.addAttribute("resultList", resultList);
        model.addAttribute("totCnt", totCnt);
        model.addAttribute("paginationInfo", paginationInfo);
        model.addAttribute("pagination", paginationRenderer.renderPagination(paginationInfo, pageLinkPrefix(request, searchVO)));
        return LIST_VIEW;
    }

    @GetMapping("/board/register.do")
    public String registerForm(@ModelAttribute("boardVO") BoardVO boardVO) {
        boardVO.normalize();
        return REGISTER_VIEW;
    }

    @PostMapping("/board/register.do")
    public String insertBoard(@Valid @ModelAttribute("boardVO") BoardVO boardVO, BindingResult bindingResult,
            @RequestParam(value = "files", required = false) List<MultipartFile> files,
            Model model, RedirectAttributes redirectAttributes) throws Exception {
        boardVO.normalize();
        if (bindingResult.hasErrors()) {
            return REGISTER_VIEW;
        }
        try {
            String boardId = boardService.insertBoard(boardVO, files);
            redirectAttributes.addFlashAttribute("message", message("success.board.insert"));
            addSearchParams(redirectAttributes, boardVO);
            redirectAttributes.addAttribute("boardId", boardId);
            return "redirect:/board/update.do";
        } catch (EgovBizException e) {
            model.addAttribute("errorMessage", e.getMessage());
            return REGISTER_VIEW;
        }
    }

    @GetMapping("/board/update.do")
    public String updateForm(@ModelAttribute("searchVO") BoardDefaultVO searchVO,
            @RequestParam("boardId") String boardId, Model model) throws Exception {
        searchVO.normalize();
        BoardVO boardVO = boardService.selectBoard(boardId);
        boardVO.setSearchCondition(searchVO.getSearchCondition());
        boardVO.setSearchKeyword(searchVO.getSearchKeyword());
        boardVO.setPageIndex(searchVO.getPageIndex());
        model.addAttribute("boardVO", boardVO);
        model.addAttribute("fileList", fileMngService.selectFileList(boardVO.getAtchFileId()));
        return UPDATE_VIEW;
    }

    @PostMapping("/board/update.do")
    public String updateBoard(@Valid @ModelAttribute("boardVO") BoardVO boardVO, BindingResult bindingResult,
            @RequestParam(value = "files", required = false) List<MultipartFile> files,
            Model model, RedirectAttributes redirectAttributes) throws Exception {
        boardVO.normalize();
        BoardVO stored = boardService.selectBoard(boardVO.getBoardId());
        if (bindingResult.hasErrors()) {
            return updateView(boardVO, stored, model, null);
        }
        try {
            boardService.updateBoard(boardVO, files);
        } catch (EgovBizException e) {
            return updateView(boardVO, stored, model, e.getMessage());
        }
        redirectAttributes.addFlashAttribute("message", message("success.board.update"));
        addSearchParams(redirectAttributes, boardVO);
        redirectAttributes.addAttribute("boardId", boardVO.getBoardId());
        return "redirect:/board/update.do";
    }

    @PostMapping("/board/delete.do")
    public String deleteBoard(@ModelAttribute("searchVO") BoardDefaultVO searchVO,
            @RequestParam("boardId") String boardId, RedirectAttributes redirectAttributes) throws Exception {
        searchVO.normalize();
        boardService.deleteBoard(boardId);
        redirectAttributes.addFlashAttribute("message", message("success.board.delete"));
        addSearchParams(redirectAttributes, searchVO);
        return "redirect:/board/list.do";
    }

    @PostMapping("/board/deleteFile.do")
    public String deleteBoardFile(@ModelAttribute("searchVO") BoardDefaultVO searchVO,
            @RequestParam("boardId") String boardId, @RequestParam("fileSn") int fileSn,
            RedirectAttributes redirectAttributes) throws Exception {
        searchVO.normalize();
        boardService.deleteBoardFile(boardId, fileSn);
        redirectAttributes.addFlashAttribute("message", message("success.file.delete"));
        addSearchParams(redirectAttributes, searchVO);
        redirectAttributes.addAttribute("boardId", boardId);
        return "redirect:/board/update.do";
    }

    private String updateView(BoardVO boardVO, BoardVO stored, Model model, String errorMessage) throws Exception {
        boardVO.setAtchFileId(stored.getAtchFileId());
        boardVO.setRegDt(stored.getRegDt());
        boardVO.setUpdDt(stored.getUpdDt());
        model.addAttribute("fileList", fileMngService.selectFileList(stored.getAtchFileId()));
        if (errorMessage != null) {
            model.addAttribute("errorMessage", errorMessage);
        }
        return UPDATE_VIEW;
    }

    private void addSearchParams(RedirectAttributes redirectAttributes, BoardDefaultVO searchVO) {
        redirectAttributes.addAttribute("searchCondition", searchVO.getSearchCondition());
        redirectAttributes.addAttribute("searchKeyword", searchVO.getSearchKeyword());
        redirectAttributes.addAttribute("pageIndex", searchVO.getPageIndex());
    }

    /** 페이지 번호만 이어 붙이면 되는, HTML 이스케이프된 목록 링크 접두어 */
    private String pageLinkPrefix(HttpServletRequest request, BoardDefaultVO searchVO) {
        String url = request.getContextPath() + "/board/list.do"
                + "?searchCondition=" + URLEncoder.encode(searchVO.getSearchCondition(), StandardCharsets.UTF_8)
                + "&searchKeyword=" + URLEncoder.encode(searchVO.getSearchKeyword(), StandardCharsets.UTF_8)
                + "&pageIndex=";
        return HtmlUtils.htmlEscape(url);
    }

    private String message(String code) {
        return messageSource.getMessage(code, null, LocaleContextHolder.getLocale());
    }

}
