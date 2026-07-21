package view;

import model.Board;
import model.Tile;
import model.Game;
import model.LawnMower;
import model.entities.plant.Plant;
import model.entities.zombie.Zombie;
import model.Sun;
import model.enums.TileType;
import model.enums.SpecialLevelType;
import model.minigame.Vasebreaker;
import model.minigame.IZombie;
import model.minigame.Beghoul;
import model.minigame.WallnutBowling;
import controller.menu.PreGameController;

public class GameView extends View {
    public void showBoard(Board board) {
        showBoardState(null);
    }

    public void showBoardState(Game game) {
        if (game == null) {
            showMessage("\n--- PvZ Battlefield ---");
            showMessage("No active game session.");
            return;
        }

        printHeader(game);
        printConveyorBeltIfActive(game);
        printGrid(game);
        showMessage("==================================================================================================\n");
    }

    private void printHeader(Game game) {
        showMessage("\n==================================================================================================");
        String ch = PreGameController.activeChapterName;
        String levelLabel = "Level 1 (Normal Mode)";

        if (ch != null) {
            if (ch.endsWith("2")) {
                if (ch.startsWith("AncientEgypt") || ch.startsWith("BigWaveBeach")) {
                    levelLabel = "Level 2 (Night Ops Mode)";
                } else {
                    levelLabel = "Level 2 (Save Our Seeds Mode)";
                }
            } else if (ch.endsWith("3")) {
                if (ch.startsWith("AncientEgypt") || ch.startsWith("BigWaveBeach")) {
                    levelLabel = "Level 3 (Dead Line Mode)";
                } else {
                    levelLabel = "Level 3 (Timed War Mode)";
                }
            } else if (game.getLevel().getSpecialLevelType() != SpecialLevelType.NONE) {
                levelLabel = "Special Mode: " + game.getLevel().getSpecialLevelType();
            }
        }

        String seasonName = (game.getCurrentSeason() != null) ? game.getCurrentSeason().getName() : "Normal";
        showMessage(" SEASON: " + seasonName.toUpperCase() + " | MATCH STATUS: " + levelLabel.toUpperCase());

        if (game.getActiveMiniGame() instanceof IZombie) {
            IZombie iz = (IZombie) game.getActiveMiniGame();
            showMessage(" TICK: " + game.getTickCount() + " | ZOMBIE SUNS: " + iz.getZombieSunCount() + " | BRAINS EATEN: " + iz.getBrainsEaten() + "/5");
        } else {
            showMessage(" TICK: " + game.getTickCount() + " | SUNS: " + game.getSunCount() + " | COINS: " + game.getCoins() + " | GEMS: " + game.getDiamonds() + " | FOODS: " + game.getPlantFoodCount());
        }
        showMessage("==================================================================================================");
    }

    private void printConveyorBeltIfActive(Game game) {
        if (game.getLevel().getSpecialLevelType() == SpecialLevelType.CONVEYOR_BELT || game.getActiveMiniGame() instanceof WallnutBowling) {
            if (!game.getConveyorBeltPlants().isEmpty()) {
                showMessage(" [CONVEYOR BELT]: " + String.join(" | ", game.getConveyorBeltPlants()));
                showMessage("--------------------------------------------------------------------------------------------------");
            }
        }
    }

    private void printGrid(Game game) {
        Board board = game.getBoard();
        LawnMower[] mowers = game.getLawnMowers();

        for (int r = 0; r < board.getRows(); r++) {
            StringBuilder rowStr = new StringBuilder();

            if (game.getActiveMiniGame() instanceof IZombie) {
                IZombie iz = (IZombie) game.getActiveMiniGame();
                rowStr.append(iz.isBrainRowEaten(r) ? "[ X] " : "[ B] ");
            } else {
                rowStr.append(mowers[r].isUsed() ? "[ X] " : "[ M] ");
            }

            for (int c = 0; c < 9; c++) {
                Tile tile = board.getTile(r, c);
                String cellContent = getCellContent(game, tile, r, c);

                String paddedContent = String.format("%-4s", cellContent);
                rowStr.append("[").append(paddedContent).append("] ");
            }
            showMessage(rowStr.toString());
        }
    }

    private String getCellContent(Game game, Tile tile, int r, int c) {
        // Vasebreaker display - FIXED
        if (game.getActiveMiniGame() instanceof Vasebreaker) {
            Vasebreaker vb = (Vasebreaker) game.getActiveMiniGame();

            // Check if vase exists at this position
            String content = vb.getVaseContent(r, c);

            // If content is null, there is NO vase here - show empty
            if (content == null) {
                return ".";
            }

            // Vase exists - show correct type if not broken
            if (!vb.isVaseBroken(r, c)) {
                if (content.equals(Vasebreaker.VASE_ZOMBIE)) {
                    return "V-Z";
                } else if (content.equals(Vasebreaker.VASE_GARGANTUAR)) {
                    return "V-G";
                } else if (content.equals(Vasebreaker.VASE_PLANT)) {
                    return "V-P";
                } else {
                    return "V-?";
                }
            }

            // Vase is broken - check for seed packet
            if (tile != null && tile.getTemporarySeedPacket() != null) {
                return "PK-" + tile.getTemporarySeedPacket().substring(0, 1).toUpperCase();
            }

            return ".";
        }

        if (game.getActiveMiniGame() instanceof Beghoul) {
            Beghoul bg = (Beghoul) game.getActiveMiniGame();
            if (bg.hasCrater(r, c)) {
                return "CRTR";
            }
        }

        Zombie z = getZombieAtTile(game, c, r);
        if (z != null) {
            if (z.getFrozenIceHealth() > 0) {
                return "#" + z.getFrozenIceHealth();
            }
            if (z.getArmorHealth() > 0) {
                String prefix = "A";
                if ("CONE".equalsIgnoreCase(z.getArmorType())) prefix = "C";
                else if ("BUCKET".equalsIgnoreCase(z.getArmorType())) prefix = "B";
                else if ("BRICK".equalsIgnoreCase(z.getArmorType())) prefix = "R";
                else if ("KNIGHT".equalsIgnoreCase(z.getArmorType())) prefix = "K";
                else if ("NEWSPAPER".equalsIgnoreCase(z.getArmorType())) prefix = "N";
                return prefix + z.getArmorHealth();
            }
            return "Z" + z.getHealth();
        }

        Plant p = game.getPlantAt(c, r);
        if (p == null && tile != null && tile.getSupportPlant() != null) {
            p = tile.getSupportPlant();
        }

        if (p != null) {
            String prefix = p.isBowlingBall() ? "B-" : (p.isFrozen() ? "#" : "P");
            return prefix + p.getHealth();
        }

        Sun s = getSunAtTile(game, c, r);
        if (s != null) {
            if (s.getValue() == 100) return "$S";
            if (s.getValue() == 50) return "$R";
            return "*";
        }

        if (tile != null) {
            if (tile.getType() == TileType.WATER) return "~~~";
            if (tile.getType() == TileType.GRAVE) {
                if (tile.getSunReward() > 0) return "$" + tile.getGraveHealth();
                if (tile.hasPlantFoodReward()) return "F" + tile.getGraveHealth();
                return "G" + tile.getGraveHealth();
            }
            if (tile.isSlideway()) {
                return tile.getSlideRowOffset() > 0 ? "v" : "^";
            }
            if (tile.isNecromancyTile()) {
                return "NEC";
            }
            if (tile.isLowBeach()) {
                return "LOW";
            }
        }

        if (game.getActiveMiniGame() instanceof WallnutBowling) {
            WallnutBowling wb = (WallnutBowling) game.getActiveMiniGame();
            if (c == wb.getRedLineX()) return "|";
        }

        if (game.getLevel().getSpecialLevelType() == SpecialLevelType.DEAD_LINE) {
            if (c == game.getLevel().getDeadlineColumn()) return "|";
        }

        return ".";
    }

    private Zombie getZombieAtTile(Game game, int x, int y) {
        for (Zombie z : game.getActiveZombies()) {
            // Check if zombie is at this tile (allow small floating point tolerance)
            int zx = (int) Math.floor(z.getX());
            if (zx == x && z.getY() == y) {
                return z;
            }
            // Also check if zombie is exactly at this position
            if (Math.abs(z.getX() - x) < 0.01 && z.getY() == y) {
                return z;
            }
        }
        return null;
    }

    private Sun getSunAtTile(Game game, int x, int y) {
        for (Sun s : game.getSuns()) {
            if (s.getColumn() == x && s.getRow() == y) {
                return s;
            }
        }
        return null;
    }
}
