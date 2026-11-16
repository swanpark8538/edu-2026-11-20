package egovframework.example.board.service;

import java.util.Date;
import java.util.List;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public class BoardVO {
    private String boardId;
    @NotBlank @Size(max = 200)
    private String title;
    @Size(max = 4000)
    private String content;
    @NotBlank @Size(max = 50)
    private String writer;
    private Date regDt;
    private List<BoardFileVO> files;

    public String getBoardId() { return boardId; }
    public void setBoardId(String boardId) { this.boardId = boardId; }
    public String getTitle() { return title; }
    public void setTitle(String title) { this.title = title; }
    public String getContent() { return content; }
    public void setContent(String content) { this.content = content; }
    public String getWriter() { return writer; }
    public void setWriter(String writer) { this.writer = writer; }
    public Date getRegDt() { return regDt; }
    public void setRegDt(Date regDt) { this.regDt = regDt; }
    public List<BoardFileVO> getFiles() { return files; }
    public void setFiles(List<BoardFileVO> files) { this.files = files; }
}
