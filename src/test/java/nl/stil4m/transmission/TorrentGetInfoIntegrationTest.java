package nl.stil4m.transmission;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;

import java.net.MalformedURLException;
import java.net.URI;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.databind.ObjectMapper;

import nl.stil4m.transmission.api.TransmissionRpcClient;
import nl.stil4m.transmission.api.domain.AddTorrentInfo;
import nl.stil4m.transmission.api.domain.Constants;
import nl.stil4m.transmission.api.domain.RemoveTorrentInfo;
import nl.stil4m.transmission.api.domain.TorrentGetRequestInfo;
import nl.stil4m.transmission.api.domain.TorrentInfo;
import nl.stil4m.transmission.api.domain.TorrentInfoCollection;
import nl.stil4m.transmission.api.domain.ids.OmittedIds;
import nl.stil4m.transmission.api.torrent.TorrentStatus;
import nl.stil4m.transmission.rpc.RpcClient;
import nl.stil4m.transmission.rpc.RpcConfiguration;
import nl.stil4m.transmission.rpc.RpcException;

public class TorrentGetInfoIntegrationTest extends IntegrationTest {

	private TransmissionRpcClient rpcClient;

	@Before
	public void before() throws RpcException, MalformedURLException, InterruptedException {
		ObjectMapper objectMapper = new ObjectMapper();
		objectMapper.setSerializationInclusion(JsonInclude.Include.NON_NULL);
		RpcConfiguration rpcConfiguration = new RpcConfiguration();
		rpcConfiguration.setHost(URI.create(nl.stil4m.transmission.Constants.TEST_URL));
		RpcClient client = new RpcClient(rpcConfiguration, objectMapper, nl.stil4m.transmission.Constants.TEST_USER,
				nl.stil4m.transmission.Constants.TEST_PASSWORD);
		rpcClient = new TransmissionRpcClient(client);
		rpcClient.removeTorrent(new RemoveTorrentInfo(new OmittedIds(), true));
		pause();
		TorrentInfoCollection result = rpcClient.getAllTorrentsInfo();
		assertThat(result.getTorrents().size(), is(0));

		AddTorrentInfo addTorrentInfo = new AddTorrentInfo();
		addTorrentInfo.setFilename(nl.stil4m.transmission.Constants.TEST_WHITEHOUSE_MAGNET);
		addTorrentInfo.setPaused(false);
		rpcClient.addTorrent(addTorrentInfo);

		addTorrentInfo = new AddTorrentInfo();
		addTorrentInfo.setFilename(nl.stil4m.transmission.Constants.TEST_OTHER_MAGNET);
		addTorrentInfo.setPaused(false);
		rpcClient.addTorrent(addTorrentInfo);

		result = rpcClient.getAllTorrentsInfo();
		assertThat(result.getTorrents().size(), is(2));

		TorrentInfo torrent = result.getTorrents().get(0);
		TorrentInfo secondTorrent = result.getTorrents().get(1);
		assertThat(torrent.getStatus(), is(TorrentStatus.DOWNLOADING.getValue()));
		assertThat(secondTorrent.getStatus(), is(TorrentStatus.DOWNLOADING.getValue()));
		pause();
	}

	@After
	public void after() throws RpcException, InterruptedException {
		pause();
		rpcClient.removeTorrent(new RemoveTorrentInfo(new OmittedIds(), true));
	}

	@Test
	public void testGetTorrentInfoForAllTorrents() throws RpcException {
		TorrentInfoCollection result = rpcClient
				.getTorrentInfo(new TorrentGetRequestInfo(new OmittedIds(), Constants.TORRENT_INFO_FIELDS));

		assertThat(result.getTorrents().size(), is(2));
		assertThat(result.getTorrents().get(0).getName(), is(nl.stil4m.transmission.Constants.TEST_WHITEHOUSE_NAME));
		assertThat(result.getTorrents().get(0).getCreator(), is(""));
		assertThat(result.getTorrents().get(1).getName(), is(nl.stil4m.transmission.Constants.TEST_OTHER_NAME));
		assertThat(result.getTorrents().get(1).getCreator(), is(""));
	}

	@Test
	public void testGetTorrentInfoForAllTorrentsWithFiles() throws RpcException {
		while (true) {
			TorrentInfoCollection result = rpcClient
					.getTorrentInfo(new TorrentGetRequestInfo(new OmittedIds(), Constants.TORRENT_INFO_FIELDS));
			TorrentInfo torrentInfo = result.getTorrents().get(0);
			TorrentInfo otherTorrentInfo = result.getTorrents().get(1);
			if ((torrentInfo.getFiles() != null && torrentInfo.getFiles().size() > 0)
					|| otherTorrentInfo.getFiles() != null && otherTorrentInfo.getFiles().size() > 0) {
				break;
			}
			pause();
		}
	}
}
