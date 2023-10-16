package com.example.searchengine;

import com.opencsv.CSVReader;
import com.opencsv.CSVWriter;
import org.springframework.stereotype.Component;

import java.io.FileReader;
import java.io.FileWriter;
import java.util.*;
@Component
public class IndexFlipper {

    public void flipIndex(String indexFileName, String flippedIndexFileName){
        try {
            CSVReader csvReader = new CSVReader(new FileReader(indexFileName));
            List<String[]> csvLines = csvReader.readAll();
            Set<String[]> lines = new HashSet<>();

            for (String[] line : csvLines) {
                String link = line[0];
                for (int i = 1; i < line.length; i++) {
                    Optional<String[]> foundArrayOpt = findArrayByFirstElement(lines, line[i]);

                    // If found, extend array by one and add link
                    if (foundArrayOpt.isPresent()) {
                        String[] foundArray = foundArrayOpt.get();
                        String[] newArray = Arrays.copyOf(foundArray, foundArray.length + 1);
                        newArray[newArray.length - 1] = link;
                        lines.remove(foundArray);  // remove the old array from the set
                        lines.add(newArray);      // add the updated array to the set
                    } else {
                        // If not found, create new array and add link
                        String[] newLine = new String[2];
                        newLine[0] = line[i];
                        newLine[1] = link;
                        lines.add(newLine);
                    }
                }
            }

            CSVWriter writer = new CSVWriter(new FileWriter(flippedIndexFileName),',', CSVWriter.NO_QUOTE_CHARACTER,' ',"\r\n");
            for (String[] line : lines) {
                writer.writeNext(line);
            }
            writer.close();

        } catch (Exception e){
            e.printStackTrace();
        }

    }

    public static Optional<String[]> findArrayByFirstElement(Set<String[]> set, String element) {
        return set.stream().filter(array -> array[0].equals(element)).findFirst();
    }

}
