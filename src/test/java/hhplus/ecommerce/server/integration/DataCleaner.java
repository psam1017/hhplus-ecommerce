package hhplus.ecommerce.server.integration;

import jakarta.persistence.EntityManager;
import jakarta.persistence.Table;
import lombok.RequiredArgsConstructor;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Objects;
import java.util.Set;

@RequiredArgsConstructor
@Component
public class DataCleaner {

    private final JdbcTemplate jdbcTemplate;
    private final EntityManager entityManager;
    private final RedisTemplate<String, Object> redisTemplate;

    public void cleanAll() {
        List<String> tableNames = entityManager.getMetamodel().getEntities()
                .stream()
                .map(entity -> entity.getJavaType().getAnnotation(Table.class).name())
                .toList();
        tableNames.forEach(t -> jdbcTemplate.execute("TRUNCATE TABLE " + t));
        Set<String> keys = Objects.requireNonNull(redisTemplate.keys("*"));
        redisTemplate.delete(keys);
    }
}
