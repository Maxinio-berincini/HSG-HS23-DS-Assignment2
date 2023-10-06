package com.example.searchengine;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import javax.annotation.PostConstruct;
import javax.servlet.http.HttpServletResponse;
import java.io.FileReader;
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
	public void initialize(){
		if (properties.getCrawler().equals("multithread")){
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
			return Files.readString(Path.of("./src/main/resources/static/index.html"));
		} catch (IOException e) {
			throw new RuntimeException(e);
		}
	}

	@GetMapping("/search")
public String search(@RequestParam(name = "q", required = false) String q,
                     @RequestHeader Map<String, String> allHeaders,
                     HttpServletResponse response) {

    // Check if q is empty or null, then redirect to /
    if (q == null || q.trim().isEmpty()) {
        response.setStatus(HttpServletResponse.SC_MOVED_TEMPORARILY);
        response.setHeader("Location", "/");
        return null;
    }

	List<String> results = searcher.search(q, flippedIndexFileName);
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

	private String getHTMLHeader() {
		// Ideally move this CSS to a separate file, or use a templating engine.
		return "<!DOCTYPE html><html><head><title>Search Results</title><style>" +
			   "/* Colors */" +
				"    :root {" +
				"      /*LIGHTTHEMECOLORS*/" +
				"      --whoogle-logo: #685e79;" +
				"      --whoogle-page-bg: #fff;" +
				"      --whoogle-element-bg: #4285f4;" +
				"      --whoogle-text: #000;" +
				"      --whoogle-contrast-text: #fff;" +
				"      --whoogle-secondary-text: #70757a;" +
				"      --whoogle-result-bg: #fff;" +
				"      --whoogle-result-title: #1967d2;" +
				"      --whoogle-result-url: #0d652d;" +
				"      --whoogle-result-visited: #4b11a8;" +
				"      /*DARKTHEMECOLORS*/" +
				"      --whoogle-dark-logo: #685e79;" +
				"      --whoogle-dark-page-bg: #101020;" +
				"      --whoogle-dark-element-bg: #4285f4;" +
				"      --whoogle-dark-text: #fff;" +
				"      --whoogle-dark-contrast-text: #fff;" +
				"      --whoogle-dark-secondary-text: #bbb;" +
				"      --whoogle-dark-result-bg: #212131;" +
				"      --whoogle-dark-result-title: #64a7f6;" +
				"      --whoogle-dark-result-url: #34a853;" +
				"      --whoogle-dark-result-visited: #bbf" +
				"    }" +
				"    body {" +
				"      background: var(--whoogle-dark-page-bg) !important;" +
				"      font-family: arial, sans-serif;" +
				"      min-width: 652px;" +
				"    }" +
				"    .search-div {" +
				"      border-radius: 8px 8px 8px 8px;" +
				"      box-shadow: 0 1px 6px rgba(32, 33, 36, 0.18);" +
				"      margin-top: 10px;" +
				"      max-width: 80%;" +
				"      margin: auto;" +
				"      margin-bottom: 15px;" +
				"    }" +
				"    #search-form {" +
				"      height: 39px;" +
				"      display: flex;" +
				"      width: 100%;" +
				"      margin: 0px;" +
				"    }" +
				"    #search-bar {" +
				"      background: transparent !important;" +
				"      border-color: var(--whoogle-dark-element-bg) !important;" +
				"      color: var(--whoogle-dark-text) !important;" +
				"      background-color: var(--whoogle-dark-result-bg) !important;" +
				"      padding-right: 50px;" +
				"      padding-left: 8px;" +
				"      border: none;" +
				"      border-radius: 8px 8px 8px 8px;" +
				"      height: 40px !important;" +
				"      display: block;" +
				"      width: 100%;" +
				"      font-family: Roboto,HelveticaNeue,Arial,sans-serif;" +
				"      font-size: 14px;" +
				"      line-height: 20px;" +
				"    }" +
				"    #search-bar:focus {" +
				"      border-bottom: 2px solid var(--whoogle-dark-element-bg);" +
				"      outline: none;" +
				"    }" +
				"    .search-results {" +
				"      max-width: 80%;" +
				"      display: block !important;" +
				"      margin: auto !important;" +
				"      .search-result-item {" +
				"        font-size: 14px;" +
				"        overflow: hidden;" +
				"        box-shadow: 0 0 0 0 !important;" +
				"        background-color: var(--whoogle-dark-result-bg) !important;" +
				"        margin-bottom: 10px !important;" +
				"        border-radius: 8px !important;" +
				"        padding: 12px 16px 12px;" +
				"        color: var(--whoogle-dark-text) !important;" +
				"      }" +
				"      .link {" +
				"        text-decoration: none;" +
				"      }" +
				"      .link p {" +
				"        color: var(--whoogle-dark-result-url) !important;" +
				"      }" +
				"      .content p {" +
				"        color: var(--whoogle-dark-text) !important;" +
				"      }" +
			   "</style></head>";
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
	public void lucky(@RequestParam(name = "q") String q, HttpServletResponse response) {
		// get first result and redirect
		List<String> urls = searcher.search(q, flippedIndexFileName);
		if (urls.size() > 0) {
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
	}
}
