package hhplus.ecommerce.server;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import hhplus.ecommerce.server.spring.component.PersistenceContextManager;
import hhplus.ecommerce.server.spring.config.RestDocsConfig;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Import;
import org.springframework.restdocs.RestDocumentationExtension;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.transaction.annotation.Transactional;

import java.util.function.Supplier;

@ExtendWith(RestDocumentationExtension.class)
@Import(RestDocsConfig.class)
@AutoConfigureMockMvc
@ActiveProfiles("test")
@Transactional
@SpringBootTest
public class ApplicationTests {

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
