package egovframework.example.board.web;

import org.egovframe.rte.fdl.cmmn.exception.EgovBizException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.multipart.MaxUploadSizeExceededException;
import org.springframework.web.servlet.NoHandlerFoundException;
import org.springframework.web.servlet.resource.NoResourceFoundException;

import jakarta.servlet.http.HttpServletRequest;

/**
 * 컨트롤러 공통 예외 처리.
 *
 * <p>표준프레임워크 업무 예외인 {@link EgovBizException} 을 우선 처리하고,
 * 나머지는 일반 오류 화면으로 연결한다.</p>
 */
@ControllerAdvice
public class EgovBoardAdvice {

    private static final Logger LOGGER = LoggerFactory.getLogger(EgovBoardAdvice.class);

    /**
     * 업무 로직에서 발생한 표준 예외
     */
    @ExceptionHandler(EgovBizException.class)
    public String handleEgovBizException(EgovBizException e, HttpServletRequest request, Model model) {
        LOGGER.error("[EgovBizException] {} {}", request.getMethod(), request.getRequestURI(), e);
        return toErrorView(model, request, e.getMessageKey(), e.getMessage());
    }

    /**
     * 업로드 허용 크기 초과
     */
    @ExceptionHandler(MaxUploadSizeExceededException.class)
    public String handleMaxUploadSizeExceededException(MaxUploadSizeExceededException e,
            HttpServletRequest request, Model model) {
        LOGGER.error("[MaxUploadSizeExceededException] {}", request.getRequestURI(), e);
        return toErrorView(model, request, "error.board.exceedUploadSize",
                "업로드 가능한 파일 크기를 초과하였습니다.");
    }

    /**
     * 그 외 미처리 예외
     */
    @ExceptionHandler(Exception.class)
    public String handleException(Exception e, HttpServletRequest request, Model model) {
        LOGGER.error("[Exception] {} {}", request.getMethod(), request.getRequestURI(), e);
        return toErrorView(model, request, "error.common.system",
                "요청을 처리하는 중 오류가 발생하였습니다.");
    }

    /**
     * 존재하지 않는 요청 (404)
     */
    @ExceptionHandler({NoHandlerFoundException.class, NoResourceFoundException.class})
    @ResponseStatus(HttpStatus.NOT_FOUND)
    public String handleNotFound(Exception e, HttpServletRequest request, Model model) {
        LOGGER.warn("[NotFound] {}", request.getRequestURI());
        return toErrorView(model, request, "error.common.notFound", "요청하신 페이지를 찾을 수 없습니다.");
    }

    private String toErrorView(Model model, HttpServletRequest request, String errorCode, String message) {
        model.addAttribute("errorCode", errorCode);
        model.addAttribute("errorMsg", message);
        model.addAttribute("requestUri", request.getRequestURI());
        return "board/error";
    }

}
