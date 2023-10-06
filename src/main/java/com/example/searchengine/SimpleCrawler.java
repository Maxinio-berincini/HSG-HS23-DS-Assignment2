package com.example.searchengine;

import com.opencsv.CSVWriter;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import java.io.FileWriter;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.Queue;
import java.util.Set;

public class SimpleCrawler extends Crawler {

    private static final String BASE_URL = "https://api.interactions.ics.unisg.ch/hypermedia-environment/";

    protected SimpleCrawler(String indexFileName) {
        super(indexFileName);
    }

    public void crawl(String startUrl) {
        try {
            long startTime = System.nanoTime();
            Set<String[]> lines = explore(startUrl, new HashSet<>(), new HashSet<>());
            try (FileWriter fileWriter = new FileWriter(indexFileName);
                 CSVWriter writer = new CSVWriter(fileWriter, ',', CSVWriter.NO_QUOTE_CHARACTER, ' ', "\r\n")) {
                for (String[] line : lines) {
                    writer.writeNext(line);
                }
            }
            long endTime = System.nanoTime();
            int duration = (int) ((endTime - startTime) / 1000000000);
            System.out.println("duration simple crawler: " + duration + "s");
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private Document getHTML(String url) {
        try {
            return Jsoup.connect(url).get();
        } catch (Exception ex) {
            System.out.println("Error while reading url: " + url);
            return null;
        }
    }

    public Set<String[]> explore(String startUrl, Set<String[]> lines, Set<String> visited) {
        Queue<String> queue = new LinkedList<>();
        queue.add(startUrl);

        while (!queue.isEmpty()) {
            String currentUrl = queue.poll();

            if (visited.contains(currentUrl)) continue;
            visited.add(currentUrl);

            Document doc = getHTML(currentUrl);
            if (doc == null) continue;

            Elements pTags = doc.select("p");
            String[] line = new String[pTags.size() + 1];
            line[0] = currentUrl.substring(currentUrl.lastIndexOf("/"));
            int index = 1;
            for (Element p : pTags) {
                line[index++] = p.text();
            }
            lines.add(line);

            Elements links = doc.select("a[href]");
            for (Element link : links) {
                String absLink = link.attr("abs:href");
                if (absLink.startsWith(BASE_URL) && !visited.contains(absLink))
                    queue.add(absLink);
            }
        }

        return lines;
    }
}
