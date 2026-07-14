package view.menu;

import controller.menu.CommandParser;
import controller.menu.MenuController;
import util.ParsedCommand;

public class NewsMenu extends Menu {

    public NewsMenu(MenuController controller) {
        super(controller);
    }

    @Override
    public void runMenu() {
        CommandParser parser = new CommandParser();
        MenuController ctrl = (MenuController) this.controller;
        while (true) {
            String input = view.getInput("news menu");
            ParsedCommand cmd = parser.parse(input);

            controller.addNews("New plant unlocked: Sunflower!");

            if (input.equalsIgnoreCase("back")) {
                manager.setCurrentMenu(new MainMenu(controller));
                break;
            }
            if(cmd.getAction().equalsIgnoreCase("menu news show-unread")){
                view.showMessage(ctrl.processNews(cmd , "show unread"));
            }
            if(cmd.getAction().equalsIgnoreCase("menu news show-all")){
                view.showMessage(ctrl.processNews(cmd , "show all"));
            }

        }
    }
}