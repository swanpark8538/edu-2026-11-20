package egovframework.example.board.web;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;

/**
 * 진입 화면 처리. 루트 요청은 게시판 목록으로 연결한다.
 */
@Controller
public class EgovIndexController {

    @GetMapping("/")
    public String gotoBoardList() {
        return "redirect:/board/list";
    }

}
