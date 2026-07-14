package controller.menu;

import controller.NewsController;
import model.SecurityQuestions;
import model.Settings;
import model.User;
import model.UserSession;
import model.entities.zombie.Zombie;
import model.entities.plant.Plant;
import model.entities.plant.loader.PlantLoader;
import model.entities.zombie.loader.ZombieLoader;
import model.enums.Gender;
import util.FileManager;
import util.HashUtil;
import util.ParsedCommand;
import view.TerminalView;

import java.util.List;

import static util.FileManager.checkPassword;
import static util.FileManager.isUsernameExists;

public class MenuController {
    private final TerminalView view = new TerminalView();
    private String currentForgetPasswordUsername;
    private final NewsController newsController = new NewsController();

    public void addNews(String content) {
        newsController.addNewsTrigger(content);
    }
    public String processRegister(ParsedCommand cmd) {
        Validator validator = new Validator();
        Validator.ValidationResult res;
        boolean hasError = false;

        res = validator.validateUsername(cmd.getArg("-u"));
        if (res != Validator.ValidationResult.VALID) {
            view.showUsernameError(res);
            hasError = true;
        }

        if (isUsernameExists(cmd.getArg("-u"))) {
            view.showUsernameExistsError();
            hasError = true;
        }

        res = validator.validateEmail(cmd.getArg("-e"));
        if (res != Validator.ValidationResult.VALID) {
            view.showEmailError(res);
            hasError = true;
        }

        String passwordArg = cmd.getArg("-p");
        String password = null;
        String passwordConfirm = null;

        if (passwordArg != null && passwordArg.contains(" ")) {
            String[] passwords = passwordArg.split(" ");
            password = passwords[0];
            passwordConfirm = passwords[1];
        } else if (passwordArg != null) {
            password = passwordArg;
        }

        res = validator.validatePassword(password, passwordConfirm);
        if (res != Validator.ValidationResult.VALID) {
            view.showPasswordError(res);
            hasError = true;
        }

        res = validator.validateNickname(cmd.getArg("-n"));
        if (res != Validator.ValidationResult.VALID) {
            view.showInvalidDisplayNameError();
            hasError = true;
        }

        res = validator.validateGender(cmd.getArg("-g"));
        if (res != Validator.ValidationResult.VALID) {
            view.showInvalidGenderError();
            hasError = true;
        }

        if (hasError) return "invalid";

        if (cmd.getArg("-q") != null && cmd.getArg("-a") != null && cmd.getArg("-c") != null) {
            if (!cmd.getArg("-a").equals(cmd.getArg("-c"))) {
                view.showMessage("Security answer confirmation does not match!");
                return "invalid";
            }


            User newUser = new User(
                    cmd.getArg("-u"),
                    HashUtil.sha256(password),
                    cmd.getArg("-n"),
                    cmd.getArg("-e"),
                    Gender.valueOf(cmd.getArg("-g").toUpperCase()),
                    cmd.getArg("-q"),
                    HashUtil.sha256(cmd.getArg("-a"))
            );

            List<User> users = FileManager.loadUsers();
            users.add(newUser);
            FileManager.saveUsers(users);

            return "SUCCESS";
        }

        List<String> questions = SecurityQuestions.getAll();
        view.showChoseQuestion(questions);
        return "VALID_STEP_1";
    }
    public String processLogin(ParsedCommand cmd) {
        if (!cmd.hasFlag("-u") || !cmd.hasFlag("-p")) {
            return "Invalid command format. Username and password are required.";
        }

        String username = cmd.getArg("-u");
        String password = cmd.getArg("-p");
        boolean stayLoggedIn = cmd.hasFlag("-stay-logged-in");
        boolean usernameIsUniq = !isUsernameExists(username);

        if (usernameIsUniq) {
            return "Username doesn't exist!";
        }

        boolean passwordIsTrue = checkPassword(username, HashUtil.sha256(password));

        if (!passwordIsTrue) {
            return "Password incorrect!";
        }

        User user = FileManager.getUser(username);
        UserSession.setCurrentUser(user);

        if (stayLoggedIn) {
            Settings settings = FileManager.loadSettings();
            settings.setAutoLoginUsername(user.getUsername());
            FileManager.saveSettings(settings);
        }

        return "Login successful!";
    }
    public String processForgetPassword(ParsedCommand cmd) {
        if (cmd.getAction().equals("forget password")) {
            if (!cmd.hasFlag("-u") || !cmd.hasFlag("-e")) {
                return "Invalid command format. Username and email are required.";
            }

            String username = cmd.getArg("-u");
            String email = cmd.getArg("-e");
            User user = FileManager.getUser(username);

            if (user == null) {
                return "Username doesn't exist!";
            }

            if (!user.getEmail().equalsIgnoreCase(email)) {
                return "Username and email doesn't match!";
            }

            currentForgetPasswordUsername = username;

            String questionNum = user.getSecurityQuestion();
            int questionIndex = Integer.parseInt(questionNum);
            String questionText = SecurityQuestions.getQuestionByIndex(questionIndex - 1);

            view.showSecurityQuestion(questionText);
            return "SUCCESS_username and email check";
        }

        if (cmd.getAction().equals("answer")) {
            if (!cmd.hasFlag("-a")) {
                return "Invalid command format. Answer is required.";
            }

            if (currentForgetPasswordUsername == null) {
                return "Please enter forget password command first!";
            }

            User user = FileManager.getUser(currentForgetPasswordUsername);
            if (user == null) {
                return "Username doesn't exist!";
            }

            String answer = user.getSecurityAnswer();
            String inputAnswer = cmd.getArg("-a");

            if (answer.equals(HashUtil.sha256(inputAnswer))) {
                return "SUCCESS_answer get";
            } else {
                return "Answer is incorrect!";
            }
        }

        if (cmd.getAction().equals("new password")) {
            if (!cmd.hasFlag("-p") || !cmd.hasFlag("-c")) {
                return "Invalid command format. Password and confirmation are required.";
            }

            if (currentForgetPasswordUsername == null) {
                return "Please verify your identity first!";
            }

            User user = FileManager.getUser(currentForgetPasswordUsername);
            if (user == null) {
                return "Username doesn't exist!";
            }

            Validator validator = new Validator();
            Validator.ValidationResult res = validator.validatePassword(cmd.getArg("-p"), cmd.getArg("-c"));
            if (res != Validator.ValidationResult.VALID) {
                view.showPasswordError(res);
                return "invalid password";
            }

            String newPassword = cmd.getArg("-p");
            String hashedPassword = HashUtil.sha256(newPassword);

            if (hashedPassword.equals(user.getPassword())) {
                return "Please enter a new password which is different from your current password.";
            }

            user.setPassword(hashedPassword);
            FileManager.updateUser(user);

            currentForgetPasswordUsername = null;
            return "SUCCESS_password changed";
        }

        return "invalid action";
    }
    public String processLogOut(ParsedCommand cmd) {
        String username = model.UserSession.getCurrentUser().getUsername();

        model.UserSession.clear();

        model.Settings settings = FileManager.loadSettings();
        settings.setAutoLoginUsername(null);
        FileManager.saveSettings(settings);

        return "User " + username + " logged out successfully!";
    }
    public String processPlay(ParsedCommand cmd , String action){
        if(cmd.getArg("-c") != null && cmd.getArg("-c").equalsIgnoreCase("test")){
            return "ok";
        }
        User currentUser = UserSession.getCurrentUser();
        if (currentUser == null) {
            return "Error: No user is logged in.";
        }
        if (action.equalsIgnoreCase("coin-wallet")) {
            return "Your current balance: " + currentUser.getCoins() + " coins.";
        }
        if(action.equalsIgnoreCase("gem-wallet")){
            return "Your current balance: " + currentUser.getGems() + " gems."; // اینجا هم سکه نوشته بودی که به gems اصلاح شد
        }
        if (action.equalsIgnoreCase("cheat add")) {
            String[] parts = cmd.getArg("VALUE").split(" ");
            int amount = Integer.parseInt(parts[0]);
            String currency = parts[1].toLowerCase();

            if (currency.equals("coin")) {
                currentUser.setCoins(currentUser.getCoins() + amount);
            } else {
                currentUser.setGems(currentUser.getGems() + amount);
            }

            FileManager.updateUser(currentUser);
            UserSession.setCurrentUser(currentUser);

            return "Cheat activated: Added " + amount + " " + currency + "s.";
        }
        return "no";
    }
    public String processSetting(ParsedCommand cmd){
        if(cmd.getArg("-l") != null){
            Settings settings = FileManager.loadSettings();
            int newDifficulty = Integer.parseInt(cmd.getArg("-l"));
            settings.setDifficulty(newDifficulty);

            FileManager.saveSettings(settings);
            return "new difficulty: " + newDifficulty;
        }
        return "error";
    }
    public String processNews(ParsedCommand cmd, String action) {
        return newsController.processNews(cmd, action);
    }
    public String processProfile(ParsedCommand cmd, String action) {
        User currentUser = UserSession.getCurrentUser();
        if (currentUser == null) {
            return "Error: No user is logged in.";
        }
        Validator validator = new Validator();
        Validator.ValidationResult res;
        if (action.equalsIgnoreCase("change-username")) {
            String newUsername = cmd.getArg("-u");
            res = validator.validateUsername(newUsername);
            if (res != Validator.ValidationResult.VALID) {
                view.showUsernameError(res);
                return "invalid username";
            }
            if (isUsernameExists(newUsername)) {
                view.showUsernameExistsError();
                return "username exists";
            }
            currentUser.setUsername(newUsername);
            FileManager.updateUser(currentUser);
            return "Username updated successfully to: " + newUsername;
        }
        else if (action.equalsIgnoreCase("change-nickname")) {
            String newNickname = cmd.getArg("-n");
            res = validator.validateNickname(newNickname);
            if (res != Validator.ValidationResult.VALID) {
                view.showInvalidDisplayNameError();
                return "invalid nickname";
            }
            currentUser.setNickname(newNickname);
            FileManager.updateUser(currentUser);
            return "Nickname updated successfully to: " + newNickname;
        }
        else if (action.equalsIgnoreCase("change-email")) {
            String newEmail = cmd.getArg("-e");
            res = validator.validateEmail(newEmail);
            if (res != Validator.ValidationResult.VALID) {
                view.showEmailError(res);
                return "invalid email";
            }
            currentUser.setEmail(newEmail);
            FileManager.updateUser(currentUser);
            return "Email updated successfully to: " + newEmail;
        }
        else if (action.equalsIgnoreCase("change-password")) {
            String oldPassword = cmd.getArg("-o");
            String newPassword = cmd.getArg("-p");
            if (oldPassword == null || newPassword == null) {
                return "Error: Both old password (-o) and new password (-p) are required.";
            }
            String hashedOld = HashUtil.sha256(oldPassword);
            if (!currentUser.getPassword().equals(hashedOld)) {
                return "Error: Old password is incorrect.";
            }
            res = validator.validatePassword(newPassword, null);
            if (res != Validator.ValidationResult.VALID) {
                view.showPasswordError(res);
                return "invalid password";
            }
            String hashedNew = HashUtil.sha256(newPassword);
            if (hashedNew.equals(currentUser.getPassword())) {
                return "Error: New password cannot be the same as old password.";
            }
            currentUser.setPassword(hashedNew);
            FileManager.updateUser(currentUser);
            return "Password updated successfully.";
        }
        else if (action.equalsIgnoreCase("show-info")) {
            StringBuilder info = new StringBuilder();
            info.append("Username: ").append(currentUser.getUsername()).append("\n");
            info.append("Nickname: ").append(currentUser.getNickname()).append("\n");
            info.append("Email: ").append(currentUser.getEmail()).append("\n");
            info.append("Gender: ").append(currentUser.getGender()).append("\n");
            info.append("Score: ").append(currentUser.getScore());
            return info.toString();
        }
        return "error";
    }
    public String processCollection(ParsedCommand cmd, String action) {
        User currentUser = UserSession.getCurrentUser();
        if (currentUser == null) {
            return "Error: No user is logged in.";
        }
        List<Plant> allPlants = PlantLoader.loadPlants();
        List<Zombie> allZombies = ZombieLoader.loadZombies();

        if (action.equalsIgnoreCase("show-plants")) {
            List<String> unlocked = currentUser.getUnlockedPlants();
            if (unlocked.isEmpty()) {
                return "You have no unlocked plants.";
            }
            StringBuilder sb = new StringBuilder("Your unlocked plants:\n");
            for (String plant : unlocked) {
                int level = currentUser.getPlantLevels().getOrDefault(plant, 1);
                sb.append("- ").append(plant).append(" (Level ").append(level).append(")\n");
            }
            return sb.toString().trim();
        }
        if (action.equalsIgnoreCase("show-all-plants")) {
            StringBuilder sb = new StringBuilder("All game plants:\n");
            for (Plant plant : allPlants) {
                sb.append("- ").append(plant.getName()).append("\n");
            }
            return sb.toString().trim();
        }
        if (action.equalsIgnoreCase("show-zombies")) {
            List<String> observed = currentUser.getObservedZombies();
            if (observed.isEmpty()) {
                return "You have not observed any zombies yet.";
            }
            StringBuilder sb = new StringBuilder("Observed zombies:\n");
            for (String zombie : observed) {
                sb.append("- ").append(zombie).append("\n");
            }
            return sb.toString().trim();
        }
        if (action.equalsIgnoreCase("show-all-zombies")) {
            StringBuilder sb = new StringBuilder("All game zombies:\n");
            for (Zombie zombie : allZombies) {
                sb.append("- ").append(zombie.getName()).append("\n");
            }
            return sb.toString().trim();
        }
        if (action.equalsIgnoreCase("show-plant")) {
            String plantName = cmd.getArg("-p");
            Plant targetPlant = null;
            for (Plant p : allPlants) {
                if (p.getName().equalsIgnoreCase(plantName)) {
                    targetPlant = p;
                    break;
                }
            }
            if (targetPlant == null) {
                return "Error: Plant not found in game data.";
            }
            StringBuilder sb = new StringBuilder();
            sb.append("Plant: ").append(targetPlant.getName()).append("\n");
            sb.append("Sun Cost: ").append(targetPlant.getCost()).append("\n");
            sb.append("HP: ").append(targetPlant.getHealth()).append("\n");
            sb.append("Shoot Behavior: ").append(targetPlant.getShootBehavior());
            if (targetPlant.getCooldown() > 0) {
                sb.append("\nCooldown: ").append(targetPlant.getCooldown()).append("s");
            }
            if (targetPlant.getSunProduce() > 0) {
                sb.append("\nSun Produce: ").append(targetPlant.getSunProduce());
            }
            return sb.toString();
        }
        if (action.equalsIgnoreCase("show-zombie")) {
            String zombieName = cmd.getArg("-z");
            Zombie targetZombie = null;
            for (Zombie z : allZombies) {
                if (z.getName().equalsIgnoreCase(zombieName)) {
                    targetZombie = z;
                    break;
                }
            }
            if (targetZombie == null) {
                return "Error: Zombie not found in game data.";
            }
            StringBuilder sb = new StringBuilder();
            sb.append("Zombie: ").append(targetZombie.getName()).append("\n");
            sb.append("HP: ").append(targetZombie.getHealth()).append("\n");
            sb.append("Speed: ").append(targetZombie.getSpeed()).append("\n");
            sb.append("Damage: ").append(targetZombie.getDamage());
            return sb.toString();
        }
        if (action.equalsIgnoreCase("upgrade-plant")) {
            String target = cmd.getArg("-p");
            int currentLevel = currentUser.getPlantLevels().getOrDefault(target, 1);
            int upgradeCost = currentLevel * 1000;
            if (currentUser.getCoins() < upgradeCost) {
                return "Error: Insufficient coins. Required: " + upgradeCost + ", You have: " + currentUser.getCoins();
            }
            currentUser.setCoins(currentUser.getCoins() - upgradeCost);
            currentUser.getPlantLevels().put(target, currentLevel + 1);
            FileManager.updateUser(currentUser);
            return "Plant " + target + " upgraded to Level " + (currentLevel + 1) + " successfully!";
        }
        if (action.equalsIgnoreCase("purchase-plant")) {
            String target = cmd.getArg("-p");
            if (currentUser.getUnlockedPlants().contains(target)) {
                return "Error: You already own this plant.";
            }
            if (currentUser.getCoins() < 2000) {
                return "Error: Not enough coins. Cost is 2000. You have: " + currentUser.getCoins();
            }
            currentUser.setCoins(currentUser.getCoins() - 2000);
            currentUser.getUnlockedPlants().add(target);
            currentUser.getPlantLevels().put(target, 1);
            FileManager.updateUser(currentUser);
            return "Plant " + target + " purchased successfully for 2000 coins!";
        }
        return "error";
    }


}


