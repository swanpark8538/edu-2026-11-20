package egovframework.example.config;

import javax.sql.DataSource;

import org.egovframe.rte.fdl.idgnr.impl.EgovTableIdGnrServiceImpl;
import org.egovframe.rte.fdl.idgnr.impl.strategy.EgovIdGnrStrategyImpl;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * ID 생성 서비스 설정 - IDS 테이블 기반 (예: BOARD_00000000000001)
 */
@Configuration
public class EgovConfigIdGnr {

    @Bean
    public EgovTableIdGnrServiceImpl boardIdGnrService(DataSource dataSource) {
        return tableIdGnrService(dataSource, "BOARD_ID", "BOARD_", 14);
    }

    @Bean
    public EgovTableIdGnrServiceImpl fileIdGnrService(DataSource dataSource) {
        return tableIdGnrService(dataSource, "FILE_ID", "FILE_", 15);
    }

    private EgovTableIdGnrServiceImpl tableIdGnrService(DataSource dataSource, String tableName, String prefix, int cipers) {
        EgovIdGnrStrategyImpl strategy = new EgovIdGnrStrategyImpl();
        strategy.setPrefix(prefix);
        strategy.setCipers(cipers);
        strategy.setFillChar('0');

        EgovTableIdGnrServiceImpl idGnrService = new EgovTableIdGnrServiceImpl();
        idGnrService.setDataSource(dataSource);
        idGnrService.setStrategy(strategy);
        idGnrService.setBlockSize(10);
        idGnrService.setTable("IDS");
        idGnrService.setTableName(tableName);
        return idGnrService;
    }
}
