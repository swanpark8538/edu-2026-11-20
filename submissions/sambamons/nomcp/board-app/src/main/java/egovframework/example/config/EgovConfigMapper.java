package egovframework.example.config;

import javax.sql.DataSource;

import org.apache.ibatis.session.SqlSessionFactory;
import org.egovframe.rte.psl.dataaccess.mapper.EgovMapper;
import org.egovframe.rte.psl.dataaccess.mapper.MapperConfigurer;
import org.mybatis.spring.SqlSessionFactoryBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.io.support.PathMatchingResourcePatternResolver;

/**
 * MyBatis 설정 - @EgovMapper 가 붙은 인터페이스를 Mapper 로 등록한다.
 */
@Configuration
public class EgovConfigMapper {

    @Bean
    public SqlSessionFactory sqlSession(DataSource dataSource) throws Exception {
        org.apache.ibatis.session.Configuration configuration = new org.apache.ibatis.session.Configuration();
        configuration.setMapUnderscoreToCamelCase(true);
        configuration.setJdbcTypeForNull(org.apache.ibatis.type.JdbcType.NULL);

        SqlSessionFactoryBean factory = new SqlSessionFactoryBean();
        factory.setDataSource(dataSource);
        factory.setConfiguration(configuration);
        factory.setMapperLocations(new PathMatchingResourcePatternResolver().getResources("classpath:/mapper/**/*.xml"));
        return factory.getObject();
    }

    @Bean
    public static MapperConfigurer mapperConfigurer() {
        MapperConfigurer configurer = new MapperConfigurer();
        configurer.setBasePackage("egovframework.example");
        configurer.setAnnotationClass(EgovMapper.class);
        configurer.setSqlSessionFactoryBeanName("sqlSession");
        return configurer;
    }
}
