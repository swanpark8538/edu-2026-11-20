package egovframework.example.cmmn.config;

import javax.sql.DataSource;

import org.egovframe.rte.fdl.idgnr.impl.EgovTableIdGnrServiceImpl;
import org.egovframe.rte.fdl.idgnr.impl.strategy.EgovIdGnrStrategyImpl;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * 실행환경 ID Generation 서비스(테이블 기반) 설정.
 * 접두어 + 0 채움으로 VARCHAR(20) 키를 만든다. 예) BOARD_00000000000001
 */
@Configuration
public class EgovConfigIdGnr {

    @Bean
    public EgovTableIdGnrServiceImpl boardIdGnrService(DataSource dataSource) {
        return tableIdGnr(dataSource, "BOARD", new EgovIdGnrStrategyImpl("BOARD_", 14, '0'));
    }

    @Bean
    public EgovTableIdGnrServiceImpl fileIdGnrService(DataSource dataSource) {
        return tableIdGnr(dataSource, "ATCH_FILE", new EgovIdGnrStrategyImpl("FILE_", 15, '0'));
    }

    private EgovTableIdGnrServiceImpl tableIdGnr(DataSource dataSource, String tableName, EgovIdGnrStrategyImpl strategy) {
        EgovTableIdGnrServiceImpl idGnrService = new EgovTableIdGnrServiceImpl();
        idGnrService.setDataSource(dataSource);
        idGnrService.setStrategy(strategy);
        idGnrService.setBlockSize(10);
        idGnrService.setTable("IDS");
        idGnrService.setTableName(tableName);
        idGnrService.setTableNameFieldName("TABLE_NAME");
        idGnrService.setNextIdFieldName("NEXT_ID");
        return idGnrService;
    }

}
