package com.pixelmetrics;

import com.pixelmetrics.io.CsvDatasetLoader;
import com.pixelmetrics.io.CsvDatasetLoader.LoadResult;

public class Main {
    public static void main(String[] args) {
        try {
            CsvDatasetLoader loader = new CsvDatasetLoader();
            // Match the exact filename inside src/main/resources/data/
            LoadResult result = loader.loadFromResource("/data/Video_Game_Sales_as_of_Jan_2017.csv");

            System.out.println(result.getSummary());
            System.out.println("Missing counts: " + result.getSummary().getMissingValuesByColumn());
            System.out.println("First 3 games:");
            result.getRecords().stream().limit(3).forEach(System.out::println);

        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}