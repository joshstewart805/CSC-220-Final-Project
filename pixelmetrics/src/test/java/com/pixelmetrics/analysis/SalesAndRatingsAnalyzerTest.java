package com.pixelmetrics.analysis;

import com.pixelmetrics.model.AnalysisReport;
import com.pixelmetrics.model.AnalysisReport.EsrbPerformance;
import com.pixelmetrics.model.AnalysisReport.PublisherPerformance;
import com.pixelmetrics.model.GameRecord;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

class SalesAndRatingsAnalyzerTest {

    private SalesAndRatingsAnalyzer analyzer;
    private List<GameRecord> sampleRecords;

    @BeforeEach
    void setUp() {
        analyzer = new SalesAndRatingsAnalyzer();

        // Controlled dataset of 5 games with known values for exact verification
        sampleRecords = Arrays.asList(
                new GameRecord("Alpha Action", "PS4", 2015, "Action", "Nintendo",
                        4.0, 3.0, 2.0, 1.0, 10.0, 90.0, 50, 9.0, 200, "DevA", "E"),
                new GameRecord("Beta Action", "PS4", 2016, "Action", "Nintendo",
                        2.0, 2.0, 1.0, 0.0, 5.0, 80.0, 40, 8.0, 150, "DevA", "E"),
                new GameRecord("Gamma RPG", "Switch", 2017, "Role-Playing", "Square Enix",
                        3.0, 2.0, 4.0, 1.0, 10.0, 85.0, 30, 8.5, 100, "DevB", "T"),
                new GameRecord("Delta Sports", "Xbox", 2018, "Sports", "EA",
                        2.0, 1.0, 0.0, 1.0, 4.0, 70.0, 20, 7.0, 80, "DevC", "E10+"),
                // Game with missing critic/user scores and Unknown publisher
                new GameRecord("Indie Mystery", "PC", 2019, "Adventure", "Unknown",
                        0.5, 0.3, 0.1, 0.1, 1.0, null, null, null, null, "Unknown", "M")
        );
    }

    //Verifies that global sales are accurately summed by game genre and sorted in descending order from highest-grossing to lowest-grossing.
    @Test
    void testComputeGlobalSalesByGenre() {
        Map<String, Double> genreSales = analyzer.computeGlobalSalesByGenre(sampleRecords);

        assertEquals(4, genreSales.size());
        assertEquals(15.0, genreSales.get("Action"), 0.001);
        assertEquals(10.0, genreSales.get("Role-Playing"), 0.001);
        assertEquals(4.0, genreSales.get("Sports"), 0.001);
        assertEquals(1.0, genreSales.get("Adventure"), 0.001);

        // Verify descending order (Action first, Adventure last)
        String firstKey = genreSales.keySet().iterator().next();
        assertEquals("Action", firstKey);
    }

    //Checks that total game sales are properly aggregated across each geographical region (North America, Europe, Japan, and Other).
    @Test
    void testComputeRegionalSalesBreakdown() {
        Map<String, Double> regional = analyzer.computeRegionalSalesBreakdown(sampleRecords);

        assertEquals(11.5, regional.get("North America"), 0.001);
        assertEquals(8.3, regional.get("Europe"), 0.001);
        assertEquals(7.1, regional.get("Japan"), 0.001);
        assertEquals(3.1, regional.get("Other"), 0.001);
    }

    //Ensures that publisher rankings accurately calculate game counts, total sales, and average critic scores while filtering out "Unknown" publishers and any publisher below the minimum game threshold.
    @Test
    void testComputeTopPublishersFiltersMinGamesAndUnknown() {
        // Require at least 2 games: only Nintendo qualifies (and Unknown is excluded)
        List<PublisherPerformance> top = analyzer.computeTopPublishers(sampleRecords, 5, 2);

        assertEquals(1, top.size());
        PublisherPerformance nintendo = top.get(0);
        assertEquals("Nintendo", nintendo.getPublisher());
        assertEquals(2, nintendo.getGameCount());
        assertEquals(15.0, nintendo.getTotalGlobalSales(), 0.001);
        assertEquals(85.0, nintendo.getAverageCriticScore(), 0.001);
    }

    //Confirms that the critic and user score-to-sales correlation calculations safely skip records with null ratings and accurately detect strong positive correlations.
    @Test
    void testCorrelationsIgnoreNullScores() {
        double criticCorr = analyzer.computeCriticScoreSalesCorrelation(sampleRecords);
        double userCorr = analyzer.computeUserScoreSalesCorrelation(sampleRecords);

        // Higher scores in sampleRecords correspond to higher sales -> strong positive correlation
        assertTrue(criticCorr > 0.8, "Expected strong positive critic correlation, got: " + criticCorr);
        assertTrue(userCorr > 0.8, "Expected strong positive user correlation, got: " + userCorr);
    }

    //Validates that performance metrics are grouped and sorted by ESRB rating based on average global sales while properly handling ratings that have null critic scores.
    @Test
    void testComputeEsrbPerformance() {
        List<EsrbPerformance> esrb = analyzer.computeEsrbPerformance(sampleRecords);

        assertEquals(4, esrb.size());
        // "T" has 1 game at 10.0M -> highest average sales
        assertEquals("T", esrb.get(0).getRating());
        assertEquals(10.0, esrb.get(0).getAverageGlobalSales(), 0.001);

        // "E" has 2 games averaging 7.5M and 85.0 critic score
        assertEquals("E", esrb.get(1).getRating());
        assertEquals(7.5, esrb.get(1).getAverageGlobalSales(), 0.001);
        assertEquals(85.0, esrb.get(1).getAverageCriticScore(), 0.001);

        // "M" has 1 game with null critic score
        EsrbPerformance mature = esrb.stream()
                .filter(e -> "M".equals(e.getRating()))
                .findFirst()
                .orElseThrow();
        assertNull(mature.getAverageCriticScore());
    }

    //Verifies that passing an empty dataset into the full report generator gracefully returns empty collections and zeroed correlation values without throwing exceptions.
    @Test
    void testGenerateFullReportHandlesEmptyDataset() {
        AnalysisReport emptyReport = analyzer.generateFullReport(Collections.emptyList(), 5, 1);

        assertTrue(emptyReport.getGlobalSalesByGenre().isEmpty());
        assertTrue(emptyReport.getTopPublishers().isEmpty());
        assertTrue(emptyReport.getEsrbBreakdown().isEmpty());
        assertEquals(0.0, emptyReport.getCriticSalesCorrelation(), 0.001);
        assertEquals(0.0, emptyReport.getUserSalesCorrelation(), 0.001);
    }
}
