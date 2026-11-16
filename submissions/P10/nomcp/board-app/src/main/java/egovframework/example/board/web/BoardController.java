package egovframework.example.board.web;

import java.nio.charset.StandardCharsets;
import java.util.List;

import org.springframework.core.io.Resource;
import org.springframework.http.ContentDisposition;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.multipart.MultipartFile;

import egovframework.example.board.service.BoardFileVO;
import egovframework.example.board.service.BoardService;
import egovframework.example.board.service.BoardVO;
import jakarta.validation.Valid;

@Controller
public class BoardController {

    private final BoardService service;

    public BoardController(BoardService service) {
        this.service = service;
    }

    @GetMapping("/")
    public String home() {
        return "redirect:/board";
    }

    @GetMapping("/board")
    public String list(Model model) {
        model.addAttribute("boards", service.list());
        return "board/list";
    }

    @GetMapping("/board/new")
    public String newForm(Model model) {
        model.addAttribute("board", new BoardVO());
        return "board/form";
    }

    @PostMapping("/board")
    public String create(@Valid @ModelAttribute("board") BoardVO board, BindingResult result,
            @RequestParam(value = "attachments", required = false) List<MultipartFile> files) {
        if (result.hasErrors()) {
            return "board/form";
        }
        String id = service.create(board, files);
        return "redirect:/board/" + id;
    }

    @GetMapping("/board/{id}")
    public String view(@PathVariable String id, Model model) {
        BoardVO vo = service.get(id);
        if (vo == null) {
            return "redirect:/board";
        }
        model.addAttribute("board", vo);
        return "board/view";
    }

    @GetMapping("/board/{id}/edit")
    public String editForm(@PathVariable String id, Model model) {
        BoardVO vo = service.get(id);
        if (vo == null) {
            return "redirect:/board";
        }
        model.addAttribute("board", vo);
        return "board/form";
    }

    @PostMapping("/board/{id}")
    public String update(@PathVariable String id, @Valid @ModelAttribute("board") BoardVO board,
            BindingResult result,
            @RequestParam(value = "attachments", required = false) List<MultipartFile> files,
            @RequestParam(value = "deleteFileIds", required = false) List<String> deleteFileIds,
            Model model) {
        board.setBoardId(id);
        if (result.hasErrors()) {
            board.setFiles(service.get(id).getFiles());
            return "board/form";
        }
        service.update(board, files, deleteFileIds);
        return "redirect:/board/" + id;
    }

    @PostMapping("/board/{id}/delete")
    public String delete(@PathVariable String id) {
        service.delete(id);
        return "redirect:/board";
    }

    @GetMapping("/board/file/{fileId}")
    public ResponseEntity<Resource> download(@PathVariable String fileId) {
        BoardFileVO f = service.getFile(fileId);
        if (f == null) {
            return ResponseEntity.notFound().build();
        }
        Resource res = service.loadFile(f);
        if (!res.exists()) {
            return ResponseEntity.notFound().build();
        }
        return ResponseEntity.ok()
                .contentType(MediaType.APPLICATION_OCTET_STREAM)
                .header(HttpHeaders.CONTENT_DISPOSITION,
                        ContentDisposition.attachment().filename(f.getOrigName(), StandardCharsets.UTF_8).build().toString())
                .contentLength(f.getFileSize())
                .body(res);
    }
}

