package controller.menu;

import model.Game;
import model.Tile;
import model.entities.plant.factory.PlantFactory;
import model.entities.plant.Plant;
import model.entities.plant.loader.PlantLoader;  // <-- ADD THIS
import model.entities.zombie.Zombie;
import model.entities.zombie.factory.ZombieFactory;
import model.Sun;
import model.minigame.Vasebreaker;
import model.minigame.WallnutBowling;
import model.minigame.IZombie;
import model.minigame.Beghoul;
import model.enums.TileType;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;

import static model.minigame.Vasebreaker.*;

public class GameController extends Controller {
    private Game game;
    private boolean cooldownCheatActive = false;
    private final List<String> accumulatedTurnLogs = new ArrayList<>();

    public GameController(MenuController controller) {
        super(controller);
    }

    public void setGame(Game game) {
        this.game = game;
    }

    public String plantPlant(String type, int x, int y) {
        if (game == null) return "Error: No active game session.";
        if (x < 0 || x >= game.getBoard().getColumns() || y < 0 || y >= game.getBoard().getRows()) {
            return "Error: Coordinates out of bounds!";
        }

        if (game.getActiveMiniGame() instanceof Beghoul) {
            return "Error: Cannot plant normally in Beghoul mode! You must swap existing plants.";
        }

        if (game.getActiveMiniGame() instanceof IZombie) {
            return "Error: Cannot plant in I, Zombie mode! You must deploy zombies using placeZombie.";
        }

        if (game.getActiveMiniGame() instanceof Vasebreaker) {
            return "Error: Cannot plant normally in Vasebreaker! Plants only come from smashing vases.";
        }

        if (game.getActiveMiniGame() instanceof WallnutBowling) {
            WallnutBowling wb = (WallnutBowling) game.getActiveMiniGame();
            if (x > wb.getRedLineX()) {
                return "Error: Cannot plant past the red line! Max column allowed: " + wb.getRedLineX();
            }

            // Check if the walnut is on the conveyor belt (case-insensitive)
            boolean onBelt = false;
            String beltType = null;
            for (String pName : game.getConveyorBeltPlants()) {
                if (pName.equalsIgnoreCase(type)) {
                    onBelt = true;
                    beltType = pName;
                    break;
                }
            }

            // Also check if the user typed a partial match
            if (!onBelt) {
                for (String pName : game.getConveyorBeltPlants()) {
                    String lowerName = pName.toLowerCase();
                    String lowerType = type.toLowerCase();
                    if (lowerName.contains(lowerType) || lowerType.contains(lowerName)) {
                        onBelt = true;
                        beltType = pName;
                        break;
                    }
                }
            }

            if (!onBelt) {
                return "Error: This walnut is not available on the conveyor belt! Available: " + String.join(", ", game.getConveyorBeltPlants());
            }

            Plant ball = PlantFactory.createPlant("WallNut");
            if (ball == null) ball = new Plant(88, beltType, "BOWLING", null, 0, 300, 50, 0, 0, null, 0, null, 0);

            game.getConveyorBeltPlants().remove(beltType);
            ball.setX(x);
            ball.setY(y);
            ball.setDx(1);
            ball.setDy(0);
            ball.setBowlingBall(true);
            game.addPlant(ball);
            game.getBoard().getTile(y, x).setPlant(ball);
            return "Successfully launched " + beltType + " bowling ball down row " + y;
        }

        Tile tile = game.getBoard().getTile(y, x);

        if (tile != null && (tile.getType() == TileType.GRAVE || tile.isSlideway())) {
            return "Error: Cannot plant on this tile! It is blocked by environment.";
        }

        Plant check = game.getPlantAt(x, y);

        if (check != null && !check.getName().equalsIgnoreCase("Lily Pad")) {
            return "Error: There is already a plant here!";
        }

        if (game.getActiveMiniGame() == null && !game.getLevel().getSpecialLevelType().name().contains("CONVEYOR")) {
            boolean isSelected = false;
            if (model.UserSession.isLoggedIn() && model.UserSession.getCurrentUser() != null) {
                for (String p : model.UserSession.getCurrentUser().getUnlockedPlants()) {
                    if (p.equalsIgnoreCase(type)) {
                        isSelected = true;
                        type = p;
                        break;
                    }
                }
            }
            if (!isSelected) {
                return "Error: You cannot plant a plant that you didn't select or haven't unlocked!";
            }
        }

        Plant newPlant = PlantFactory.createPlant(type);
        if (newPlant == null) {
            // Try case-insensitive matching by checking all loaded plants
            List<Plant> allPlants = PlantLoader.loadPlants();
            for (Plant p : allPlants) {
                if (p.getName().equalsIgnoreCase(type)) {
                    newPlant = PlantFactory.createPlant(p.getName());
                    break;
                }
            }
            // If still null, try common variations
            if (newPlant == null) {
                // Try with common name variations
                String[] variations = {type, type.toLowerCase(), type.toUpperCase(),
                        type.replace(" ", ""), type.replace(" ", "_")};
                for (String var : variations) {
                    newPlant = PlantFactory.createPlant(var);
                    if (newPlant != null) break;
                }
            }
            if (newPlant == null) {
                return "Error: Plant type not found! Try: PeaShooter, Sunflower, WallNut, etc.";
            }
        }
        if (game.getSunCount() < newPlant.getCost()) {
            return "Error: Not enough suns! Required: " + newPlant.getCost();
        }

        if (tile != null && tile.getType() == TileType.WATER) {
            boolean isNewAquatic = newPlant.isAquatic();
            boolean hasLilyPad = (check != null && check.getName().equalsIgnoreCase("Lily Pad")) ||
                    (tile.getSupportPlant() != null && tile.getSupportPlant().getName().equalsIgnoreCase("Lily Pad"));
            if (!isNewAquatic && !hasLilyPad) {
                return "Error: Cannot plant non-aquatic plant on water without a Lily Pad!";
            }
            if (check != null && check.getName().equalsIgnoreCase("Lily Pad") && !isNewAquatic) {
                tile.setSupportPlant(check);
            }
        }

        game.spendSun(newPlant.getCost());
        newPlant.setX(x);
        newPlant.setY(y);
        game.addPlant(newPlant);
        tile.setPlant(newPlant);
        return "Successfully planted " + type + " at (" + x + ", " + y + ")";
    }

    public String swapPlants(int x1, int y1, int x2, int y2) {
        if (game == null || !(game.getActiveMiniGame() instanceof Beghoul)) {
            return "Error: Not currently in a Beghoul mini-game.";
        }
        if (x1 < 0 || x1 >= 9 || y1 < 0 || y1 >= 5 || x2 < 0 || x2 >= 9 || y2 < 0 || y2 >= 5) {
            return "Error: Coordinates out of bounds.";
        }
        if (Math.abs(x1 - x2) + Math.abs(y1 - y2) != 1) {
            return "Error: You can only swap adjacent tiles (horizontally or vertically).";
        }

        Beghoul bg = (Beghoul) game.getActiveMiniGame();
        if (bg.hasCrater(y1, x1) || bg.hasCrater(y2, x2)) {
            return "Error: Cannot swap items in a crater grid tile!";
        }

        Tile t1 = game.getBoard().getTile(y1, x1);
        Tile t2 = game.getBoard().getTile(y2, x2);
        Plant p1 = t1.getPlant();
        Plant p2 = t2.getPlant();

        if (p1 == null || p2 == null) {
            return "Error: Both tiles must contain a plant to swap.";
        }

        t1.setPlant(p2);
        t2.setPlant(p1);
        p1.setX(x2); p1.setY(y2);
        p2.setX(x1); p2.setY(y1);

        boolean initialMatch = bg.checkAndProcessMatches(game, false);
        if (!initialMatch) {
            t1.setPlant(p1);
            t2.setPlant(p2);
            p1.setX(x1); p1.setY(y1);
            p2.setX(x2); p2.setY(y2);
            return "Error: Invalid move! Swap does not create a combination of 3 or more.";
        }

        while (bg.checkAndProcessMatches(game, true)) {
            System.out.println("Beghoul: Cascade reaction triggered additional combinations!");
        }

        return "Successfully swapped plants. Combination formed!";
    }

    public String upgradePlants(String fromType, String toType) {
        if (game == null || !(game.getActiveMiniGame() instanceof Beghoul)) {
            return "Error: Not currently in a Beghoul mini-game.";
        }

        int cost = 500;
        if (fromType.equalsIgnoreCase("peashooter") && toType.equalsIgnoreCase("repeater")) cost = 500;
        else if (fromType.equalsIgnoreCase("repeater") && toType.equalsIgnoreCase("threepeater")) cost = 1500;
        else if (fromType.equalsIgnoreCase("wall-nut") && toType.equalsIgnoreCase("tall-nut")) cost = 500;
        else if (fromType.equalsIgnoreCase("puff-shroom") && toType.equalsIgnoreCase("scaredy-shroom")) cost = 250;
        else if (fromType.equalsIgnoreCase("cabbage-pult") && toType.equalsIgnoreCase("melon-pult")) cost = 1000;
        else if (fromType.equalsIgnoreCase("melon-pult") && toType.equalsIgnoreCase("winter-melon")) cost = 750;
        else return "Error: Invalid upgrade combination specified.";

        if (game.getSunCount() < cost) {
            return "Error: Not enough suns! Required: " + cost + ", Available: " + game.getSunCount();
        }

        game.spendSun(cost);
        int upgradedCount = 0;

        for (int r = 0; r < 5; r++) {
            for (int c = 0; c < 9; c++) {
                Tile tile = game.getBoard().getTile(r, c);
                if (tile.getPlant() != null && tile.getPlant().getName().equalsIgnoreCase(fromType)) {
                    game.removePlant(tile.getPlant());
                    Plant up = PlantFactory.createPlant(toType);
                    if (up == null) {
                        up = new Plant(new Random().nextInt(1000) + 200, toType, "BEGHOULD", null, 0, 400, 40, 1.5, 0, null, 0, null, 0);
                    }
                    up.setX(c);
                    up.setY(r);
                    game.addPlant(up);
                    tile.setPlant(up);
                    upgradedCount++;
                }
            }
        }

        return "Successfully upgraded " + upgradedCount + " plants from " + fromType + " to " + toType + ".";
    }

    public String placeZombie(String type, int lane) {
        return placeZombie(type, 8, lane);
    }

    public String placeZombie(String type, int x, int y) {
        if (game == null || !(game.getActiveMiniGame() instanceof IZombie)) {
            return "Error: Not currently in an I, Zombie mini-game.";
        }
        if (y < 0 || y >= game.getBoard().getRows()) {
            return "Error: Invalid lane number.";
        }

        IZombie iz = (IZombie) game.getActiveMiniGame();
        boolean placed = iz.placeZombie(type, x, y, game);
        if (placed) {
            return "Successfully deployed " + type + " at (" + x + ", " + y + ").";
        } else {
            return "Error: Could not place zombie. Check sun cost, available zombies, or coordinates (must be x > 4).";
        }
    }

    public String pluckPlant(int x, int y) {
        if (game == null) return "Error: No active game session.";
        Plant target = game.getPlantAt(x, y);
        Tile tile = game.getBoard().getTile(y, x);
        if (target == null && tile != null && tile.getSupportPlant() != null) {
            target = tile.getSupportPlant();
        }
        if (target == null) return "Error: There is no plant at this location to pluck.";

        game.removePlant(target);
        if (tile != null) {
            if (tile.getPlant() == target) {
                tile.setPlant(null);
                if (tile.getSupportPlant() != null) {
                    tile.setPlant(tile.getSupportPlant());
                    game.addPlant(tile.getSupportPlant());
                    tile.setSupportPlant(null);
                }
            } else if (tile.getSupportPlant() == target) {
                tile.setSupportPlant(null);
            }
        }
        return "Successfully plucked plant at (" + x + ", " + y + ")";
    }

    public String feedPlant(int x, int y) {
        if (game == null) return "Error: No active game session.";
        Plant target = game.getPlantAt(x, y);
        if (target == null) return "Error: There is no plant here to feed.";
        if (game.getPlantFoodCount() <= 0) return "Error: You do not have any plant food left.";

        if (game.usePlantFood()) {
            target.heal(target.getMaxHealth());
            return "Successfully fed plant at (" + x + ", " + y + "). HP fully restored!";
        }
        return "Error: Could not use plant food.";
    }

    public String collectSun(int x, int y) {
        if (game == null) return "Error: No active game session.";
        Sun targetSun = null;
        for (Sun s : game.getSuns()) {
            if (s.getColumn() == x && s.getRow() == y) {
                targetSun = s;
                break;
            }
        }
        Plant targetPlant = game.getPlantAt(x, y);
        if (targetSun != null) {
            game.addSun(targetSun.getValue());
            game.getSuns().remove(targetSun);
            return "Collected sun at (" + x + ", " + y + "). Total: " + game.getSunCount();
        } else if (targetPlant != null && targetPlant.isHasSunToCollect()) {
            game.addSun(25);
            targetPlant.setHasSunToCollect(false);
            return "Collected sun from " + targetPlant.getName() + " at (" + x + ", " + y + "). Total: " + game.getSunCount();
        }
        return "Error: No sun available to collect at this location.";
    }

    public String smashVase(int x, int y) {
        if (game == null || !(game.getActiveMiniGame() instanceof Vasebreaker)) {
            return "Error: Not currently in a Vasebreaker mini-game.";
        }
        Vasebreaker vb = (Vasebreaker) game.getActiveMiniGame();
        if (!vb.hasVase(y, x)) {
            return "Error: No vase exists at tile (" + x + ", " + y + ").";
        }
        if (vb.isVaseBroken(y, x)) {
            return "Error: Vase at (" + x + ", " + y + ") is already smashed.";
        }

        String content = vb.getVaseContent(y, x);
        vb.breakVase(y, x, game);

        Tile tile = game.getBoard().getTile(y, x);
        if (tile != null && tile.getTemporarySeedPacket() != null) {
            return "Smashed vase at (" + x + ", " + y + "): Dropped a Seed Packet! Pick it up quickly.";
        }

        if (content == null || content.equals(Vasebreaker.VASE_EMPTY)) {
            return "Smashed vase at (" + x + ", " + y + "): Found nothing! The vase was empty.";
        } else if (content.equals(Vasebreaker.VASE_ZOMBIE)) {
            return "Smashed vase at (" + x + ", " + y + "): A Zombie appeared!";
        } else if (content.equals(Vasebreaker.VASE_GARGANTUAR)) {
            return "Smashed vase at (" + x + ", " + y + "): A Gargantuar appeared!";
        } else if (content.equals(Vasebreaker.VASE_PLANT) || content.equals(Vasebreaker.VASE_SPECIAL_PLANT)) {
            return "Smashed vase at (" + x + ", " + y + "): Dropped a Seed Packet! Pick it up quickly.";
        } else if (content.equals(Vasebreaker.VASE_SUN)) {
            return "Smashed vase at (" + x + ", " + y + "): Found 50 suns!";
        } else {
            return "Smashed vase at (" + x + ", " + y + ")";
        }
    }
    public String pickupPacket(int x, int y) {
        if (game == null || !(game.getActiveMiniGame() instanceof Vasebreaker)) {
            return "Error: Not currently in a Vasebreaker mini-game.";
        }
        Tile tile = game.getBoard().getTile(y, x);
        String packet = tile.getTemporarySeedPacket();
        if (packet == null) {
            return "Error: No dropped seed packet available at this tile.";
        }

        Plant droppedPlant = PlantFactory.createPlant(packet);
        if (droppedPlant == null) droppedPlant = PlantFactory.createPlant("PeaShooter");

        tile.setTemporarySeedPacket(null);
        tile.setSeedPacketTimer(0);

        droppedPlant.setX(x);
        droppedPlant.setY(y);
        game.addPlant(droppedPlant);
        tile.setPlant(droppedPlant);
        return "Picked up and successfully planted " + packet + " at tile (" + x + ", " + y + ").";
    }


    public String processZombieDeathDrops(Zombie zombie) {
        StringBuilder message = new StringBuilder();
        Random r = new Random();

        if (zombie.isGlowing()) {
            if (game.getPlantFoodCount() < 3) {
                game.addPlantFood();
                message.append("The glowing zombie dropped a plant food; you have ")
                        .append(game.getPlantFoodCount())
                        .append(" plant foods now.\n");
            }
        }

        if (r.nextInt(100) < 10) {
            int dropType = r.nextInt(3);
            if (dropType == 0) {
                game.addCoins(50);
                message.append("A zombie dropped a coin; you have ")
                        .append(game.getCoins())
                        .append(" coins now.");
            } else if (dropType == 1) {
                game.addDiamonds(1);
                message.append("A zombie dropped a diamond; you have ")
                        .append(game.getDiamonds())
                        .append(" diamonds now.");
            } else {
                if (game.getGreenhouse() != null) {
                    game.getGreenhouse().addPot(new model.greenhouse.Pot(0, 0));
                }
                message.append("A zombie dropped a pot; you have ")
                        .append(game.getGreenhouse() != null ? game.getGreenhouse().getUnlockedPotCount() : 1)
                        .append(" pots now.");
            }
        }
        return message.toString().trim();
    }

    public String executeNuke() {
        if (game == null) return "Error: No active game session.";
        int count = game.getActiveZombies().size();
        for (Zombie z : game.getActiveZombies()) {
            game.getBoard().getTile(z.getY(), (int) z.getX()).setZombie(null);
        }
        game.getActiveZombies().clear();
        return "Nuke released! " + count + " zombies wiped off the map.";
    }

    public String executeRemoveCooldownCheat() {
        cooldownCheatActive = true;
        return "Cheat activated: Cooldown limits removed for all plants.";
    }

    public String executeAddPlantFoodCheat() {
        if (game == null) return "Error: No active game session.";
        game.addPlantFood();
        return "Cheat activated: Added 1 plant food. Total: " + game.getPlantFoodCount();
    }

    public String addCheatSuns(int amount) {
        if (game == null) return "Error: No active game session.";
        game.addSun(amount);
        return "Cheat activated: Added " + amount + " suns.";
    }

    public int advanceTime(int ticks) {
        if (game == null) return 0;
        int actualTicksExecuted = 0;
        int activeZombiesAtStart = game.getActiveZombies().size();
        for (int i = 0; i < ticks; i++) {
            if (game.isLost() || game.isWon() || !game.isRunning()) {
                break;
            }
            if (game.getSpawner() != null && game.getSpawner().ticksSinceLastSpawn == 0 && game.getActiveZombies().size() > activeZombiesAtStart) {
                game.getSpawner().ticksSinceLastSpawn = 1;
            }
            game.tick();


            accumulatedTurnLogs.addAll(game.getRawLogMessagesDirectly());

            actualTicksExecuted++;
        }
        return actualTicksExecuted;
    }

    public List<String> extractAccumulatedTurnLogs() {
        List<String> copy = new ArrayList<>(accumulatedTurnLogs);
        accumulatedTurnLogs.clear();
        return copy;
    }

    public boolean isCooldownCheatActive() {
        return cooldownCheatActive;
    }
}
