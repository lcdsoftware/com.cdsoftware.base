package com.cdsoftware.lirionizer.base;

import static org.assertj.core.api.Assertions.assertThat;

import org.compiere.process.ProcessInfoParameter;
import org.junit.jupiter.api.Test;

class CustomProcessTest {

	@Test
	void returnsMatchingParameterValue() {
		Object expected = new Object();
		TestProcess process = new TestProcess(parameter("Target", expected));

		assertThat(process.parameter("Target")).isSameAs(expected);
	}

	@Test
	void returnsNullForUnknownOrNullParameterName() {
		TestProcess process = new TestProcess(parameter(null, "ignored"), parameter("Known", "value"));

		assertThat(process.parameter("Missing")).isNull();
		assertThat(process.parameter(null)).isNull();
	}

	@Test
	void returnsFirstMatchingParameter() {
		TestProcess process = new TestProcess(parameter("Repeated", "first"), parameter("Repeated", "second"));

		assertThat(process.parameter("Repeated")).isEqualTo("first");
	}

	private static ProcessInfoParameter parameter(String name, Object value) {
		return new ProcessInfoParameter(name, value, null, null, null);
	}

	private static class TestProcess extends CustomProcess {
		private final ProcessInfoParameter[] parameters;

		TestProcess(ProcessInfoParameter... parameters) {
			this.parameters = parameters;
		}

		Object parameter(String name) {
			return getParameter(name);
		}

		@Override
		protected ProcessInfoParameter[] getParameter() {
			return parameters;
		}

		@Override
		protected void prepare() {
		}

		@Override
		protected String doIt() {
			return null;
		}
	}
}
