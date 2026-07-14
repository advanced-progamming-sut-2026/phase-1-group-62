package model;

public class UserSession {
    private static User currentUser;

    public static User getCurrentUser() {
        return currentUser;
    }

    public static void setCurrentUser(User user) {
        currentUser = user;
    }

    public static boolean isLoggedIn() {
        return currentUser != null;
    }

    public static void clear() {
        currentUser = null;
    }

    public static int getCoins() {
        return currentUser != null ? currentUser.getCoins() : 0;
    }

    public static void addCoins(int amount) {
        if (currentUser != null) {
            currentUser.setCoins(currentUser.getCoins() + amount);
        }
    }

    public static boolean spendCoins(int amount) {
        if (currentUser != null && currentUser.getCoins() >= amount) {
            currentUser.setCoins(currentUser.getCoins() - amount);
            return true;
        }
        return false;
    }

    public static int getGems() {
        return currentUser != null ? currentUser.getGems() : 0;
    }

    public static void addGems(int amount) {
        if (currentUser != null) {
            currentUser.setGems(currentUser.getGems() + amount);
        }
    }

    public static boolean spendGems(int amount) {
        if (currentUser != null && currentUser.getGems() >= amount) {
            currentUser.setGems(currentUser.getGems() - amount);
            return true;
        }
        return false;
    }
}