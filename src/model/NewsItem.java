package model;

public class NewsItem {
    private String content;
    private boolean isRead;

    public NewsItem(String content) {
        this.content = content;
        this.isRead = false;
    }

    public String getContent() {
        return content;
    }

    public boolean isRead() {
        return isRead;
    }

    public void setRead(boolean read) {
        isRead = read;
    }
}