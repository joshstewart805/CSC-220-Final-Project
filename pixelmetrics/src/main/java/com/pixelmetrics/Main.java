package com.pixelmetrics;

import com.pixelmetrics.analysis.SalesAndRatingsAnalyzer;
import com.pixelmetrics.io.CsvDatasetLoader;
import com.pixelmetrics.io.CsvDatasetLoader.LoadResult;
import com.pixelmetrics.model.AnalysisReport;

public class Main {
    public static void main(String[] args) {
        try {
            CsvDatasetLoader loader = new CsvDatasetLoader();
            LoadResult result = loader.loadFromResource("/data/Video_Game_Sales_as_of_Jan_2017.csv");

            SalesAndRatingsAnalyzer analyzer = new SalesAndRatingsAnalyzer();
            AnalysisReport report = analyzer.generateFullReport(result.getRecords(), 5, 10);

            System.out.println("=== Top 3 Genres ===");
            report.getGlobalSalesByGenre().entrySet().stream().limit(3)
                  .forEach(e -> System.out.printf("%s: %.2fM%n", e.getKey(), e.getValue()));

            System.out.println("\n=== Regional Sales ===");
            report.getRegionalSalesBreakdown()
                  .forEach((region, sales) -> System.out.printf("%s: %.2fM%n", region, sales));

            System.out.printf("%nCritic-Sales Correlation (r): %.4f%n", report.getCriticSalesCorrelation());
            System.out.printf("User-Sales Correlation (r):   %.4f%n", report.getUserSalesCorrelation());

        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
