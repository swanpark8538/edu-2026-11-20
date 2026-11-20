package egov.board.config;

import org.egovframe.rte.fdl.idgnr.EgovIdGnrService;
import org.egovframe.rte.fdl.idgnr.EgovIdGnrStrategy;
import org.egovframe.rte.fdl.idgnr.impl.EgovTableIdGnrServiceImpl;
import org.egovframe.rte.fdl.idgnr.impl.strategy.EgovIdGnrStrategyImpl;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import javax.sql.DataSource;

/**
 * 표준프레임워크 공통기반 ID 발번(fdl-idgnr) 설정.
 *
 * <p>{@link EgovTableIdGnrServiceImpl} 은 HSQLDB 에 생성된 {@code IDS} 테이블
 * ({@code TABLE_NAME}, {@code NEXT_ID}) 을 블록 단위로 증가시켜 번호를 발급한다.
 * BOARD_ID·ATCFILE_ID 는 VARCHAR(20) 이므로 접두어 4자리 + 숫자 16자리(0 채움) 로 발급한다.</p>
 */
@Configuration
public class EgovIdGnrConfig {

    /** 발번에 사용할 물리 테이블명 */
    private static final String ID_TABLE = "IDS";

    /** 게시물 ID 채번 전략 : BBS-0000000000000001 (20자리) */
    @Bean
    public EgovIdGnrStrategy boardIdGnrStrategy() {
        EgovIdGnrStrategyImpl strategy = new EgovIdGnrStrategyImpl();
        strategy.setPrefix("BBS-");
        strategy.setCipers(16);
        strategy.setFillChar('0');
        return strategy;
    }

    /** 게시물 ID 채번 서비스 */
    @Bean
    public EgovIdGnrService boardIdGnrService(DataSource dataSource, EgovIdGnrStrategy boardIdGnrStrategy) {
        EgovTableIdGnrServiceImpl idGnrService = new EgovTableIdGnrServiceImpl();
        idGnrService.setDataSource(dataSource);
        idGnrService.setTable(ID_TABLE);
        idGnrService.setTableName("BOARD");
        idGnrService.setBlockSize(1);
        idGnrService.setStrategy(boardIdGnrStrategy);
        return idGnrService;
    }

    /** 첨부파일 ID 채번 전략 : ATF-0000000000000001 (20자리) */
    @Bean
    public EgovIdGnrStrategy atcFileIdGnrStrategy() {
        EgovIdGnrStrategyImpl strategy = new EgovIdGnrStrategyImpl();
        strategy.setPrefix("ATF-");
        strategy.setCipers(16);
        strategy.setFillChar('0');
        return strategy;
    }

    /** 첨부파일 ID 채번 서비스 */
    @Bean
    public EgovIdGnrService atcFileIdGnrService(DataSource dataSource, EgovIdGnrStrategy atcFileIdGnrStrategy) {
        EgovTableIdGnrServiceImpl idGnrService = new EgovTableIdGnrServiceImpl();
        idGnrService.setDataSource(dataSource);
        idGnrService.setTable(ID_TABLE);
        idGnrService.setTableName("ATC_FILE");
        idGnrService.setBlockSize(1);
        idGnrService.setStrategy(atcFileIdGnrStrategy);
        return idGnrService;
    }
}
