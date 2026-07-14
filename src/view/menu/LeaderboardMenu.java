package view.menu;

import controller.menu.CommandParser;
import controller.menu.MenuController;
import util.ParsedCommand;

public class LeaderboardMenu extends Menu{
    public LeaderboardMenu(MenuController controller) {
        super(controller);
    }

    @Override
    public void runMenu() {
        CommandParser parser = new CommandParser();
        MenuController ctrl = (MenuController) this.controller;
        while (true) {
            String input = view.getInput("LeaderboardMenu");
            ParsedCommand cmd = parser.parse(input);

            if (input.equalsIgnoreCase("back")) {
                manager.setCurrentMenu(new PlayMenu(controller));
                break;
            }

        }
    }
}
