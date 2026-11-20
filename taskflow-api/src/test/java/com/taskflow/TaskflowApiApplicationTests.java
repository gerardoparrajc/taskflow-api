package com.taskflow;

import org.junit.jupiter.api.Test;
import static org.assertj.core.api.Assertions.assertThat;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.testcontainers.service.connection.ServiceConnection;
import org.springframework.jdbc.core.JdbcTemplate;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;
import org.testcontainers.postgresql.PostgreSQLContainer;

@SpringBootTest
@Testcontainers 
class TaskflowApiApplicationTests {

	@Container 
	@ServiceConnection 
	static final PostgreSQLContainer postgres = new PostgreSQLContainer("postgres:18");

	private final JdbcTemplate jdbcTemplate;

	@Autowired 
	TaskflowApiApplicationTests(JdbcTemplate jdbcTemplate) {
		this.jdbcTemplate = jdbcTemplate;
	}

	@Test 
	void appliesLiquibaseBaselineOnStartup() {
		Integer appliedChangesets = jdbcTemplate.queryForObject("""
			SELECT COUNT(*)
			FROM databasechangelog
			WHERE id = ?
				AND author = ?
				AND tag = ?
				AND exectype = 'EXECUTED'
			""", Integer.class, "001-baseline", "taskflow", "taskflow-baseline");

		assertThat(appliedChangesets).isEqualTo(1);
	}

}
