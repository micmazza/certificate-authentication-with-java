package certificates.server;

import junit.framework.Test;
import junit.framework.TestCase;
import junit.framework.TestSuite;

/**
 * Unit test for simple SSLSocketEchoServer.
 */
public class SSLSocketEchoServerTest extends TestCase {
	/**
	 * Create the test case
	 *
	 * @param testName name of the test case
	 */
	public SSLSocketEchoServerTest(String testName) {
		super(testName);
	}

	/**
	 * @return the suite of tests being tested
	 */
	public static Test suite() {
		return new TestSuite(SSLSocketEchoServerTest.class);
	}

	/**
	 * Rigourous Test :-)
	 */
	public void testSSLSocketEchoServer() {
		assertTrue(true);
	}
}
