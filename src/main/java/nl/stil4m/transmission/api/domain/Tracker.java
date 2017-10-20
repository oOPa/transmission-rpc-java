package nl.stil4m.transmission.api.domain;

import lombok.Data;

@Data
public class Tracker {

    private String announce;
    private Long id;
    private String scrape;
    private Long tier;
}
