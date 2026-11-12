package com.pixelmetrics.model;

import java.util.Collections;
import java.util.Map;

public class DatasetSummary {
    private final int totalRowsRead;
    private final int validRecordsLoaded;
    private final int skippedMalformedRows;
    private final int duplicateRecordsFound;
    private final int minYear;
    private final int maxYear;
    private final Map<String, Integer> missingValuesByColumn;

    public DatasetSummary(
            int totalRowsRead,
            int validRecordsLoaded,
            int skippedMalformedRows,
            int duplicateRecordsFound,
            int minYear,
            int maxYear,
            Map<String, Integer> missingValuesByColumn) {
        this.totalRowsRead = totalRowsRead;
        this.validRecordsLoaded = validRecordsLoaded;
        this.skippedMalformedRows = skippedMalformedRows;
        this.duplicateRecordsFound = duplicateRecordsFound;
        this.minYear = minYear;
        this.maxYear = maxYear;
        this.missingValuesByColumn = Collections.unmodifiableMap(missingValuesByColumn);
    }

    public int getTotalRowsRead() { return totalRowsRead; }
    public int getValidRecordsLoaded() { return validRecordsLoaded; }
    public int getSkippedMalformedRows() { return skippedMalformedRows; }
    public int getDuplicateRecordsFound() { return duplicateRecordsFound; }
    public int getMinYear() { return minYear; }
    public int getMaxYear() { return maxYear; }
    public Map<String, Integer> getMissingValuesByColumn() { return missingValuesByColumn; }

    @Override
    public String toString() {
        return String.format(
                "Loaded %,d valid games (from %,d rows | %d skipped | %d duplicates) | Years: %d-%d",
                validRecordsLoaded, totalRowsRead, skippedMalformedRows,
                duplicateRecordsFound, minYear, maxYear);
    }
}