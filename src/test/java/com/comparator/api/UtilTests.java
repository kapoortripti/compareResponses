package com.comparator.api;

import org.testng.annotations.Test;

public class UtilTests extends Util {

	/**
	 * Positive Scenario Url should take http/https requests
	 */
	@Test(enabled = true, priority = 0)
	public void verifyUrlProtocol() throws Exception {
		urlReader("file1", "file2").forEach(System.out::println);

	}

	/**
	 * Positive Scenario Url should compare api response of both xml and json
	 */
	@Test(enabled = true, priority = 1)
	public void compareResponse() throws Exception {
		System.out.println(urlReader("file1", "file2"));

	}

	/**
	 * Positive Scenario Comparison of api Response is not terminated if any
	 * exception is occurred in previous steps
	 */
	@Test(enabled = true, priority = 2)
	public void verifyhandling() throws Exception {
		System.out.println(urlReader("file3", "file4"));

	}

	/**
	 * Negative Scenario Url should give error for non http/https requests
	 */

	@Test(enabled = true, priority = 3)
	public void invalidUrl() throws Exception {
		System.out.println(urlReader("file5", "file6"));

	}

}
