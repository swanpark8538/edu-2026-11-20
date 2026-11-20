package kr.egovlab.board;

import java.time.LocalDateTime;
import java.util.List;

import org.egovframe.rte.fdl.cmmn.EgovAbstractServiceImpl;
import org.springframework.http.HttpStatus;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

@Service
public class BoardService extends EgovAbstractServiceImpl {
    private final JdbcTemplate jdbc;

    public BoardService(JdbcTemplate jdbc) {
        this.jdbc = jdbc;
    }

    public List<Summary> list() {
        return jdbc.query("""
                SELECT BOARD_ID, TITLE, WRITER, CREATED_AT, FILE_NAME
                FROM BOARD ORDER BY CREATED_AT DESC, BOARD_ID DESC
                """, (row, index) -> new Summary(row.getString("BOARD_ID"), row.getString("TITLE"),
                row.getString("WRITER"), row.getObject("CREATED_AT", LocalDateTime.class),
                row.getString("FILE_NAME") != null));
    }

    public Post find(String id) {
        return jdbc.query("""
                SELECT BOARD_ID, TITLE, WRITER, CONTENT, CREATED_AT, FILE_NAME
                FROM BOARD WHERE BOARD_ID = ?
                """, (row, index) -> new Post(row.getString("BOARD_ID"), row.getString("TITLE"),
                row.getString("WRITER"), row.getString("CONTENT"),
                row.getObject("CREATED_AT", LocalDateTime.class), row.getString("FILE_NAME")), id)
                .stream().findFirst().orElseThrow(() -> missing("게시글을 찾을 수 없습니다."));
    }

    public String create(BoardForm form, Attachment attachment) {
        String id = jdbc.queryForObject("VALUES (NEXT VALUE FOR BOARD_SEQ)", String.class);
        jdbc.update("""
                INSERT INTO BOARD (BOARD_ID, TITLE, WRITER, CONTENT, FILE_NAME, FILE_DATA)
                VALUES (?, ?, ?, ?, ?, ?)
                """, id, form.title().strip(), form.writer().strip(), form.content(),
                attachment == null ? null : attachment.name(), attachment == null ? null : attachment.data());
        return id;
    }

    public void update(String id, BoardForm form, Attachment attachment) {
        int updated;
        if (attachment != null || Boolean.TRUE.equals(form.removeAttachment())) {
            updated = jdbc.update("""
                    UPDATE BOARD SET TITLE = ?, WRITER = ?, CONTENT = ?, FILE_NAME = ?, FILE_DATA = ?
                    WHERE BOARD_ID = ?
                    """, form.title().strip(), form.writer().strip(), form.content(),
                    attachment == null ? null : attachment.name(), attachment == null ? null : attachment.data(), id);
        } else {
            updated = jdbc.update("""
                    UPDATE BOARD SET TITLE = ?, WRITER = ?, CONTENT = ? WHERE BOARD_ID = ?
                    """, form.title().strip(), form.writer().strip(), form.content(), id);
        }
        requirePost(updated);
    }

    public Attachment attachment(String id) {
        return jdbc.query("SELECT FILE_NAME, FILE_DATA FROM BOARD WHERE BOARD_ID = ? AND FILE_NAME IS NOT NULL",
                (row, index) -> new Attachment(row.getString("FILE_NAME"), row.getBytes("FILE_DATA")), id)
                .stream().findFirst().orElseThrow(() -> missing("첨부파일을 찾을 수 없습니다."));
    }

    public void delete(String id) {
        requirePost(jdbc.update("DELETE FROM BOARD WHERE BOARD_ID = ?", id));
    }

    private void requirePost(int updated) {
        if (updated == 0) {
            throw missing("게시글을 찾을 수 없습니다.");
        }
    }

    private ResponseStatusException missing(String message) {
        return new ResponseStatusException(HttpStatus.NOT_FOUND, message);
    }

    public record Summary(String id, String title, String writer, LocalDateTime createdAt, boolean hasAttachment) {}

    public record Post(String id, String title, String writer, String content, LocalDateTime createdAt,
                       String attachmentName) {}

    public record Attachment(String name, byte[] data) {}
}
