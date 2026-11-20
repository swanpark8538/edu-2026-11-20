package egovframework.board.service;

import java.io.Serializable;
import java.time.LocalDateTime;
import java.util.List;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public class BoardVO implements Serializable {

	private static final long serialVersionUID = 1L;

	private String boardId;

	@NotBlank(message = "제목을 입력하세요.")
	@Size(max = 200, message = "제목은 200자 이내로 입력하세요.")
	private String title;

	@Size(max = 4000, message = "내용은 4000자 이내로 입력하세요.")
	private String content;

	@NotBlank(message = "작성자를 입력하세요.")
	@Size(max = 50, message = "작성자는 50자 이내로 입력하세요.")
	private String writer;

	private int viewCnt;
	private int fileCnt;
	private LocalDateTime regDt;
	private List<BoardFileVO> attachments;

	public String getBoardId() { return boardId; }
	public void setBoardId(String boardId) { this.boardId = boardId; }
	public String getTitle() { return title; }
	public void setTitle(String title) { this.title = title; }
	public String getContent() { return content; }
	public void setContent(String content) { this.content = content; }
	public String getWriter() { return writer; }
	public void setWriter(String writer) { this.writer = writer; }
	public int getViewCnt() { return viewCnt; }
	public void setViewCnt(int viewCnt) { this.viewCnt = viewCnt; }
	public int getFileCnt() { return fileCnt; }
	public void setFileCnt(int fileCnt) { this.fileCnt = fileCnt; }
	public LocalDateTime getRegDt() { return regDt; }
	public void setRegDt(LocalDateTime regDt) { this.regDt = regDt; }
	public List<BoardFileVO> getAttachments() { return attachments; }
	public void setAttachments(List<BoardFileVO> attachments) { this.attachments = attachments; }
}
