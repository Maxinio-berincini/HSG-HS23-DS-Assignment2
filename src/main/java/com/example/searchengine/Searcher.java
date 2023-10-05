package com.example.searchengine;

import com.opencsv.CSVReader;
import org.springframework.stereotype.Component;

import java.io.FileReader;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Optional;

@Component
public class Searcher {
    /**
     *
     * @param keyword to search
     * @param flippedIndexFileName the file where the search is performed.
     * @return the list of urls
     */
    public List<String> search(String keyword, String flippedIndexFileName){
        long startTime = System.nanoTime();
        List<String> urls = new ArrayList<>();

        try {
            List<String[]> csvLines = new CSVReader(new FileReader(flippedIndexFileName)).readAll();
            Optional<String[]> foundArrayOpt = IndexFlipper.findArrayByFirstElement(new HashSet<>(csvLines), keyword);

            if (foundArrayOpt.isPresent()) {
                String[] foundArray = foundArrayOpt.get();
                for (int i = 1; i < foundArray.length; i++) {
                    urls.add("https://api.interactions.ics.unisg.ch/hypermedia-environment" + foundArray[i]);
                }
            }
        } catch (Exception e){
            e.printStackTrace();
        }
        long endTime = System.nanoTime();
        int duration = (int) ((endTime - startTime)/1000000);
        System.out.println("duration searcher flipped: "+duration + "ms");
        return urls;
    }


}
