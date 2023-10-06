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
import java.util.List;
import java.util.Map;



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
	public List<String> search(@RequestParam(name = "q") String q, @RequestHeader Map<String,String> allHeaders) {
		return searcher.search(q, flippedIndexFileName);
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
