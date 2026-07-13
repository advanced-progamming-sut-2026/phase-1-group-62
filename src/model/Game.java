package model;

import model.enums.Difficulty;

public class Game {
    private Board board;
    private Level level;
    private Difficulty difficulty;
    private int sunCount;
    private boolean running;

    public Game() {
        this.board = new Board(5, 9);
        this.level = new Level(1);
        this.difficulty = Difficulty.NORMAL;
        this.sunCount = 50;
    }

    public void start() {
        running = true;
    }

    public void stop() {
        running = false;
    }

    public boolean isRunning() {
        return running;
    }

    public Board getBoard() {
        return board;
    }

    public Level getLevel() {
        return level;
    }

    public Difficulty getDifficulty() {
        return difficulty;
    }

    public int getSunCount() {
        return sunCount;
    }

    public void addSun(int amount) {
        sunCount += amount;
    }

    public boolean spendSun(int amount) {
        if (sunCount < amount) {
            return false;
        }
        sunCount -= amount;
        return true;
    }
}

// UPDATED ALL MODELS TO BE COMPATIBLE, WILL BE PASTED HERE LATEER
