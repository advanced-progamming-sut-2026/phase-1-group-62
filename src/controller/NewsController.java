package controller;

import model.NewsItem;
import model.User;
import model.UserSession;
import util.FileManager;
import util.ParsedCommand;

public class NewsController {

    public String processNews(ParsedCommand cmd, String action) {
        User currentUser = UserSession.getCurrentUser();
        if (currentUser == null) {
            return "Error: No user is logged in.";
        }

        StringBuilder result = new StringBuilder();

        if (action.equalsIgnoreCase("show unread")) {
            boolean hasUnread = false;
            for (NewsItem item : currentUser.getNews()) {
                if (!item.isRead()) {
                    result.append("- ").append(item.getContent()).append("\n");
                    item.setRead(true);
                    hasUnread = true;
                }
            }
            if (!hasUnread) {
                return "No unread news.";
            }
            FileManager.updateUser(currentUser);
            return result.toString().trim();
        }

        if (action.equalsIgnoreCase("show all")) {
            if (currentUser.getNews().isEmpty()) {
                return "No news available.";
            }
            for (NewsItem item : currentUser.getNews()) {
                String status = item.isRead() ? " (Read)" : " (New!)";
                result.append("- ").append(item.getContent()).append(status).append("\n");
                item.setRead(true);
            }
            FileManager.updateUser(currentUser);
            return result.toString().trim();
        }

        return "error";
    }

    public void addNewsTrigger(String content) {
        User currentUser = UserSession.getCurrentUser();
        if (currentUser != null) {
            currentUser.addNews(content);
            FileManager.updateUser(currentUser);
        }
    }
}