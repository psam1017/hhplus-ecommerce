package hhplus.ecommerce.server.integration;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.testcontainers.containers.MySQLContainer;
import org.testcontainers.utility.DockerImageName;

@ActiveProfiles("test")
@AutoConfigureMockMvc
@SpringBootTest
public abstract class TestContainerEnvironment {

    @Autowired
    protected MockMvc mockMvc;

    private static final String FULL_IMAGE_NAME = "mysql:8.0.36";

    private static final MySQLContainer<?> MY_SQL_CONTAINER;

    static {
        try (MySQLContainer<?> mySQLContainer = new MySQLContainer<>(DockerImageName.parse(FULL_IMAGE_NAME))) {
            MY_SQL_CONTAINER = mySQLContainer.withReuse(true);
            MY_SQL_CONTAINER.start();
        }
    }
}
