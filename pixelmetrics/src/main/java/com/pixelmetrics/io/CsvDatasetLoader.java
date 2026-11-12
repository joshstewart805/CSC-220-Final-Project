package com.pixelmetrics.io;

import com.pixelmetrics.model.DatasetSummary;
import com.pixelmetrics.model.GameRecord;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

public class CsvDatasetLoader {

    public static class LoadResult {
        private final List<GameRecord> records;
        private final DatasetSummary summary;

        public LoadResult(List<GameRecord> records, DatasetSummary summary) {
            this.records = records;
            this.summary = summary;
        }

        public List<GameRecord> getRecords() { return records; }
        public DatasetSummary getSummary() { return summary; }
    }

    public LoadResult loadFromResource(String resourcePath) throws IOException {
        InputStream inputStream = getClass().getResourceAsStream(resourcePath);
        if (inputStream == null) {
            throw new IOException("Dataset resource not found at: " + resourcePath);
        }

        List<GameRecord> records = new ArrayList<>();
        Set<GameRecord> seenRecords = new HashSet<>();

        int totalRows = 0;
        int skippedRows = 0;
        int duplicateRows = 0;
        int minYear = Integer.MAX_VALUE;
        int maxYear = Integer.MIN_VALUE;

        Map<String, Integer> missingCounts = new LinkedHashMap<>();
        missingCounts.put("Year_of_Release", 0);
        missingCounts.put("Genre", 0);
        missingCounts.put("Publisher", 0);
        missingCounts.put("Critic_Score", 0);
        missingCounts.put("User_Score", 0);
        missingCounts.put("Developer", 0);
        missingCounts.put("Rating", 0);

        try (BufferedReader reader = new BufferedReader(new InputStreamReader(inputStream, StandardCharsets.UTF_8))) {
            String headerLine = reader.readLine();
            if (headerLine == null) {
                throw new IOException("CSV file is empty.");
            }

            // Map column names (lowercase) to their index position
            List<String> headers = parseCsvLine(headerLine);
            Map<String, Integer> colIndex = new LinkedHashMap<>();
            for (int i = 0; i < headers.size(); i++) {
                colIndex.put(headers.get(i).trim().toLowerCase(), i);
            }

            String line;
            while ((line = reader.readLine()) != null) {
                if (line.trim().isEmpty()) {
                    continue;
                }
                totalRows++;

                List<String> cols = parseCsvLine(line);
                if (cols.size() < 5) {
                    skippedRows++;
                    continue;
                }

                try {
                    String name = cleanString(getCol(cols, colIndex, "name"), "Unknown");
                    String platform = cleanString(getCol(cols, colIndex, "platform"), "Unknown");

                    Integer year = parseNullableInt(getCol(cols, colIndex, "year_of_release"));
                    if (year == null) {
                        increment(missingCounts, "Year_of_Release");
                    } else {
                        minYear = Math.min(minYear, year);
                        maxYear = Math.max(maxYear, year);
                    }

                    String genre = cleanString(getCol(cols, colIndex, "genre"), "Unknown");
                    if ("Unknown".equals(genre)) increment(missingCounts, "Genre");

                    String publisher = cleanString(getCol(cols, colIndex, "publisher"), "Unknown");
                    if ("Unknown".equals(publisher)) increment(missingCounts, "Publisher");

                    double naSales = parseDoubleOrDefault(getCol(cols, colIndex, "na_sales"), 0.0);
                    double euSales = parseDoubleOrDefault(getCol(cols, colIndex, "eu_sales"), 0.0);
                    double jpSales = parseDoubleOrDefault(getCol(cols, colIndex, "jp_sales"), 0.0);
                    double otherSales = parseDoubleOrDefault(getCol(cols, colIndex, "other_sales"), 0.0);
                    double globalSales = parseDoubleOrDefault(getCol(cols, colIndex, "global_sales"), 0.0);

                    Double criticScore = parseNullableDouble(getCol(cols, colIndex, "critic_score"));
                    if (criticScore == null) increment(missingCounts, "Critic_Score");

                    Integer criticCount = parseNullableInt(getCol(cols, colIndex, "critic_count"));

                    Double userScore = parseNullableDouble(getCol(cols, colIndex, "user_score"));
                    if (userScore == null) increment(missingCounts, "User_Score");

                    Integer userCount = parseNullableInt(getCol(cols, colIndex, "user_count"));

                    String developer = cleanString(getCol(cols, colIndex, "developer"), "Unknown");
                    if ("Unknown".equals(developer)) increment(missingCounts, "Developer");

                    String rating = cleanString(getCol(cols, colIndex, "rating"), "Unrated");
                    if ("Unrated".equals(rating)) increment(missingCounts, "Rating");

                    GameRecord record = new GameRecord(
                            name, platform, year, genre, publisher,
                            naSales, euSales, jpSales, otherSales, globalSales,
                            criticScore, criticCount, userScore, userCount,
                            developer, rating
                    );

                    if (!seenRecords.add(record)) {
                        duplicateRows++;
                    }
                    records.add(record);

                } catch (Exception e) {
                    skippedRows++;
                }
            }
        }

        if (minYear == Integer.MAX_VALUE) minYear = 0;
        if (maxYear == Integer.MIN_VALUE) maxYear = 0;

        DatasetSummary summary = new DatasetSummary(
                totalRows, records.size(), skippedRows, duplicateRows,
                minYear, maxYear, missingCounts
        );

        return new LoadResult(records, summary);
    }

    private String getCol(List<String> cols, Map<String, Integer> colIndex, String colName) {
        Integer idx = colIndex.get(colName);
        if (idx == null || idx >= cols.size()) {
            return null;
        }
        return cols.get(idx);
    }

    private List<String> parseCsvLine(String line) {
        List<String> tokens = new ArrayList<>();
        StringBuilder current = new StringBuilder();
        boolean inQuotes = false;

        for (int i = 0; i < line.length(); i++) {
            char ch = line.charAt(i);
            if (ch == '"') {
                inQuotes = !inQuotes;
            } else if (ch == ',' && !inQuotes) {
                tokens.add(current.toString().trim());
                current.setLength(0);
            } else {
                current.append(ch);
            }
        }
        tokens.add(current.toString().trim());
        return tokens;
    }

    private String cleanString(String raw, String fallback) {
        if (raw == null || raw.isEmpty() || raw.equalsIgnoreCase("N/A")) {
            return fallback;
        }
        return raw;
    }

    private Integer parseNullableInt(String raw) {
        if (raw == null || raw.isEmpty() || raw.equalsIgnoreCase("N/A") || raw.equalsIgnoreCase("tbd")) {
            return null;
        }
        try {
            return (int) Double.parseDouble(raw);
        } catch (NumberFormatException e) {
            return null;
        }
    }

    private Double parseNullableDouble(String raw) {
        if (raw == null || raw.isEmpty() || raw.equalsIgnoreCase("N/A") || raw.equalsIgnoreCase("tbd")) {
            return null;
        }
        try {
            return Double.parseDouble(raw);
        } catch (NumberFormatException e) {
            return null;
        }
    }

    private double parseDoubleOrDefault(String raw, double fallback) {
        Double parsed = parseNullableDouble(raw);
        return parsed != null ? parsed : fallback;
    }

    private void increment(Map<String, Integer> map, String key) {
        map.put(key, map.getOrDefault(key, 0) + 1);
    }
}