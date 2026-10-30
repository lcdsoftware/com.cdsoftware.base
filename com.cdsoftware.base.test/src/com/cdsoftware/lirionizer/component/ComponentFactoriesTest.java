package com.cdsoftware.lirionizer.component;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.Test;

class ComponentFactoriesTest {

	@Test
	void calloutFactoryScansModelAndCalloutPackages() {
		assertThat(new CalloutFactory().getPackages())
				.containsExactly("com.cdsoftware.lirionizer.model", "com.cdsoftware.lirionizer.callout");
	}

	@Test
	void eventFactoryScansEventPackage() {
		assertThat(new EventFactory().getPackages()).containsExactly("com.cdsoftware.lirionizer.event");
	}

	@Test
	void formFactoryScansFormPackage() {
		assertThat(new FormFactory().getPackages()).containsExactly("com.cdsoftware.lirionizer.form");
	}

	@Test
	void modelFactoryScansModelPackage() {
		assertThat(new ModelFactory().getPackages()).containsExactly("com.cdsoftware.lirionizer.model");
	}

	@Test
	void processFactoryScansProcessPackage() {
		assertThat(new ProcessFactory().getPackages()).containsExactly("com.cdsoftware.lirionizer.process");
	}
}
