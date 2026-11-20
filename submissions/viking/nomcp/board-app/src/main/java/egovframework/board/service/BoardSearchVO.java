package egovframework.board.service;

import java.io.Serializable;

public class BoardSearchVO implements Serializable {

	private static final long serialVersionUID = 1L;

	private String keyword;
	private int page = 1;
	private int pageSize = 10;

	public String getKeyword() { return keyword; }
	public void setKeyword(String keyword) { this.keyword = keyword; }
	public int getPage() { return page; }
	public void setPage(int page) { this.page = Math.max(page, 1); }
	public int getPageSize() { return pageSize; }
	public void setPageSize(int pageSize) { this.pageSize = pageSize; }
	public int getOffset() { return (page - 1) * pageSize; }
}
