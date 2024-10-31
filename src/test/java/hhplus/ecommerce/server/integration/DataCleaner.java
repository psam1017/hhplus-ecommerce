package hhplus.ecommerce.server.integration;

import jakarta.persistence.EntityManager;
import jakarta.persistence.Table;
import jakarta.persistence.metamodel.EntityType;
import lombok.RequiredArgsConstructor;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

@RequiredArgsConstructor
@Component
public class DataCleaner {

    private final JdbcTemplate jdbcTemplate;
    private final EntityManager entityManager;

    public void cleanAll() {
        List<String> tableNames = entityManager.getMetamodel().getEntities()
                .stream()
                .map(entity -> entity.getJavaType().getAnnotation(Table.class).name())
                .toList();
        tableNames.forEach(t -> jdbcTemplate.execute("TRUNCATE TABLE " + t));
    }
}
