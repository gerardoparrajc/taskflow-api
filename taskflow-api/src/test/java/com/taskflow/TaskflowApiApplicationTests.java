package com.taskflow;

import org.junit.jupiter.api.Test;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.testcontainers.service.connection.ServiceConnection;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.jdbc.core.JdbcTemplate;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;
import org.testcontainers.postgresql.PostgreSQLContainer;

import com.taskflow.user.User;
import com.taskflow.user.UserRepository;

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

	@Autowired 
	UserRepository userRepository;

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
	
	@Test 
	void rejectsDuplicateEmail() {
		userRepository.saveAndFlush(
			new User("person@example.com", "$2a$12$hash")
		);

		assertThatThrownBy(() ->
			userRepository.saveAndFlush(
				new User("person@example.com", "$2a$12$hash")
			)
		).isInstanceOf(DataIntegrityViolationException.class);
	}

	@Test 
	void createsUsersTable() {
		Integer columnCount = jdbcTemplate.queryForObject("""
			SELECT COUNT(*)
			FROM information_schema.columns
			WHERE table_name = 'users'
				AND column_name IN (
					'email',
					'password_hash',
					'email_verified',
					'credential_version'
				)
			""", Integer.class);

		assertThat(columnCount).isEqualTo(4);
	}

}
