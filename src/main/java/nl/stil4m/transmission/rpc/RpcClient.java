package nl.stil4m.transmission.rpc;

import java.io.IOException;
import java.nio.charset.Charset;
import java.util.Base64;
import java.util.HashMap;
import java.util.Map;

import org.apache.http.HttpHeaders;
import org.apache.http.HttpResponse;
import org.apache.http.auth.AuthScope;
import org.apache.http.auth.UsernamePasswordCredentials;
import org.apache.http.client.AuthCache;
import org.apache.http.client.CredentialsProvider;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.client.protocol.HttpClientContext;
import org.apache.http.client.utils.URIUtils;
import org.apache.http.impl.auth.BasicScheme;
import org.apache.http.impl.client.BasicAuthCache;
import org.apache.http.impl.client.BasicCredentialsProvider;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.impl.client.HttpClientBuilder;
import org.apache.http.util.EntityUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.fasterxml.jackson.databind.ObjectMapper;

import nl.stil4m.transmission.http.InvalidResponseStatus;
import nl.stil4m.transmission.http.RequestExecutor;
import nl.stil4m.transmission.http.RequestExecutorException;

public class RpcClient {

	private static final Logger LOGGER = LoggerFactory.getLogger(RpcClient.class);
	private static final Integer STATUS_OK = 200;

	private final RpcConfiguration configuration;
	private final ObjectMapper objectMapper;
	private final Map<String, String> headers;
	private final HttpClient defaultHttpClient = HttpClientBuilder.create().build();
	private final RequestExecutor requestExecutor;
	private HttpClientContext context = HttpClientContext.create();

	public RpcClient(RpcConfiguration configuration, ObjectMapper objectMapper) {
		this.requestExecutor = new RequestExecutor(objectMapper, configuration, defaultHttpClient,context);
		this.configuration = configuration;
		this.objectMapper = objectMapper;
		headers = new HashMap<>();

	}

	public RpcClient(RpcConfiguration configuration, ObjectMapper objectMapper, String username, String password) {
		this.requestExecutor = new RequestExecutor(objectMapper, configuration, defaultHttpClient,context);
		this.configuration = configuration;
		this.objectMapper = objectMapper;
		this.configuration.setUsername(username);
		this.configuration.setPassword(password);
		headers = new HashMap<>();
	}

	private boolean isBlank(String password) {

		return password == null || "".equals(password);
	}

	public <T, V> void execute(RpcCommand<T, V> command, Map<String, String> h) throws RpcException {
		try {
			executeCommandInner(command, h);
		} catch (RequestExecutorException | IOException e) {
			throw new RpcException(e);
		} catch (InvalidResponseStatus e) {
			LOGGER.trace("Failed execute command. Now setup and try again", e);
			setup();
			try {
				executeCommandInner(command, h);
			} catch (Exception | RequestExecutorException | InvalidResponseStatus inner) {
				throw new RpcException(inner);
			}
		}
	}

	private <T, V> void executeCommandInner(RpcCommand<T, V> command, Map<String, String> h)
			throws RequestExecutorException, InvalidResponseStatus, IOException, RpcException {
		for (Map.Entry<String, String> entry : h.entrySet()) {
			requestExecutor.removeAllHeaders();
			requestExecutor.configureHeader(entry.getKey(), entry.getValue());
		}

		RpcRequest<T> request = command.buildRequestPayload();
		RpcResponse<V> response = requestExecutor.execute(request, RpcResponse.class, STATUS_OK);

		Map args = (Map) response.getArguments();
		String stringValue = objectMapper.writeValueAsString(args);
		response.setArguments((V) objectMapper.readValue(stringValue, command.getArgumentsObject()));
		if (!command.getTag().equals(response.getTag())) {
			throw new RpcException(
					String.format("Invalid response tag. Expected %d, but got %d", command.getTag(), request.getTag()));
		}
		command.setResponse(response);

		if (!"success".equals(response.getResult())) {
			throw new RpcException("Rpc Command Failed: " + response.getResult(), command);
		}
	}

	private void setup() throws RpcException {
		try {
			if (!isBlank(configuration.getUsername()) && !isBlank(configuration.getPassword())) {
				String auth = configuration.getUsername()+ ":" + configuration.getPassword();
				
				String authHeader = "Basic " + Base64.getEncoder().encodeToString(
						  auth.getBytes(Charset.forName("ISO-8859-1")));;
				headers.put(HttpHeaders.AUTHORIZATION, authHeader);
				CredentialsProvider credsProvider = new BasicCredentialsProvider();
				credsProvider.setCredentials(
						new AuthScope(configuration.getHost().getHost(), configuration.getHost().getPort()),
						new UsernamePasswordCredentials(configuration.getUsername(), configuration.getPassword()));

				// Create AuthCache instance
				AuthCache authCache = new BasicAuthCache();
				// Generate BASIC scheme object and add it to the local auth cache
				BasicScheme basicAuth = new BasicScheme();
				authCache.put(URIUtils.extractHost(configuration.getHost()), basicAuth);

				// Add AuthCache to the execution context

				context.setCredentialsProvider(credsProvider);
				context.setAuthCache(authCache);
				
			}

			HttpPost httpPost = createPost();

			HttpResponse result = defaultHttpClient.execute(URIUtils.extractHost(configuration.getHost()), httpPost,
					context);
			putSessionHeader(result);
			EntityUtils.consume(result.getEntity());
		} catch (IOException e) {
			throw new RpcException(e);
		}
	}

	protected HttpPost createPost() {
		return new HttpPost(configuration.getHost());
	}

	protected HttpClient getClient() {
		return defaultHttpClient;
	}

	public void executeWithHeaders(RpcCommand command) throws RpcException {
		execute(command, headers);
	}

	private void putSessionHeader(HttpResponse result) {
		headers.put("X-Transmission-Session-Id", result.getFirstHeader("X-Transmission-Session-Id").getValue());
	}
}
