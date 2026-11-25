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

	@Test 
	void createsEmailVerificationTokensTable() {
		Integer columnCount = jdbcTemplate.queryForObject("""
			SELECT COUNT(*)
			FROM information_schema.columns
			WHERE table_name = 'email_verification_tokens'
				AND column_name IN (
					'user_id',
					'token_hash',
					'expires_at',
					'used_at',
					'created_at'
				)
			""", Integer.class);

		assertThat(columnCount).isEqualTo(5);
	}

	@Test 
	void rejectDuplicateTokenHash() {
		Long userId = userRepository.saveAndFlush(
			new User("duplicate-token@example.com", "$2a$12$hash")
		).getId();
		insertToken(userId, "a".repeat(64));

		assertThatThrownBy(() -> insertToken(userId, "a".repeat(64)))
			.isInstanceOf(DataIntegrityViolationException.class);
	}

	@Test 
	void deletesTokensWhenTheirUserIsDeleted() {
		Long userId = userRepository.saveAndFlush(
			new User("cascade@example.com", "$2a$12$hash")
		).getId();

		jdbcTemplate.update("DELETE FROM users WHERE id= ?", userId);

		Integer remainingTokens = jdbcTemplate.queryForObject(
			"SELECT COUNT(*) FROM email_verification_tokens WHERE user_id = ?", 
			Integer.class, userId);

		assertThat(remainingTokens).isZero();
	}

	private void insertToken(Long userId, String tokenHash) {
		jdbcTemplate.update("""
			INSERT INTO email_verification_tokens (user_id, token_hash, expires_at, created_at)
			VALUES (?, ?, now() + interval '15 minutes', now())
			""", userId, tokenHash);
	}

}
