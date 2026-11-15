package egovframework.example.cmmn.config;

import java.util.HashMap;
import java.util.Locale;
import java.util.Map;

import org.egovframe.rte.fdl.cmmn.trace.LeaveaTrace;
import org.egovframe.rte.fdl.cmmn.trace.handler.DefaultTraceHandler;
import org.egovframe.rte.fdl.cmmn.trace.handler.TraceHandler;
import org.egovframe.rte.fdl.cmmn.trace.manager.DefaultTraceHandleManager;
import org.egovframe.rte.fdl.cmmn.trace.manager.TraceHandlerService;
import org.egovframe.rte.fdl.property.impl.EgovPropertyServiceImpl;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.support.ReloadableResourceBundleMessageSource;
import org.springframework.util.AntPathMatcher;
import org.springframework.web.servlet.LocaleResolver;
import org.springframework.web.servlet.i18n.FixedLocaleResolver;

import egovframework.example.cmmn.web.KrdsPaginationRenderer;

@Configuration
public class EgovConfigCommon {

    @Bean
    public ReloadableResourceBundleMessageSource messageSource() {
        ReloadableResourceBundleMessageSource messageSource = new ReloadableResourceBundleMessageSource();
        messageSource.setBasenames(
                "classpath:/egovframework/message/message-common",
                "classpath:/org/egovframe/rte/fdl/idgnr/messages/idgnr",
                "classpath:/org/egovframe/rte/fdl/property/messages/properties",
                "classpath:/messages/message-common");
        messageSource.setDefaultEncoding("UTF-8");
        messageSource.setFallbackToSystemLocale(false);
        messageSource.setCacheSeconds(-1);
        return messageSource;
    }

    @Bean
    public LocaleResolver localeResolver() {
        return new FixedLocaleResolver(Locale.KOREAN);
    }

    @Bean
    public LeaveaTrace leaveaTrace() {
        DefaultTraceHandleManager traceHandlerService = new DefaultTraceHandleManager();
        traceHandlerService.setReqExpMatcher(new AntPathMatcher());
        traceHandlerService.setPatterns(new String[] {"*"});
        traceHandlerService.setHandlers(new TraceHandler[] {new DefaultTraceHandler()});

        LeaveaTrace leaveaTrace = new LeaveaTrace();
        leaveaTrace.setTraceHandlerServices(new TraceHandlerService[] {traceHandlerService});
        return leaveaTrace;
    }

    /** 화면 공통 설정값(페이징 단위 등)을 실행환경 Property 서비스로 제공한다. */
    @Bean
    public EgovPropertyServiceImpl propertiesService() {
        Map<String, String> properties = new HashMap<>();
        properties.put("pageUnit", "10");
        properties.put("pageSize", "10");

        EgovPropertyServiceImpl propertyService = new EgovPropertyServiceImpl();
        propertyService.setProperties(properties);
        return propertyService;
    }

    @Bean
    public KrdsPaginationRenderer krdsPaginationRenderer() {
        return new KrdsPaginationRenderer();
    }

}
