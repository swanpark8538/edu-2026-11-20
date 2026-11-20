package kr.egovlab.board;

import java.io.IOException;
import java.nio.charset.StandardCharsets;

import jakarta.validation.Valid;
import kr.egovlab.board.BoardService.Attachment;
import kr.egovlab.board.BoardService.Post;
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
import org.springframework.web.multipart.MultipartFile;

@Controller
public class BoardController {
    private final BoardService service;

    public BoardController(BoardService service) {
        this.service = service;
    }

    @GetMapping("/")
    public String home() {
        return "redirect:/boards";
    }

    @GetMapping("/boards")
    public String list(Model model) {
        model.addAttribute("posts", service.list());
        return "boards/list";
    }

    @GetMapping("/boards/new")
    public String newPost(Model model) {
        model.addAttribute("form", new BoardForm("", "", "", null, false));
        return "boards/form";
    }

    @PostMapping("/boards")
    public String create(@Valid @ModelAttribute("form") BoardForm form, BindingResult errors) throws IOException {
        Attachment attachment = readAttachment(form.file(), errors);
        if (errors.hasErrors()) {
            return "boards/form";
        }
        return "redirect:/boards/" + service.create(form, attachment);
    }

    @GetMapping("/boards/{id}")
    public String detail(@PathVariable String id, Model model) {
        model.addAttribute("post", service.find(id));
        return "boards/detail";
    }

    @GetMapping("/boards/{id}/edit")
    public String edit(@PathVariable String id, Model model) {
        Post post = service.find(id);
        model.addAttribute("post", post);
        model.addAttribute("form", new BoardForm(post.title(), post.writer(), post.content(), null, false));
        return "boards/form";
    }

    @PostMapping("/boards/{id}/edit")
    public String update(@PathVariable String id, @Valid @ModelAttribute("form") BoardForm form,
                         BindingResult errors, Model model) throws IOException {
        Attachment attachment = readAttachment(form.file(), errors);
        if (errors.hasErrors()) {
            model.addAttribute("post", service.find(id));
            return "boards/form";
        }
        service.update(id, form, attachment);
        return "redirect:/boards/" + id;
    }

    @PostMapping("/boards/{id}/delete")
    public String delete(@PathVariable String id) {
        service.delete(id);
        return "redirect:/boards";
    }

    @GetMapping("/boards/{id}/attachment")
    public ResponseEntity<byte[]> download(@PathVariable String id) {
        Attachment attachment = service.attachment(id);
        String disposition = ContentDisposition.attachment()
                .filename(attachment.name(), StandardCharsets.UTF_8)
                .build().toString();
        return ResponseEntity.ok()
                .contentType(MediaType.APPLICATION_OCTET_STREAM)
                .header(HttpHeaders.CONTENT_DISPOSITION, disposition)
                .header("X-Content-Type-Options", "nosniff")
                .body(attachment.data());
    }

    private Attachment readAttachment(MultipartFile file, BindingResult errors) throws IOException {
        if (file == null || file.getOriginalFilename() == null || file.getOriginalFilename().isEmpty()) {
            return null;
        }
        String path = file.getOriginalFilename().replace('\\', '/');
        String name = path.substring(path.lastIndexOf('/') + 1).strip();
        if (name.isEmpty() || name.equals(".") || name.equals("..") || name.length() > 255
                || name.codePoints().anyMatch(Character::isISOControl)) {
            errors.rejectValue("file", "file.name", "첨부파일 이름을 확인해 주세요. 최대 255자까지 사용할 수 있습니다.");
            return null;
        }
        return errors.hasErrors() ? null : new Attachment(name, file.getBytes());
    }
}
