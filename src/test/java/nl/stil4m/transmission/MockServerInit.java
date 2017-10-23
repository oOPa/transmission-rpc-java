package nl.stil4m.transmission;

import org.mockserver.client.server.MockServerClient;
import org.mockserver.initialize.ExpectationInitializer;

public class MockServerInit implements ExpectationInitializer {

	public static MockServerClient mockServerClient;

	@Override
	public void initializeExpectations(MockServerClient mockServerClient) {
		MockServerInit.mockServerClient = mockServerClient;
	}
}
