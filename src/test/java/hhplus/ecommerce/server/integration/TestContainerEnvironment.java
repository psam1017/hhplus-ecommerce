package hhplus.ecommerce.server.integration;

import com.redis.testcontainers.RedisContainer;
import org.junit.jupiter.api.AfterEach;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.event.ApplicationEvents;
import org.springframework.test.context.event.RecordApplicationEvents;
import org.springframework.test.web.servlet.MockMvc;
import org.testcontainers.containers.MySQLContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.utility.DockerImageName;

@SuppressWarnings("SpringJavaInjectionPointsAutowiringInspection")
@RecordApplicationEvents
@ActiveProfiles("test")
@AutoConfigureMockMvc
@SpringBootTest
public abstract class TestContainerEnvironment {

    @Autowired
    protected MockMvc mockMvc;

    @Autowired
    private DataCleaner dataCleaner;

    @Autowired
    private ApplicationEvents applicationEvents;

    @AfterEach
    void tearDown() {
        dataCleaner.cleanAll();
        applicationEvents.clear();
    }

    // MySQL 시작
    @Container
    public static final String MY_SQL_FULL_IMAGE_NAME = "mysql:8.0.36";
    private static final int MY_SQL_PORT = 3306;
    private static final MySQLContainer<?> MY_SQL_CONTAINER;

    static {
        try (MySQLContainer<?> mySQLContainer = new MySQLContainer<>(DockerImageName.parse(MY_SQL_FULL_IMAGE_NAME))) {
            MY_SQL_CONTAINER = mySQLContainer
                    .withExposedPorts(MY_SQL_PORT)
                    .withEnv("MYSQL_ROOT_PASSWORD", "password")
                    .withReuse(true);
            MY_SQL_CONTAINER.start();
        }
    }
    // MySQL 끝

    // Redis 시작
    private static final String REDIS_FULL_IMAGE_NAME = "redis:7.4.1-alpine";
    private static final int REDIS_PORT = 6379;
    @Container
    public static final RedisContainer REDIS_CONTAINER;

    static {
        REDIS_CONTAINER = new RedisContainer(DockerImageName.parse(REDIS_FULL_IMAGE_NAME))
                .withExposedPorts(REDIS_PORT)
                .withReuse(true);
        REDIS_CONTAINER.start();
    }
    // Redis 끝
}
