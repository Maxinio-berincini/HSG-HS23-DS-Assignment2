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
	public String search(@RequestParam(name = "q") String q, @RequestHeader Map<String,String> allHeaders) {
		List<String> results = searcher.search(q, flippedIndexFileName);
		Map<String, List<String>> keywordsByUrl = loadKeywordsFromIndex();

		StringBuilder html = new StringBuilder();
		html.append("<!DOCTYPE html><html><head><title>Search Results</title>");
		html.append("<style>")
			.append("/* Colors */" +
					":root {" +
					"/*LIGHTTHEMECOLORS*/" +
					"--whoogle-logo: #685e79;" +
					"--whoogle-page-bg: #fff;" +
					"--whoogle-element-bg: #4285f4;" +
					"--whoogle-text: #000;" +
					"--whoogle-contrast-text: #fff;" +
					"--whoogle-secondary-text: #70757a;" +
					"--whoogle-result-bg: #fff;" +
					"--whoogle-result-title: #1967d2;" +
					"--whoogle-result-url: #0d652d;" +
					"--whoogle-result-visited: #4b11a8;" +
					"/*DARKTHEMECOLORS*/" +
					"--whoogle-dark-logo: #685e79;" +
					"--whoogle-dark-page-bg: #101020;" +
					"--whoogle-dark-element-bg: #4285f4;" +
					"--whoogle-dark-text: #fff;" +
					"--whoogle-dark-contrast-text: #fff;" +
					"--whoogle-dark-secondary-text: #bbb;" +
					"--whoogle-dark-result-bg: #212131;" +
					"--whoogle-dark-result-title: #64a7f6;" +
					"--whoogle-dark-result-url: #34a853;" +
					"--whoogle-dark-result-visited: #bbf" +
					"}")
			.append("body {background: var(--whoogle-dark-page-bg) !important;}")
			.append(".search-results {")
			.append("max-width: 80%;")
			.append("display: block !important;")
			.append("margin: auto !important;")
		   .append(".search-result-item {")
		   .append("font-size: 14px;")
		   .append("line-height: 22px;")
		   .append("overflow: hidden;")
		   .append("box-shadow: 0 0 0 0 !important;")
		   .append("background-color: var(--whoogle-dark-result-bg) !important;")
		   .append("margin-bottom: 10px !important;")
		   .append("border-radius: 8px !important;")
		   .append("padding: 12px 16px 12px;")
		   .append("color: var(--whoogle-dark-text) !important;")
		   .append("}")
		   .append(".link {")
		   .append("text-decoration: none;")
		   .append("}")
		   .append(".link p {")
		   .append("color: var(--whoogle-dark-result-url) !important;")
		   .append("}")
		   .append(".content p {")
		   .append("color: var(--whoogle-dark-text) !important;")
		   .append("}")
		   .append("</style>");
		html.append("</head><body>");

		Function<String, String> boldKeyword = (text) -> {
			String boldedKeyword = "<strong>" + q + "</strong>";
			return text.replace(q, boldedKeyword);
		};

		html.append("<div class='search-results'>");

		for (String url : results) {
			String lastBitOfUrl = url.substring(url.lastIndexOf('/'));
			List<String> keywords = keywordsByUrl.getOrDefault(lastBitOfUrl, Collections.emptyList());

			html.append("<div class='search-result-item'>")
				.append("<div class='link'>")
				.append("<a href='").append(url).append("' target='_blank'>")
				.append("<h3>").append(lastBitOfUrl).append("</h3>")
				.append("<p>").append(url).append("</p>")
				.append("</a>")
				.append("</div>")
				.append("<div class='content'>")
				.append("<p>").append(boldKeyword.apply(String.join(", ", keywords))).append("</p>")
				.append("</div>")
				.append("</div>");
		}

		html.append("</div></body></html>");

		return html.toString();
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
			// Optional: You can handle the case when no URLs are found here.
			response.setStatus(HttpServletResponse.SC_NOT_FOUND);
			try {
				response.getWriter().write("Not Found");
			} catch (IOException e) {
				throw new RuntimeException(e);
			}
		}
	}
}
