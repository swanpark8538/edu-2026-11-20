package edu.board;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import org.springframework.http.ContentDisposition;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.multipart.MaxUploadSizeExceededException;
import org.springframework.web.multipart.MultipartFile;

@Controller
class BoardController {
    private final BoardStore board;

    BoardController(BoardStore board) {
        this.board = board;
    }

    @GetMapping("/")
    String list(Model model) {
        model.addAttribute("posts", board.list());
        return "list";
    }

    @GetMapping("/posts/new")
    String createForm(Model model) {
        model.addAttribute("post", new BoardStore.Post(null, "", "", "", null));
        return "form";
    }

    @GetMapping("/posts/{id}")
    String detail(@PathVariable String id, Model model) {
        model.addAttribute("post", board.get(id));
        return "detail";
    }

    @GetMapping("/posts/{id}/edit")
    String editForm(@PathVariable String id, Model model) {
        model.addAttribute("post", board.get(id));
        return "form";
    }

    @PostMapping({"/posts", "/posts/{id}/edit"})
    String save(@PathVariable(required = false) String id,
                @RequestParam String title, @RequestParam String writer, @RequestParam String content,
                @RequestParam(required = false) MultipartFile file,
                @RequestParam(defaultValue = "false") boolean removeAttachment, Model model) throws IOException {
        title = title.strip();
        writer = writer.strip();
        if (title.isEmpty() || title.length() > 200 || writer.isEmpty() || writer.length() > 50
                || content.length() > 4000) {
            String currentFile = id == null ? null : board.get(id).fileName();
            model.addAttribute("post", new BoardStore.Post(id, title, writer, content, currentFile));
            model.addAttribute("error", "제목은 1~200자, 작성자는 1~50자, 내용은 4000자 이하로 입력하세요.");
            return "form";
        }
        BoardStore.Attachment attachment = null;
        if (file != null && file.getOriginalFilename() != null && !file.getOriginalFilename().isBlank()) {
            String name = file.getOriginalFilename().replace('\\', '/');
            name = name.substring(name.lastIndexOf('/') + 1).replaceAll("[\\p{Cntrl}]", "").strip();
            if (name.isEmpty() || name.length() > 255 || name.equals(".") || name.equals("..")) {
                String currentFile = id == null ? null : board.get(id).fileName();
                model.addAttribute("post", new BoardStore.Post(id, title, writer, content, currentFile));
                model.addAttribute("error", "파일 이름은 1~255자로 입력하세요.");
                return "form";
            }
            attachment = new BoardStore.Attachment(name, file.getBytes());
        }
        return "redirect:/posts/" + board.save(id, title, writer, content, attachment, removeAttachment);
    }

    @PostMapping("/posts/{id}/delete")
    String delete(@PathVariable String id) {
        board.delete(id);
        return "redirect:/";
    }

    @GetMapping("/posts/{id}/attachment")
    ResponseEntity<byte[]> download(@PathVariable String id) {
        var attachment = board.attachment(id);
        String disposition = ContentDisposition.attachment()
            .filename(attachment.name(), StandardCharsets.UTF_8).build().toString();
        return ResponseEntity.ok().contentType(MediaType.APPLICATION_OCTET_STREAM)
            .header(HttpHeaders.CONTENT_DISPOSITION, disposition)
            .header("X-Content-Type-Options", "nosniff")
            .body(attachment.body());
    }

    @ExceptionHandler(MaxUploadSizeExceededException.class)
    ResponseEntity<String> uploadTooLarge() {
        return ResponseEntity.status(413).contentType(new MediaType("text", "plain", StandardCharsets.UTF_8))
            .body("첨부파일 크기는 5 MB 이하여야 합니다.");
    }
}
