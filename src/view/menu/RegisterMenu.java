package view.menu;

import controller.menu.MenuController;
import controller.menu.CommandParser;
import util.ParsedCommand;

public class RegisterMenu extends Menu {

    public RegisterMenu(MenuController controller) {
        super(controller);
    }

    @Override
    public void runMenu() {
        MenuController ctrl = (MenuController) this.controller;
        CommandParser parser = new CommandParser();
        ParsedCommand currentCmd = null;

        while (true) {
            if (currentCmd == null) {
                String input = view.getInput("Register");

                if (input.equalsIgnoreCase("back")) {
                    manager.setCurrentMenu(null);
                    break;
                }

                ParsedCommand cmd = parser.parse(input);

                if (!cmd.getAction().equals("register")) {
                    view.showMessage("Invalid command. Use register format.");
                    continue;
                }

                String result = ctrl.processRegister(cmd);

                if (result.equals("VALID_STEP_1")) {
                    currentCmd = cmd;
                } else {
                    view.showMessage("Please try again or type 'back' to return.");
                }
            } else {
                String questionSelection = view.getInput("Choose Question (pick question -q <number> -a <answer> -c <confirm>)");

                if (questionSelection.equalsIgnoreCase("back")) {
                    currentCmd = null;
                    continue;
                }

                if (!questionSelection.startsWith("pick question")) {
                    view.showMessage("Invalid format. Please use: pick question -q <number> -a <answer> -c <confirm>");
                    continue;
                }

                ParsedCommand securityPart = parser.parse(questionSelection);

                if (securityPart.hasFlag("-q") && securityPart.hasFlag("-a") && securityPart.hasFlag("-c")) {
                    currentCmd.addArg("-q", securityPart.getArg("-q"));
                    currentCmd.addArg("-a", securityPart.getArg("-a"));
                    currentCmd.addArg("-c", securityPart.getArg("-c"));

                    String result = ctrl.processRegister(currentCmd);

                    if (result.equals("SUCCESS")) {
                        view.showMessage("Registration successful!");
                        manager.setCurrentMenu(new LoginMenu(controller));
                        break;
                    } else {
                        view.showMessage("Registration failed. Answer confirmation does not match. Please try picking the question again.");
                    }
                } else {
                    view.showMessage("Invalid format. Please use: pick question -q <number> -a <answer> -c <confirm>");
                }
            }
        }
    }
}