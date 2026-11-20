package egovframework.board.web;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.servlet.ModelAndView;

import lombok.extern.slf4j.Slf4j;

@ControllerAdvice
@Slf4j
public class BoardExceptionHandler {

    @ExceptionHandler(Exception.class)
    public ModelAndView handle(Exception exception) {
        log.error("request failed", exception);
        ModelAndView modelAndView = new ModelAndView("board/error");
        modelAndView.setStatus(HttpStatus.INTERNAL_SERVER_ERROR);
        modelAndView.addObject("exception", exception);
        return modelAndView;
    }
}
