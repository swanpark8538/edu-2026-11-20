package egovframework.board;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.multipart;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.model;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.nio.charset.StandardCharsets;
import java.nio.file.Path;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;

@SpringBootTest
@AutoConfigureMockMvc
class BoardAppTest {

	@TempDir
	static Path uploadDir;

	@DynamicPropertySource
	static void props(DynamicPropertyRegistry registry) {
		registry.add("board.upload-dir", () -> uploadDir.toString());
	}

	@Autowired
	MockMvc mvc;

	@Test
	void createViewDownloadAndDelete() throws Exception {
		MockMultipartFile file = new MockMultipartFile("files", "한글 첨부.txt", "text/plain",
				"hello attachment".getBytes(StandardCharsets.UTF_8));

		MvcResult created = mvc.perform(multipart("/board").file(file)
				.param("title", "첫 글").param("writer", "홍길동").param("content", "본문"))
				.andExpect(status().is3xxRedirection()).andReturn();
		String location = created.getResponse().getRedirectedUrl();
		assertEquals("/board/BRD-00000001", location);

		mvc.perform(get(location)).andExpect(status().isOk())
				.andExpect(content().string(org.hamcrest.Matchers.containsString("한글 첨부.txt")));

		mvc.perform(get("/board/file/FILE-00000001")).andExpect(status().isOk())
				.andExpect(content().string("hello attachment"));

		mvc.perform(get("/board/list").param("keyword", "첫")).andExpect(status().isOk())
				.andExpect(model().attribute("totalCount", 1));

		mvc.perform(post("/board/BRD-00000001/delete")).andExpect(status().is3xxRedirection());
		mvc.perform(get("/board/file/FILE-00000001")).andExpect(status().isNotFound());
	}

	@Test
	void validationFailureReturnsForm() throws Exception {
		mvc.perform(multipart("/board").param("title", "").param("writer", ""))
				.andExpect(status().isOk())
				.andExpect(model().attributeHasFieldErrors("board", "title", "writer"));
	}
}
