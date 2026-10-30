/**
 * This program is free software; you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation; either version 2 of the License, or
 * (at your option) any later version.
 */
package com.cdsoftware.lirionizer.util;

import static com.cdsoftware.lirionizer.test.util.ReflectionTestUtil.getFieldValue;
import static com.cdsoftware.lirionizer.test.util.ReflectionTestUtil.setFieldValue;
import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.ArgumentMatchers.matches;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;

import java.lang.reflect.Method;
import java.time.LocalDate;
import java.time.ZonedDateTime;
import java.util.Arrays;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.UUID;
import java.util.function.Consumer;
import java.util.logging.Level;

import org.compiere.util.CLogger;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

class KeyValueLoggerTest {

	private KeyValueLogger keyValueLogger;
	private CLogger logger;

	@BeforeEach
	void setUp() throws ReflectiveOperationException {
		keyValueLogger = KeyValueLogger.instance(KeyValueLoggerTest.class);
		logger = mock(CLogger.class);
		setFieldValue(keyValueLogger, "logger", logger);
	}

	@Test
	void createsInstancesFromClassAndLogger() throws ReflectiveOperationException {
		assertThat(KeyValueLogger.logger(KeyValueLoggerTest.class)).isNotNull();
		assertThat(getFieldValue(KeyValueLogger.instance(KeyValueLoggerTest.class), "logger"))
				.isInstanceOf(CLogger.class);
		assertThat(getFieldValue(KeyValueLogger.instance(logger), "logger")).isSameAs(logger);
		assertThat(getFieldValue(KeyValueLogger.instance(logger), "pairs")).isNotNull();
	}

	@Test
	void dispatchesEverySupportedLogLevel() {
		Map<Level, Consumer<KeyValueLogger>> operations = new LinkedHashMap<>();
		operations.put(Level.INFO, KeyValueLogger::info);
		operations.put(Level.FINE, KeyValueLogger::fine);
		operations.put(Level.FINER, KeyValueLogger::finer);
		operations.put(Level.FINEST, KeyValueLogger::finest);
		operations.put(Level.CONFIG, KeyValueLogger::config);
		operations.put(Level.WARNING, KeyValueLogger::warning);
		operations.put(Level.SEVERE, KeyValueLogger::severe);
		operations.put(Level.ALL, KeyValueLogger::all);

		operations.forEach((level, operation) -> {
			operation.accept(keyValueLogger);
			verify(logger).log(level, "level=\"" + level + "\"");
		});
	}

	@Test
	void addingPairsIsImmutableAndKeepsInsertionOrder() {
		KeyValueLogger first = keyValueLogger.add("first", "one");
		KeyValueLogger second = first.add("second", "two");

		assertThat(first).isNotSameAs(keyValueLogger);
		assertThat(second).isNotSameAs(first);
		second.info();

		verifyInfo("first=\"one\" second=\"two\"");
	}

	@Test
	void normalizesKeysAndValuesAndSkipsEmptyKeys() {
		keyValueLogger
				.add(" Me's$ sag e 1*\n# ", " \"hello\"\n'world' ")
				.add("", "ignored")
				.add(null, null)
				.info();

		verifyInfo("Message1=\"hello world\" null=\"null\"");
	}

	@Test
	void formatsScalarsObjectsAndCustomPatterns() {
		keyValueLogger
				.add("int", 12)
				.add("float", 1.5f)
				.add("double", 2.75d)
				.add("object", new Dummy())
				.add("formatted", "%s-%s", "left", 9)
				.add("default", null, "value")
				.info();

		verifyInfo("int=\"12\" float=\"1.5\" double=\"2.75\" object=\"To String Dummy\" "
				+ "formatted=\"left-9\" default=\"value\"");
	}

	@Test
	void convenienceMethodsUseTheirDocumentedKeys() {
		keyValueLogger
				.message("hello")
				.endpoint("/api")
				.service("billing")
				.name("worker")
				.duration(2.5d)
				.duration(10L)
				.status("ready")
				.action("run")
				.environment("test")
				.code("A1")
				.code(200)
				.type("job")
				.value("42")
				.language("es")
				.httpMethod("GET")
				.httpStatus("201")
				.httpStatus(202)
				.fail()
				.success()
				.info();

		verifyInfo("message=\"hello\" endpoint=\"/api\" service=\"billing\" name=\"worker\" "
				+ "duration=\"2.5\" duration=\"10\" status=\"ready\" action=\"run\" environment=\"test\" "
				+ "code=\"A1\" code=\"200\" type=\"job\" value=\"42\" language=\"es\" httpMethod=\"GET\" "
				+ "httpStatus=\"201\" httpStatus=\"202\" status=\"fail\" status=\"success\"");
	}

	@Test
	void javaMetadataMethodsHandleValuesAndNulls() throws NoSuchMethodException {
		Method method = Dummy.class.getDeclaredMethod("toString");
		keyValueLogger
				.javaMethod("start")
				.javaMethod(method)
				.javaMethod((Method) null)
				.javaClass("example.Type")
				.javaClass(KeyValueLogger.class)
				.javaClass((Class<?>) null)
				.javaPackage("example")
				.javaPackage(KeyValueLogger.class)
				.javaPackage(KeyValueLogger.class.getPackage())
				.javaPackage((Package) null)
				.info();

		verifyInfo("method=\"start\" method=\"toString\" method=\"null\" class=\"example.Type\" "
				+ "class=\"com.cdsoftware.lirionizer.util.KeyValueLogger\" class=\"null\" package=\"example\" "
				+ "package=\"com.cdsoftware.lirionizer.util\" package=\"com.cdsoftware.lirionizer.util\" package=\"null\"");
	}

	@Test
	void identifierMethodsSupportUuidAndStringValues() {
		UUID id = UUID.fromString("123e4567-e89b-42d3-a456-556642440000");
		keyValueLogger
				.track(id).track("track")
				.request(id).request("request")
				.id(id).id("id")
				.transaction(id).transaction("transaction")
				.session(id).session("session")
				.info();

		String uuid = id.toString();
		verifyInfo("track=\"" + uuid + "\" track=\"track\" request=\"" + uuid + "\" request=\"request\" "
				+ "id=\"" + uuid + "\" id=\"id\" transaction=\"" + uuid + "\" transaction=\"transaction\" "
				+ "session=\"" + uuid + "\" session=\"session\"");
	}

	@Test
	void formatsCollectionsObjectArraysAndPrimitiveArrays() {
		keyValueLogger
				.add("list", Arrays.asList("a", "b"))
				.add("objects", (Object) new String[] { "a", null })
				.add("ints", new int[] { 1, 2 })
				.add("doubles", new double[] { 1.5d, 2.5d })
				.add("longs", new long[] { 3L, 4L })
				.add("booleans", new boolean[] { true, false })
				.add("bytes", new byte[] { 5, 6 })
				.add("shorts", new short[] { 7, 8 })
				.add("floats", new float[] { 1.25f, 2.25f })
				.add("chars", new char[] { 'x', 'y' })
				.arguments(new Object[] { "one", 2 })
				.info();

		verifyInfo("list=\"[a, b]\" objects=\"[a, null]\" ints=\"[1, 2]\" doubles=\"[1.5, 2.5]\" "
				+ "longs=\"[3, 4]\" booleans=\"[true, false]\" bytes=\"[5, 6]\" shorts=\"[7, 8]\" "
				+ "floats=\"[1.25, 2.25]\" chars=\"[x, y]\" arguments=\"[one, 2]\"");
	}

	@Test
	void calendarMethodsUseCurrentDate() {
		LocalDate today = LocalDate.now();
		keyValueLogger.day().dayName().month().monthName().year().date().info();

		verifyInfo("day=\"" + today.getDayOfMonth() + "\" day=\"" + today.getDayOfWeek()
				+ "\" month=\"" + today.getMonthValue() + "\" month=\"" + today.getMonth()
				+ "\" year=\"" + today.getYear() + "\" date=\"" + today + "\"");
	}

	@Test
	void timeMethodsUseRequestedFormatsAndKeys() {
		keyValueLogger.time().dateTime().dateTime("yyyy-MM-dd").dateTime("custom", "HH:mm")
				.time("HH:mm:ss.SSS").date("yyyy-MM-dd").timeZone().timeZoneName().info();

		String zone = ZonedDateTime.now().getZone().toString().replace("/", "\\/");
		verify(logger).log(eq(Level.INFO), matches(
				"time=\"\\d{2}:\\d{2}:\\d{2}\\.\\d{3}\" "
				+ "dateTime=\"\\d{4}-\\d{2}-\\d{2} \\d{2}:\\d{2}:\\d{2}\\.\\d{3} [-+]\\d{4}\" "
				+ "dateTime=\"\\d{4}-\\d{2}-\\d{2}\" custom=\"\\d{2}:\\d{2}\" "
				+ "time=\"\\d{2}:\\d{2}:\\d{2}\\.\\d{3}\" date=\"\\d{4}-\\d{2}-\\d{2}\" "
				+ "timeZone=\"[-+]\\d{4}\" timeZone=\"" + zone + "\" level=\"INFO\""));
	}

	@Test
	void messageAndExceptionOverloadsFormatExternalValues() {
		keyValueLogger
				.message("%s-%s", "left", 9)
				.exception("failed")
				.exception(new RuntimeException("broken"))
				.info();

		verifyInfo("message=\"left-9\" exception=\"failed\" exception=\"java.lang.RuntimeException: broken\"");
	}

	@Test
	void stackTraceOverloadsPassThrowableToUnderlyingLogger() {
		RuntimeException exception = new RuntimeException("broken");

		keyValueLogger.exceptionWithStackTrace("custom", exception).info();

		verify(logger).log(Level.INFO, "exception=\"custom\" level=\"INFO\"", exception);
		verify(logger, never()).log(eq(Level.INFO), anyString());
	}

	private void verifyInfo(String pairs) {
		String prefix = pairs.isEmpty() ? "" : pairs + " ";
		verify(logger).log(Level.INFO, prefix + "level=\"INFO\"");
	}

	private static class Dummy {
		@Override
		public String toString() {
			return "To String Dummy";
		}
	}
}
