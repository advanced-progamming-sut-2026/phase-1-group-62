package util;

public class HelpGuide {

    public static String getGuideForMenu(String menuName) {
        if (menuName == null) return "No guide available.";

        switch (menuName.toLowerCase()) {
            case "register":
                return """
                       === REGISTER MENU COMMANDS ===
                       1. register -u <username> -p <pass> <pass_confirm> -n <nickname> -e <email> -g <gender>
                       2. pick question -q <question_num> -a <answer> -c <answer_confirm>
                       3. menu enter login
                       4. menu exit
                       """;

            case "login":
                return """
                       === LOGIN MENU COMMANDS ===
                       1. login -u <username> -p <password>
                       2. login -u <username> -p <password> -stay-logged-in
                       3. forget password -u <username> -e <email>
                       4. answer -a <answer>
                       5. new password -p <new_password> -c <confirm_password>
                       6. menu exit
                       """;

            case "main":
                return """
                       === MAIN MENU COMMANDS ===
                       1. menu play
                       2. menu settings
                       3. menu profile
                       4. menu news
                       5. menu leaderboard
                       6. menu show current
                       7. menu logout
                       """;

            case "profile":
                return """
                       === PROFILE MENU COMMANDS ===
                       1. menu profile change-username -u <username>
                       2. menu profile change-nickname -n <nickname>
                       3. menu profile change-email -e <email>
                       4. menu profile change-password -p <new_password> -o <old_password>
                       5. menu profile show-info
                       6. back
                       """;

            case "news":
                return """
                       === NEWS MENU COMMANDS ===
                       1. menu news show-unread
                       2. menu news show-all
                       3. back
                       """;

            case "settings":
                return """
                       === SETTINGS MENU COMMANDS ===
                       1. menu settings change-difficulty -l <level_1_to_5>
                       2. back
                       """;

            case "play":
                return """
                       === PLAY MENU COMMANDS ===
                       1. menu enter chapter -c <chapter_name> (DarkAges, FrostbiteCaves, AncientEgypt , BigWaveBeach)
                       2. menu enter minigame -m <minigame_name> ( Vasebreaker, WallnutBowling, IZombie, Beghoul, Zombotany)
                       3. show all plants / show available plants
                       4. add plant -t <type> / remove plant -t <type>
                       5. boost plant -t <type>
                       6. start game
                       7. menu collection / menu greenhouse / menu travel-log / menu leaderboard
                       8. menu coin-wallet  / menu gem-wallet 
                       9. cheat add <count> <coin/diamond>
                       10. back
                       """;

            case "collection":
                return """
                       === COLLECTION MENU COMMANDS ===
                       1. menu collection show-plants / menu collection show-all-plants
                       2. menu collection show-zombies / menu collection show-all-zombies
                       3. menu collection show-plant -p <plant_name>
                       4. menu collection show-zombie -z <zombie_name>
                       5. menu collection purchase-plant -p <plant_name>
                       6. menu collection upgrade-plant -p <plant_name>
                       7. back
                       """;

            case "travel-log":
            case "quest":
                return """
                       === TRAVEL LOG / QUESTS COMMANDS ===
                       1. travel log page <daily/story/epic/minigame>
                       2. claim
                       3. back
                       """;

            case "game":
            case "gameplay":
                return """
                       === IN-GAME COMMANDS ===
                       1. advance time -t <count> ticks (or press Enter)
                       2. show sun amount
                       3. collect sun -l (<x>, <y>)
                       4. plant plant -t <type> -l (<x>, <y>)
                       5. pluck plant -l (<x>, <y>)
                       6. feed plant -l (<x>, <y>)
                       7. show map
                       8. show plant status / show tile status -l (<x>, <y>)
                       9. zombies info
                       10. MINI-GAME COMMANDS:
                          - Vasebreaker: smash vase -l (<x>, <y>) | pickup packet -l (<x>, <y>)
                          - I,Zombie: place zombie -t <type> -l <lane>
                          - Beghouled: swap plants -l (<x1>,<y1>) -m (<x2>,<y2>) | upgrade plants -f <from> -t <to>
                          - Wallnut Bowling: plant plant -t <type> -l (<x>, <y>) 
                       11. CHEATS:
                          - cheat add <count> suns
                          - cheat add-plant-food
                          - cheat remove-cooldown
                          - cheat spawn-zombie -t <type> -l (<x>, <y>)
                          - release the nuke
                       12. exit game
                       """;

            case "greenhouse":
                return """
                       === GREENHOUSE COMMANDS ===
                       1. show greenhouse
                       2. plant pot at (<x>, <y>)
                       3. collect (<x>, <y>)
                       4. grow (<x>, <y>)
                       5. unlock (<x>, <y>)
                       6. enter shop
                       7. back
                       """;

            case "shop":
                return """
           === SHOP COMMANDS ===
           1. shop list (View permanent shop items)
           2. shop daily (View 20% discounted daily offer)
           3. shop buy -i <item_id> -n <count> [-t <plant_type>]
              - Daily Offer Example: shop buy -i peashooter -n 1
           4. back
           """;

            case "leaderboard":
                return """
                       === LEADERBOARD COMMANDS ===
                       1. menu leaderboard
                       2. menu leaderboard -s <score/level/minigame/dailyquest/nondailyquest/scoring> -o <asc/desc>
                       3. back
                       """;

            default:
                return "Type 'guide' or 'help' to view available commands for this menu.";
        }
    }
}