package model.minigame;

import java.util.HashMap;
import java.util.Map;

public class MiniGame {
    private String name;
    private boolean completed;
    private int bestScore;
    private int timesPlayed;
    private boolean unlocked;
    private int currentLevel;
    private Map<String, Integer> levelScores;

    public MiniGame(String name) {
        this.name = name;
        this.completed = false;
        this.bestScore = 0;
        this.timesPlayed = 0;
        this.unlocked = true;
        this.currentLevel = 1;
        this.levelScores = new HashMap<>();
    }

    public void complete() {
        completed = true;
    }

    public void completeLevel(int level, int score) {
        if (score > getLevelBestScore(level)) {
            levelScores.put("level_" + level, score);
        }
        if (score > bestScore) {
            bestScore = score;
        }
        currentLevel = Math.max(currentLevel, level + 1);
        timesPlayed++;
    }

    public int getLevelBestScore(int level) {
        return levelScores.getOrDefault("level_" + level, 0);
    }

    public boolean isLevelCompleted(int level) {
        return levelScores.containsKey("level_" + level);
    }

    public boolean isAllLevelsCompleted() {
        return isLevelCompleted(1) && isLevelCompleted(2) && isLevelCompleted(3);
    }

    public void unlock() {
        unlocked = true;
    }

    public void lock() {
        unlocked = false;
    }

    public String getName() { return name; }
    public boolean isCompleted() { return completed; }
    public int getBestScore() { return bestScore; }
    public int getTimesPlayed() { return timesPlayed; }
    public boolean isUnlocked() { return unlocked; }
    public int getCurrentLevel() { return currentLevel; }
    public void setCurrentLevel(int currentLevel) { this.currentLevel = currentLevel; }
    public Map<String, Integer> getLevelScores() { return new HashMap<>(levelScores); }
}
