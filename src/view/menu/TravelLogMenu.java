package view.menu;

import controller.menu.CommandParser;
import controller.menu.MenuController;
import util.ParsedCommand;

public class TravelLogMenu extends Menu{
    public TravelLogMenu(MenuController controller) {
        super(controller);
    }

    @Override
    public void runMenu() {
        CommandParser parser = new CommandParser();
        MenuController ctrl = (MenuController) this.controller;
        while (true) {
            String input = view.getInput("TravelLogMenu");
            ParsedCommand cmd = parser.parse(input);

            if (input.equalsIgnoreCase("back")) {
                manager.setCurrentMenu(new PlayMenu(controller));
                break;
            }

        }
    }
}
