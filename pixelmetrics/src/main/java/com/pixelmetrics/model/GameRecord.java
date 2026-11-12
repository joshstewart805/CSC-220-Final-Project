package com.pixelmetrics.model;

import java.util.Objects;

public class GameRecord {
    private final String name;
    private final String platform;
    private final Integer yearOfRelease; // Nullable ("N/A" in dataset)
    private final String genre;
    private final String publisher;
    private final double naSales;
    private final double euSales;
    private final double jpSales;
    private final double otherSales;
    private final double globalSales;
    private final Double criticScore;    // Nullable (0-100 scale)
    private final Integer criticCount;   // Nullable
    private final Double userScore;      // Nullable (0-10 scale; "tbd" -> null)
    private final Integer userCount;     // Nullable
    private final String developer;
    private final String rating;         // ESRB rating (E, E10+, T, M, etc.)

    public GameRecord(
            String name,
            String platform,
            Integer yearOfRelease,
            String genre,
            String publisher,
            double naSales,
            double euSales,
            double jpSales,
            double otherSales,
            double globalSales,
            Double criticScore,
            Integer criticCount,
            Double userScore,
            Integer userCount,
            String developer,
            String rating) {
        this.name = name;
        this.platform = platform;
        this.yearOfRelease = yearOfRelease;
        this.genre = genre;
        this.publisher = publisher;
        this.naSales = naSales;
        this.euSales = euSales;
        this.jpSales = jpSales;
        this.otherSales = otherSales;
        this.globalSales = globalSales;
        this.criticScore = criticScore;
        this.criticCount = criticCount;
        this.userScore = userScore;
        this.userCount = userCount;
        this.developer = developer;
        this.rating = rating;
    }

    public String getName() { return name; }
    public String getPlatform() { return platform; }
    public Integer getYearOfRelease() { return yearOfRelease; }
    public String getGenre() { return genre; }
    public String getPublisher() { return publisher; }
    public double getNaSales() { return naSales; }
    public double getEuSales() { return euSales; }
    public double getJpSales() { return jpSales; }
    public double getOtherSales() { return otherSales; }
    public double getGlobalSales() { return globalSales; }
    public Double getCriticScore() { return criticScore; }
    public Integer getCriticCount() { return criticCount; }
    public Double getUserScore() { return userScore; }
    public Integer getUserCount() { return userCount; }
    public String getDeveloper() { return developer; }
    public String getRating() { return rating; }

    public boolean hasCriticScore() { return criticScore != null; }
    public boolean hasUserScore() { return userScore != null; }

    @Override
    public String toString() {
        return String.format("%s (%s, %s) - Global: %.2fM | Critic: %s | User: %s",
                name,
                platform,
                yearOfRelease != null ? yearOfRelease : "N/A",
                globalSales,
                criticScore != null ? criticScore : "N/A",
                userScore != null ? userScore : "N/A");
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof GameRecord)) return false;
        GameRecord that = (GameRecord) o;
        return Objects.equals(name, that.name)
                && Objects.equals(platform, that.platform)
                && Objects.equals(yearOfRelease, that.yearOfRelease)
                && Objects.equals(publisher, that.publisher);
    }

    @Override
    public int hashCode() {
        return Objects.hash(name, platform, yearOfRelease, publisher);
    }
}