package certificates.client;

import junit.framework.Test;
import junit.framework.TestCase;
import junit.framework.TestSuite;

/**
 * Unit test for SSLScocketClient.
 */
public class SSLScocketClientTest extends TestCase {

	/**
	 * Create the test case
	 *
	 * @param testName name of the test case
	 */
	public SSLScocketClientTest(String testName) {
		super(testName);
	}

	/**
	 * @return the suite of tests being tested
	 */
	public static Test suite() {
		return new TestSuite(SSLScocketClientTest.class);
	}

	/**
	 * Rigourous Test :-)
	 */
	public void testSSLScocketClient() {
		assertTrue(true);
	}
}
