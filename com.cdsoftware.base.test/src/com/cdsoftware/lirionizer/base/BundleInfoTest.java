package com.cdsoftware.lirionizer.base;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.Map;

import org.junit.jupiter.api.Test;

class BundleInfoTest {

	@Test
	void mapUsesPublicBundleAccessors() {
		Map<String, String> bundle = BundleInfo.toMap();

		assertThat(bundle).containsOnlyKeys("BundleID", "BundleVersion", "BundleVendor", "BundleName");
		assertThat(bundle.get("BundleID")).isEqualTo(BundleInfo.getBundleID());
		assertThat(bundle.get("BundleVersion")).isEqualTo(BundleInfo.getBundleVersion());
		assertThat(bundle.get("BundleVendor")).isEqualTo(BundleInfo.getBundleVendor());
		assertThat(bundle.get("BundleName")).isEqualTo(BundleInfo.getBundleName());
	}
}
