package egovframework.example.cmmn.web;

import org.egovframe.rte.fdl.cmmn.exception.EgovBizException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.MessageSource;
import org.springframework.context.i18n.LocaleContextHolder;
import org.springframework.http.HttpStatus;
import org.springframework.ui.Model;
import org.springframework.web.HttpRequestMethodNotSupportedException;
import org.springframework.web.bind.MissingServletRequestParameterException;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException;
import org.springframework.web.multipart.MaxUploadSizeExceededException;
import org.springframework.web.multipart.MultipartException;
import org.springframework.web.servlet.resource.NoResourceFoundException;

import jakarta.annotation.Resource;

/**
 * 예외를 사용자용 오류 화면으로 바꾼다. 스택 트레이스 등 내부 정보는 화면에 노출하지 않고 로그에만 남긴다.
 */
@ControllerAdvice
public class EgovExceptionAdvice {

    private static final Logger LOGGER = LoggerFactory.getLogger(EgovExceptionAdvice.class);

    private static final String ERROR_VIEW = "error";

    @Resource(name = "messageSource")
    private MessageSource messageSource;

    @ExceptionHandler(EgovBizException.class)
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    public String handleBizException(EgovBizException e, Model model) {
        LOGGER.info("업무 예외: {}", e.getMessageKey());
        return errorView(model, HttpStatus.BAD_REQUEST, e.getMessage());
    }

    @ExceptionHandler(EgovResourceNotFoundException.class)
    @ResponseStatus(HttpStatus.NOT_FOUND)
    public String handleFileNotFound(Model model) {
        return errorView(model, HttpStatus.NOT_FOUND, message("fail.file.notFound"));
    }

    @ExceptionHandler(NoResourceFoundException.class)
    @ResponseStatus(HttpStatus.NOT_FOUND)
    public String handleNotFound(Model model) {
        return errorView(model, HttpStatus.NOT_FOUND, message("fail.common.notFound"));
    }

    @ExceptionHandler({MissingServletRequestParameterException.class, MethodArgumentTypeMismatchException.class,
            IllegalArgumentException.class})
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    public String handleBadRequest(Exception e, Model model) {
        LOGGER.info("잘못된 요청: {}", e.getClass().getSimpleName());
        return errorView(model, HttpStatus.BAD_REQUEST, message("fail.common.badRequest"));
    }

    @ExceptionHandler(HttpRequestMethodNotSupportedException.class)
    @ResponseStatus(HttpStatus.METHOD_NOT_ALLOWED)
    public String handleMethodNotAllowed(Model model) {
        return errorView(model, HttpStatus.METHOD_NOT_ALLOWED, message("fail.common.badRequest"));
    }

    @ExceptionHandler({MaxUploadSizeExceededException.class, MultipartException.class})
    @ResponseStatus(HttpStatus.PAYLOAD_TOO_LARGE)
    public String handleUploadSize(Model model) {
        return errorView(model, HttpStatus.PAYLOAD_TOO_LARGE, message("fail.file.maxSize"));
    }

    @ExceptionHandler(Exception.class)
    @ResponseStatus(HttpStatus.INTERNAL_SERVER_ERROR)
    public String handleException(Exception e, Model model) {
        LOGGER.error("처리 중 오류", e);
        return errorView(model, HttpStatus.INTERNAL_SERVER_ERROR, message("fail.common.msg"));
    }

    private String errorView(Model model, HttpStatus status, String errorMessage) {
        model.addAttribute("status", status.value());
        model.addAttribute("errorMessage", errorMessage);
        return ERROR_VIEW;
    }

    private String message(String code) {
        return messageSource.getMessage(code, null, LocaleContextHolder.getLocale());
    }

}
