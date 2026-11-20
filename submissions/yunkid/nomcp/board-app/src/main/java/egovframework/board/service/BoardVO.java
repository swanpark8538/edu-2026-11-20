package egovframework.board.service;

import java.util.Date;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class BoardVO extends BoardDefaultVO {

    private static final long serialVersionUID = 1L;

    private String boardId;

    @NotBlank(message = "제목은 필수입니다.")
    @Size(max = 200, message = "제목은 200자 이내입니다.")
    private String title;

    @Size(max = 4000, message = "내용은 4000자 이내입니다.")
    private String content;

    @NotBlank(message = "작성자는 필수입니다.")
    @Size(max = 50, message = "작성자는 50자 이내입니다.")
    private String writer;

    private Date regDt;

    private int fileCnt;
}
