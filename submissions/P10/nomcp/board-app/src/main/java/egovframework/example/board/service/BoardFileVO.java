package egovframework.example.board.service;

public class BoardFileVO {
    private String fileId;
    private String boardId;
    private String origName;
    private String storedName;
    private long fileSize;

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
}
