package egovframework.board.service;

import java.io.Serializable;

public class BoardFileVO implements Serializable {

	private static final long serialVersionUID = 1L;

	private String fileId;
	private String boardId;
	private String origName;
	private String storedName;
	private long fileSize;
	private String contentType;

	public String getFileId() { return fileId; }
	public void setFileId(String fileId) { this.fileId = fileId; }
	public String getBoardId() { return boardId; }
	public void setBoardId(String boardId) { this.boardId = boardId; }
	public String getOrigName() { return origName; }
	public void setOrigName(String origName) { this.origName = origName; }
	public String getStoredName() { return storedName; }
	public void setStoredName(String storedName) { this.storedName = storedName; }
	public long getFileSize() { return fileSize; }
	public void setFileSize(long fileSize) { this.fileSize = fileSize; }
	public String getContentType() { return contentType; }
	public void setContentType(String contentType) { this.contentType = contentType; }
}
