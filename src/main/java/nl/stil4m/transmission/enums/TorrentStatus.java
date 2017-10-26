package nl.stil4m.transmission.enums;

public enum TorrentStatus {

	SEEDING(6l, "Seeding"), QUEUED(3L, "Queued"), DOWNLOADING(4l, "Downloading");
	private Long code;
	private String desc;

	private TorrentStatus(Long x, String des) {
		this.code = x;
		this.desc = des;
	}

	public static String getStatusDescription(Long code) {
		String result = null;
		for (TorrentStatus en : TorrentStatus.values()) {
			if (en.code == code)
				result = en.desc;
		}
		return result;
	}
}
