package hhplus.ecommerce.server;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import hhplus.ecommerce.server.support.PersistenceContextManager;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;

import java.util.function.Supplier;

@AutoConfigureMockMvc
@Transactional
@SpringBootTest
public class ApplicationTests {

	@Autowired
	protected MockMvc mockMvc;

	@Autowired
	private PersistenceContextManager persistenceContextManager;

	@Autowired
	protected ObjectMapper objectMapper;

	protected String createJson(Object body) {
		try {
			return objectMapper.writeValueAsString(body);
		} catch (JsonProcessingException e) {
			throw new RuntimeException(e);
		}
	}

	protected <T> T refreshAnd(Supplier<T> action) {
		return persistenceContextManager.refreshAnd(action);
	}

	protected void refreshAnd(Runnable action) {
		persistenceContextManager.refreshAnd(action);
	}

	protected void refresh() {
		persistenceContextManager.refresh();
	}
}
