package egovframework.board.config;

import javax.sql.DataSource;

import org.egovframe.rte.fdl.idgnr.impl.EgovTableIdGnrServiceImpl;
import org.egovframe.rte.fdl.idgnr.impl.strategy.EgovIdGnrStrategyImpl;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class EgovConfigIdGeneration {

    @Bean
    EgovIdGnrStrategyImpl boardIdStrategy() {
        EgovIdGnrStrategyImpl strategy = new EgovIdGnrStrategyImpl();
        strategy.setPrefix("BOARD-");
        strategy.setCipers(5);
        strategy.setFillChar('0');
        return strategy;
    }

    @Bean
    EgovIdGnrStrategyImpl fileIdStrategy() {
        EgovIdGnrStrategyImpl strategy = new EgovIdGnrStrategyImpl();
        strategy.setPrefix("FILE-");
        strategy.setCipers(5);
        strategy.setFillChar('0');
        return strategy;
    }

    @Bean(name = "boardIdGnrService", destroyMethod = "destroy")
    EgovTableIdGnrServiceImpl boardIdGnrService(@Qualifier("dataSource") DataSource dataSource) {
        EgovTableIdGnrServiceImpl idGnrService = new EgovTableIdGnrServiceImpl();
        idGnrService.setDataSource(dataSource);
        idGnrService.setStrategy(boardIdStrategy());
        idGnrService.setBlockSize(10);
        idGnrService.setTable("IDS");
        idGnrService.setTableName("BOARD");
        return idGnrService;
    }

    @Bean(name = "fileIdGnrService", destroyMethod = "destroy")
    EgovTableIdGnrServiceImpl fileIdGnrService(@Qualifier("dataSource") DataSource dataSource) {
        EgovTableIdGnrServiceImpl idGnrService = new EgovTableIdGnrServiceImpl();
        idGnrService.setDataSource(dataSource);
        idGnrService.setStrategy(fileIdStrategy());
        idGnrService.setBlockSize(10);
        idGnrService.setTable("IDS");
        idGnrService.setTableName("FILE");
        return idGnrService;
    }
}
