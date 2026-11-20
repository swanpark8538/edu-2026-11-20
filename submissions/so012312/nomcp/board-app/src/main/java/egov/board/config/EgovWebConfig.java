package egov.board.config;

import org.egovframe.rte.ptl.mvc.filter.HTMLTagFilter;
import org.springframework.boot.web.servlet.FilterRegistrationBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.Ordered;

/**
 * 표준프레임워크 화면기반(ptl-mvc) 필터 설정.
 *
 * <p>{@link HTMLTagFilter} 는 요청 파라미터에 포함된 HTML 태그를 걸러 주는
 * 표준프레임워크의 XSS 방지 필터이다. 게시판 요청(/board/*)에 적용한다.</p>
 */
@Configuration
public class EgovWebConfig {

    @Bean
    public FilterRegistrationBean<HTMLTagFilter> htmlTagFilterRegistration() {
        FilterRegistrationBean<HTMLTagFilter> registration = new FilterRegistrationBean<>(new HTMLTagFilter());
        registration.addUrlPatterns("/board/*");
        registration.setName("htmlTagFilter");
        registration.setOrder(Ordered.HIGHEST_PRECEDENCE + 10);
        return registration;
    }
}
