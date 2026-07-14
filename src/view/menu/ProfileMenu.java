package view.menu;

import controller.menu.CommandParser;
import controller.menu.MenuController;
import util.ParsedCommand;

public class ProfileMenu extends Menu {

    public ProfileMenu(MenuController controller) {
        super(controller);
    }

    @Override
    public void runMenu() {
        CommandParser parser = new CommandParser();
        MenuController ctrl = (MenuController) this.controller;
        while (true) {
            String input = view.getInput("profile menu");
            ParsedCommand cmd = parser.parse(input);

            if (input.equalsIgnoreCase("back")) {
                manager.setCurrentMenu(new MainMenu(controller));
                break;
            }

            if(cmd.getAction().equalsIgnoreCase("menu profile change-username")){
                view.showMessage(controller.processProfile(cmd , "change-username"));
            }
            else if (cmd.getAction().equalsIgnoreCase("menu profile change-nickname")){
                view.showMessage(controller.processProfile(cmd , "change-nickname"));
            }
            else if (cmd.getAction().equalsIgnoreCase("menu profile change-email")){
                view.showMessage(controller.processProfile(cmd , "change-email"));
            }
            else if (cmd.getAction().equalsIgnoreCase("menu profile change-password")){
                view.showMessage(controller.processProfile(cmd , "change-password"));
            }
            else if (cmd.getAction().equalsIgnoreCase("menu profile show-info")){
                view.showMessage(controller.processProfile(cmd , "show-info"));
            }
        }
    }
}