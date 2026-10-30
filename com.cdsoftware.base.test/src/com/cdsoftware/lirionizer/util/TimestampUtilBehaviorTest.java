package com.cdsoftware.lirionizer.util;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import java.sql.Timestamp;
import java.time.LocalDate;
import java.time.format.DateTimeParseException;

import org.junit.jupiter.api.Test;

class TimestampUtilBehaviorTest {

	@Test
	void tomorrowAndYesterdayAreAtStartOfDay() {
		assertThat(TimestampUtil.tomorrow().toLocalDateTime()).isEqualTo(LocalDate.now().plusDays(1).atStartOfDay());
		assertThat(TimestampUtil.yesterday().toLocalDateTime()).isEqualTo(LocalDate.now().minusDays(1).atStartOfDay());
	}

	@Test
	void nullTimestampIsNotToday() {
		assertThat(TimestampUtil.isToday(null)).isFalse();
	}

	@Test
	void parsingDateOnlyStartsAtMidnight() {
		Timestamp parsed = TimestampUtil.parse("2024-02-29", "yyyy-MM-dd");

		assertThat(parsed.toLocalDateTime()).isEqualTo(LocalDate.of(2024, 2, 29).atStartOfDay());
	}

	@Test
	void malformedDateIsRejected() {
		assertThatThrownBy(() -> TimestampUtil.parse("not-a-date", "yyyy-MM-dd"))
				.isInstanceOf(DateTimeParseException.class);
	}
}
