package view.menu;

import controller.menu.CommandParser;
import controller.menu.MenuController;
import util.ParsedCommand;

public class CollectionMenu extends Menu {

    public CollectionMenu(MenuController controller) {
        super(controller);
    }

    @Override
    public void runMenu() {
        CommandParser parser = new CommandParser();
        MenuController ctrl = (MenuController) this.controller;

        while (true) {
            String input = view.getInput("CollectionMenu");
            ParsedCommand cmd = parser.parse(input);

            if (input.equalsIgnoreCase("back")) {
                manager.setCurrentMenu(new MainMenu(controller));
                break;
            }

            if (cmd.getAction().equalsIgnoreCase("menu collection show-plants")) {
                view.showMessage(ctrl.processCollection(cmd, "show-plants"));
            }
            else if (cmd.getAction().equalsIgnoreCase("menu collection show-all-plants")) {
                view.showMessage(ctrl.processCollection(cmd, "show-all-plants"));
            }
            else if (cmd.getAction().equalsIgnoreCase("menu collection show-zombies")) {
                view.showMessage(ctrl.processCollection(cmd, "show-zombies"));
            }
            else if (cmd.getAction().equalsIgnoreCase("menu collection show-all-zombies")) {
                view.showMessage(ctrl.processCollection(cmd, "show-all-zombies"));
            }
            else if (cmd.getAction().equalsIgnoreCase("menu collection show-plant")) {
                view.showMessage(ctrl.processCollection(cmd, "show-plant"));
            }
            else if (cmd.getAction().equalsIgnoreCase("menu collection show-zombie")) {
                view.showMessage(ctrl.processCollection(cmd, "show-zombie"));
            }
            else if (cmd.getAction().equalsIgnoreCase("menu collection upgrade-plant")) {
                view.showMessage(ctrl.processCollection(cmd, "upgrade-plant"));
            }
            else if (cmd.getAction().equalsIgnoreCase("menu collection purchase-plant")) {
                view.showMessage(ctrl.processCollection(cmd, "purchase-plant"));
            }
            else {
                view.showMessage("Invalid command inside Collection Menu.");
            }
        }
    }
}