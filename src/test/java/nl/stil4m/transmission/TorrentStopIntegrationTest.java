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
import nl.stil4m.transmission.api.domain.RemoveTorrentInfo;
import nl.stil4m.transmission.api.domain.TorrentAction;
import nl.stil4m.transmission.api.domain.TorrentInfo;
import nl.stil4m.transmission.api.domain.TorrentInfoCollection;
import nl.stil4m.transmission.api.domain.ids.NumberListIds;
import nl.stil4m.transmission.api.domain.ids.OmittedIds;
import nl.stil4m.transmission.api.domain.ids.ShaListIds;
import nl.stil4m.transmission.api.torrent.TorrentStatus;
import nl.stil4m.transmission.rpc.RpcClient;
import nl.stil4m.transmission.rpc.RpcConfiguration;
import nl.stil4m.transmission.rpc.RpcException;

public class TorrentStopIntegrationTest extends IntegrationTest {

	private TransmissionRpcClient rpcClient;

	private TorrentInfo torrent;

	@Before
	public void before() throws RpcException, MalformedURLException, InterruptedException {
		ObjectMapper objectMapper = new ObjectMapper();
		objectMapper.setSerializationInclusion(JsonInclude.Include.NON_NULL);
		RpcConfiguration rpcConfiguration = new RpcConfiguration();
		rpcConfiguration.setHost(URI.create(Constants.TEST_URL));
		RpcClient client = new RpcClient(rpcConfiguration, objectMapper, Constants.TEST_USER, Constants.TEST_PASSWORD);
		rpcClient = new TransmissionRpcClient(client);
		rpcClient.removeTorrent(new RemoveTorrentInfo(new OmittedIds(), true));
		pause();
		TorrentInfoCollection result = rpcClient.getAllTorrentsInfo();
		assertThat(result.getTorrents().size(), is(0));

		AddTorrentInfo addTorrentInfo = new AddTorrentInfo();
		addTorrentInfo.setFilename(Constants.TEST_WHITEHOUSE_MAGNET);
		addTorrentInfo.setPaused(false);
		rpcClient.addTorrent(addTorrentInfo);

		addTorrentInfo = new AddTorrentInfo();
		addTorrentInfo.setFilename(Constants.TEST_OTHER_MAGNET);
		addTorrentInfo.setPaused(false);
		rpcClient.addTorrent(addTorrentInfo);

		result = rpcClient.getAllTorrentsInfo();
		assertThat(result.getTorrents().size(), is(2));

		torrent = result.getTorrents().get(0);
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
	public void testStopTorrentWithTorrentId() throws RpcException {
		rpcClient.doAction(new NumberListIds(torrent.getId()), TorrentAction.STOP);
		pause();

		TorrentInfoCollection result = rpcClient.getAllTorrentsInfo();
		TorrentInfo torrentInfo = result.getTorrents().get(0);
		assertThat(torrentInfo.getStatus(), is(TorrentStatus.STOPPED.getValue()));

		torrentInfo = result.getTorrents().get(1);
		assertThat(torrentInfo.getStatus(), is(TorrentStatus.DOWNLOADING.getValue()));
	}

	@Test
	public void testStopTorrentWithTorrentHash() throws RpcException {
		rpcClient.doAction(new ShaListIds(torrent.getHashString()), TorrentAction.STOP);
		pause();

		TorrentInfoCollection result = rpcClient.getAllTorrentsInfo();
		TorrentInfo torrentInfo = result.getTorrents().get(0);
		assertThat(torrentInfo.getStatus(), is(TorrentStatus.STOPPED.getValue()));

		torrentInfo = result.getTorrents().get(1);
		assertThat(torrentInfo.getStatus(), is(TorrentStatus.DOWNLOADING.getValue()));
	}

	@Test
	public void testStopAllTorrents() throws RpcException {
		rpcClient.doAction(new OmittedIds(), TorrentAction.STOP);
		pause();

		TorrentInfoCollection result = rpcClient.getAllTorrentsInfo();
		TorrentInfo torrentInfo = result.getTorrents().get(0);
		assertThat(torrentInfo.getStatus(), is(TorrentStatus.STOPPED.getValue()));

		torrentInfo = result.getTorrents().get(1);
		assertThat(torrentInfo.getStatus(), is(TorrentStatus.STOPPED.getValue()));
	}

}
