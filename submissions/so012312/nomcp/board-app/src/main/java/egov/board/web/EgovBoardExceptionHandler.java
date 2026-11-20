package egov.board.web;

import jakarta.servlet.http.HttpServletRequest;
import org.egovframe.rte.fdl.cmmn.EgovAbstractServiceImpl;
import org.egovframe.rte.fdl.cmmn.exception.EgovBizException;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.multipart.MaxUploadSizeExceededException;
import org.springframework.web.servlet.ModelAndView;

/**
 * 게시판 예외 처리 Advice.
 *
 * <p>표준프레임워크 공통기반의 업무 예외 {@link EgovBizException} 은 사용자에게 알릴 메시지를
 * 그대로 화면에 보여 주고, 그 밖의 예외는 상세 정보를 감춘 채 서버 로그에 남긴다.</p>
 */
@ControllerAdvice(basePackages = "egov.board")
public class EgovBoardExceptionHandler extends EgovAbstractServiceImpl {

    /** 게시물이 없다거나 첨부파일 검증 실패 등 업무 오류 */
    @ExceptionHandler(EgovBizException.class)
    public ModelAndView handleEgovBizException(EgovBizException e, HttpServletRequest request) {
        egovLogger.warn("업무 오류 [{}] : {}", request.getRequestURI(), e.getMessage());
        return errorView(e.getMessage(), HttpStatus.OK);
    }

    /** 첨부파일 확장자·크기 검증 실패 등 잘못된 요청 */
    @ExceptionHandler({IllegalArgumentException.class, IllegalStateException.class})
    public ModelAndView handleBadRequest(RuntimeException e, HttpServletRequest request) {
        egovLogger.warn("잘못된 요청 [{}] : {}", request.getRequestURI(), e.getMessage());
        return errorView(e.getMessage(), HttpStatus.BAD_REQUEST);
    }

    /** multipart 최대 크기를 넘긴 업로드 */
    @ExceptionHandler(MaxUploadSizeExceededException.class)
    public ModelAndView handleMaxUploadSizeExceeded(MaxUploadSizeExceededException e, HttpServletRequest request) {
        egovLogger.warn("첨부파일 크기 초과 [{}] : {}", request.getRequestURI(), e.getMessage());
        return errorView("요청한 첨부파일의 크기가 허용된 한도를 넘었습니다. 첨부파일 설정을 확인해 주세요.",
                HttpStatus.BAD_REQUEST);
    }

    /** 그 밖의 시스템 오류 */
    @ExceptionHandler(Exception.class)
    public ModelAndView handleException(Exception e, HttpServletRequest request) {
        egovLogger.error("시스템 오류 [" + request.getRequestURI() + "]", e);
        return errorView("요청을 처리하는 동안 문제가 발생했습니다. 잠시 후 다시 시도해 주세요.",
                HttpStatus.INTERNAL_SERVER_ERROR);
    }

    private ModelAndView errorView(String message, HttpStatus status) {
        ModelAndView mav = new ModelAndView("board/error");
        mav.setStatus(status);
        mav.addObject("errorMessage", message == null ? "문제가 발생했습니다." : message);
        return mav;
    }
}
