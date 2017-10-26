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
import nl.stil4m.transmission.api.domain.QueueAction;
import nl.stil4m.transmission.api.domain.RemoveTorrentInfo;
import nl.stil4m.transmission.api.domain.TorrentInfo;
import nl.stil4m.transmission.api.domain.TorrentInfoCollection;
import nl.stil4m.transmission.api.domain.ids.NumberListIds;
import nl.stil4m.transmission.api.domain.ids.OmittedIds;
import nl.stil4m.transmission.api.torrent.TorrentStatus;
import nl.stil4m.transmission.rpc.RpcClient;
import nl.stil4m.transmission.rpc.RpcConfiguration;
import nl.stil4m.transmission.rpc.RpcException;

public class QueueMoveUpIntegrationTest extends IntegrationTest {

	private TransmissionRpcClient rpcClient;
	private TorrentInfo torrent;
	private TorrentInfo secondTorrent;
	private TorrentInfo thirdTorrent;

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
		addTorrentInfo.setPaused(true);
		rpcClient.addTorrent(addTorrentInfo);

		addTorrentInfo = new AddTorrentInfo();
		addTorrentInfo.setFilename(Constants.TEST_OTHER_MAGNET);
		addTorrentInfo.setPaused(false);
		rpcClient.addTorrent(addTorrentInfo);

		addTorrentInfo = new AddTorrentInfo();
		addTorrentInfo.setFilename(Constants.MAN_OF_STEEL_MAGNET);
		addTorrentInfo.setPaused(true);
		rpcClient.addTorrent(addTorrentInfo);

		result = rpcClient.getAllTorrentsInfo();
		assertThat(result.getTorrents().size(), is(3));

		torrent = result.getTorrents().get(0);
		secondTorrent = result.getTorrents().get(1);
		thirdTorrent = result.getTorrents().get(2);

		assertThat(torrent.getStatus(), is(TorrentStatus.STOPPED.getValue()));
		assertThat(secondTorrent.getStatus(), is(TorrentStatus.DOWNLOADING.getValue()));
		pause();
	}

	@After
	public void after() throws RpcException, InterruptedException {
		pause();
		rpcClient.removeTorrent(new RemoveTorrentInfo(new OmittedIds(), true));
	}

	@Test
	public void moveBottomOneUp() throws RpcException {
		rpcClient.doQueueAction(QueueAction.MOVE_UP, new NumberListIds(thirdTorrent.getId()));
		pause();

		TorrentInfoCollection result = rpcClient.getAllTorrentsInfo();
		assertThat(result.getTorrents().get(0).getId(), is(torrent.getId()));
		assertThat(result.getTorrents().get(0).getQueuePosition(), is(0L));

		assertThat(result.getTorrents().get(1).getId(), is(secondTorrent.getId()));
		assertThat(result.getTorrents().get(1).getQueuePosition(), is(2L));

		assertThat(result.getTorrents().get(2).getId(), is(thirdTorrent.getId()));
		assertThat(result.getTorrents().get(2).getQueuePosition(), is(1L));
	}

	@Test
	public void moveTopOneDown() throws RpcException {
		rpcClient.doQueueAction(QueueAction.MOVE_DOWN, new NumberListIds(torrent.getId()));
		pause();

		TorrentInfoCollection result = rpcClient.getAllTorrentsInfo();
		assertThat(result.getTorrents().get(0).getId(), is(torrent.getId()));
		assertThat(result.getTorrents().get(0).getQueuePosition(), is(1L));

		assertThat(result.getTorrents().get(1).getId(), is(secondTorrent.getId()));
		assertThat(result.getTorrents().get(1).getQueuePosition(), is(0L));

		assertThat(result.getTorrents().get(2).getId(), is(thirdTorrent.getId()));
		assertThat(result.getTorrents().get(2).getQueuePosition(), is(2L));
	}

	@Test
	public void moveTopToBottom() throws RpcException {
		rpcClient.doQueueAction(QueueAction.MOVE_BOTTOM, new NumberListIds(torrent.getId()));
		pause();

		TorrentInfoCollection result = rpcClient.getAllTorrentsInfo();
		assertThat(result.getTorrents().get(0).getId(), is(torrent.getId()));
		assertThat(result.getTorrents().get(0).getQueuePosition(), is(2L));

		assertThat(result.getTorrents().get(1).getId(), is(secondTorrent.getId()));
		assertThat(result.getTorrents().get(1).getQueuePosition(), is(0L));

		assertThat(result.getTorrents().get(2).getId(), is(thirdTorrent.getId()));
		assertThat(result.getTorrents().get(2).getQueuePosition(), is(1L));
	}

	@Test
	public void moveBottomToTop() throws RpcException {
		rpcClient.doQueueAction(QueueAction.MOVE_TOP, new NumberListIds(thirdTorrent.getId()));
		pause();

		TorrentInfoCollection result = rpcClient.getAllTorrentsInfo();
		assertThat(result.getTorrents().get(0).getId(), is(torrent.getId()));
		assertThat(result.getTorrents().get(0).getQueuePosition(), is(1L));

		assertThat(result.getTorrents().get(1).getId(), is(secondTorrent.getId()));
		assertThat(result.getTorrents().get(1).getQueuePosition(), is(2L));

		assertThat(result.getTorrents().get(2).getId(), is(thirdTorrent.getId()));
		assertThat(result.getTorrents().get(2).getQueuePosition(), is(0L));
	}

}