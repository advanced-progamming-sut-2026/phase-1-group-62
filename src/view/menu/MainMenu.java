package view.menu;

import controller.menu.CommandParser;
import controller.menu.MenuController;
import util.ParsedCommand;

public class MainMenu extends Menu {

    public MainMenu(MenuController controller) {
        super(controller);
    }

    @Override
    public void runMenu() {
        CommandParser parser = new CommandParser();
        MenuController ctrl = (MenuController) this.controller;

        while (true) {
            String input = view.getInput("main menu");
            ParsedCommand cmd = parser.parse(input);

            if (cmd.getAction().equals("menu logout")) {
                String result = ctrl.processLogOut(cmd);
                view.showLogoutResult(result);

                MenuManager.getInstance().setCurrentMenu(new LoginMenu(ctrl));
                break;
            }
            else if(cmd.getAction().equalsIgnoreCase("menu settings")){
                MenuManager.getInstance().setCurrentMenu(new SettingsMenu(ctrl) );
                break;
            }

            else if(cmd.getAction().equalsIgnoreCase("menu profile")){
                MenuManager.getInstance().setCurrentMenu(new ProfileMenu(ctrl) );
                break;
            }
            else if(cmd.getAction().equalsIgnoreCase("menu news")){
                MenuManager.getInstance().setCurrentMenu(new NewsMenu(ctrl) );
                break;
            }
            else if(cmd.getAction().equalsIgnoreCase("menu play")){
                MenuManager.getInstance().setCurrentMenu(new PlayMenu(ctrl) );
                break;
            }
            else if(cmd.getAction().equalsIgnoreCase("menu collection")){
                MenuManager.getInstance().setCurrentMenu(new CollectionMenu(ctrl) );
                break;
            }

        }
    }
}