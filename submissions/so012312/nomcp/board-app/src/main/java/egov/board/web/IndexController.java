package egov.board.web;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;

/**
 * 첫 화면 진입을 게시판 목록으로 안내하는 컨트롤러.
 */
@Controller
public class IndexController {

    @GetMapping("/")
    public String index() {
        return "redirect:/board";
    }
}
