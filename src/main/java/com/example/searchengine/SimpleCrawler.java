package com.example.searchengine;

import com.opencsv.CSVWriter;

import java.io.FileWriter;
import java.net.URL;
import java.net.URLConnection;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class SimpleCrawler extends Crawler {


    protected SimpleCrawler(String indexFileName) {
        super(indexFileName);
    }

    public void crawl(String startUrl) {
        try {
            long startTime = System.nanoTime();
            Set<String[]> lines = explore(startUrl, new HashSet<>(), new HashSet<>());
            FileWriter fileWriter = new FileWriter(indexFileName);
            CSVWriter writer = new CSVWriter(fileWriter, ',', CSVWriter.NO_QUOTE_CHARACTER, ' ', "\r\n"); //TODO: macOS and Linux users should change Line to "\n".
            for (String[] line : lines) {
                writer.writeNext(line);
            }
            writer.close();
            long endTime = System.nanoTime();
            int duration = (int) ((endTime - startTime) / 1000000000);
            System.out.println("duration simple crawler: " + duration + "s");
        } catch (Exception e) {
            e.printStackTrace();
        }

    }

    private String getHTML(String url) {
        String content = "";
        try {
            URLConnection connection = new URL(url).openConnection();
            Scanner scanner = new Scanner(connection.getInputStream());
            scanner.useDelimiter("\\Z");
            content = scanner.next();
            scanner.close();
        } catch (Exception ex) {
            ex.printStackTrace();
        }
        return content;
    }

    /**
     * @param startUrl the url where the crawling operation starts
     * @param lines    stores the lines to print on the index file
     * @param visited  stores the urls that the program has already visited
     * @return the set of lines to print on the index file
     */
    public Set<String[]> explore(String startUrl, Set<String[]> lines, Set<String> visited) {
        // Create a queue for BFS
        LinkedList<String> queue = new LinkedList<>();

        // Add the starting URL to the queue
        queue.add(startUrl);

        // BFS algorithm
        while (!queue.isEmpty()) {
            String url = queue.poll();

            // Check if URL has already been visited
            if (visited.contains(url)) {
                continue; // skip the already visited URL
            }
            visited.add(url);

            // Retrieve and process the HTML content
            String html = getHTML(url);

            // Count number of <p> tags and gather their contents
            List<String> pTags = new ArrayList<>();
            Pattern p = Pattern.compile("<p>(.*?)</p>", Pattern.DOTALL);
            Matcher m = p.matcher(html);
            while (m.find()) {
                String content = m.group(1).trim();
                content = content.replaceAll("<a [^>]*>([^<]*)</a>", "$1");
                pTags.add(content);
            }

            // Prepare a line for the current page
            String[] line = new String[pTags.size() + 1];
            line[0] = url.substring(url.lastIndexOf("/")); // Only get the last part of the URL

            for (int i = 0; i < pTags.size(); i++) {
                line[i + 1] = pTags.get(i);
            }

            // Add the prepared line to the set of lines
            lines.add(line);

            // Find all the links in the page
            p = Pattern.compile("<a\\s+(?:[^>]*?\\s+)?href=([\"'])(.*?)\\1", Pattern.DOTALL);
            m = p.matcher(html);
            while (m.find()) {
                String link = m.group(2);

                // Prepare absolute URL if necessary
                if (!link.startsWith("http")) {
                    link = url.substring(0, url.lastIndexOf("/")) + "/" + link;
                }

                // Skip unwanted URLs
                if (!link.contains("https://api.interactions.ics.unisg.ch/hypermedia-environment/")) {
                    continue;
                }

                // If the link is a proper URL and hasn't been visited yet, add it to the queue
                if (link.startsWith("http") && !visited.contains(link)) {
                    queue.add(link);
                }
            }
        }

        // Return the set of lines containing data gathered during the crawl
        return lines;
    }
}
