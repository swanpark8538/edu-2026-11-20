package egovframework.example.board.config;

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
import org.springframework.context.support.ReloadableResourceBundleMessageSource;

/**
 * 전자정부 표준프레임워크 공통 컴포넌트(FDL) 빈 등록 설정.
 *
 * <p>{@code EgovAbstractServiceImpl} 은 {@code messageSource}, {@code leaveaTrace}
 * 빈을 이름 기준으로 주입받으므로 아래 빈 반드시 존재해야 한다.</p>
 *
 * <ul>
 *   <li>messageSource : 표준 메시지 + FDL 메시지 공통 번들</li>
 *   <li>leaveaTrace  : 서비스 계층 이력(트레이스) 처리</li>
 *   <li>egovBoardIdGnrService / egovAtchfileIdGnrService : IDS 테이블 기반 채번 서비스</li>
 * </ul>
 */
@Configuration
public class EgovCoreConfiguration {

    /** 채번 관리 테이블명 (eGovFrame ID Generation 표준 테이블) */
    private static final String ID_TABLE = "IDS";

    /**
     * 메시지 번들 설정.
     * 애플리케이션 메시지와 FDL 공통 메시지(cmmn / idgnr)를 함께 로드한다.
     */
    @Bean("messageSource")
    public ReloadableResourceBundleMessageSource messageSource() {
        ReloadableResourceBundleMessageSource messageSource = new ReloadableResourceBundleMessageSource();
        messageSource.setBasenames(
                "classpath:/messages/message",
                "classpath:/messages/message-common",
                "classpath:/org/egovframe/rte/fdl/idgnr/messages/idgnr");
        messageSource.setDefaultEncoding("UTF-8");
        messageSource.setCacheSeconds(-1);
        messageSource.setFallbackToSystemLocale(false);
        messageSource.setAlwaysUseMessageFormat(false);
        return messageSource;
    }

    /**
     * 트레이스 핸들러 - 남겨진 트레이스 정보를 로그로 처리한다.
     */
    @Bean("traceHandler")
    public DefaultTraceHandler traceHandler() {
        return new DefaultTraceHandler();
    }

    /**
     * 트레이스 핸들 매니저 - 대상 패키지 패턴에 해당하는 경우 핸들러를 실행한다.
     */
    @Bean("traceHandleManager")
    public DefaultTraceHandleManager traceHandleManager(DefaultTraceHandler traceHandler) {
        DefaultTraceHandleManager traceHandleManager = new DefaultTraceHandleManager();
        traceHandleManager.setPatterns(new String[] { "egovframework.example.**" });
        traceHandleManager.setHandlers(new TraceHandler[] { traceHandler });
        return traceHandleManager;
    }

    /**
     * 이력 처리(트레이스) 서비스. 서비스 계층에서 leaveaTrace() 호출 시 사용된다.
     */
    @Bean("leaveaTrace")
    public LeaveaTrace leaveaTrace(DefaultTraceHandleManager traceHandleManager) {
        LeaveaTrace leaveaTrace = new LeaveaTrace();
        leaveaTrace.setTraceHandlerServices(new TraceHandlerService[] { traceHandleManager });
        return leaveaTrace;
    }

    /**
     * 게시물 ID 채번 서비스 - 0000000001 형태의 10자리 문자열 ID를 발급한다.
     */
    @Bean("egovBoardIdGnrService")
    public EgovTableIdGnrServiceImpl egovBoardIdGnrService(DataSource dataSource) {
        return createIdGnrService(dataSource, "BOARD");
    }

    /**
     * 첨부파일 ID 채번 서비스 - 0000000001 형태의 10자리 문자열 ID를 발급한다.
     */
    @Bean("egovAtchfileIdGnrService")
    public EgovTableIdGnrServiceImpl egovAtchfileIdGnrService(DataSource dataSource) {
        return createIdGnrService(dataSource, "BOARD_FILE");
    }

    private EgovTableIdGnrServiceImpl createIdGnrService(DataSource dataSource, String tableName) {
        EgovTableIdGnrServiceImpl idGnrService = new EgovTableIdGnrServiceImpl();
        idGnrService.setDataSource(dataSource);
        idGnrService.setTable(ID_TABLE);
        idGnrService.setTableName(tableName);
        idGnrService.setTableNameFieldName("TABLE_NAME");
        idGnrService.setNextIdFieldName("NEXT_ID");
        idGnrService.setBlockSize(1);
        idGnrService.setStrategy(new EgovIdGnrStrategyImpl("", 10, '0'));
        return idGnrService;
    }

}
