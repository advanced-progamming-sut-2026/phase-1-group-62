package view.menu;

import controller.menu.CommandParser;
import controller.menu.MenuController;
import controller.menu.PreGameController;
import util.ParsedCommand;

public class PlayMenu extends Menu {
    private final PreGameController preGameController = new PreGameController();

    public PlayMenu(MenuController controller) {
        super(controller);
    }

    @Override
    public void runMenu() {
        CommandParser parser = new CommandParser();
        MenuController ctrl = (MenuController) this.controller;
        while (true) {
            String input = view.getInput("play menu");
            ParsedCommand cmd = parser.parse(input);

            if (input.equalsIgnoreCase("back")) {
                manager.setCurrentMenu(new MainMenu(controller));
                break;
            }
            else if(cmd.getAction().equalsIgnoreCase("menu enter chapter")){
                if(controller.processPlay(cmd , "chapter").equals("ok")) view.showMessage("ok");
                else view.showMessage("error");
            }
            else if(cmd.getAction().equalsIgnoreCase("menu coin-wallet")){
                view.showMessage(controller.processPlay(cmd , "coin-wallet"));
            }
            else if(cmd.getAction().equalsIgnoreCase("menu gem-wallet")){
                view.showMessage(controller.processPlay(cmd , "gem-wallet"));
            }
            else if(cmd.getAction().equalsIgnoreCase("cheat add")){
                view.showMessage(controller.processPlay(cmd , "cheat add"));
            }
            else if(cmd.getAction().equalsIgnoreCase("show all plants")){
                view.showMessage(preGameController.processCommand(cmd, "show all plants"));
            }
            else if(cmd.getAction().equalsIgnoreCase("show available plants")){
                view.showMessage(preGameController.processCommand(cmd, "show available plants"));
            }
            else if(cmd.getAction().equalsIgnoreCase("add plant")){
                view.showMessage(preGameController.processCommand(cmd, "add plant"));
            }
            else if(cmd.getAction().equalsIgnoreCase("remove plant")){
                view.showMessage(preGameController.processCommand(cmd, "remove plant"));
            }
            else if(cmd.getAction().equalsIgnoreCase("boost plant")){
                view.showMessage(preGameController.processCommand(cmd, "boost plant"));
            }
            else if(cmd.getAction().equalsIgnoreCase("start game")){
                String result = preGameController.processCommand(cmd, "start game");
                if (result.equals("START_GAME_CONFIRMED")) {
                    view.showMessage("Entering the battlefield...");
                    manager.setCurrentMenu(new GameMenu(controller));
                    break;
                } else {
                    view.showMessage(result);
                }
            }
            else if(cmd.getAction().equalsIgnoreCase("menu Collection")){
                manager.setCurrentMenu(new CollectionMenu(controller));
                break;
            }
            else if(cmd.getAction().equalsIgnoreCase("menu greenhouse")){
                manager.setCurrentMenu(new GreenhouseMenu(controller));
                break;
            }
            else if(cmd.getAction().equalsIgnoreCase("menu travel-log")){
                manager.setCurrentMenu(new TravelLogMenu(controller));
                break;
            }
            else if(cmd.getAction().equalsIgnoreCase("menu leaderboard")){
                manager.setCurrentMenu(new LeaderboardMenu(controller));
                break;
            }
        }
    }
}