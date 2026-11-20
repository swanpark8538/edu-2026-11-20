package kr.egovlab.board;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import org.springframework.web.multipart.MultipartFile;

public record BoardForm(
        @NotBlank(message = "제목을 입력해 주세요.")
        @Size(max = 200, message = "제목은 200자까지 입력할 수 있습니다.")
        String title,

        @NotBlank(message = "작성자를 입력해 주세요.")
        @Size(max = 50, message = "작성자는 50자까지 입력할 수 있습니다.")
        String writer,

        @Size(max = 4000, message = "본문은 4,000자까지 입력할 수 있습니다.")
        String content,

        MultipartFile file,
        Boolean removeAttachment
) {}
