package controller.menu;

import util.ParsedCommand;
import java.util.Arrays;
import java.util.List;

public class CommandParser {

    private final List<String> KNOWN_ACTIONS = Arrays.asList(
            // منوهای پایه
            "menu enter chapter", "menu enter minigame", "menu enter", "menu show current", "menu exit", "menu logout",
            "register", "pick question", "login", "forget password", "answer","new password","menu settings","menu profile" , "menu news"
            ,"menu play","menu play",
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
            // IZombie command
            "place zombie",
            // Beghoul commands
            "swap plants",
            "upgrade plants",   // <-- ADD THIS
            // Vasebreaker commands
            "smash vase",
            "pickup packet",
            // داخل بازی
            "advance time", "collect sun", "show sun amount", "plant plant", "pluck plant", "feed plant",
            "show map", "show plants status", "show tile status", "zombies info",
            // گلخانه و فروشگاه
            "show greenhouse", "collect", "grow", "enter shop", "shop list", "shop daily", "shop buy",
            // لاگ و چیت‌ها
            "travel log page", "cheat add-plant-food", "cheat remove-cooldown", "cheat add", "cheat spawn-zombie","menu cheat add"
    );

    public CommandParser() {
        KNOWN_ACTIONS.sort((a, b) -> Integer.compare(b.length(), a.length()));
    }

    public ParsedCommand parse(String input) {
        String normalizedInput = input.trim().replaceAll("\\s+", " ");
        String lowerInput = normalizedInput.toLowerCase();

        String matchedAction = "unknown";
        String remainingPart = "";

        for (String action : KNOWN_ACTIONS) {
            if (lowerInput.startsWith(action)) {
                matchedAction = action;
                if (normalizedInput.length() > action.length()) {
                    remainingPart = normalizedInput.substring(action.length()).trim();
                }
                break;
            }
        }

        ParsedCommand command = new ParsedCommand(matchedAction);

        if (matchedAction.equals("unknown")) {
            return command;
        }

        if (!remainingPart.isEmpty()) {
            if (remainingPart.startsWith("-")) {
                String[] parts = remainingPart.split("(?=\\s-)");
                for (String part : parts) {
                    part = part.trim();
                    int spaceIndex = part.indexOf(" ");
                    if (spaceIndex != -1) {
                        String flag = part.substring(0, spaceIndex);
                        String value = part.substring(spaceIndex + 1).trim();
                        command.addArg(flag, value);
                    } else {
                        command.addArg(part, "true");
                    }
                }
            } else {
                command.addArg("VALUE", remainingPart);
            }
        }

        return command;
    }
}
