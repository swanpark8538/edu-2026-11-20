package egovframework.example.board;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.Locale;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.MessageSource;

import egovframework.example.board.service.EgovBoardService;

/**
 * 애플리케이션 기동 및 eGovFrame 공통 빈(messageSource / leaveaTrace) 등록 여부 확인.
 */
@SpringBootTest
@AutoConfigureMockMvc
class EgovBoardApplicationTests {

    @Autowired
    private MessageSource messageSource;

    @Autowired
    private EgovBoardService egovBoardService;

    @Test
    void contextLoadsWithEgovFrameBeans() {
        assertThat(egovBoardService).isNotNull();
        assertThat(messageSource).isNotNull();
    }

    @Test
    void 표준메시지번들을해석한다() {
        // 애플리케이션 메시지
        assertThat(messageSource.getMessage("error.board.notFound", new String[] { "1" }, Locale.KOREA))
                .contains("1");
        // eGovFrame FDL-IDGNR 메시지 (채번 서비스가 내부에서 사용)
        assertThat(messageSource.getMessage("debug.idgnr.allocate.idblock",
                new Object[] { "10", "BOARD" }, Locale.KOREA)).isNotBlank();
    }

}
