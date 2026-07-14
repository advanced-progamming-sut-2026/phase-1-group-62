package controller.menu;

import model.User;
import model.UserSession;
import model.entities.plant.Plant;
import model.entities.plant.loader.PlantLoader;
import util.FileManager;
import util.ParsedCommand;
import java.util.ArrayList;
import java.util.List;

public class PreGameController {
    private final List<String> selectedPlants = new ArrayList<>();
    private final List<String> boostedPlants = new ArrayList<>();
    private final int maxSlots = 8; // تعداد جایگاه‌های پیش‌فرض

    public String processCommand(ParsedCommand cmd, String action) {
        User currentUser = UserSession.getCurrentUser();
        if (currentUser == null) {
            return "Error: No user is logged in.";
        }

        // ۱. نمایش همه گیاهان بازی
        if (action.equalsIgnoreCase("show all plants")) {
            List<Plant> allPlants = PlantLoader.loadPlants();
            StringBuilder sb = new StringBuilder("All game plants:\n");
            for (Plant p : allPlants) {
                sb.append("- ").append(p.getName()).append("\n");
            }
            return sb.toString().trim();
        }

        // ۲. نمایش گیاهان آنلاک شده کاربر (موجود)
        if (action.equalsIgnoreCase("show available plants")) {
            List<String> unlocked = currentUser.getUnlockedPlants();
            if (unlocked.isEmpty()) {
                return "You have no unlocked plants.";
            }
            StringBuilder sb = new StringBuilder("Your available plants:\n");
            for (String plant : unlocked) {
                int level = currentUser.getPlantLevels().getOrDefault(plant, 1);
                sb.append("- ").append(plant).append(" (Level ").append(level).append(")");
                if (boostedPlants.contains(plant)) {
                    sb.append(" [BOOSTED]");
                }
                sb.append("\n");
            }
            return sb.toString().trim();
        }

        // ۳. اضافه کردن گیاه به لیست انتخاب‌ها
        if (action.equalsIgnoreCase("add plant")) {
            String plantName = cmd.getArg("-t");
            if (plantName == null) return "Invalid format. Use: add plant -t <type>";

            // الف) بررسی وجود گیاه در کل بازی
            boolean existsInGame = PlantLoader.loadPlants().stream()
                    .anyMatch(p -> p.getName().equalsIgnoreCase(plantName));
            if (!existsInGame) {
                return "Error: Plant type does not exist in the game.";
            }

            // ب) بررسی قفل نبودن گیاه برای کاربر (Case-insensitive)
            String exactPlantName = findExactPlantName(currentUser.getUnlockedPlants(), plantName);
            if (exactPlantName == null) {
                return "Error: This plant is locked! Purchase it from the collection menu.";
            }

            // ج) بررسی انتخاب نشدن از قبل
            if (selectedPlants.contains(exactPlantName)) {
                return "Error: " + exactPlantName + " is already selected.";
            }

            // د) بررسی پر نبودن ظرفیت اسلات‌ها
            if (selectedPlants.size() >= maxSlots) {
                return "Error: Your selection slots are full (Max " + maxSlots + ").";
            }

            selectedPlants.add(exactPlantName);
            return "Plant " + exactPlantName + " added. (" + selectedPlants.size() + "/" + maxSlots + ")";
        }

        // ۴. حذف گیاه از لیست انتخاب‌ها
        if (action.equalsIgnoreCase("remove plant")) {
            String plantName = cmd.getArg("-t");
            if (plantName == null) return "Invalid format. Use: remove plant -t <type>";

            String exactPlantName = findExactPlantName(selectedPlants, plantName);
            if (exactPlantName == null) {
                return "Error: This plant is not in your selected list.";
            }

            selectedPlants.remove(exactPlantName);
            boostedPlants.remove(exactPlantName); // در صورت حذف گیاه، بوست آن هم برداشته می‌شود
            return "Plant " + exactPlantName + " removed. (" + selectedPlants.size() + "/" + maxSlots + ")";
        }

        // ۵. بوست کردن گیاه با خرج ۲ الماس
        if (action.equalsIgnoreCase("boost plant")) {
            String plantName = cmd.getArg("-t");
            if (plantName == null) return "Invalid format. Use: boost plant -t <type>";

            String exactPlantName = findExactPlantName(currentUser.getUnlockedPlants(), plantName);
            if (exactPlantName == null) {
                return "Error: You can only boost plants you own.";
            }

            if (boostedPlants.contains(exactPlantName)) {
                return "Error: This plant is already boosted for this game.";
            }

            // بررسی هزینه (۲ الماس)
            if (currentUser.getGems() < 2) {
                return "Error: Insufficient gems! Boost costs 2 gems. You have: " + currentUser.getGems();
            }

            // کسر الماس و ذخیره در دیتابیس
            currentUser.setGems(currentUser.getGems() - 2);
            FileManager.updateUser(currentUser);
            UserSession.setCurrentUser(currentUser);

            boostedPlants.add(exactPlantName);
            return "Plant " + exactPlantName + " boosted successfully! 2 gems deducted.";
        }

        // ۶. شروع بازی
        if (action.equalsIgnoreCase("start game")) {
            if (selectedPlants.isEmpty()) {
                return "Error: You must select at least one plant to start the game.";
            }
            // اینجا بازی را با لیست selectedPlants و boostedPlants استارت می‌زنیم
            return "START_GAME_CONFIRMED";
        }

        return "invalid command";
    }

    // یک متد کمکی برای مقایسه بدون حساسیت به حروف بزرگ و کوچک (Case-Insensitive search)
    private String findExactPlantName(List<String> list, String searchName) {
        for (String s : list) {
            if (s.equalsIgnoreCase(searchName)) {
                return s;
            }
        }
        return null;
    }

    // گترها برای فرستادن اطلاعات نهایی به کلاس بازی
    public List<String> getSelectedPlants() { return selectedPlants; }
    public List<String> getBoostedPlants() { return boostedPlants; }
}