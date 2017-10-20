package nl.stil4m.transmission.api.domain;

import com.fasterxml.jackson.annotation.JsonProperty;

import lombok.Data;

@Data
public class TrackerStat {

	private String announce;
	private Long announceState;
	private Long downloadCount;
	private Boolean hasAnnounced;
	private Boolean hasScraped;
	private String host;
	private Long id;
	@JsonProperty("isBackup")
	private Boolean isBackup;
	private Long lastAnnouncePeerCount;
	private String lastAnnounceResult;
	private Long lastAnnounceStartTime;
	private Boolean lastAnnounceSucceeded;
	private Long lastAnnounceTime;
	private Boolean lastAnnounceTimedOut;
	private String lastScrapeResult;
	private Long lastScrapeStartTime;
	private Boolean lastScrapeSucceeded;
	private Long lastScrapeTime;
	private Boolean lastScrapeTimedOut;
	private Long leecherCount;
	private Long nextAnnounceTime;
	private Long nextScrapeTime;
	private String scrape;
	private Long scrapeState;
	private Long seederCount;
	private Long tier;

}
