package egovframework.example.cmm.web;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;

import egovframework.example.cmm.service.EgovFileMngService;
import egovframework.example.cmm.service.EgovFileMngUtil;
import egovframework.example.cmm.service.FileVO;

import jakarta.annotation.Resource;
import jakarta.servlet.http.HttpServletResponse;

/**
 * 첨부파일 다운로드 컨트롤러.
 *
 * 표준 다운로드 URL: GET /cmm/fms/FileDown.do?atchFileId=&fileSn=
 *
 * 보안:
 * - `EgovFileMngUtil.downFile` 가 path traversal 차단
 * - atchFileId 자체는 평문 노출 (세션 단위 암호화는 v1 범위 밖 — 필요 시 별도 구현)
 *
 * 인증/인가:
 * - 접근 제어가 필요하면 별도 정책(예: Spring Security 의 인증 규칙 또는 Interceptor)으로 본 URL 을 보호한다. 4.3 Boot 산출물에는 기본 접근 제어가 없다
 */
@Controller
public class EgovFileDownloadController {

	@Resource(name = "EgovFileMngService")
	private EgovFileMngService fileService;

	@Resource(name = "EgovFileMngUtil")
	private EgovFileMngUtil fileUtil;

	@GetMapping("/cmm/fms/FileDown.do")
	public void fileDownload(
			@RequestParam("atchFileId") String atchFileId,
			@RequestParam("fileSn") String fileSn,
			HttpServletResponse response) throws Exception {

		FileVO key = new FileVO();
		key.setAtchFileId(atchFileId);
		key.setFileSn(fileSn);

		FileVO fvo = fileService.selectFileInf(key);
		if (fvo == null) {
			response.sendError(HttpServletResponse.SC_NOT_FOUND);
			return;
		}
		fileUtil.downFile(response, fvo.getFileStreCours(), fvo.getStreFileNm(), fvo.getOrignlFileNm());
	}
}
