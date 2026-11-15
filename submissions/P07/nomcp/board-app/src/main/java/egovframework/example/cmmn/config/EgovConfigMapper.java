package egovframework.example.cmmn.config;

import javax.sql.DataSource;

import org.apache.ibatis.session.SqlSessionFactory;
import org.mybatis.spring.SqlSessionFactoryBean;
import org.mybatis.spring.SqlSessionTemplate;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.io.support.PathMatchingResourcePatternResolver;

/**
 * 실행환경 Data Access(MyBatis) 설정.
 * EgovAbstractMapper 는 이름이 "sqlSession" 인 SqlSessionFactory 를 주입받는다.
 */
@Configuration
public class EgovConfigMapper {

    @Bean(name = "sqlSession")
    public SqlSessionFactory sqlSession(DataSource dataSource) throws Exception {
        PathMatchingResourcePatternResolver resolver = new PathMatchingResourcePatternResolver();

        SqlSessionFactoryBean factoryBean = new SqlSessionFactoryBean();
        factoryBean.setDataSource(dataSource);
        factoryBean.setConfigLocation(resolver.getResource("classpath:/egovframework/mapper/config/mapper-config.xml"));
        factoryBean.setMapperLocations(resolver.getResources("classpath:/egovframework/mapper/**/*_SQL.xml"));
        return factoryBean.getObject();
    }

    @Bean
    public SqlSessionTemplate sqlSessionTemplate(SqlSessionFactory sqlSession) {
        return new SqlSessionTemplate(sqlSession);
    }

}
