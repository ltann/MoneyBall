package com.example.moneyball;

public class Game {
    String homeTeam, awayTeam, score, stats;
    public Game(String homeTeam, String awayTeam, String score, String stats){
        this.homeTeam = homeTeam;
        this.awayTeam = awayTeam;
        this.score = score;
        this.stats = stats;
    }

    public String getawayTeam() {
        return awayTeam;
    }

    public void setawayTeam(String awayTeam) {
        this.awayTeam = awayTeam;
    }

    public String gethomeTeam() {
        return homeTeam;
    }

    public void sethomeTeam(String homeTeam) {
        this.homeTeam = homeTeam;
    }

    public String getScore() {
        return score;
    }

    public void setScore(String score) {
        this.score = score;
    }

    public String getStats() {
        return stats;
    }

    public void setStats(String stats) {
        this.stats = stats;
    }
}

