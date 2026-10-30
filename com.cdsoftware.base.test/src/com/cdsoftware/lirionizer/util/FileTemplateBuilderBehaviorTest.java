package com.cdsoftware.lirionizer.util;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Collections;

import org.adempiere.exceptions.AdempiereException;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

class FileTemplateBuilderBehaviorTest {

	@TempDir
	Path temporaryDirectory;

	@Test
	void buildWithoutTemplateReturnsEmptyText() {
		assertThat(FileTemplateBuilder.builder().build()).isEmpty();
	}

	@Test
	void missingTemplateIsReportedAsAdempiereException() {
		assertThatThrownBy(() -> FileTemplateBuilder.builder().file("missing-template.ftl"))
				.isInstanceOf(AdempiereException.class);
	}

	@Test
	void exportWritesTheSameRenderedContentReturnedByBuild() throws Exception {
		Invoice invoice = new Invoice("INV-001");
		FileTemplateBuilder builder = FileTemplateBuilder.builder(FileTemplateBuilderBehaviorTest.class)
				.file("resources/xml/xml-invoice.xml")
				.inject("invoice", invoice);
		Path output = temporaryDirectory.resolve("invoice.xml");

		String rendered = builder.export(output.toString());

		assertThat(output).hasContent(rendered);
		assertThat(new String(Files.readAllBytes(output), StandardCharsets.UTF_8)).contains("<id>INV-001</id>");
	}

	public static class Invoice {
		private final String id;

		Invoice(String id) {
			this.id = id;
		}

		public String getId() {
			return id;
		}

		public String getName() {
			return "Example";
		}

		public java.util.List<Object> getInvoiceLines() {
			return Collections.emptyList();
		}
	}
}
