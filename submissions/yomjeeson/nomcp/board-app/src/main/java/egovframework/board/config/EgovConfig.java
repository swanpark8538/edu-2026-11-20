package egovframework.board.config;

import javax.sql.DataSource;

import org.egovframe.rte.fdl.cmmn.trace.LeaveaTrace;
import org.egovframe.rte.fdl.cmmn.trace.handler.DefaultTraceHandler;
import org.egovframe.rte.fdl.cmmn.trace.handler.TraceHandler;
import org.egovframe.rte.fdl.cmmn.trace.manager.DefaultTraceHandleManager;
import org.egovframe.rte.fdl.cmmn.trace.manager.TraceHandlerService;
import org.egovframe.rte.fdl.idgnr.impl.EgovTableIdGnrServiceImpl;
import org.egovframe.rte.fdl.idgnr.impl.strategy.EgovIdGnrStrategyImpl;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.util.AntPathMatcher;

/**
 * eGovFrame 실행환경 공통 빈 설정.
 * - leaveaTrace : EgovAbstractServiceImpl 이 주입받는 trace 빈
 * - boardIdGnrService : IDS 테이블 기반 게시물 ID 생성기 (BOARD_00000000000001)
 */
@Configuration
public class EgovConfig {

    @Bean
    public DefaultTraceHandleManager traceHandlerService() {
        DefaultTraceHandleManager manager = new DefaultTraceHandleManager();
        manager.setReqExpMatcher(new AntPathMatcher());
        manager.setPatterns(new String[] {"*"});
        manager.setHandlers(new TraceHandler[] {new DefaultTraceHandler()});
        return manager;
    }

    @Bean
    public LeaveaTrace leaveaTrace(DefaultTraceHandleManager traceHandlerService) {
        LeaveaTrace leaveaTrace = new LeaveaTrace();
        leaveaTrace.setTraceHandlerServices(new TraceHandlerService[] {traceHandlerService});
        return leaveaTrace;
    }

    @Bean
    public EgovIdGnrStrategyImpl boardIdStrategy() {
        EgovIdGnrStrategyImpl strategy = new EgovIdGnrStrategyImpl();
        strategy.setPrefix("BOARD_");
        strategy.setCipers(14);
        strategy.setFillChar('0');
        return strategy;
    }

    @Bean
    public EgovTableIdGnrServiceImpl boardIdGnrService(DataSource dataSource, EgovIdGnrStrategyImpl boardIdStrategy) {
        EgovTableIdGnrServiceImpl service = new EgovTableIdGnrServiceImpl();
        service.setDataSource(dataSource);
        service.setStrategy(boardIdStrategy);
        service.setBlockSize(10);
        service.setTable("IDS");
        service.setTableName("BOARD_ID");
        return service;
    }
}
