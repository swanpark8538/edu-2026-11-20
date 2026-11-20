package egovframework.board.service;

import java.io.Serializable;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class BoardDefaultVO implements Serializable {

    private static final long serialVersionUID = 1L;

    private String searchCondition = "";

    private String searchKeyword = "";

    private int pageIndex = 1;

    private Integer pageUnit;

    private Integer pageSize;

    private int firstIndex;

    private int recordCountPerPage = 10;
}
