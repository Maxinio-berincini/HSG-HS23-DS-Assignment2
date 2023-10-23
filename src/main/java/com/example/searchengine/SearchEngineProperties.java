package com.example.searchengine;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Primary;
import org.springframework.stereotype.Component;

@Component
@ConfigurationProperties
@Primary
public class SearchEngineProperties {

    @Value("${crawl}")
    private boolean crawl;
    @Value("${crawler}")
    private String crawler;

    public boolean getCrawl() {
        return crawl;
    }

    public void setCrawl(boolean crawl) {
        this.crawl = crawl;
    }

    public String getCrawler() {
        return crawler;
    }

    public void setCrawler(String crawler) {
        this.crawler = crawler;
    }
}
