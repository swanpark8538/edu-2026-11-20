package egovframework.example.board.web;

import java.util.List;

import org.egovframe.rte.fdl.cmmn.exception.EgovBizException;
import org.egovframe.rte.fdl.property.EgovPropertyService;
import org.egovframe.rte.ptl.mvc.tags.ui.pagination.PaginationInfo;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.ui.ModelMap;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.support.SessionStatus;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.multipart.MultipartHttpServletRequest;

import egovframework.example.cmm.service.EgovFileMngService;
import egovframework.example.cmm.service.EgovFileMngUtil;
import egovframework.example.cmm.service.FileVO;

import egovframework.example.board.service.EgovBoardService;
import egovframework.example.board.service.BoardVO;
import jakarta.annotation.Resource;
import jakarta.validation.Valid;
import lombok.extern.slf4j.Slf4j;

@Controller
@Slf4j
public class EgovBoardController {

	@Resource(name = "boardService")
	private EgovBoardService boardService;

	@Resource(name = "propertiesService")
	private EgovPropertyService propertiesService;

	/** 첨부파일 메타 CRUD (그룹 키 채번/INSERT/UPDATE/DELETE). */
	@Resource(name = "EgovFileMngService")
	private EgovFileMngService fileMngService;

	/** multipart → 디스크 IO + FileVO 변환 유틸. */
	@Resource(name = "EgovFileMngUtil")
	private EgovFileMngUtil fileUtil;

	@GetMapping("/")
	public String index(@ModelAttribute("boardVO") BoardVO boardVO, ModelMap model) throws Exception {
		return this.selectBoardList(boardVO, model);
	}

	@GetMapping("/egovBoardList.do")
	public String selectBoardList(@ModelAttribute("boardVO") BoardVO boardVO, ModelMap model) throws Exception {

		boardVO.setPageUnit(propertiesService.getInt("pageUnit"));
		boardVO.setPageSize(propertiesService.getInt("pageSize"));

		PaginationInfo paginationInfo = new PaginationInfo();
		paginationInfo.setCurrentPageNo(boardVO.getPageIndex());
		paginationInfo.setRecordCountPerPage(boardVO.getPageUnit());
		paginationInfo.setPageSize(boardVO.getPageSize());

		boardVO.setFirstIndex(paginationInfo.getFirstRecordIndex());
		boardVO.setLastIndex(paginationInfo.getLastRecordIndex());
		boardVO.setRecordCountPerPage(paginationInfo.getRecordCountPerPage());

		List<?> boardList = boardService.selectBoardList(boardVO);
		model.addAttribute("resultList", boardList);

		int totCnt = boardService.selectBoardListTotCnt(boardVO);
		paginationInfo.setTotalRecordCount(totCnt);

		model.addAttribute("paginationInfo", paginationInfo);

		return "board/egovBoardList";
	}

	@PostMapping("/addBoardView.do")
	public String addBoardView(@ModelAttribute("boardVO") BoardVO boardVO, Model model) throws Exception {
		model.addAttribute("boardVO", boardVO);
		return "board/egovBoardRegister";
	}

	@PostMapping("/addBoard.do")
	public String addBoard(final MultipartHttpServletRequest multiRequest, @Valid @ModelAttribute("boardVO") BoardVO boardVO, BindingResult bindingResult, Model model, SessionStatus status) throws Exception {
		if (bindingResult.hasErrors()) {
			model.addAttribute("boardVO", boardVO);
			return "board/egovBoardRegister";
		}
		// 첨부파일 처리 — parseFileInf 로 디스크에 저장한 뒤 insertFileInfs 로 그룹 키 채번/INSERT.
		// 반환된 atchFileId 를 도메인 VO 에 세팅한 다음 도메인 INSERT 를 수행한다.
		String atchFileId = "";
		final java.util.List<MultipartFile> files = multiRequest.getFiles("file_1");
		if (files != null && !files.isEmpty()) {
			java.util.List<FileVO> fvoList = fileUtil.parseFileInf(files, "BOARD_", 0, "", "");
			atchFileId = fileMngService.insertFileInfs(fvoList);
		}
		boardVO.setAtchFileId(atchFileId);
		boardService.insertBoard(boardVO);
		status.setComplete();
		return "redirect:/egovBoardList.do";
	}

	@PostMapping("/updateBoardView.do")
	public String updateBoardView(@ModelAttribute("boardVO") BoardVO boardVO, Model model) throws Exception {
		BoardVO detail = boardService.selectBoard(boardVO);
		detail.setSearchCondition(boardVO.getSearchCondition());
		detail.setSearchKeyword(boardVO.getSearchKeyword());
		detail.setPageIndex(boardVO.getPageIndex());
		model.addAttribute("boardVO", detail);
		// 기존 첨부파일 목록을 model 에 주입 — Register view 에서 다운로드 링크 형태로 렌더된다.
		// atchFileId 가 없거나 비어 있으면 빈 목록을 (또는 미주입) — 화면이 보호 코드로 처리.
		String existingAtchFileId = detail.getAtchFileId();
		if (existingAtchFileId != null && !existingAtchFileId.isEmpty()) {
			FileVO attachKey = new FileVO();
			attachKey.setAtchFileId(existingAtchFileId);
			model.addAttribute("attachList", fileMngService.selectFileInfs(attachKey));
		}
		return "board/egovBoardRegister";
	}

	@PostMapping("/updateBoard.do")
	public String updateBoard(final MultipartHttpServletRequest multiRequest, @Valid @ModelAttribute("boardVO") BoardVO boardVO, BindingResult bindingResult,
			Model model, RedirectAttributes redirectAttributes, SessionStatus status) throws Exception {
		if (bindingResult.hasErrors()) {
			model.addAttribute("boardVO", boardVO);
			return "board/egovBoardRegister";
		}
		// 첨부파일 update — 신규 그룹이면 채번, 기존 그룹이면 다음 FILE_SN 부터 append.
		// 그룹 키는 요청 값을 신뢰하지 않고 DB 에 저장된 값에서 읽는다 (다른 레코드의 첨부 그룹 탈취 방지).
		BoardVO stored = boardService.selectBoard(boardVO);
		String atchFileId = stored.getAtchFileId();
		final java.util.List<MultipartFile> files = multiRequest.getFiles("file_1");
		if (files != null && !files.isEmpty()) {
			if (atchFileId == null || atchFileId.isBlank()) {
				// (A) 첨부 그룹이 아직 없음 → 신규 채번
				java.util.List<FileVO> fvoList = fileUtil.parseFileInf(files, "BOARD_", 0, "", "");
				atchFileId = fileMngService.insertFileInfs(fvoList);
				boardVO.setAtchFileId(atchFileId);
			} else {
				// (B) 기존 그룹에 추가 — getMaxFileSN 으로 다음 FILE_SN 부터 (중복 PK 방지)
				FileVO key = new FileVO();
				key.setAtchFileId(atchFileId);
				int cnt = fileMngService.getMaxFileSN(key);
				java.util.List<FileVO> fvoList = fileUtil.parseFileInf(files, "BOARD_", cnt, atchFileId, "");
				fileMngService.updateFileInfs(fvoList);
			}
		}
		// 요청에 실려 온 값을 덮어써 DB 값(또는 새로 채번한 값)만 저장한다.
		boardVO.setAtchFileId(atchFileId);
		boardService.updateBoard(boardVO);
		status.setComplete();
		redirectAttributes.addAttribute("searchCondition", boardVO.getSearchCondition());
		redirectAttributes.addAttribute("searchKeyword", boardVO.getSearchKeyword());
		redirectAttributes.addAttribute("pageIndex", boardVO.getPageIndex());
		return "redirect:/egovBoardList.do";
	}

	@PostMapping("/deleteBoard.do")
	public String deleteBoard(@ModelAttribute("boardVO") BoardVO boardVO, RedirectAttributes redirectAttributes, SessionStatus status) throws Exception {
		boardService.deleteBoard(boardVO);
		status.setComplete();
		redirectAttributes.addAttribute("searchCondition", boardVO.getSearchCondition());
		redirectAttributes.addAttribute("searchKeyword", boardVO.getSearchKeyword());
		redirectAttributes.addAttribute("pageIndex", boardVO.getPageIndex());
		return "redirect:/egovBoardList.do";
	}

	/**
	 * 첨부 그룹의 단일 파일(atchFileId + fileSn) 1건 삭제.
	 * 도메인 행은 유지하고 첨부 한 건만 떼어내는 용도 — 수정 화면에서 호출.
	 * 반환: 단순 텍스트 "ok" (성공) — 화면 JS 가 location.reload 로 목록 갱신.
	 *
	 * 첨부 그룹 키(atchFileId)는 요청 값을 신뢰하지 않고 boardId 로 조회한 DB 값에서 읽는다.
	 * (updateBoard 와 동일한 원칙 — 다른 게시물의 첨부파일을 지우는 것을 막는다.)
	 * 요청에 atchFileId 가 함께 오면 DB 값과 일치하는지 확인하고, 다르면 거부한다.
	 */
	@PostMapping("/deleteBoardAttachFile.do")
	@ResponseBody
	public ResponseEntity<String> deleteBoardAttachFile(
			@RequestParam("boardId") String boardId,
			@RequestParam("fileSn") String fileSn,
			@RequestParam(value = "atchFileId", required = false) String requestAtchFileId) throws Exception {
		BoardVO key = new BoardVO();
		key.setBoardId(boardId);
		BoardVO stored;
		try {
			// 게시물이 없으면 서비스가 EgovBizException(info.nodata.msg) 을 던진다.
			stored = boardService.selectBoard(key);
		} catch (EgovBizException e) {
			return ResponseEntity.status(HttpStatus.NOT_FOUND).body("not found");
		}
		String atchFileId = stored.getAtchFileId();
		if (atchFileId == null || atchFileId.isBlank()
				|| (requestAtchFileId != null && !requestAtchFileId.equals(atchFileId))) {
			log.warn("첨부파일 삭제 거부: boardId={}, fileSn={}, 요청 atchFileId={}", boardId, fileSn, requestAtchFileId);
			return ResponseEntity.status(HttpStatus.FORBIDDEN).body("forbidden");
		}
		FileVO fvo = new FileVO();
		fvo.setAtchFileId(atchFileId);
		fvo.setFileSn(fileSn);
		fileMngService.deleteFileInf(fvo);
		return ResponseEntity.ok("ok");
	}

}
