package egov.board;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.properties.ConfigurationPropertiesScan;

/**
 * 전자정부 표준프레임워크(eGovFrame) 5.0 Boot 기반 게시판 애플리케이션 시작 클래스.
 *
 * <p>내장 HSQLDB 로 기동하며, 게시물 목록/조회/등록/수정/삭제와 첨부파일 업로드·다운로드를 제공한다.</p>
 */
@SpringBootApplication
@ConfigurationPropertiesScan
public class BoardApplication {

    public static void main(String[] args) {
        SpringApplication.run(BoardApplication.class, args);
    }
}
