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

@ControllerAdvice
public class BoardExceptionHandler {

    private static final Logger LOGGER = LoggerFactory.getLogger(BoardExceptionHandler.class);

    @ExceptionHandler(EgovBizException.class)
    @ResponseStatus(HttpStatus.NOT_FOUND)
    public String handleBiz(EgovBizException e, Model model) {
        model.addAttribute("errorMessage", e.getMessage());
        return "error/message";
    }

    @ExceptionHandler(MaxUploadSizeExceededException.class)
    @ResponseStatus(HttpStatus.PAYLOAD_TOO_LARGE)
    public String handleUploadSize(MaxUploadSizeExceededException e, Model model) {
        model.addAttribute("errorMessage", "첨부파일 크기가 허용 범위(파일당 10MB, 요청당 50MB)를 넘었습니다.");
        return "error/message";
    }

    @ExceptionHandler(Exception.class)
    @ResponseStatus(HttpStatus.INTERNAL_SERVER_ERROR)
    public String handle(Exception e, Model model) {
        LOGGER.error("처리 중 오류", e);
        model.addAttribute("errorMessage", "처리 중 오류가 발생했습니다.");
        return "error/message";
    }
}
