package egovframework.example.board;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;

import org.egovframe.rte.fdl.cmmn.exception.EgovBizException;
import org.egovframe.rte.fdl.idgnr.EgovIdGnrService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.web.multipart.MultipartFile;

import egovframework.example.board.service.EgovBoardService;
import egovframework.example.board.service.vo.BoardFileVO;
import egovframework.example.board.service.vo.BoardSearchVO;
import egovframework.example.board.service.vo.BoardVO;

/**
 * 게시판 서비스 계층 통합 테스트 (내장 HSQLDB + 첨부파일 저장)
 */
@SpringBootTest
@AutoConfigureMockMvc
class EgovBoardServiceTests {

    @Autowired
    private EgovBoardService egovBoardService;

    @Autowired
    @Qualifier("egovBoardIdGnrService")
    private EgovIdGnrService egovBoardIdGnrService;

    @Test
    void 첨부포함_등록_조회_삭제된다() throws Exception {
        BoardVO boardVO = new BoardVO();
        boardVO.setTitle("첨부 포함 테스트");
        boardVO.setContent("본문 내용");
        boardVO.setWriter("tester");

        MultipartFile file = new MockMultipartFile("files", "test-file.txt", "text/plain",
                "hello egovframe".getBytes(StandardCharsets.UTF_8));

        String boardId = egovBoardService.insertBoard(boardVO, List.of(file));
        assertThat(boardId).isNotBlank();

        // 1) 상세 조회 + 첨부 확인
        BoardVO found = egovBoardService.selectBoard(boardId);
        assertThat(found.getTitle()).isEqualTo("첨부 포함 테스트");
        assertThat(found.getReadcnt()).isZero();
        assertThat(found.getBoardFileList()).hasSize(1);

        BoardFileVO attached = found.getBoardFileList().get(0);
        assertThat(attached.getFileOrglnm()).isEqualTo("test-file.txt");
        assertThat(attached.getFileExtsn()).isEqualTo("txt");
        assertThat(attached.getFileSize()).isEqualTo("hello egovframe".length());

        // 2) 실제 파일 저장 확인
        Path storedPath = egovBoardService.getStoredFilePath(attached);
        assertThat(Files.exists(storedPath)).isTrue();
        assertThat(Files.readString(storedPath)).isEqualTo("hello egovframe");

        // 3) 상세 조회 시 조회 수 증가
        egovBoardService.readBoard(boardId);
        assertThat(egovBoardService.selectBoard(boardId).getReadcnt()).isEqualTo(1);

        // 4) 삭제 시 DB 레코드와 파일이 함께 정리되는지
        egovBoardService.deleteBoard(boardId);

        assertThat(Files.exists(storedPath)).isFalse();
        assertThatThrownBy(() -> egovBoardService.selectBoard(boardId))
                .isInstanceOf(EgovBizException.class);
    }

    @Test
    void 수정시_첨부를_추가하고_선택한_첨부를_삭제한다() throws Exception {
        BoardVO boardVO = new BoardVO();
        boardVO.setTitle("수정 테스트");
        boardVO.setContent("원본 본문");
        boardVO.setWriter("tester");

        String boardId = egovBoardService.insertBoard(boardVO,
                List.of(new MockMultipartFile("files", "first.txt", "text/plain",
                        "first".getBytes(StandardCharsets.UTF_8))));

        List<BoardFileVO> before = egovBoardService.selectBoardFileList(boardId);
        assertThat(before).hasSize(1);
        Path firstPath = egovBoardService.getStoredFilePath(before.get(0));

        BoardVO updateVO = new BoardVO();
        updateVO.setBoardId(boardId);
        updateVO.setTitle("수정된 제목");
        updateVO.setContent("수정된 본문");

        egovBoardService.updateBoard(updateVO,
                List.of(new MockMultipartFile("files", "second.txt", "text/plain",
                        "second".getBytes(StandardCharsets.UTF_8))),
                List.of(before.get(0).getAtchfileId()));

        List<BoardFileVO> after = egovBoardService.selectBoardFileList(boardId);
        assertThat(after).hasSize(1);
        assertThat(after.get(0).getFileOrglnm()).isEqualTo("second.txt");
        assertThat(Files.exists(firstPath)).isFalse();

        BoardVO updated = egovBoardService.selectBoard(boardId);
        assertThat(updated.getTitle()).isEqualTo("수정된 제목");
        assertThat(updated.getWriter()).isEqualTo("tester"); // 작성자는 유지

        egovBoardService.deleteBoard(boardId);
    }

    @Test
    void 검색조건과_페이징_동작한다() throws Exception {
        for (int i = 1; i <= 3; i++) {
            BoardVO boardVO = new BoardVO();
            boardVO.setTitle("검색테스트 " + i);
            boardVO.setContent("검색용 본문");
            boardVO.setWriter("pagger");
            egovBoardService.insertBoard(boardVO, null);
        }

        BoardSearchVO searchVO = new BoardSearchVO();
        searchVO.setSearchCon("WRITER");
        searchVO.setSearchWd("pagger");
        searchVO.setFirstRecordIndex(0);
        searchVO.setRecordCountPerPage(2);

        assertThat(egovBoardService.countBoardList(searchVO)).isGreaterThanOrEqualTo(3);
        assertThat(egovBoardService.selectBoardList(searchVO)).hasSize(2);

        // 후속 테스트에 영향을 주지 않도록 테스트용으로 등록한 게시물을 모두 정리한다.
        searchVO.setRecordCountPerPage(50);
        for (BoardVO posted : egovBoardService.selectBoardList(searchVO)) {
            if ("pagger".equals(posted.getWriter())) {
                egovBoardService.deleteBoard(posted.getBoardId());
            }
        }
        assertThat(egovBoardService.countBoardList(searchVO)).isZero();
    }

    @Test
    void 채번서비스는_10자리_중복없는_ID를_발급한다() throws Exception {
        String first = egovBoardIdGnrService.getNextStringId();
        String second = egovBoardIdGnrService.getNextStringId();

        assertThat(first).hasSize(10).matches("\\d{10}");
        assertThat(second).isNotEqualTo(first);
    }

}
