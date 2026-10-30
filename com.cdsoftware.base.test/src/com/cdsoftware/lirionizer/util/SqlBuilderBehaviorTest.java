package com.cdsoftware.lirionizer.util;

import static org.assertj.core.api.Assertions.assertThat;

import java.io.IOException;

import org.junit.jupiter.api.Test;

class SqlBuilderBehaviorTest {

	@Test
	void statementsAreTrimmedAndJoinedWithOneSpace() {
		String sql = SqlBuilder.builder()
				.statement("  SELECT *  ")
				.statement(" FROM C_Order ")
				.build();

		assertThat(sql).isEqualTo("SELECT * FROM C_Order");
	}

	@Test
	void commentStatementsAreIgnored() {
		String sql = SqlBuilder.builder()
				.statement("-- explanation")
				.statement("SELECT 1")
				.build();

		assertThat(sql).isEqualTo("SELECT 1");
	}

	@Test
	void builderIsImmutable() {
		SqlBuilder original = SqlBuilder.builder().statement("SELECT 1");
		SqlBuilder extended = original.statement("FROM DUAL");

		assertThat(original.build()).isEqualTo("SELECT 1");
		assertThat(extended.build()).isEqualTo("SELECT 1 FROM DUAL");
	}

	@Test
	void fileAndStatementCanBeComposed() throws IOException {
		String sql = SqlBuilder.builder(SqlBuilderBehaviorTest.class)
				.file("resources/sql/read-bpartner.sql")
				.statement("ORDER BY name")
				.build();

		assertThat(sql).contains("select name from c_bpartner").endsWith("ORDER BY name");
	}
}
