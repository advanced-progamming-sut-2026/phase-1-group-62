package view.menu;

import controller.menu.MenuController;
import controller.menu.CommandParser;
import util.ParsedCommand;

public class LoginMenu extends Menu {

    public LoginMenu(MenuController controller) {
        super(controller);
    }

    @Override
    public void runMenu() {
        CommandParser parser = new CommandParser();
        MenuController ctrl = (MenuController) this.controller;
        String prompt = "Login";
        while (true) {
            String input = view.getInput(prompt);

            if (input.equalsIgnoreCase("back")) {
                manager.setCurrentMenu(new RegisterMenu(controller));
                break;
            }

            ParsedCommand cmd = parser.parse(input);

            if (cmd.getAction().equals("login")) {
                String result = ctrl.processLogin(cmd);
                view.handleLoginResult(result);
                if (result.equals("Login successful!")) {
                    manager.setCurrentMenu(new MainMenu(controller));
                    break;
                }
                prompt = "Login";
            } else if (cmd.getAction().equals("forget password")) {
                String result = ctrl.processForgetPassword(cmd);
                if (result.equals("SUCCESS_username and email check")) {
                    prompt = "Login/forgetPassword";
                }
                view.handleForgetPasswordResult(result);
            } else if (cmd.getAction().equals("answer")) {
                String result = ctrl.processForgetPassword(cmd);
                if (result.equals("SUCCESS_answer get")) {
                    prompt = "Login/newPassword";
                }
                view.handleAnswerResult(result);
            } else if (cmd.getAction().equals("new password")) {
                String result = ctrl.processForgetPassword(cmd);
                if (result.equals("SUCCESS_password changed")) {
                    prompt = "Login";
                }
                view.handleSetPasswordResult(result);
            } else {
                view.showMessage("Invalid command. Use login or forget password format or type 'back'.");
            }
        }
    }
}