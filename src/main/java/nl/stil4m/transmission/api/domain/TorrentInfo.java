package nl.stil4m.transmission.api.domain;


import com.fasterxml.jackson.annotation.JsonProperty;

import lombok.Data;

import java.util.List;
@Data
public class TorrentInfo {

    private Long activityDate;
    private Long addedDate;
    private Long bandwidthPriority;

    private String comment;

    private Long corruptEver;

    private String creator;

    private Long dateAdded;

    private Long dateCreated;
    private Long desiredAvailable;
    private Long doneDate;
    private String downloadDir;
    private Long downloadedEver;
    private Long downloadLimit;
    private Boolean downloadLimited;
    private Long error;
    private String errorString;
    private Long eta;
    private Long etaIdle;
    private List<File> files;
    private List<FileStat> fileStats;
    private String hashString;
    private Long haveUnchecked;
    private Long haveValid;
    private Boolean honorsSessionLimits;
    private Long id;

    @JsonProperty("isFinished")
    private Boolean isFinished;

    @JsonProperty("isPrivate")
    private Boolean isPrivate;

    @JsonProperty("isStalled")
    private Boolean isStalled;

    private Long leftUntilDone;
    private String magnetLink;
    private Long manualAnnounceTime;
    private Long maxConnectedPeers;
    private Double metadataPercentComplete;
    private String name;

    @JsonProperty("peer-limit")
    private Long peerLimit;
    private List<Peer> peers;
    private Long peersConnected;
    private Object peersFrom;
    private Long peersGettingFromUs;
    private Long peersSendingToUs;
    private Double percentDone;
    private String pieces;
    private Long pieceCount;
    private Long pieceSize;
    private List<Integer> priorities;
    private Long queuePosition;
    private Long rateDownload;
    private Long rateUpload;
    private Double recheckProgress;
    private Long secondsDownloading;
    private Long secondsSeeding;
    private Long seedIdleLimit;
    private Long seedIdleMode;
    private Double seedRatioLimit;
    private Long seedRatioMode;
    private Long sizeWhenDone;
    private Long startDate;
    private Long status;
    private List<Tracker> trackers;
    private List<TrackerStat> trackerStats;
    private Long totalSize;
    private String torrentFile;
    private Long uploadedEver;
    private Long uploadLimit;
    private Boolean uploadLimited;
    private Double uploadRatio;
    private List wanted;
    private List webseeds;
    private Long webseedsSendingToUs;

}
