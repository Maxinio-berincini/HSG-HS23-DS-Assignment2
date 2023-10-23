package com.example.searchengine;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import javax.annotation.PostConstruct;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.*;
import java.util.function.Function;


@RestController
public class SearchEngine {

    public final String indexFileName = "./src/main/resources/index.csv";

    public final String flippedIndexFileName = "./src/main/resources/index_flipped.csv";

    public final String startUrl = "https://api.interactions.ics.unisg.ch/hypermedia-environment/cc2247b79ac48af0";

    @Autowired
    Searcher searcher;

    @Autowired
    IndexFlipper indexFlipper;

    @Qualifier("searchEngineProperties")
    @Autowired
    SearchEngineProperties properties;

    Crawler crawler;

    @PostConstruct
    public void initialize() {
        if (properties.getCrawler().equals("multithread")) {
            this.crawler = new MultithreadCrawler(indexFileName);
        } else {
            this.crawler = new SimpleCrawler(indexFileName);
        }
        if (properties.getCrawl()) {
            crawler.crawl(startUrl);
            indexFlipper.flipIndex(indexFileName, flippedIndexFileName);
        }
    }

    @GetMapping("/")
    public String index() {
        try {
            String content = Files.readString(Path.of("./src/main/resources/static/index.html"));
            String adminButtons = getAdminButtons();
            return content + adminButtons;
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }


    @GetMapping("/search")
    public String search(@RequestParam(name = "q", required = false) String q,
                         @RequestHeader Map<String, String> allHeaders,
                         HttpServletResponse response) {

        // if q is empty, return 400
        if (q == null || q.isEmpty()) {
            response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
            return "Bad Request";
        }

        List<String> results = searcher.search(q, flippedIndexFileName);

        // check if accepts application/json
        if (allHeaders.containsKey("accept") && allHeaders.get("accept").contains("application/json")) {
            response.setStatus(HttpServletResponse.SC_OK);
            // format as json
            StringBuilder json = new StringBuilder();
            json.append("[");
            for (String url : results) {
                json.append("\"").append(url).append("\",");
            }
            json.deleteCharAt(json.length() - 1);
            json.append("]");
            try {
                response.getWriter().write(json.toString());
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        }

        Map<String, List<String>> keywordsByUrl = loadKeywordsFromIndex();

        StringBuilder html = new StringBuilder();
        html.append(getHTMLHeader());
        html.append("<body>");
        html.append(getSearchBar(q));

        Function<String, String> boldKeyword = (text) -> {
            String boldedKeyword = "<strong>" + q + "</strong>";
            return text.replace(q, boldedKeyword);
        };

        html.append("<div class='search-results'>");
        for (String url : results) {
            String lastBitOfUrl = url.substring(url.lastIndexOf('/'));
            List<String> keywords = keywordsByUrl.getOrDefault(lastBitOfUrl, Collections.emptyList());
            html.append(getResultItem(url, lastBitOfUrl, boldKeyword.apply(String.join(", ", keywords))));
        }

        html.append("</div></body></html>");
        return html.toString();
    }

    private String getAdminButtons() {
        return "<div class='admin-buttons'>" +
                "<h2>Admin Interface</h2>" +
                "<div class='form-fields'>" +
                "<label for='adminUrl'>URL:</label>" +
                "<input type='text' id='adminUrl' name='adminUrl'>" +
                "<label for='adminKeywords'>Keywords (comma separated):</label>" +
                "<input type='text' id='adminKeywords' name='adminKeywords'>" +
                "</div>" +
                "<div class='action-buttons'>" +
                "<button onclick=\"adminAction('admin/crawl')\">Start Crawl</button>" +
                "<button onclick=\"adminAction('admin/regenerate-flipped-index')\">Regenerate Flipped Index</button>" +
                "<button onclick=\"adminAction('admin/delete-url')\">Delete URL</button>" +
                "<button onclick=\"adminAction('admin/update-url')\">Update URL</button>" +
                "<button onclick=\"loadKeywordsForUrl()\">Load Keywords from URL</button>" +
                "</div>" +
                "</div>";


    }


    private String getHTMLHeader() {
        return "<!DOCTYPE html><html><head><title>Search Results</title>" +
                "<link rel='stylesheet' type='text/css' href='styles.css'>" +
                "</head>";
    }

    private String getSearchBar(String q) {
        return "<div class=\"search-div\"><form id=\"search-form\" method=\"GET\">" +
                "<input id=\"search-bar\" autocomplete=\"off\" class=\"search-bar-input\" name=\"q\" type=\"text\" value=\"" + q + "\">" +
                "</form></div>";
    }

    private String getResultItem(String url, String lastBitOfUrl, String keywords) {
        return "<div class='search-result-item'>" +
                "<div class='link'>" +
                "<a href='" + url + "' target='_blank'>" +
                "<h3>" + lastBitOfUrl + "</h3>" +
                "<p>" + url + "</p>" +
                "</a>" +
                "</div>" +
                "<div class='content'>" +
                "<p>" + keywords + "</p>" +
                "</div>" +
                "</div>";
    }

    private Map<String, List<String>> loadKeywordsFromIndex() {
        Map<String, List<String>> keywordsByUrl = new HashMap<>();
        try {
            List<String> lines = Files.readAllLines(Path.of(indexFileName));
            for (String line : lines) {
                String[] parts = line.split(",");
                String url = parts[0];
                List<String> keywords = Arrays.asList(parts).subList(1, parts.length);
                keywordsByUrl.put(url, keywords);
            }
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
        return keywordsByUrl;
    }

    @GetMapping("/lucky")
    public String lucky(@RequestParam(name = "q") String q, @RequestHeader Map<String, String> allHeaders, HttpServletResponse response) {
        // if q is empty, return 400
        if (q == null || q.isEmpty()) {
            response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
            return "";
        }
        // get first result and redirect
        List<String> urls = searcher.search(q, flippedIndexFileName);
        if (urls.size() > 0) {

            if (allHeaders.containsKey("accept") && allHeaders.get("accept").contains("application/json")) {
                response.setStatus(HttpServletResponse.SC_OK);
                return "\"" + urls.get(0) + "\"";
            }

            response.setStatus(HttpServletResponse.SC_MOVED_TEMPORARILY);
            response.setHeader("Location", urls.get(0));
        } else {
            response.setStatus(HttpServletResponse.SC_NOT_FOUND);
            try {
                response.getWriter().write("Not Found");
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        }

        return "";
    }

    @PostMapping("/admin/crawl")
    public ResponseEntity<String> startCrawl() {
        try {
            // create a new crawler to initialize the executor service
            if (properties.getCrawler().equals("multithread")) {
                this.crawler = new MultithreadCrawler(indexFileName);
            }
            crawler.crawl(startUrl);
            return new ResponseEntity<>("Crawling finished successfully", HttpStatus.OK);
        } catch (Exception e) {
            // Log the error for debugging
            e.printStackTrace();
            return new ResponseEntity<>("Error during crawling: " + e.getMessage(), HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    @PostMapping("/admin/regenerate-flipped-index")
    public ResponseEntity<String> regenerateFlippedIndex() {
        indexFlipper.flipIndex(indexFileName, flippedIndexFileName);
        return new ResponseEntity<>("Flipped index regenerated successfully", HttpStatus.OK);
    }

    @PostMapping("/admin/delete-url")
    public ResponseEntity<String> deleteUrl(@RequestParam String url) {
        crawler.deleteUrlFromIndex(url);
        return new ResponseEntity<>("URL deleted successfully", HttpStatus.OK);
    }


    @PostMapping("/admin/update-url")
    public ResponseEntity<String> updateUrl(@RequestParam String url, @RequestParam String keywords) {
        List<String> keywordList = Arrays.asList(keywords.split(","));
        crawler.updateUrlInIndex(url, keywordList);
        return new ResponseEntity<>("URL updated successfully", HttpStatus.OK);
    }

    @GetMapping("/admin/load-keywords")
    public ResponseEntity<String> loadKeywordsForUrl(@RequestParam String url) {
        List<String> keywords = crawler.getKeywordsForUrl(url);
        if (keywords != null) {
            return new ResponseEntity<>(String.join(",", keywords), HttpStatus.OK);
        } else {
            return new ResponseEntity<>("URL not found", HttpStatus.NOT_FOUND);
        }
    }

}
