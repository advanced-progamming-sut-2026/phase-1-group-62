package model;

import model.enums.Gender;
import model.greenhouse.Pot;
import model.quest.Quest;
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
    private Map<String, Boolean> greenhouseBoosts = new HashMap<>();
    private int plantFoodInventory;
    private Map<String, Integer> seedPackets = new HashMap<>();
    private List<Pot> greenhousePots = new ArrayList<>();
    private List<Quest> userQuests = new ArrayList<>();

    private int lastSeasonCompleted;
    private int lastLevelCompleted;
    private int completedMiniGames;
    private int completedDailyQuests;
    private int completedNonDailyQuests;
    private int highestScoreInScoringGame;

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
        this.plantFoodInventory = 0;
        this.lastSeasonCompleted = 1;
        this.lastLevelCompleted = 1;
        this.completedMiniGames = 0;
        this.completedDailyQuests = 0;
        this.completedNonDailyQuests = 0;
        this.highestScoreInScoringGame = 0;
        for (int row = 0; row < 4; row++) {
            for (int col = 0; col < 5; col++) {
                Pot pot = new Pot(row, col);
                if (row > 0) {
                    pot.setLocked(true);
                }
                this.greenhousePots.add(pot);
            }
        }
    }

    public List<Pot> getGreenhousePots() {
        if (greenhousePots == null || greenhousePots.isEmpty()) {
            greenhousePots = new ArrayList<>();
            for (int row = 0; row < 4; row++) {
                for (int col = 0; col < 5; col++) {
                    Pot pot = new Pot(row, col);
                    if (row > 0) {
                        pot.setLocked(true);
                    }
                    greenhousePots.add(pot);
                }
            }
        }
        return greenhousePots;
    }

    public void setGreenhousePots(List<Pot> greenhousePots) {
        this.greenhousePots = greenhousePots;
    }

    public List<Quest> getUserQuests() {
        if (userQuests == null || userQuests.isEmpty()) {
            userQuests = new ArrayList<>();

            Quest q1 = new Quest("Daily Sun Collector", "Collect the specified amount of sun in a single day", Quest.QuestType.DAILY, Quest.Priority.MEDIUM);
            q1.setTarget(3000);
            q1.setRewardCoins(30);
            userQuests.add(q1);

            Quest q2 = new Quest("Chapter Hunter", "Defeat 50 zombies in the chapter", Quest.QuestType.STORY, Quest.Priority.HIGH);
            q2.setTarget(50);
            userQuests.add(q2);

            Quest q3 = new Quest("Professional Plant Slayer", "Kill 10 zombies only using plants that have attack capabilities", Quest.QuestType.DAILY, Quest.Priority.HIGH);
            q3.setTarget(10);
            userQuests.add(q3);

            Quest q4 = new Quest("Only Cactus", "Kill 10 zombies using only Cactus plants", Quest.QuestType.DAILY, Quest.Priority.HIGH);
            q4.setTarget(10);
            q4.setRewardDiamonds(20);
            userQuests.add(q4);

            Quest q5 = new Quest("Economic Vegetarian", "Win a level without losing more than n plants", Quest.QuestType.STORY, Quest.Priority.HIGH);
            q5.setVariableN(2);
            q5.setTarget(1);
            userQuests.add(q5);

            Quest q6 = new Quest("Master of Defense", "Complete a level with exactly zero suns", Quest.QuestType.EPIC, Quest.Priority.CRITICAL);
            q6.setTarget(1);
            q6.setRewardDiamonds(200);
            userQuests.add(q6);

            Quest q7 = new Quest("Quick Reflexes", "Kill 10 zombies in less than 30 seconds from the start of the first wave", Quest.QuestType.STORY, Quest.Priority.MEDIUM);
            q7.setTarget(10);
            q7.setRewardCoins(500);
            userQuests.add(q7);

            Quest q8 = new Quest("Pro Demolitionist", "Use 3 explosive plants in a single level", Quest.QuestType.DAILY, Quest.Priority.LOW);
            q8.setTarget(3);
            q8.setRewardCoins(100);
            userQuests.add(q8);

            Quest q9 = new Quest("Symmetry", "The garden must be symmetrical at the end of the game", Quest.QuestType.DAILY, Quest.Priority.HIGH);
            q9.setTarget(1);
            q9.setRewardCoins(500);
            userQuests.add(q9);

            Quest q10 = new Quest("Family Slaughter", "Only use plants from the specified family_type to kill zombies", Quest.QuestType.DAILY, Quest.Priority.MEDIUM);
            q10.setTarget(10);
            q10.setRewardCoins(1000);
            userQuests.add(q10);

            Quest q11 = new Quest("Flourish in Limitations", "Win a level without using any plants from the specified family_type", Quest.QuestType.DAILY, Quest.Priority.HIGH);
            q11.setTarget(1);
            q11.setRewardDiamonds(100);
            userQuests.add(q11);

            Quest q12 = new Quest("Night or Day", "Complete a day level using only night plants (mushrooms)", Quest.QuestType.EPIC, Quest.Priority.HIGH);
            q12.setTarget(1);
            q12.setRewardDiamonds(20);
            userQuests.add(q12);

            Quest q13 = new Quest("Win Streak", "Win 5 levels in a row at the highest difficulty", Quest.QuestType.DAILY, Quest.Priority.HIGH);
            q13.setTarget(5);
            q13.setRewardCoins(5000);
            userQuests.add(q13);

            Quest q14 = new Quest("Almost Victorious", "Kill 10 zombies in the first column of a row that has no lawn mower", Quest.QuestType.DAILY, Quest.Priority.MEDIUM);
            q14.setTarget(10);
            q14.setRewardCoins(300);
            userQuests.add(q14);

            Quest q15 = new Quest("OCD Obsession", "Win a level where there is no symmetry in the garden (except for the middle row)", Quest.QuestType.DAILY, Quest.Priority.MEDIUM);
            q15.setTarget(1);
            q15.setRewardCoins(800);
            userQuests.add(q15);

            Quest q16 = new Quest("Cloudy Day", "Complete a level using only a maximum of 3 sun-producing plants", Quest.QuestType.DAILY, Quest.Priority.HIGH);
            q16.setTarget(1);
            q16.setRewardDiamonds(10);
            userQuests.add(q16);

            Quest q17 = new Quest("Empty Column", "Win a level without planting anything in column n", Quest.QuestType.DAILY, Quest.Priority.HIGH);
            q17.setVariableN(5);
            q17.setTarget(1);
            q17.setRewardDiamonds(10);
            userQuests.add(q17);

            Quest q18 = new Quest("Empty Row", "Win a level without planting anything in row n", Quest.QuestType.DAILY, Quest.Priority.HIGH);
            q18.setVariableN(3);
            q18.setTarget(1);
            q18.setRewardDiamonds(20);
            userQuests.add(q18);

            Quest q19 = new Quest("Defenseless Cross", "Win a level where row n and column n are completely empty", Quest.QuestType.DAILY, Quest.Priority.HIGH);
            q19.setVariableN(4);
            q19.setTarget(1);
            q19.setRewardDiamonds(25);
            userQuests.add(q19);

            Quest q20 = new Quest("Lawn Mowing Time", "Kill at least n zombies using lawn mowers", Quest.QuestType.EPIC, Quest.Priority.MEDIUM);
            q20.setVariableN(20);
            q20.setTarget(20);
            userQuests.add(q20);
        }
        return userQuests;
    }

    public void setUserQuests(List<Quest> userQuests) {
        this.userQuests = userQuests;
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

    public Map<String, Boolean> getGreenhouseBoosts() {
        if (greenhouseBoosts == null) {
            greenhouseBoosts = new HashMap<>();
        }
        return greenhouseBoosts;
    }

    public int getPlantFoodInventory() {
        return plantFoodInventory;
    }

    public Map<String, Integer> getSeedPackets() {
        if (seedPackets == null) {
            seedPackets = new HashMap<>();
        }
        return seedPackets;
    }

    public int getLastSeasonCompleted() {
        return lastSeasonCompleted;
    }

    public int getLastLevelCompleted() {
        return lastLevelCompleted;
    }

    public int getCompletedMiniGames() {
        return completedMiniGames;
    }

    public int getCompletedDailyQuests() {
        return completedDailyQuests;
    }

    public int getCompletedNonDailyQuests() {
        return completedNonDailyQuests;
    }

    public int getHighestScoreInScoringGame() {
        return highestScoreInScoringGame;
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

    public void setGreenhouseBoosts(Map<String, Boolean> greenhouseBoosts) {
        this.greenhouseBoosts = greenhouseBoosts;
    }

    public void setPlantFoodInventory(int plantFoodInventory) {
        this.plantFoodInventory = plantFoodInventory;
    }

    public void setSeedPackets(Map<String, Integer> seedPackets) {
        this.seedPackets = seedPackets;
    }

    public void setLastSeasonCompleted(int lastSeasonCompleted) {
        this.lastSeasonCompleted = lastSeasonCompleted;
    }

    public void setLastLevelCompleted(int lastLevelCompleted) {
        this.lastLevelCompleted = lastLevelCompleted;
    }

    public void setCompletedMiniGames(int completedMiniGames) {
        this.completedMiniGames = completedMiniGames;
    }

    public void setCompletedDailyQuests(int completedDailyQuests) {
        this.completedDailyQuests = completedDailyQuests;
    }

    public void setCompletedNonDailyQuests(int completedNonDailyQuests) {
        this.completedNonDailyQuests = completedNonDailyQuests;
    }

    public void setHighestScoreInScoringGame(int highestScoreInScoringGame) {
        this.highestScoreInScoringGame = highestScoreInScoringGame;
    }

    public void addScore(int amount) {
        score += amount;
    }

    /**
     * Updates progress for all quests of a specific type
     */
    public void updateQuestProgress(Quest.QuestType type, int amount) {
        for (Quest q : getUserQuests()) {
            if (q.getType() == type && q.getStatus() != Quest.QuestStatus.COMPLETED && q.getStatus() != Quest.QuestStatus.CLAIMED) {
                q.updateProgress(amount);
            }
        }
    }

    /**
     * Updates progress for quests that match a specific title pattern
     */
    public void updateQuestProgressByTitle(String titlePattern, int amount) {
        for (Quest q : getUserQuests()) {
            if (q.getTitle().toLowerCase().contains(titlePattern.toLowerCase()) && 
                q.getStatus() != Quest.QuestStatus.COMPLETED && q.getStatus() != Quest.QuestStatus.CLAIMED) {
                q.updateProgress(amount);
            }
        }
    }

    /**
     * Updates progress for a specific quest by exact title
     */
    public void updateQuestProgressByExactTitle(String exactTitle, int amount) {
        for (Quest q : getUserQuests()) {
            if (q.getTitle().equalsIgnoreCase(exactTitle) && 
                q.getStatus() != Quest.QuestStatus.COMPLETED && q.getStatus() != Quest.QuestStatus.CLAIMED) {
                q.updateProgress(amount);
            }
        }
    }

    /**
     * Checks and claims all completed quests
     */
    public String claimAllCompletedQuests() {
        StringBuilder result = new StringBuilder();
        int totalCoins = 0;
        int totalGems = 0;
        int claimedCount = 0;
        
        for (Quest q : getUserQuests()) {
            if (q.getStatus() == Quest.QuestStatus.COMPLETED) {
                q.applyReward(this);
                claimedCount++;
                totalCoins += q.getRewardCoins();
                totalGems += q.getRewardDiamonds();
                result.append("✓ Claimed: ").append(q.getTitle()).append("\n");
            }
        }
        
        if (claimedCount == 0) {
            return "No quests ready to claim.";
        }
        
        result.insert(0, "Claimed " + claimedCount + " quests!\n");
        result.append("Total: ").append(totalCoins).append(" coins, ")
              .append(totalGems).append(" gems received.\n");
        return result.toString();
    }

    /**
     * Gets the number of completed quests by type
     */
    public int getCompletedQuestCount(Quest.QuestType type) {
        int count = 0;
        for (Quest q : getUserQuests()) {
            if (q.getType() == type && q.getStatus() == Quest.QuestStatus.COMPLETED) {
                count++;
            }
        }
        return count;
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
