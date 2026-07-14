package controller.menu;

import util.ParsedCommand;
import java.util.Arrays;
import java.util.List;

public class CommandParser {

    private final List<String> KNOWN_ACTIONS = Arrays.asList(
            // منوهای پایه
            "menu enter chapter", "menu enter", "menu show current", "menu exit", "menu logout",
            "register", "pick question", "login", "forget password", "answer","new password","menu settings","menu profile" , "menu news"
            ,"menu play","menu play","menu enter chapter",
            // ورود به منوهای مختلف
            "menu collection", "menu greenhouse", "menu travel-log", "menu leaderboard",
            "menu coin-wallet", "menu gem-wallet", "menu settings change-difficulty",
            "menu news show-unread", "menu news show-all",
            // کالکشن
            "menu collection show-plants","menu collection show-all-plants","menu collection show-zombies"
            ,"menu collection show-all-zombies","menu collection show-plant","menu collection show-zombie"
            ,"menu collection upgrade-plant","menu collection purchase-plant",
            // پروفایل
            "menu profile change-username", "menu profile change-nickname", "menu profile change-email",
            "menu profile change-password", "menu profile show-info",
            // کالکشن و قبل از بازی
            "show all plants", "show available plants", "add plant", "remove plant", "boost plant", "start game",
            // داخل بازی
            "advance time", "collect sun", "show sun amount", "plant plant", "pluck plant", "feed plant",
            "show map", "show plants status", "show tile status", "zombies info",
            // گلخانه و فروشگاه
            "show greenhouse", "collect", "grow", "enter shop", "shop list", "shop daily", "shop buy",
            // لاگ و چیت‌ها
            "travel log page", "cheat add-plant-food", "cheat remove-cooldown", "cheat add", "cheat spawn-zombie","menu cheat add"
    );

    public CommandParser() {
        // مرتب‌سازی بر اساس طول (نزولی) تا دستورات چند کلمه‌ای زودتر مچ شوند
        // مثلاً "menu enter chapter" قبل از "menu enter" چک می‌شود
        KNOWN_ACTIONS.sort((a, b) -> Integer.compare(b.length(), a.length()));
    }

    public ParsedCommand parse(String input) {
        // تمیز کردن ورودی و حذف فاصله‌های اضافی
        String normalizedInput = input.trim().replaceAll("\\s+", " ");
        String lowerInput = normalizedInput.toLowerCase();

        String matchedAction = "unknown";
        String remainingPart = "";

        // ۱. پیدا کردن عملیات اصلی (Action)
        for (String action : KNOWN_ACTIONS) {
            if (lowerInput.startsWith(action)) {
                matchedAction = action;
                // جدا کردن بقیه رشته (که شامل آرگومان‌هاست)
                if (normalizedInput.length() > action.length()) {
                    remainingPart = normalizedInput.substring(action.length()).trim();
                }
                break;
            }
        }

        ParsedCommand command = new ParsedCommand(matchedAction);

        // اگر دستور ناشناخته بود، سریعاً برگرد
        if (matchedAction.equals("unknown")) {
            return command;
        }

        // ۲. استخراج آرگومان‌ها (Flags)
        if (!remainingPart.isEmpty()) {
            if (remainingPart.startsWith("-")) {
                // حالتی که فلگ داریم (مثل: -u ali -p pass1 pass2)
                String[] parts = remainingPart.split("(?=\\s-)"); // اسپلیت کردن بر اساس اسپیسِ قبل از دش
                for (String part : parts) {
                    part = part.trim();
                    int spaceIndex = part.indexOf(" ");
                    if (spaceIndex != -1) {
                        String flag = part.substring(0, spaceIndex);
                        String value = part.substring(spaceIndex + 1).trim();
                        command.addArg(flag, value);
                    } else {
                        // فلگ‌های بدون مقدار (مثل -stay-logged-in)
                        command.addArg(part, "true");
                    }
                }
            } else {
                // حالتی که فلگ نداریم اما مقدار داریم (مثل: menu enter Greenhouse یا collect (2, 3))
                command.addArg("VALUE", remainingPart);
            }
        }

        return command;
    }
}