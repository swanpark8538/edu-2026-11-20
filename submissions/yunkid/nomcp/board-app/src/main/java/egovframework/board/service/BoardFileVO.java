package egovframework.board.service;

import java.io.Serializable;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class BoardFileVO implements Serializable {

    private static final long serialVersionUID = 1L;

    private String fileId;

    private String boardId;

    private int fileSn;

    private String orgFileNm;

    private String saveFileNm;

    private String filePath;

    private long fileSize;

    private String contentType;
}
