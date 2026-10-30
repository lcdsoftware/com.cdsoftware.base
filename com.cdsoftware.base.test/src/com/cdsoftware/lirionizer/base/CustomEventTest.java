package com.cdsoftware.lirionizer.base;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.util.Dictionary;
import java.util.Hashtable;

import org.compiere.model.PO;
import org.compiere.process.ProcessInfo;
import org.junit.jupiter.api.Test;
import org.osgi.service.event.Event;

class CustomEventTest {

	@Test
	void poEventExposesPersistentObjectAndTopic() {
		PO po = mock(PO.class);
		Event event = event("po/topic");
		TestEvent handler = new TestEvent();

		handler.handle(po, event);

		assertThat(handler.executions).isEqualTo(1);
		assertThat(handler.po()).isSameAs(po);
		assertThat(handler.event()).isSameAs(event);
		assertThat(handler.eventType()).isEqualTo("po/topic");
	}

	@Test
	void processEventExposesProcessInfoAndTopic() {
		ProcessInfo processInfo = mock(ProcessInfo.class);
		Event event = event("process/topic");
		TestEvent handler = new TestEvent();

		handler.handle(processInfo, event);

		assertThat(handler.executions).isEqualTo(1);
		assertThat(handler.processInfo()).isSameAs(processInfo);
		assertThat(handler.eventType()).isEqualTo("process/topic");
	}

	@Test
	void genericEventExecutesWithoutDomainObject() {
		Event event = event("generic/topic");
		TestEvent handler = new TestEvent();

		handler.handle(event);

		assertThat(handler.executions).isEqualTo(1);
		assertThat(handler.event()).isSameAs(event);
		assertThat(handler.eventType()).isEqualTo("generic/topic");
	}

	@Test
	void eventTypeIsNullBeforeAnEventIsHandled() {
		assertThat(new TestEvent().eventType()).isNull();
	}

	private static Event event(String topic) {
		Dictionary<String, Object> properties = new Hashtable<String, Object>();
		return new Event(topic, properties);
	}

	private static class TestEvent extends CustomEvent {
		private int executions;

		void handle(PO po, Event event) {
			doHandleEvent(po, event);
		}

		void handle(ProcessInfo processInfo, Event event) {
			doHandleEvent(processInfo, event);
		}

		void handle(Event event) {
			doHandleEvent(event);
		}

		PO po() {
			return getPO();
		}

		ProcessInfo processInfo() {
			return getProcessInfo();
		}

		Event event() {
			return getEvent();
		}

		String eventType() {
			return getEventType();
		}

		@Override
		protected void doHandleEvent() {
			executions++;
		}
	}
}
