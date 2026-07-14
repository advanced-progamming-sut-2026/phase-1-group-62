package model;

import model.enums.Gender;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class User {
    private String username;
    private String passwordHash;
    private String nickname;
    private String email;
    private Gender gender;
    private String securityQuestion;
    private String securityAnswer;
    private int score;
    private List<NewsItem> news = new ArrayList<>();
    private int coins;
    private int gems;
    private List<String> unlockedPlants = new ArrayList<>();
    private List<String> observedZombies = new ArrayList<>();
    private Map<String, Integer> plantLevels = new HashMap<>();

    public User(String username, String passwordHash, String nickname, String email, Gender gender , String securityQuestion , String securityAnswer) {
        this.username = username;
        this.passwordHash = passwordHash;
        this.nickname = nickname;
        this.email = email;
        this.gender = gender;
        this.securityQuestion = securityQuestion;
        this.securityAnswer = securityAnswer;
        this.coins = 0;
        this.gems = 0;
        this.unlockedPlants.add("PeaShooter");
        this.plantLevels.put("PeaShooter", 1);
    }

    public String getUsername() {
        return username;
    }

    public String getPassword() {
        return passwordHash;
    }

    public String getNickname() {
        return nickname;
    }

    public String getEmail() {
        return email;
    }

    public Gender getGender() {
        return gender;
    }

    public int getScore() {
        return score;
    }

    public String getSecurityQuestion() {
        return securityQuestion;
    }

    public String getSecurityAnswer() {
        return securityAnswer;
    }

    public List<NewsItem> getNews() {
        if (news == null) {
            news = new ArrayList<>();
        }
        return news;
    }

    public int getCoins() {
        return coins;
    }

    public int getGems() {
        return gems;
    }

    public List<String> getUnlockedPlants() {
        if (unlockedPlants == null) {
            unlockedPlants = new ArrayList<>();
        }
        return unlockedPlants;
    }

    public List<String> getObservedZombies() {
        if (observedZombies == null) {
            observedZombies = new ArrayList<>();
        }
        return observedZombies;
    }

    public Map<String, Integer> getPlantLevels() {
        if (plantLevels == null) {
            plantLevels = new HashMap<>();
        }
        return plantLevels;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public void setPassword(String passwordHash) {
        this.passwordHash = passwordHash;
    }

    public void setNickname(String nickname) {
        this.nickname = nickname;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public void setGender(Gender gender) {
        this.gender = gender;
    }

    public void setScore(int score) {
        this.score = score;
    }

    public void setSecurityQuestion(String securityQuestion) {
        this.securityQuestion = securityQuestion;
    }

    public void setSecurityAnswer(String securityAnswer) {
        this.securityAnswer = securityAnswer;
    }

    public void setCoins(int coins) {
        this.coins = coins;
    }

    public void setGems(int gems) {
        this.gems = gems;
    }

    public void setUnlockedPlants(List<String> unlockedPlants) {
        this.unlockedPlants = unlockedPlants;
    }

    public void setObservedZombies(List<String> observedZombies) {
        this.observedZombies = observedZombies;
    }

    public void setPlantLevels(Map<String, Integer> plantLevels) {
        this.plantLevels = plantLevels;
    }

    public void addScore(int amount) {
        score += amount;
    }

    public void addNews(String content) {
        if (this.news == null) {
            this.news = new ArrayList<>();
        }
        this.news.add(new NewsItem(content));
    }

    public boolean hasUnreadNews() {
        if (this.news == null) {
            return false;
        }
        for (NewsItem item : news) {
            if (!item.isRead()) {
                return true;
            }
        }
        return false;
    }
}