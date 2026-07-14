package model.leaderboard;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.stream.Collectors;
import model.User;

public class Leaderboard {
    private final List<User> users;
    private SortType currentSortType;

    public enum SortType {
        BY_SCORE,
        BY_LEVEL,
        BY_MINI_GAMES,
        BY_QUESTS,
        BY_BONUS
    }

    public Leaderboard() {
        this.users = new ArrayList<>();
        this.currentSortType = SortType.BY_SCORE;
    }

    public void addUser(User user) {
        users.add(user);
    }

    public void removeUser(User user) {
        users.remove(user);
    }

    public List<User> getTopUsers() {
        return getTopUsers(10);
    }

    public List<User> getTopUsers(int limit) {
        return sortUsers(currentSortType).stream()
                .limit(limit)
                .collect(Collectors.toList());
    }

    public List<User> sortUsers(SortType sortType) {
        this.currentSortType = sortType;
        List<User> sorted = new ArrayList<>(users);
        
        switch (sortType) {
            case BY_SCORE:
                sorted.sort(Comparator.comparingInt(User::getScore).reversed());
                break;
            case BY_LEVEL:
                sorted.sort((u1, u2) -> {
                    // Compare by highest level completed
                    int level1 = getHighestLevel(u1);
                    int level2 = getHighestLevel(u2);
                    return Integer.compare(level2, level1);
                });
                break;
            case BY_MINI_GAMES:
                sorted.sort((u1, u2) -> {
                    int games1 = getMiniGamesCompleted(u1);
                    int games2 = getMiniGamesCompleted(u2);
                    return Integer.compare(games2, games1);
                });
                break;
            case BY_QUESTS:
                sorted.sort((u1, u2) -> {
                    int quests1 = getQuestsCompleted(u1);
                    int quests2 = getQuestsCompleted(u2);
                    return Integer.compare(quests2, quests1);
                });
                break;
            case BY_BONUS:
                sorted.sort((u1, u2) -> {
                    int bonus1 = getHighestBonus(u1);
                    int bonus2 = getHighestBonus(u2);
                    return Integer.compare(bonus2, bonus1);
                });
                break;
        }
        return sorted;
    }

    private int getHighestLevel(User user) {
        // In real implementation, this would come from user data
        return user.getScore() / 100;
    }

    private int getMiniGamesCompleted(User user) {
        // In real implementation, this would come from user data
        return user.getScore() / 50;
    }

    private int getQuestsCompleted(User user) {
        // In real implementation, this would come from user data
        return user.getScore() / 25;
    }

    private int getHighestBonus(User user) {
        // In real implementation, this would come from user data
        return user.getScore() / 10;
    }

    public List<User> getUsers() {
        return new ArrayList<>(users);
    }

    public SortType getCurrentSortType() {
        return currentSortType;
    }

    public void clear() {
        users.clear();
    }

    public int size() {
        return users.size();
    }

    public User getUserByRank(int rank) {
        if (rank < 0 || rank >= users.size()) return null;
        return sortUsers(currentSortType).get(rank);
    }

    public int getUserRank(User user) {
        List<User> sorted = sortUsers(currentSortType);
        return sorted.indexOf(user);
    }
}
