package com.pixelmetrics.analysis;

import java.util.List;

public final class StatsHelper {

    private StatsHelper() {
        // Utility class
    }

    public static double mean(List<Double> values) {
        if (values == null || values.isEmpty()) {
            return 0.0;
        }
        double sum = 0.0;
        for (double v : values) {
            sum += v;
        }
        return sum / values.size();
    }

    /**
     * Computes Pearson correlation coefficient r in [-1.0, 1.0].
     * Returns 0.0 if lists have fewer than 2 elements or zero variance.
     */
    public static double pearsonCorrelation(List<Double> xs, List<Double> ys) {
        if (xs == null || ys == null || xs.size() != ys.size() || xs.size() < 2) {
            return 0.0;
        }

        double meanX = mean(xs);
        double meanY = mean(ys);

        double numerator = 0.0;
        double sumSqX = 0.0;
        double sumSqY = 0.0;

        for (int i = 0; i < xs.size(); i++) {
            double dx = xs.get(i) - meanX;
            double dy = ys.get(i) - meanY;
            numerator += dx * dy;
            sumSqX += dx * dx;
            sumSqY += dy * dy;
        }

        double denominator = Math.sqrt(sumSqX * sumSqY);
        if (denominator == 0.0) {
            return 0.0;
        }
        return numerator / denominator;
    }
}
