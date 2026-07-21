package controller.menu;

import model.Game;
import model.Tile;
import model.Settings;
import model.enums.SpecialLevelType;
import model.entities.zombie.factory.ZombieFactory;
import model.entities.zombie.Zombie;
import model.entities.plant.Plant;
import model.season.AncientEgypt;
import model.season.BigWaveBeach;
import model.season.DarkAges;
import model.season.FrostbiteCaves;
import model.season.Season;
import model.minigame.MiniGame;
import util.FileManager;
import util.ParsedCommand;
import view.menu.MainMenu;
import view.menu.MenuManager;
import java.util.List;

public class GameMenuController extends Controller {
    private final Game game;
    private final GameController gameController;
    private final MenuController menuController;
    private final CommandParser parser;

    public GameMenuController(MenuController menuController) {
        super(menuController);
        this.menuController = menuController;

        Settings settings = FileManager.loadSettings();
        int diffVal = settings != null ? settings.getDifficulty() : 3;

        this.game = new Game(5, 9, 1, diffVal);
        String ch = PreGameController.activeChapterName;

        if (ch != null && ch.endsWith("_MG")) {
            MiniGame mg = null;
            if (ch.startsWith("Vasebreaker")) {
                mg = new model.minigame.Vasebreaker();
                this.game.setActiveMiniGame(mg);
            } else if (ch.startsWith("WallnutBowling")) {
                mg = new model.minigame.WallnutBowling();
                this.game.setActiveMiniGame(mg);
            } else if (ch.startsWith("IZombie")) {
                mg = new model.minigame.IZombie();
                this.game.setActiveMiniGame(mg);
            } else if (ch.startsWith("Beghoul")) {
                mg = new model.minigame.Beghoul();
                this.game.setActiveMiniGame(mg);
            } else if (ch.startsWith("Zombotany")) {
                mg = new model.minigame.Zombotany();
                this.game.setActiveMiniGame(mg);
            }
        } else if (ch != null) {
            Season season = null;

            if (ch.startsWith("AncientEgypt")) {
                season = new AncientEgypt();
                if (ch.endsWith("2")) this.game.getLevel().setSpecialLevelType(SpecialLevelType.NIGHT_OPS);
                else if (ch.endsWith("3")) this.game.getLevel().setSpecialLevelType(SpecialLevelType.DEAD_LINE);
            }
            else if (ch.startsWith("FrostbiteCaves")) {
                season = new FrostbiteCaves();
                if (ch.endsWith("2")) this.game.getLevel().setSpecialLevelType(SpecialLevelType.SAVE_OUR_SEEDS);
                else if (ch.endsWith("3")) this.game.getLevel().setSpecialLevelType(SpecialLevelType.TIMED_WAR);
            }
            else if (ch.startsWith("BigWaveBeach")) {
                season = new BigWaveBeach();
                if (ch.endsWith("2")) this.game.getLevel().setSpecialLevelType(SpecialLevelType.NIGHT_OPS);
                else if (ch.endsWith("3")) this.game.getLevel().setSpecialLevelType(SpecialLevelType.DEAD_LINE);
            }
            else if (ch.startsWith("DarkAges")) {
                season = new DarkAges();
                if (ch.endsWith("2")) this.game.getLevel().setSpecialLevelType(SpecialLevelType.SAVE_OUR_SEEDS);
                else if (ch.endsWith("3")) this.game.getLevel().setSpecialLevelType(SpecialLevelType.TIMED_WAR);
            }

            if (season != null) {
                this.game.setCurrentSeason(season);
            }
        }

        this.game.start();
        this.game.setupSpecialLevelFeatures();
        this.game.setSunCount(this.game.getLevel().getInitialSunAmount());

        // --- FORCE SETUP FOR MINIGAMES AFTER START ---
        if (this.game.getActiveMiniGame() instanceof model.minigame.IZombie) {
            ((model.minigame.IZombie) this.game.getActiveMiniGame()).setupStage(this.game, 1);
        }
        if (this.game.getActiveMiniGame() instanceof model.minigame.Beghoul) {
            ((model.minigame.Beghoul) this.game.getActiveMiniGame()).setupStage(this.game, 1);
        }
        if (this.game.getActiveMiniGame() instanceof model.minigame.Vasebreaker) {
            ((model.minigame.Vasebreaker) this.game.getActiveMiniGame()).setupVaseGrid(5, 9, 1);
        }
        // WallnutBowling and Zombotany don't need forced setup - they work differently

        this.gameController = new GameController(menuController);
        this.gameController.setGame(this.game);
        this.parser = new CommandParser();
    }

    public Game getGame() {
        return game;
    }

    public String handleGameMenuInput(String input) {
        if (input.equalsIgnoreCase("exit") || input.equalsIgnoreCase("exit game")) {
            MenuManager.getInstance().setCurrentMenu(new MainMenu(menuController));
            return "EXIT_GAME";
        }

        ParsedCommand cmd = parser.parse(input);
        String action = cmd.getAction();

        if (action.equalsIgnoreCase("advance time")) {
            String ticksStr = cmd.getArg("-t");
            int ticks = 1;
            if (ticksStr != null) {
                try {
                    ticksStr = ticksStr.trim().split(" ")[0];
                    ticks = Integer.parseInt(ticksStr);
                } catch (Exception e) {
                    ticks = 1;
                }
            }
            int waveBefore = game.getSpawner() != null ? game.getSpawner().getCurrentWave() : 0;
            int sunsBefore = game.getSuns().size();

            int executed = gameController.advanceTime(ticks);
            String report = "Time advanced by " + executed + " ticks.";

            if (game.getSpawner() != null && game.getSpawner().getCurrentWave() > waveBefore) {
                report += "\n[WAVE] ---> A new Wave started! Current Wave: " + game.getSpawner().getCurrentWave();
            }
            if (game.getSuns().size() > sunsBefore && !"DarkAges".equals(PreGameController.activeChapterName)) {
                report += "\n[SUN] A natural sun fell from sky!";
            }

            List<String> logs = gameController.extractAccumulatedTurnLogs();
            for (String log : logs) {
                report += "\n" + log;
            }
            return report;
        }
        if (action.equalsIgnoreCase("show map")) {
            return "SHOW_MAP_TRIGGER";
        }
        if (action.equalsIgnoreCase("zombies info")) {
            if (game.getActiveZombies().isEmpty()) {
                return "No active zombies on the battlefield.";
            }
            StringBuilder sb = new StringBuilder();
            for (Zombie z : game.getActiveZombies()) {
                sb.append(z.getName()).append(":\n");
                sb.append("    position: ").append((int) Math.round(z.getX())).append(", ").append(z.getY()).append("\n");
                sb.append("    health: ").append(z.getHealth()).append("\n");
                if (z.getArmorHealth() > 0) {
                    sb.append("    armor: ").append(z.getArmorType()).append(" (").append(z.getArmorHealth()).append(")\n");
                } else {
                    sb.append("    armor: none\n");
                }
                sb.append("    effects:");
                boolean hasEffect = false;
                if (z.getChilledDuration() > 0) {
                    sb.append("\n        chilled: ").append(String.format("%.1fs", z.getChilledDuration() / 10.0));
                    hasEffect = true;
                }
                if (z.getFrozenDuration() > 0 || z.getFrozenIceHealth() > 0) {
                    sb.append("\n        frozen: ").append(String.format("%.1fs", z.getFrozenDuration() / 10.0));
                    hasEffect = true;
                }
                if (!hasEffect) {
                    sb.append(" none");
                }
                sb.append("\n");
            }
            return sb.toString().trim();
        }
        if (action.equalsIgnoreCase("plant plant")) {
            String type = cmd.getArg("-t");
            String loc = cmd.getArg("-l");
            if (type == null || loc == null) {
                return "Usage: plant plant -t <type> -l (<x>, <y>)";
            }
            try {
                loc = loc.replace("(", "").replace(")", "");
                String[] coords = loc.split(",");
                int x = Integer.parseInt(coords[0].trim());
                int y = Integer.parseInt(coords[1].trim());
                return gameController.plantPlant(type, x, y);
            } catch (Exception e) {
                return "Invalid format! Coordinates must be inside (-l (x, y))";
            }
        }
        if (action.equalsIgnoreCase("pluck plant")) {
            String loc = cmd.getArg("-l");
            if (loc == null) {
                return "Usage: pluck plant -l (<x>, <y>)";
            }
            try {
                loc = loc.replace("(", "").replace(")", "");
                String[] coords = loc.split(",");
                int x = Integer.parseInt(coords[0].trim());
                int y = Integer.parseInt(coords[1].trim());
                return gameController.pluckPlant(x, y);
            } catch (Exception e) {
                return "Invalid format! Use pluck plant -l (<x>, <y>)";
            }
        }
        if (action.equalsIgnoreCase("feed plant")) {
            String loc = cmd.getArg("-l");
            if (loc == null) {
                return "Usage: feed plant -l (<x>, <y>)";
            }
            try {
                loc = loc.replace("(", "").replace(")", "");
                String[] coords = loc.split(",");
                int x = Integer.parseInt(coords[0].trim());
                int y = Integer.parseInt(coords[1].trim());
                return gameController.feedPlant(x, y);
            } catch (Exception e) {
                return "Invalid format! Use feed plant -l (<x>, <y>)";
            }
        }
        if (action.equalsIgnoreCase("upgrade plants")) {
            String fromType = cmd.getArg("-f");
            String toType = cmd.getArg("-t");
            if (fromType == null || toType == null) {
                return "Usage: upgrade plants -f <from_type> -t <to_type>";
            }
            return gameController.upgradePlants(fromType, toType);
        }
        if (action.equalsIgnoreCase("smash vase")) {
            String loc = cmd.getArg("-l");
            if (loc == null) {
                return "Usage: smash vase -l (<x>,<y>)";
            }
            try {
                loc = loc.replaceAll("[^0-9,]", "");
                loc = loc.replaceAll(",{2,}", ",");
                if (loc.startsWith(",")) loc = loc.substring(1);
                if (loc.endsWith(",")) loc = loc.substring(0, loc.length() - 1);

                String[] coords = loc.split(",");
                if (coords.length < 2) {
                    return "Invalid format! Use: smash vase -l (<x>,<y>)";
                }
                int x = Integer.parseInt(coords[0].trim());
                int y = Integer.parseInt(coords[1].trim());
                return gameController.smashVase(x, y);
            } catch (Exception e) {
                return "Invalid format! Use: smash vase -l (<x>,<y>)";
            }
        }


        if (action.equalsIgnoreCase("place zombie")) {
            String type = cmd.getArg("-t");
            String laneStr = cmd.getArg("-l");
            if (type == null || laneStr == null) {
                return "Usage: place zombie -t <type> -l <lane>";
            }
            try {
                int lane = Integer.parseInt(laneStr.trim());
                return gameController.placeZombie(type, lane);
            } catch (Exception e) {
                return "Invalid format! Use: place zombie -t <type> -l <lane>";
            }
        }

        if (action.equalsIgnoreCase("pickup packet")) {
            String loc = cmd.getArg("-l");
            if (loc == null) {
                return "Usage: pickup packet -l (<x>,<y>)";
            }
            try {
                // Remove ALL non-digit characters EXCEPT comma
                loc = loc.replaceAll("[^0-9,]", "");
                // Remove duplicate commas
                loc = loc.replaceAll(",{2,}", ",");
                // Remove leading/trailing commas
                if (loc.startsWith(",")) loc = loc.substring(1);
                if (loc.endsWith(",")) loc = loc.substring(0, loc.length() - 1);

                String[] coords = loc.split(",");
                if (coords.length < 2) {
                    return "Invalid format! Use: pickup packet -l (<x>,<y>)";
                }
                int x = Integer.parseInt(coords[0].trim());
                int y = Integer.parseInt(coords[1].trim());
                return gameController.pickupPacket(x, y);
            } catch (Exception e) {
                return "Invalid format! Use: pickup packet -l (<x>,<y>)";
            }
        }
        if (action.equalsIgnoreCase("collect sun")) {
            String loc = cmd.getArg("-l");
            if (loc == null) {
                return "Usage: collect sun -l (<x>, <y>)";
            }
            try {
                loc = loc.replace("(", "").replace(")", "");
                String[] coords = loc.split(",");
                int x = Integer.parseInt(coords[0].trim());
                int y = Integer.parseInt(coords[1].trim());
                return gameController.collectSun(x, y);
            } catch (Exception e) {
                return "Invalid coordinates format!";
            }
        }
        if (action.equalsIgnoreCase("show sun amount")) {
            return "Current sun amount: " + game.getSunCount();
        }
        if (action.equalsIgnoreCase("show plants status")) {
            if (game.getActivePlants().isEmpty()) {
                return "No plants currently active on the field.";
            }
            StringBuilder sb = new StringBuilder();
            for (Plant p : game.getActivePlants()) {
                sb.append("- ").append(p.getName()).append(" at (").append(p.getX()).append(", ").append(p.getY())
                        .append(") | HP: ").append(p.getHealth()).append("/").append(p.getMaxHealth())
                        .append(" | Can Produce Sun: ").append(p.getSunProduce() > 0).append("\n");
            }
            return sb.toString().trim();
        }
        if (action.equalsIgnoreCase("show tile status")) {
            String loc = cmd.getArg("-l");
            if (loc == null) {
                return "Usage: show tile status -l (<x>, <y>)";
            }
            try {
                loc = loc.replace("(", "").replace(")", "");
                String[] coords = loc.split(",");
                int x = Integer.parseInt(coords[0].trim());
                int y = Integer.parseInt(coords[1].trim());
                Tile tile = game.getBoard().getTile(y, x);
                if (tile != null) {
                    return "Tile (" + x + ", " + y + ") Status:\n" +
                            "- Type: " + tile.getType() + "\n" +
                            "- Plant: " + (tile.getPlant() != null ? tile.getPlant().getName() : "None") + "\n" +
                            "- Zombie: " + (tile.getZombie() != null ? tile.getZombie().getName() + " (HP: " + tile.getZombie().getHealth() + ")" : "None");
                }
                return "Error: Tile coordinates out of bounds.";
            } catch (Exception e) {
                return "Invalid format! Use show tile status -l (<x>, <y>)";
            }
        }
        if (action.equalsIgnoreCase("swap plants")) {
            String loc1 = cmd.getArg("-l");
            String loc2 = cmd.getArg("-m");
            if (loc1 == null || loc2 == null) {
                return "Usage: swap plants -l (<x1>,<y1>) -m (<x2>,<y2>)";
            }
            try {
                loc1 = loc1.replace("(", "").replace(")", "").trim();
                loc2 = loc2.replace("(", "").replace(")", "").trim();
                String[] coords1 = loc1.split(",");
                String[] coords2 = loc2.split(",");
                if (coords1.length < 2 || coords2.length < 2) {
                    return "Invalid format! Use: swap plants -l (<x1>,<y1>) -m (<x2>,<y2>)";
                }
                int x1 = Integer.parseInt(coords1[0].trim());
                int y1 = Integer.parseInt(coords1[1].trim());
                int x2 = Integer.parseInt(coords2[0].trim());
                int y2 = Integer.parseInt(coords2[1].trim());
                return gameController.swapPlants(x1, y1, x2, y2);
            } catch (Exception e) {
                return "Invalid format! Use: swap plants -l (<x1>,<y1>) -m (<x2>,<y2>)";
            }
        }
        if (input.toLowerCase().startsWith("release the nuke")) {
            return gameController.executeNuke();
        }
        if (action.equalsIgnoreCase("cheat remove-cooldown")) {
            return gameController.executeRemoveCooldownCheat();
        }
        if (action.equalsIgnoreCase("cheat add-plant-food")) {
            return gameController.executeAddPlantFoodCheat();
        }
        if (action.equalsIgnoreCase("cheat spawn-zombie")) {
            String type = cmd.getArg("-t");
            String loc = cmd.getArg("-l");
            if (type == null || loc == null) {
                return "Usage: cheat spawn-zombie -t <type> -l (<x>, <y>)";
            }
            try {
                loc = loc.replace("(", "").replace(")", "");
                String[] coords = loc.split(",");
                int x = Integer.parseInt(coords[0].trim());
                int y = Integer.parseInt(coords[1].trim());

                if (y < 0 || y >= game.getBoard().getRows() || x < 0 || x >= game.getBoard().getColumns()) {
                    return "Error: Coordinates out of board bounds! Maximum row allowed is " + (game.getBoard().getRows() - 1);
                }

                String formattedType = type.equalsIgnoreCase("normalzombie") ? "NormalZombie" : type;
                Zombie z = ZombieFactory.createZombieAtColumn(formattedType, y, x, game.getDifficultyLevel());
                if (z != null) {
                    if (model.UserSession.isLoggedIn() && model.UserSession.getCurrentUser() != null) {
                        List<String> observed = model.UserSession.getCurrentUser().getObservedZombies();
                        if (!observed.contains(z.getName())) {
                            observed.add(z.getName());
                            util.FileManager.updateUser(model.UserSession.getCurrentUser());
                        }
                    }
                    game.addZombie(z);
                    return "Zombie spawned via cheat.";
                }
                return "Invalid zombie type.";
            } catch (Exception e) {
                return "Invalid format! Use: cheat spawn-zombie -t <type> -l (<x>, <y>)";
            }
        }
        if (action.equalsIgnoreCase("cheat add")) {
            if (cmd.hasFlag("-n")) {
                try {
                    String countStr = cmd.getArg("-n");
                    int amount = Integer.parseInt(countStr.split(" ")[0]);
                    return gameController.addCheatSuns(amount);
                } catch (Exception e) {
                    return "Invalid cheat format.";
                }
            } else {
                String valueStr = cmd.getArg("VALUE");
                if (valueStr != null && valueStr.toLowerCase().contains("sun")) {
                    try {
                        int amount = Integer.parseInt(valueStr.toLowerCase().replace("suns", "").trim());
                        return gameController.addCheatSuns(amount);
                    } catch (Exception e) {
                        return "Invalid cheat format.";
                    }
                }
            }
        }

        return "Unknown game command. Type 'exit' to return to menu.";
    }
}
