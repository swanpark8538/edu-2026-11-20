package egovframework.board.config;

import javax.sql.DataSource;

import org.apache.ibatis.session.SqlSessionFactory;
import org.egovframe.rte.fdl.idgnr.impl.EgovTableIdGnrServiceImpl;
import org.egovframe.rte.fdl.idgnr.impl.strategy.EgovIdGnrStrategyImpl;
import org.mybatis.spring.SqlSessionFactoryBean;
import org.mybatis.spring.annotation.MapperScan;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.support.ReloadableResourceBundleMessageSource;
import org.springframework.core.io.support.PathMatchingResourcePatternResolver;
import org.springframework.jdbc.datasource.embedded.EmbeddedDatabaseBuilder;
import org.springframework.jdbc.datasource.embedded.EmbeddedDatabaseType;

/**
 * 내장 HSQL DB, MyBatis, ID 생성 서비스 설정.
 */
@Configuration
@MapperScan(basePackages = "egovframework.board.service.impl")
public class BoardConfig {

	/** egov ID 생성 서비스가 사용하는 메시지 번들 */
	@Bean
	ReloadableResourceBundleMessageSource messageSource() {
		ReloadableResourceBundleMessageSource ms = new ReloadableResourceBundleMessageSource();
		ms.setBasenames("classpath:/org/egovframe/rte/fdl/idgnr/messages/idgnr");
		ms.setDefaultEncoding("UTF-8");
		return ms;
	}

	@Bean(name = "dataSource")
	DataSource dataSource() {
		return new EmbeddedDatabaseBuilder()
				.setType(EmbeddedDatabaseType.HSQL)
				.setScriptEncoding("UTF-8")
				.addScript("classpath:/db/schema.sql")
				.addScript("classpath:/db/data.sql")
				.build();
	}

	@Bean
	SqlSessionFactory sqlSessionFactory(@Qualifier("dataSource") DataSource dataSource) throws Exception {
		PathMatchingResourcePatternResolver resolver = new PathMatchingResourcePatternResolver();
		SqlSessionFactoryBean factoryBean = new SqlSessionFactoryBean();
		factoryBean.setDataSource(dataSource);
		factoryBean.setConfigLocation(resolver.getResource("classpath:/egovframework/sqlmap/board/sql-mapper-config.xml"));
		factoryBean.setMapperLocations(resolver.getResources("classpath:/egovframework/sqlmap/board/mappers/*.xml"));
		return factoryBean.getObject();
	}

	@Bean
	EgovIdGnrStrategyImpl boardIdStrategy() {
		return strategy("BRD-");
	}

	@Bean
	EgovIdGnrStrategyImpl fileIdStrategy() {
		return strategy("FILE-");
	}

	@Bean(name = "boardIdGnrService", destroyMethod = "destroy")
	EgovTableIdGnrServiceImpl boardIdGnrService(@Qualifier("dataSource") DataSource dataSource) {
		return idGnr(dataSource, boardIdStrategy(), "BOARD");
	}

	@Bean(name = "fileIdGnrService", destroyMethod = "destroy")
	EgovTableIdGnrServiceImpl fileIdGnrService(@Qualifier("dataSource") DataSource dataSource) {
		return idGnr(dataSource, fileIdStrategy(), "BOARD_FILE");
	}

	private EgovIdGnrStrategyImpl strategy(String prefix) {
		EgovIdGnrStrategyImpl strategy = new EgovIdGnrStrategyImpl();
		strategy.setPrefix(prefix);
		strategy.setCipers(8);
		strategy.setFillChar('0');
		return strategy;
	}

	private EgovTableIdGnrServiceImpl idGnr(DataSource dataSource, EgovIdGnrStrategyImpl strategy, String tableName) {
		EgovTableIdGnrServiceImpl service = new EgovTableIdGnrServiceImpl();
		service.setDataSource(dataSource);
		service.setStrategy(strategy);
		service.setBlockSize(1);
		service.setTable("IDS");
		service.setTableName(tableName);
		return service;
	}
}
