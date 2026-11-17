package com.pixelmetrics.analysis;

import com.pixelmetrics.model.AnalysisReport;
import com.pixelmetrics.model.AnalysisReport.EsrbPerformance;
import com.pixelmetrics.model.AnalysisReport.PublisherPerformance;
import com.pixelmetrics.model.GameRecord;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

public class SalesAndRatingsAnalyzer {

    public AnalysisReport generateFullReport(List<GameRecord> records, int topNPublishers, int minGamesPerPublisher) {
        Map<String, Double> genreSales = computeGlobalSalesByGenre(records);
        Map<String, Double> regionalSales = computeRegionalSalesBreakdown(records);
        List<PublisherPerformance> topPublishers = computeTopPublishers(records, topNPublishers, minGamesPerPublisher);
        double criticCorr = computeCriticScoreSalesCorrelation(records);
        double userCorr = computeUserScoreSalesCorrelation(records);
        List<EsrbPerformance> esrbStats = computeEsrbPerformance(records);

        return new AnalysisReport(
                genreSales,
                regionalSales,
                topPublishers,
                criticCorr,
                userCorr,
                esrbStats
        );
    }

    /** RQ1: Total global sales grouped by genre, sorted descending. */
    public Map<String, Double> computeGlobalSalesByGenre(List<GameRecord> records) {
        Map<String, Double> totals = new LinkedHashMap<>();
        for (GameRecord g : records) {
            totals.merge(g.getGenre(), g.getGlobalSales(), Double::sum);
        }

        return totals.entrySet().stream()
                .sorted(Map.Entry.<String, Double>comparingByValue().reversed())
                .collect(Collectors.toMap(
                        Map.Entry::getKey,
                        Map.Entry::getValue,
                        (a, b) -> a,
                        LinkedHashMap::new
                ));
    }

    /** RQ2: Total sales across North America, Europe, Japan, and Other regions. */
    public Map<String, Double> computeRegionalSalesBreakdown(List<GameRecord> records) {
        double na = 0.0, eu = 0.0, jp = 0.0, other = 0.0;
        for (GameRecord g : records) {
            na += g.getNaSales();
            eu += g.getEuSales();
            jp += g.getJpSales();
            other += g.getOtherSales();
        }

        Map<String, Double> breakdown = new LinkedHashMap<>();
        breakdown.put("North America", na);
        breakdown.put("Europe", eu);
        breakdown.put("Japan", jp);
        breakdown.put("Other", other);
        return breakdown;
    }

    /** RQ3: Top publishers by total global sales (filtering out small publishers below minGames). */
    public List<PublisherPerformance> computeTopPublishers(List<GameRecord> records, int topN, int minGames) {
        Map<String, List<GameRecord>> byPublisher = records.stream()
                .filter(g -> !"Unknown".equalsIgnoreCase(g.getPublisher()))
                .collect(Collectors.groupingBy(GameRecord::getPublisher));

        List<PublisherPerformance> results = new ArrayList<>();
        for (Map.Entry<String, List<GameRecord>> entry : byPublisher.entrySet()) {
            List<GameRecord> games = entry.getValue();
            if (games.size() < minGames) {
                continue;
            }

            double totalSales = games.stream().mapToDouble(GameRecord::getGlobalSales).sum();
            List<Double> criticScores = games.stream()
                    .filter(GameRecord::hasCriticScore)
                    .map(GameRecord::getCriticScore)
                    .collect(Collectors.toList());

            Double avgCritic = criticScores.isEmpty() ? null : StatsHelper.mean(criticScores);
            results.add(new PublisherPerformance(entry.getKey(), games.size(), totalSales, avgCritic));
        }

        results.sort(Comparator.comparingDouble(PublisherPerformance::getTotalGlobalSales).reversed());
        return results.stream().limit(topN).collect(Collectors.toList());
    }

    /** RQ4a: Pearson correlation between Critic Score and Global Sales. */
    public double computeCriticScoreSalesCorrelation(List<GameRecord> records) {
        List<Double> scores = new ArrayList<>();
        List<Double> sales = new ArrayList<>();
        for (GameRecord g : records) {
            if (g.hasCriticScore()) {
                scores.add(g.getCriticScore());
                sales.add(g.getGlobalSales());
            }
        }
        return StatsHelper.pearsonCorrelation(scores, sales);
    }

    /** RQ4b: Pearson correlation between User Score and Global Sales. */
    public double computeUserScoreSalesCorrelation(List<GameRecord> records) {
        List<Double> scores = new ArrayList<>();
        List<Double> sales = new ArrayList<>();
        for (GameRecord g : records) {
            if (g.hasUserScore()) {
                scores.add(g.getUserScore());
                sales.add(g.getGlobalSales());
            }
        }
        return StatsHelper.pearsonCorrelation(scores, sales);
    }

    /** RQ5: Average global sales and critic score by ESRB rating, sorted by average sales descending. */
    public List<EsrbPerformance> computeEsrbPerformance(List<GameRecord> records) {
        Map<String, List<GameRecord>> byRating = records.stream()
                .collect(Collectors.groupingBy(GameRecord::getRating));

        List<EsrbPerformance> results = new ArrayList<>();
        for (Map.Entry<String, List<GameRecord>> entry : byRating.entrySet()) {
            List<GameRecord> games = entry.getValue();
            double avgSales = games.stream().mapToDouble(GameRecord::getGlobalSales).average().orElse(0.0);

            List<Double> criticScores = games.stream()
                    .filter(GameRecord::hasCriticScore)
                    .map(GameRecord::getCriticScore)
                    .collect(Collectors.toList());

            Double avgCritic = criticScores.isEmpty() ? null : StatsHelper.mean(criticScores);
            results.add(new EsrbPerformance(entry.getKey(), games.size(), avgSales, avgCritic));
        }

        results.sort(Comparator.comparingDouble(EsrbPerformance::getAverageGlobalSales).reversed());
        return results;
    }
}
