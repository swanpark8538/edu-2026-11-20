package egov.board.web;

import egov.board.service.BoardService;
import egov.board.service.vo.BoardListResultVO;
import egov.board.service.vo.BoardSearchVO;
import egov.board.service.vo.BoardVO;
import jakarta.servlet.http.HttpSession;
import jakarta.validation.Valid;
import org.egovframe.rte.fdl.cmmn.EgovAbstractServiceImpl;
import org.egovframe.rte.fdl.cmmn.exception.EgovBizException;
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

import java.util.Collections;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;

/**
 * 게시판 화면 요청을 처리하는 컨트롤러.
 *
 * <p>목록 / 상세 / 등록 / 수정 / 삭제 요청을 처리하고, 조회수는 같은 세션에서
 * 한 게시물에 대해 1회만 증가시킨다.</p>
 */
@Controller
@RequestMapping("/board")
public class BoardController extends EgovAbstractServiceImpl {

    private static final String VIEWED_BOARD_IDS_SESSION_KEY = "BOARD_VIEWED_IDS";

    private final BoardService boardService;

    public BoardController(BoardService boardService) {
        this.boardService = boardService;
    }

    /** 게시물 목록 */
    @GetMapping
    public String list(@ModelAttribute("searchVO") BoardSearchVO searchVO, Model model) throws EgovBizException {
        BoardListResultVO resultVO = boardService.selectBoardList(searchVO);
        model.addAttribute("boardList", resultVO.getBoardList());
        model.addAttribute("paginationInfo", resultVO.getPaginationInfo());
        return "board/boardList";
    }

    /** 게시물 상세 (첨부파일 목록 포함) */
    @GetMapping("/detail")
    public String detail(@RequestParam("boardId") String boardId,
                         @ModelAttribute("searchVO") BoardSearchVO searchVO,
                         HttpSession session,
                         Model model) throws EgovBizException {
        boolean increaseViewCnt = !viewedBoardIds(session).contains(boardId);
        BoardVO boardVO = boardService.selectBoard(boardId, increaseViewCnt);
        if (increaseViewCnt) {
            markViewed(session, boardId);
        }
        model.addAttribute("board", boardVO);
        return "board/boardDetail";
    }

    /** 게시물 등록 화면 */
    @GetMapping("/writeForm")
    public String writeForm(@ModelAttribute("searchVO") BoardSearchVO searchVO, Model model) {
        model.addAttribute("board", new BoardVO());
        return "board/boardForm";
    }

    /** 게시물 등록 */
    @PostMapping("/write")
    public String write(@Valid @ModelAttribute("board") BoardVO boardVO,
                        BindingResult bindingResult,
                        @RequestParam(value = "atcFile", required = false) List<MultipartFile> atcFileList,
                        RedirectAttributes redirectAttributes) throws EgovBizException {
        if (bindingResult.hasErrors()) {
            return "board/boardForm";
        }
        String boardId = boardService.insertBoard(boardVO, nullSafe(atcFileList));
        egovLogger.info("게시물 등록 : boardId={}", boardId);
        redirectAttributes.addAttribute("boardId", boardId);
        return "redirect:/board/detail";
    }

    /** 게시물 수정 화면 */
    @GetMapping("/editForm")
    public String editForm(@RequestParam("boardId") String boardId,
                           @ModelAttribute("searchVO") BoardSearchVO searchVO,
                           Model model) throws EgovBizException {
        model.addAttribute("board", boardService.selectBoard(boardId, false));
        return "board/boardForm";
    }

    /** 게시물 수정 (첨부파일 추가·삭제 포함) */
    @PostMapping("/edit")
    public String edit(@Valid @ModelAttribute("board") BoardVO boardVO,
                       BindingResult bindingResult,
                       @RequestParam(value = "atcFile", required = false) List<MultipartFile> atcFileList,
                       @RequestParam(value = "delAtcFileId", required = false) List<String> delAtcFileIdList,
                       RedirectAttributes redirectAttributes) throws EgovBizException {
        if (bindingResult.hasErrors()) {
            return "board/boardForm";
        }
        boardService.updateBoard(boardVO, nullSafe(atcFileList), delAtcFileIdList);
        redirectAttributes.addAttribute("boardId", boardVO.getBoardId());
        return "redirect:/board/detail";
    }

    /** 게시물 삭제 */
    @PostMapping("/delete")
    public String delete(@RequestParam("boardId") String boardId,
                         @ModelAttribute("searchVO") BoardSearchVO searchVO,
                         RedirectAttributes redirectAttributes) throws EgovBizException {
        boardService.deleteBoard(boardId);
        egovLogger.info("게시물 삭제 : boardId={}", boardId);
        redirectAttributes.addFlashAttribute("deletedBoardId", boardId);
        // 삭제 전 보던 페이지·검색 조건을 유지한 채 목록으로 돌아간다.
        redirectAttributes.addAttribute("currentPageNo", searchVO.getCurrentPageNo());
        redirectAttributes.addAttribute("searchCond", searchVO.getSearchCond());
        if (searchVO.getSearchWord() != null) {
            redirectAttributes.addAttribute("searchWord", searchVO.getSearchWord());
        }
        return "redirect:/board";
    }

    private List<MultipartFile> nullSafe(List<MultipartFile> atcFileList) {
        return atcFileList == null ? Collections.emptyList() : atcFileList;
    }

    @SuppressWarnings("unchecked")
    private Set<String> viewedBoardIds(HttpSession session) {
        Object stored = session.getAttribute(VIEWED_BOARD_IDS_SESSION_KEY);
        return stored instanceof Set ? (Set<String>) stored : Collections.emptySet();
    }

    private void markViewed(HttpSession session, String boardId) {
        Set<String> viewedIds = new LinkedHashSet<>(viewedBoardIds(session));
        viewedIds.add(boardId);
        session.setAttribute(VIEWED_BOARD_IDS_SESSION_KEY, viewedIds);
    }
}

