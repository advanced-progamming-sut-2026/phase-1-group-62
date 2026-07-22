package view;

import controller.menu.Validator;
import util.HelpGuide;
import java.util.List;
import java.util.Scanner;

public class TerminalView extends View {
    private final Scanner scanner = new Scanner(System.in);

    @Override
    public String getInput(String prompt) {
        while (true) {
            System.out.print(prompt + "> ");
            String input = scanner.nextLine().trim();

            if (input.equalsIgnoreCase("guide") || input.equalsIgnoreCase("help")) {
                String menuKey = extractMenuKey(prompt);
                showMessage(HelpGuide.getGuideForMenu(menuKey));
                continue;
            }

            return input;
        }
    }

    private String extractMenuKey(String prompt) {
        if (prompt == null) return "main";
        String lower = prompt.toLowerCase();
        if (lower.contains("register")) return "register";
        if (lower.contains("login")) return "login";
        if (lower.contains("game")) return "game";
        if (lower.contains("play")) return "play";
        if (lower.contains("profile")) return "profile";
        if (lower.contains("news")) return "news";
        if (lower.contains("settings")) return "settings";
        if (lower.contains("collection")) return "collection";
        if (lower.contains("travel") || lower.contains("quest")) return "travel-log";
        if (lower.contains("greenhouse")) return "greenhouse";
        if (lower.contains("shop")) return "shop";
        if (lower.contains("leaderboard")) return "leaderboard";
        return "main";
    }

    public void showEmptyFieldMessage(String elemt){
        showMessage("you should enter" + elemt);
    }

    //userneme
    public void showUsernameExistsError() {
        showMessage("Username is already taken.");
    }

    public void showUsernameError(Validator.ValidationResult result) {
        String message = switch (result) {
            case INVALID_FORMAT -> "Username can only contain letters, numbers, and hyphens.";
            case INVALID_LENGTH -> "Username must be between 3 and 15 characters.";
            default -> "Invalid username.";
        };
        showMessage(message);
    }

    //password
    public void showPasswordError(Validator.ValidationResult result) {
        String message = switch (result) {
            case INVALID_LENGTH -> "Password must be at least 8 characters long.";
            case PASSWORD_MISMATCH -> "Password and confirmation do not match.";
            case WEAK_PASSWORD_NO_UPPER -> "Password must contain at least one uppercase letter.";
            case WEAK_PASSWORD_NO_LOWER -> "Password must contain at least one lowercase letter.";
            case WEAK_PASSWORD_NO_DIGIT -> "Password must contain at least one digit.";
            case WEAK_PASSWORD_NO_SPECIAL -> "Password must contain at least one special character.";
            default -> "Invalid password.";
        };
        showMessage(message);
    }

    public void showInvalidDisplayNameError(){
        showMessage("Nickname must be between 3 and 30 characters.");
    }

    public void showInvalidGenderError(){
        showMessage("Gender must be male or female.");
    }

    //email
    public void showEmailError(Validator.ValidationResult result) {
        String message = switch (result) {
            case INVALID_EMAIL_FORMAT -> "Invalid email format. Please check for @, dots, and valid domain.";
            default -> "Invalid email.";
        };
        showMessage(message);
    }

    public void showUnknownCommandError(){
        showMessage("unknown command");
    }

    public void showChoseQuestion(List<String> questions) {
        showMessage("Choose a question:");
        for (int i = 0; i < questions.size(); i++) {
            showMessage((i + 1) + ". " + questions.get(i));
        }
    }

    //login
    public void showSecurityQuestion(String question) {
        showMessage("Your security question is: " + question);
    }

    public void handleLoginResult(String result) {
        switch (result) {
            case "Username doesn't exist!" -> showMessage("Username doesn't exist!!");
            case "Password incorrect!" -> showMessage("Password incorrect!");
            case "Login successful!" -> showMessage("Login successful");
            default -> showMessage(result);
        }
    }

    public void handleForgetPasswordResult(String result) {
        switch (result) {
            case "Username doesn't exist!" -> showMessage("Username doesn't exist!!");
            case "Username and email doesn't match!" -> showMessage("Username and email doesn't match!");
            case "SUCCESS_username and email check" -> showMessage("Please answer the security question using: answer -a <answer>");
            default -> showMessage(result);
        }
    }

    public void handleAnswerResult(String result) {
        switch (result) {
            case "SUCCESS_answer get" -> showMessage("Identity verified successfully! enter your new password(new password -p -c.");
            case "Answer is incorrect!" -> showMessage("Answer is incorrect!");
            default -> showMessage(result);
        }
    }

    public void handleSetPasswordResult(String result) {
        switch (result) {
            case "SUCCESS_password changed" -> showMessage("Password changed successfully! You can now login with your new password.");
            case "invalid password" -> {}
            default -> showMessage(result);
        }
    }

    public void showLogoutResult(String result){
        showMessage(result);
    }
}