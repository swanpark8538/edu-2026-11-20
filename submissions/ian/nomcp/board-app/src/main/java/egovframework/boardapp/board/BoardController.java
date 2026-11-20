package egovframework.boardapp.board;

import java.io.IOException;
import java.util.List;

import org.springframework.core.io.FileSystemResource;
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
import org.springframework.web.server.ResponseStatusException;
import org.springframework.http.HttpStatus;
import jakarta.validation.Valid;
import java.nio.charset.StandardCharsets;

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
        model.addAttribute("boards", service.list());
        return "board/list";
    }

    @GetMapping("/boards/new")
    public String createForm(Model model) {
        model.addAttribute("board", new BoardVO());
        return "board/form";
    }

    @PostMapping("/boards")
    public String create(@Valid @ModelAttribute("board") BoardVO board, BindingResult result,
                         @RequestParam(value = "uploadFiles", required = false) List<MultipartFile> files) {
        if (result.hasErrors()) {
            return "board/form";
        }
        String id = service.create(board, files);
        return "redirect:/boards/" + id;
    }

    @GetMapping("/boards/{id}")
    public String view(@PathVariable String id, Model model) {
        model.addAttribute("board", find(id));
        model.addAttribute("prev", service.getPrev(id));
        model.addAttribute("next", service.getNext(id));
        return "board/view";
    }

    @GetMapping("/boards/{id}/edit")
    public String editForm(@PathVariable String id, Model model) {
        model.addAttribute("board", find(id));
        return "board/form";
    }

    @PostMapping("/boards/{id}")
    public String update(@PathVariable String id, @Valid @ModelAttribute("board") BoardVO board,
                         BindingResult result,
                         @RequestParam(value = "uploadFiles", required = false) List<MultipartFile> files,
                         @RequestParam(value = "deleteFileIds", required = false) List<String> deleteFileIds,
                         Model model) {
        board.setBoardId(id);
        if (result.hasErrors()) {
            board.setFiles(find(id).getFiles());
            return "board/form";
        }
        find(id);
        service.update(board, files, deleteFileIds);
        return "redirect:/boards/" + id;
    }

    @PostMapping("/boards/{id}/delete")
    public String delete(@PathVariable String id) {
        find(id);
        service.delete(id);
        return "redirect:/boards";
    }

    @GetMapping("/files/{fileId}")
    public ResponseEntity<Resource> download(@PathVariable String fileId) throws IOException {
        BoardFileVO file = service.getFile(fileId);
        if (file == null) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND);
        }
        FileSystemResource resource = new FileSystemResource(service.resolve(file));
        if (!resource.exists()) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND);
        }
        MediaType type = MediaType.APPLICATION_OCTET_STREAM;
        if (file.getContentType() != null) {
            try {
                type = MediaType.parseMediaType(file.getContentType());
            } catch (IllegalArgumentException ignored) {
                // 알 수 없는 타입은 octet-stream
            }
        }
        return ResponseEntity.ok()
                .contentType(type)
                .header(HttpHeaders.CONTENT_DISPOSITION,
                        ContentDisposition.attachment().filename(file.getOrigName(), StandardCharsets.UTF_8).build().toString())
                .contentLength(file.getFileSize())
                .body(resource);
    }

    private BoardVO find(String id) {
        BoardVO board = service.get(id);
        if (board == null) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "게시물을 찾을 수 없습니다.");
        }
        return board;
    }
}
