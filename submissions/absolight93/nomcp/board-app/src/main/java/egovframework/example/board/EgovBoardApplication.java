package egovframework.example.board;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

/**
 * 전자정부 표준프레임워크 5.0 Boot 기반 게시판 애플리케이션 시작 클래스.
 *
 * <p>내장 HSQLDB(classpath:db)로 기동하며, 게시물 등록 시 첨부파일을 함께 저장한다.</p>
 */
@SpringBootApplication
public class EgovBoardApplication {

    public static void main(String[] args) {
        SpringApplication.run(EgovBoardApplication.class, args);
    }

}
