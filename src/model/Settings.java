package model;

public class Settings {
    private String autoLoginUsername;
    private int musicVolume = 50;
    private int sfxVolume = 50;
    private int difficulty = 3;

    public String getAutoLoginUsername() {
        return autoLoginUsername;
    }

    public void setAutoLoginUsername(String autoLoginUsername) {
        this.autoLoginUsername = autoLoginUsername;
    }

    public int getMusicVolume() {
        return musicVolume;
    }

    public void setMusicVolume(int musicVolume) {
        this.musicVolume = musicVolume;
    }

    public int getSfxVolume() {
        return sfxVolume;
    }

    public void setSfxVolume(int sfxVolume) {
        this.sfxVolume = sfxVolume;
    }

    public int getDifficulty() {
        return difficulty;
    }

    public void setDifficulty(int difficulty) {
        this.difficulty = difficulty;
    }
}