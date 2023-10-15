package com.example.searchengine;

import com.opencsv.CSVWriter;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;
import org.springframework.scheduling.concurrent.ConcurrentTaskExecutor;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;

import java.io.FileWriter;
import java.io.IOException;
import java.util.*;
import java.util.concurrent.*;

public class MultithreadCrawler extends Crawler {

    private static final String BASE_URL = "https://api.interactions.ics.unisg.ch/hypermedia-environment/";
    private final ThreadPoolTaskExecutor executorService;
    private final CopyOnWriteArraySet<String> visited;
    private final CopyOnWriteArraySet<String[]> lines;

    public MultithreadCrawler(String indexFileName) {
        super(indexFileName);

        int numThreads = Runtime.getRuntime().availableProcessors();
        executorService = new ThreadPoolTaskExecutor();
        executorService.setCorePoolSize(numThreads);
        executorService.initialize();

        visited = new CopyOnWriteArraySet<>();
        lines = new CopyOnWriteArraySet<>();
    }

    public void crawl(String startUrl) {
        double startTime = System.nanoTime();
        executorService.submit(new CrawlerRunnable(this, startUrl));

        while (executorService.getActiveCount() > 0) {
            try {
                Thread.sleep(1000);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }

        // Save lines to file (similar to the simple crawler)
        try (FileWriter fileWriter = new FileWriter(indexFileName); CSVWriter writer = new CSVWriter(fileWriter, ',', CSVWriter.NO_QUOTE_CHARACTER, ' ', "\r\n")) {
            for (String[] line : lines) {
                writer.writeNext(line);
            }
        } catch (IOException e) {
            e.printStackTrace();
        }

        executorService.shutdown();

        long endTime = System.nanoTime();
        int duration = (int) ((endTime - startTime) / 1000000000);
        System.out.println("Duration multithread crawler: " + duration + "s");
    }

    class CrawlerRunnable implements Runnable {

        MultithreadCrawler crawler;

        String startUrl;

        public CrawlerRunnable(MultithreadCrawler crawler, String startUrl) {
            this.crawler = crawler;
            this.startUrl = startUrl;
        }

        private Document getHTML(String url) {
            try {
                return Jsoup.connect(url).get();
            } catch (Exception ex) {
                System.out.println("Error while reading url: " + url);
                return null;
            }
        }

        @Override
        public void run() {
            visited.add(startUrl);
            Document doc = getHTML(startUrl);
            if (doc != null) {
                Elements pTags = doc.select("p");
                String[] line = new String[pTags.size() + 1];
                line[0] = startUrl.substring(startUrl.lastIndexOf("/"));
                int index = 1;
                for (Element p : pTags) {
                    line[index++] = p.text();
                }
                lines.add(line);

                Elements links = doc.select("a[href]");
                for (Element link : links) {
                    String absLink = link.attr("abs:href");
                    synchronized (visited) {
                        if (absLink.startsWith(BASE_URL) && !visited.contains(absLink)) {
                            visited.add(absLink);
                            crawler.executorService.submit(new CrawlerRunnable(crawler, absLink));
                        }
                    }
                }
            }
        }
    }
}
