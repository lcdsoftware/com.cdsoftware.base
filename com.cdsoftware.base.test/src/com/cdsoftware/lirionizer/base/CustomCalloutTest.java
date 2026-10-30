package com.cdsoftware.lirionizer.base;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.Properties;

import org.compiere.model.GridField;
import org.compiere.model.GridTab;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

class CustomCalloutTest {

	private Properties context;
	private GridTab tab;
	private GridField field;
	private TestCallout callout;

	@BeforeEach
	void setUp() {
		context = new Properties();
		tab = mock(GridTab.class);
		field = mock(GridField.class);
		callout = new TestCallout();
		when(tab.getTableName()).thenReturn("C_Order");
		when(field.getColumnName()).thenReturn("Description");
	}

	@Test
	void startCapturesInvocationContextBeforeDelegating() {
		Object value = new Object();
		Object oldValue = new Object();

		String result = callout.start(context, 7, tab, field, value, oldValue);

		assertThat(result).isEqualTo("executed");
		assertThat(callout.executed).isTrue();
		assertThat(callout.getCtx()).isSameAs(context);
		assertThat(callout.getWindowNo()).isEqualTo(7);
		assertThat(callout.getTab()).isSameAs(tab);
		assertThat(callout.getField()).isSameAs(field);
		assertThat(callout.getValue()).isSameAs(value);
		assertThat(callout.getOldValue()).isSameAs(oldValue);
		assertThat(callout.getTableName()).isEqualTo("C_Order");
		assertThat(callout.getColumnName()).isEqualTo("Description");
	}

	@Test
	void setValueUsesCurrentColumn() {
		when(tab.setValue("Description", "new value")).thenReturn("");
		callout.start(context, 7, tab, field, null, null);

		assertThat(callout.setValue("new value")).isEmpty();
		verify(tab).setValue("Description", "new value");
	}

	@Test
	void setValueUsesExplicitColumn() {
		when(tab.setValue("DocumentNo", "10001")).thenReturn("warning");
		callout.start(context, 7, tab, field, null, null);

		assertThat(callout.setValue("DocumentNo", "10001")).isEqualTo("warning");
		verify(tab).setValue("DocumentNo", "10001");
	}

	private static class TestCallout extends CustomCallout {
		private boolean executed;

		@Override
		protected String start() {
			executed = true;
			return "executed";
		}
	}
}
