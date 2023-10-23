package com.example.searchengine;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;


import java.io.IOException;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public abstract class Crawler {

     final String indexFileName;
    private Path path;

    private String baseUrl = "https://api.interactions.ics.unisg.ch/hypermedia-environment/";

    /**
     *
     * @param indexFileName the name of the file that is used as index.
     */
    protected Crawler(String indexFileName) {
        this.indexFileName = indexFileName;
        this.path = Path.of(indexFileName);
    }

    /**
     *
     * @param url the url where the crawling starts
     */
    public abstract void crawl(String url);

    public  List<List<String>> getInfo(String urlString){
        List<String> keywords = new ArrayList<>();
        List<String> hyperlinks = new ArrayList<>();
        List<List<String>> returnList = new ArrayList<>();
        try {
            URL url = new URL(urlString);
            Elements elements; //TODO: initialize elements based on the webpage at the given url.
            //TODO: Use elements to put the keywords in the webpage in the list keywords.
            //TODO: Use elements to the hyperlinks to other pages in the environment in the list hyperlinks.
        } catch (Exception e){
            e.printStackTrace();
        }
        returnList.add(keywords);
        returnList.add(hyperlinks);
        return returnList;
    }

    public void deleteUrlFromIndex(String urlToDelete) {

        List<String> updatedLines = new ArrayList<>();
        try {
            List<String> lines = Files.readAllLines(path);
            for (String line : lines) {
                if (!line.startsWith(urlToDelete + ",")) {
                    updatedLines.add(line);
                }
            }
            Files.write(path, updatedLines);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    public boolean updateUrlInIndex(String urlToUpdate, List<String> newKeywords) {
        List<String> updatedLines = new ArrayList<>();
        boolean updated = false;

        try {
            List<String> lines = Files.readAllLines(path);
            for (String line : lines) {
                if (line.startsWith(urlToUpdate + ",")) {
                    updatedLines.add(urlToUpdate + "," + String.join(",", newKeywords));
                    updated = true;
                } else {
                    updatedLines.add(line);
                }
            }
            Files.write(path, updatedLines);

            return updated;

        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    public List<String> getKeywordsForUrl(String urlToFind) {
        try {
            List<String> lines = Files.readAllLines(path);
            for (String line : lines) {
                String[] parts = line.split(",");
                String url = parts[0];
                if (url.equals(urlToFind)) {
                    return Arrays.asList(parts).subList(1, parts.length);
                }
            }
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
        return null;
    }



}
