package egovframework.board.config;

import javax.sql.DataSource;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.jdbc.datasource.embedded.EmbeddedDatabaseBuilder;
import org.springframework.jdbc.datasource.embedded.EmbeddedDatabaseType;

@Configuration
public class EgovConfigDatasource {

    @Bean(name = "dataSource")
    DataSource dataSource() {
        return new EmbeddedDatabaseBuilder()
                .setName("boarddb")
                .setType(EmbeddedDatabaseType.HSQL)
                .setScriptEncoding("UTF-8")
                .addScript("classpath:/db/boarddb.sql")
                .build();
    }
}
