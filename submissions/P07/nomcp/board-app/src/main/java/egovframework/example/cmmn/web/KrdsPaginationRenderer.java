package egovframework.example.cmmn.web;

import org.egovframe.rte.ptl.mvc.tags.ui.pagination.AbstractKrdsPaginationRenderer;

/**
 * KRDS 페이지네이션 마크업 렌더러.
 * renderPagination 의 두 번째 인자에는 자바스크립트 함수명 대신
 * HTML 이스케이프된 링크 접두어(…&amp;pageIndex=)를 넘긴다.
 */
public class KrdsPaginationRenderer extends AbstractKrdsPaginationRenderer {

    public KrdsPaginationRenderer() {
        firstPageLabel = "<a class=\"page-link\" href=\"{0}{1}\">{2}</a>";
        previousPageLabel = "<a class=\"page-navi prev\" href=\"{0}{1}\">이전</a>";
        previousPageDisabledLabel = "<span class=\"page-navi prev disabled\" aria-disabled=\"true\">이전</span>";
        currentPageLabel = "<a class=\"page-link active\" aria-current=\"page\"><span class=\"sr-only\">현재페이지 </span>{0}</a>";
        otherPageLabel = "<a class=\"page-link\" href=\"{0}{1}\">{2}</a>";
        nextPageLabel = "<a class=\"page-navi next\" href=\"{0}{1}\">다음</a>";
        nextPageDisabledLabel = "<span class=\"page-navi next disabled\" aria-disabled=\"true\">다음</span>";
        lastPageLabel = "<a class=\"page-link\" href=\"{0}{1}\">{2}</a>";
        dotPageLabel = "<span class=\"page-link link-dot\"></span>";
    }

}
