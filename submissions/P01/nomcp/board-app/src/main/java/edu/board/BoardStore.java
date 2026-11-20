package edu.board;

import java.util.List;
import java.util.UUID;
import javax.sql.DataSource;
import org.springframework.http.HttpStatus;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

@Repository
class BoardStore {
    record Post(String id, String title, String writer, String content, String fileName) {}
    record Attachment(String name, byte[] body) {}

    private static final String SELECT_POST = """
        SELECT b.BOARD_ID, b.TITLE, b.WRITER, b.CONTENT, a.FILE_NAME
        FROM BOARD b LEFT JOIN BOARD_ATTACHMENT a ON b.BOARD_ID = a.BOARD_ID
        """;

    private final JdbcTemplate jdbc;

    BoardStore(DataSource dataSource) {
        jdbc = new JdbcTemplate(dataSource);
    }

    List<Post> list() {
        return posts(SELECT_POST + " ORDER BY b.CREATED_AT DESC, b.BOARD_ID DESC");
    }

    Post get(String id) {
        return posts(SELECT_POST + " WHERE b.BOARD_ID = ?", id).stream().findFirst()
            .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND));
    }

    private List<Post> posts(String sql, Object... parameters) {
        return jdbc.query(sql, (row, index) -> new Post(row.getString("BOARD_ID"),
            row.getString("TITLE"), row.getString("WRITER"), row.getString("CONTENT"),
            row.getString("FILE_NAME")), parameters);
    }

    Attachment attachment(String id) {
        return jdbc.query("SELECT FILE_NAME, FILE_BODY FROM BOARD_ATTACHMENT WHERE BOARD_ID = ?",
            (row, index) -> new Attachment(row.getString(1), row.getBytes(2)), id)
            .stream().findFirst().orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND));
    }

    @Transactional
    public String save(String id, String title, String writer, String content,
                       Attachment attachment, boolean removeAttachment) {
        if (id == null) {
            id = UUID.randomUUID().toString().replace("-", "").substring(0, 20);
            jdbc.update("INSERT INTO BOARD (BOARD_ID, TITLE, WRITER, CONTENT) VALUES (?, ?, ?, ?)",
                id, title, writer, content);
        } else if (jdbc.update("UPDATE BOARD SET TITLE = ?, WRITER = ?, CONTENT = ? WHERE BOARD_ID = ?",
                title, writer, content, id) == 0) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND);
        }
        if (removeAttachment || attachment != null) {
            jdbc.update("DELETE FROM BOARD_ATTACHMENT WHERE BOARD_ID = ?", id);
        }
        if (attachment != null) {
            jdbc.update("INSERT INTO BOARD_ATTACHMENT (BOARD_ID, FILE_NAME, FILE_BODY) VALUES (?, ?, ?)",
                id, attachment.name(), attachment.body());
        }
        return id;
    }

    void delete(String id) {
        if (jdbc.update("DELETE FROM BOARD WHERE BOARD_ID = ?", id) == 0) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND);
        }
    }
}
