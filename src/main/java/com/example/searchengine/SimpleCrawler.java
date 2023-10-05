package com.example.searchengine;

import com.opencsv.CSVWriter;

import java.io.*;
import java.net.URL;
import java.net.URLConnection;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class SimpleCrawler extends Crawler {


    protected SimpleCrawler(String indexFileName) {
        super(indexFileName);
    }

    public void crawl(String startUrl){
        try {
            int duration = 0;
            long startTime = System.nanoTime();
            Set<String[]> lines = explore(startUrl, new HashSet<>(), new HashSet<>());
            FileWriter fileWriter = new FileWriter(indexFileName);
            CSVWriter writer = new CSVWriter(fileWriter,',', CSVWriter.NO_QUOTE_CHARACTER,' ',"\r\n"); //TODO: macOS and Linux users should change Line to "\n".
            for (String[] line : lines) {
                writer.writeNext(line);
            }
            writer.close();
            long endTime = System.nanoTime();
            duration = (int) ((endTime - startTime)/1000000000);
            System.out.println("duration simple crawler: "+duration + "s");
        } catch (Exception e){
            e.printStackTrace();
        }

    }

    private String getHTML(String url) {
        String content = "";
        System.out.println(url);
        try {
            URLConnection connection =  new URL(url).openConnection();
            Scanner scanner = new Scanner(connection.getInputStream());
            scanner.useDelimiter("\\Z");
            content = scanner.next();
            scanner.close();
        }catch ( Exception ex ) {
            ex.printStackTrace();
        }
        return content;
    }

    /**
     *
     * @param startUrl the url where the crawling operation starts
     * @param lines stores the lines to print on the index file
     * @param visited stores the urls that the program has already visited
     * @return the set of lines to print on the index file
     */
    public Set<String[]> explore(String startUrl, Set<String[]> lines, Set<String> visited){
        if (visited.contains(startUrl)) return lines;
        visited.add(startUrl);
        String html = getHTML(startUrl);
        // count number of <p> tags
        List<String> pTags = new ArrayList<>();
        Pattern p = Pattern.compile("<p>(.*?)</p>", Pattern.DOTALL);
        Matcher m = p.matcher(html);
        while (m.find()){
            pTags.add(m.group(1).trim());
        }

        String[] line = new String[pTags.size() + 1];
        // only get the last part of the url
        line[0] = startUrl.substring(startUrl.lastIndexOf("/"));

        for (int i = 0; i < pTags.size(); i++) {
            line[i+1] = pTags.get(i);
        }

        // System.out.println(Arrays.toString(line));

        lines.add(line);

        // find all the links in the page. Example <a href="609ada9fcd0d4297">609ada9fcd0d4297</a>
        p = Pattern.compile("<a\\s+(?:[^>]*?\\s+)?href=([\"'])(.*?)\\1", Pattern.DOTALL);
        m = p.matcher(html);
        while (m.find()){
            String link = m.group(2);

            if (!link.startsWith("http"))
                link = startUrl.substring(0, startUrl.lastIndexOf("/")) + "/" + link;

            if (link.contains("https://api.interactions.ics.unisg.ch/hypermedia-environment/") == false)
                continue;

            if (link.startsWith("http")) {
                // explore link and merge the result with lines
                Set<String[]> newLines = explore(link, lines, visited);
                // merge lines and newLines, but avoid duplicates
                Set<String[]> mergedLines = new HashSet<>();
                mergedLines.addAll(lines);
                mergedLines.addAll(newLines);
                lines = mergedLines;
            }
        }

        return lines;
    }

}
