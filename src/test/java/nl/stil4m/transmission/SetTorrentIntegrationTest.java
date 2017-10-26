package nl.stil4m.transmission;

import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.CoreMatchers.notNullValue;
import static org.hamcrest.MatcherAssert.assertThat;

import java.net.MalformedURLException;
import java.net.URI;
import java.util.List;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.databind.ObjectMapper;

import nl.stil4m.transmission.api.TransmissionRpcClient;
import nl.stil4m.transmission.api.domain.AddTorrentInfo;
import nl.stil4m.transmission.api.domain.AddedTorrentInfo;
import nl.stil4m.transmission.api.domain.Constants;
import nl.stil4m.transmission.api.domain.RemoveTorrentInfo;
import nl.stil4m.transmission.api.domain.SetTorrentInfo;
import nl.stil4m.transmission.api.domain.TorrentGetRequestInfo;
import nl.stil4m.transmission.api.domain.TorrentInfo;
import nl.stil4m.transmission.api.domain.TorrentInfoCollection;
import nl.stil4m.transmission.api.domain.ids.NumberListIds;
import nl.stil4m.transmission.rpc.RpcClient;
import nl.stil4m.transmission.rpc.RpcConfiguration;
import nl.stil4m.transmission.rpc.RpcException;

public class SetTorrentIntegrationTest extends IntegrationTest {

	private TransmissionRpcClient rpcClient;
	private TorrentInfo targetTorrent;

	@Before
	public void before() throws RpcException, MalformedURLException, InterruptedException {
		ObjectMapper objectMapper = new ObjectMapper();
		objectMapper.setSerializationInclusion(JsonInclude.Include.NON_NULL);
		RpcConfiguration rpcConfiguration = new RpcConfiguration();
		rpcConfiguration.setHost(URI.create(nl.stil4m.transmission.Constants.TEST_URL));
		RpcClient client = new RpcClient(rpcConfiguration, objectMapper, nl.stil4m.transmission.Constants.TEST_USER,
				nl.stil4m.transmission.Constants.TEST_PASSWORD);
		rpcClient = new TransmissionRpcClient(client);

		AddTorrentInfo addTorrentInfo = new AddTorrentInfo();
		addTorrentInfo.setFilename(nl.stil4m.transmission.Constants.TEST_OTHER_MAGNET);
		addTorrentInfo.setPaused(true);
		AddedTorrentInfo result = rpcClient.addTorrent(addTorrentInfo);
		targetTorrent = result.getTorrentAdded();

		pause();
	}

	@After
	public void after() throws RpcException, InterruptedException {
		if (targetTorrent != null) {
			pause();
			rpcClient.removeTorrent(new RemoveTorrentInfo(new NumberListIds(targetTorrent.getId()), true));
		}
	}

	@Test
	public void testAddTorrent() throws RpcException {
		SetTorrentInfo setTorrentInfo = SetTorrentInfo.builder().downloadLimited(Boolean.TRUE).downloadLimitInKBps(33L)
				.build();

		rpcClient.setTorrent(setTorrentInfo);

		TorrentInfoCollection actualTorrentInfo = rpcClient.getTorrentInfo(
				new TorrentGetRequestInfo(new NumberListIds(targetTorrent.getId()), Constants.TORRENT_INFO_FIELDS));

		assertThat(actualTorrentInfo, is(notNullValue()));
		List<TorrentInfo> actualTorrents = actualTorrentInfo.getTorrents();
		assertThat(actualTorrents.size(), equalTo(1));
		assertThat(actualTorrents.get(0).getDownloadLimit(), equalTo(33L));
		assertThat(actualTorrents.get(0).getDownloadLimited(), equalTo(Boolean.TRUE));
	}
}