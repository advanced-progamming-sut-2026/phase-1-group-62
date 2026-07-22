package model.minigame;

import model.Game;
import model.Tile;
import model.entities.plant.Plant;
import model.entities.zombie.Zombie;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;

public class WallnutBowling extends MiniGame {
    private int deadlineColumn;
    private int redLineX;
    private int stageLevel;
    private int maxStageLevel;
    private int zombiesDefeated;
    private int targetZombies;
    private int waveCount;
    private int maxWaves;

    public WallnutBowling() {
        super("WallnutBowling");
        this.deadlineColumn = 3;
        this.redLineX = 3;
        this.stageLevel = 1;
        this.maxStageLevel = 3;
        this.zombiesDefeated = 0;
        this.targetZombies = 10;
        this.waveCount = 0;
        this.maxWaves = 5;
    }

    public int getDeadlineColumn() {
        return deadlineColumn;
    }

    public void setDeadlineColumn(int deadlineColumn) {
        this.deadlineColumn = deadlineColumn;
    }

    public int getRedLineX() {
        return redLineX;
    }

    public void setRedLineX(int redLineX) {
        this.redLineX = redLineX;
    }

    public int getStageLevel() {
        return stageLevel;
    }

    public void setStageLevel(int stageLevel) {
        this.stageLevel = Math.min(stageLevel, maxStageLevel);
        updateLevelParameters();
    }

    private void updateLevelParameters() {
        switch (stageLevel) {
            case 1:
                targetZombies = 10;
                maxWaves = 5;
                deadlineColumn = 3;
                break;
            case 2:
                targetZombies = 20;
                maxWaves = 7;
                deadlineColumn = 4;
                break;
            case 3:
                targetZombies = 30;
                maxWaves = 10;
                deadlineColumn = 5;
                break;
            default:
                targetZombies = 10;
                maxWaves = 5;
                deadlineColumn = 3;
        }
    }

    private void spawnZombieWave(Game game) {
        Random rand = new Random();
        int numZombies = 1 + (stageLevel - 1) + rand.nextInt(stageLevel + 1);
        List<String> zombieTypes = new ArrayList<>();
        zombieTypes.add("NormalZombie");
        if (stageLevel >= 2) zombieTypes.add("ConeZombie");
        if (stageLevel >= 3) zombieTypes.add("BucketZombie");

        for (int i = 0; i < numZombies; i++) {
            String type = zombieTypes.get(rand.nextInt(zombieTypes.size()));
            int lane = rand.nextInt(game.getBoard().getRows());
            int spawnCol = game.getBoard().getColumns() - 1;
            Zombie z = model.entities.zombie.factory.ZombieFactory.createZombieAtColumn(type, lane, spawnCol);
            if (z != null) {
                game.addZombie(z);
                game.getBoard().getTile(lane, spawnCol).setZombie(z);
                game.getGameLogMessages().add("WallnutBowling: " + type + " spawned in lane " + lane);
            }
        }
        waveCount++;
        game.getGameLogMessages().add("WallnutBowling: Wave " + waveCount + " of " + maxWaves + " started!");
    }

    public void updateMiniGame(Game game) {
        if (game.getTickCount() == 1 || game.getTickCount() % 120 == 0) {
            Random rand = new Random();
            int roll = rand.nextInt(100);
            String walnutType;
            if (roll < 70) {
                walnutType = "Bowling Wallnut";
            } else if (roll < 90) {
                walnutType = "Explode O' Nut";
            } else {
                walnutType = "Giant Wallnut";
            }
            game.getConveyorBeltPlants().add(walnutType);
        }

        if (game.getTickCount() % 200 == 0 && waveCount < maxWaves && game.getActiveZombies().size() < 5) {
            spawnZombieWave(game);
        }

        if (game.getActiveZombies().isEmpty() && waveCount >= maxWaves) {
            if (stageLevel < maxStageLevel) {
                completeLevel(stageLevel, zombiesDefeated);
                stageLevel++;
                updateLevelParameters();
                waveCount = 0;
                zombiesDefeated = 0;
                for (Zombie z : new ArrayList<>(game.getActiveZombies())) {
                    game.getBoard().getTile(z.getY(), (int) z.getX()).setZombie(null);
                    game.removeZombie(z);
                }
                game.getGameLogMessages().add("WallnutBowling: Stage " + (stageLevel - 1) + " complete! Advanced to Stage " + stageLevel);
                spawnZombieWave(game);
                return;
            } else {
                game.setWon(true);
                game.stop();
                game.getGameLogMessages().add("Dear humanz, zis is not done yet; we will come back to eat your brainz, humanz.");
                return;
            }
        }

        for (Plant ball : new ArrayList<>(game.getActivePlants())) {
            if (ball.isBowlingBall()) {
                if (game.getTickCount() % 5 == 0) {
                    Tile oldTile = game.getBoard().getTile(ball.getY(), ball.getX());
                    if (oldTile != null && oldTile.getPlant() == ball) {
                        oldTile.setPlant(null);
                    }

                    ball.setX(ball.getX() + ball.getDx());
                    ball.setY(ball.getY() + ball.getDy());

                    if (ball.getY() < 0) {
                        ball.setY(0);
                        ball.setDy(-ball.getDy());
                    } else if (ball.getY() >= game.getBoard().getRows()) {
                        ball.setY(game.getBoard().getRows() - 1);
                        ball.setDy(-ball.getDy());
                    }

                    if (ball.getX() >= game.getBoard().getColumns()) {
                        game.getActivePlants().remove(ball);
                        continue;
                    }

                    Tile newTile = game.getBoard().getTile(ball.getY(), ball.getX());
                    if (newTile != null) {
                        newTile.setPlant(ball);
                    }

                    Zombie target = null;
                    for (Zombie z : game.getActiveZombies()) {
                        if (z.getY() == ball.getY() && Math.abs(z.getX() - ball.getX()) <= 0.8) {
                            target = z;
                            break;
                        }
                    }

                    if (target != null) {
                        if (ball.getName().equalsIgnoreCase("Giant Wallnut")) {
                            target.takeDamage(target.getMaxHealth(), false);
                            if (!target.isAlive()) {
                                zombiesDefeated++;
                                game.getScoreGame().onZombieKilled(target, game);
                                game.getActiveZombies().remove(target);
                                game.getBoard().getTile(target.getY(), (int) Math.round(target.getX())).setZombie(null);
                            }
                        } else if (ball.getName().equalsIgnoreCase("Explode O' Nut") || ball.getName().contains("Explode")) {
                            List<Zombie> blastTargets = new ArrayList<>();
                            for (Zombie az : game.getActiveZombies()) {
                                if (Math.abs(az.getY() - ball.getY()) <= 1 && Math.abs(az.getX() - ball.getX()) <= 1.5) {
                                    blastTargets.add(az);
                                }
                            }
                            for (Zombie bt : blastTargets) {
                                bt.takeDamage(1800, false);
                                if (!bt.isAlive()) {
                                    zombiesDefeated++;
                                    game.getScoreGame().onZombieKilled(bt, game);
                                    game.getActiveZombies().remove(bt);
                                    game.getBoard().getTile(bt.getY(), (int) Math.round(bt.getX())).setZombie(null);
                                }
                            }
                            game.getActivePlants().remove(ball);
                            if (newTile != null) newTile.setPlant(null);
                        } else {
                            target.takeDamage(200, false);
                            ball.incrementHitCount();
                            if (!target.isAlive()) {
                                zombiesDefeated++;
                                game.getScoreGame().onZombieKilled(target, game);
                                game.getActiveZombies().remove(target);
                                game.getBoard().getTile(target.getY(), (int) Math.round(target.getX())).setZombie(null);
                            }

                            if (ball.getHitCount() == 1) {
                                int dy = new Random().nextBoolean() ? 1 : -1;
                                if (ball.getY() == 0) dy = 1;
                                else if (ball.getY() == game.getBoard().getRows() - 1) dy = -1;
                                ball.setDy(dy);
                            } else {
                                ball.setDy(-ball.getDy());
                            }
                        }
                    }
                }
            }
        }
    }
}