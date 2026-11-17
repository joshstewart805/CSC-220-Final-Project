package com.pixelmetrics.model;

import java.util.Collections;
import java.util.List;
import java.util.Map;

public class AnalysisReport {

    public static class PublisherPerformance {
        private final String publisher;
        private final int gameCount;
        private final double totalGlobalSales;
        private final Double averageCriticScore; // Nullable if no rated games

        public PublisherPerformance(String publisher, int gameCount, double totalGlobalSales, Double averageCriticScore) {
            this.publisher = publisher;
            this.gameCount = gameCount;
            this.totalGlobalSales = totalGlobalSales;
            this.averageCriticScore = averageCriticScore;
        }

        public String getPublisher() { return publisher; }
        public int getGameCount() { return gameCount; }
        public double getTotalGlobalSales() { return totalGlobalSales; }
        public Double getAverageCriticScore() { return averageCriticScore; }
    }

    public static class EsrbPerformance {
        private final String rating;
        private final int gameCount;
        private final double averageGlobalSales;
        private final Double averageCriticScore;

        public EsrbPerformance(String rating, int gameCount, double averageGlobalSales, Double averageCriticScore) {
            this.rating = rating;
            this.gameCount = gameCount;
            this.averageGlobalSales = averageGlobalSales;
            this.averageCriticScore = averageCriticScore;
        }

        public String getRating() { return rating; }
        public int getGameCount() { return gameCount; }
        public double getAverageGlobalSales() { return averageGlobalSales; }
        public Double getAverageCriticScore() { return averageCriticScore; }
    }

    private final Map<String, Double> globalSalesByGenre;
    private final Map<String, Double> regionalSalesBreakdown;
    private final List<PublisherPerformance> topPublishers;
    private final double criticSalesCorrelation;
    private final double userSalesCorrelation;
    private final List<EsrbPerformance> esrbBreakdown;

    public AnalysisReport(
            Map<String, Double> globalSalesByGenre,
            Map<String, Double> regionalSalesBreakdown,
            List<PublisherPerformance> topPublishers,
            double criticSalesCorrelation,
            double userSalesCorrelation,
            List<EsrbPerformance> esrbBreakdown) {
        this.globalSalesByGenre = Collections.unmodifiableMap(globalSalesByGenre);
        this.regionalSalesBreakdown = Collections.unmodifiableMap(regionalSalesBreakdown);
        this.topPublishers = Collections.unmodifiableList(topPublishers);
        this.criticSalesCorrelation = criticSalesCorrelation;
        this.userSalesCorrelation = userSalesCorrelation;
        this.esrbBreakdown = Collections.unmodifiableList(esrbBreakdown);
    }

    public Map<String, Double> getGlobalSalesByGenre() { return globalSalesByGenre; }
    public Map<String, Double> getRegionalSalesBreakdown() { return regionalSalesBreakdown; }
    public List<PublisherPerformance> getTopPublishers() { return topPublishers; }
    public double getCriticSalesCorrelation() { return criticSalesCorrelation; }
    public double getUserSalesCorrelation() { return userSalesCorrelation; }
    public List<EsrbPerformance> getEsrbBreakdown() { return esrbBreakdown; }
}
