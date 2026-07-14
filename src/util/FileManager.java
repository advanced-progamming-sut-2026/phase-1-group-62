package util;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.reflect.TypeToken;
import model.Settings;
import model.User;
import model.enums.Gender;

import java.io.*;
import java.util.ArrayList;
import java.util.List;

public class FileManager {
    private static final String FOLDER_PATH = "database";
    private static final String FILE_PATH = FOLDER_PATH + "/users.json";
    private static final Gson gson = new GsonBuilder().setPrettyPrinting().create();
    private static final String SETTINGS_PATH = FOLDER_PATH + "/settings.json";

    public static void saveUsers(List<User> users) {
        // ۱. ساختنِ پوشه اگر وجود ندارد
        File folder = new File(FOLDER_PATH);
        if (!folder.exists()) {
            folder.mkdir();
        }

        // ۲. نوشتنِ فایل
        try (FileWriter writer = new FileWriter(FILE_PATH)) {
            gson.toJson(users, writer);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
    public static void updateUser(User updatedUser) {
        List<User> users = loadUsers();
        for (int i = 0; i < users.size(); i++) {
            if (users.get(i).getUsername().equalsIgnoreCase(updatedUser.getUsername())) {
                users.set(i, updatedUser);
                break;
            }
        }
        saveUsers(users);
    }

    public static Settings loadSettings() {
        try {
            java.io.File file = new java.io.File(SETTINGS_PATH);
            if (!file.exists()) {
                if (file.getParentFile() != null) {
                    file.getParentFile().mkdirs();
                }
                Settings defaultSettings = new Settings();
                saveSettings(defaultSettings);
                return defaultSettings;
            }
            java.io.FileReader reader = new java.io.FileReader(SETTINGS_PATH);
            Settings settings = gson.fromJson(reader, Settings.class);
            reader.close();
            if (settings == null) {
                return new Settings();
            }
            return settings;
        } catch (Exception e) {
            return new Settings();
        }
    }

    public static void saveSettings(Settings settings) {
        try {
            java.io.FileWriter writer = new java.io.FileWriter(SETTINGS_PATH);
            gson.toJson(settings, writer);
            writer.close();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public static boolean isUsernameExists(String username) {
        List<User> users = loadUsers();
        for (User user : users) {
            if (user.getUsername().equalsIgnoreCase(username)) {
                return true;
            }
        }
        return false;
    }

    public static boolean checkPassword(String username, String hashedConfirmPassword) {
        List<User> users = loadUsers();
        for (User user : users) {
            if (user.getUsername().equalsIgnoreCase(username)) {
                return user.getPassword().equals(hashedConfirmPassword);
            }
        }
        return false;
    }

    public static List<User> loadUsers() {
        File file = new File(FILE_PATH);
        if (!file.exists()) {
            return new ArrayList<>();
        }
        try (FileReader reader = new FileReader(FILE_PATH)) {
            // خواندن لیست از فایل
            List<User> users = gson.fromJson(reader, new TypeToken<List<User>>(){}.getType());


            return (users != null) ? users : new ArrayList<>();

        } catch (IOException e) {
            e.printStackTrace();
            return new ArrayList<>();
        }

    }
    public static User getUser(String username) {
        List<User> users = loadUsers();
        for (User user : users) {
            if (user.getUsername().equalsIgnoreCase(username)) {
                return user;
            }
        }
        return null;
    }
    public void changeDifficulty(int newDifficulty) {

        Settings settings = FileManager.loadSettings();

        settings.setDifficulty(newDifficulty);

        FileManager.saveSettings(settings);

    }
}