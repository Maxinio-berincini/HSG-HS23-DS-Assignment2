package com.example.searchengine;

import java.util.Collections;
import java.util.HashSet;
import java.util.Set;

public abstract class TestBase {

    public final String indexFileName = "./src/main/resources/index.csv";

    public final String flippedIndexFileName = "./src/main/resources/index_flipped.csv";

    public  final String startUrl = "https://api.interactions.ics.unisg.ch/hypermedia-environment/cc2247b79ac48af0";


    public Set<String> createSet(String... values){
        Set<String> valueSet = new HashSet<>();
        Collections.addAll(valueSet, values);
        return valueSet;
    }
}
