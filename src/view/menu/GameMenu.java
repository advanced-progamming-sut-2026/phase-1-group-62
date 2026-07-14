package view.menu;

import controller.menu.CommandParser;
import controller.menu.MenuController;
import model.Board;
import util.ParsedCommand;
import view.GameView;

public class GameMenu extends Menu {
    private final Board board;
    private final GameView gameView = new GameView();

    public GameMenu(MenuController controller) {
        super(controller);
        this.board = new Board(6, 10);
    }

    @Override
    public void runMenu() {
        CommandParser parser = new CommandParser();

        while (true) {
            gameView.showBoard(board);

            String input = view.getInput("game play");
            ParsedCommand cmd = parser.parse(input);

            if (input.equalsIgnoreCase("exit") || input.equalsIgnoreCase("exit game")) {
                view.showMessage("Returning to main menu...");
                manager.setCurrentMenu(new MainMenu(controller));
                break;
            }

            if (cmd.getAction().equalsIgnoreCase("advance time")) {
                view.showMessage("Time advanced by 1 turn.");
            }
            else {
                view.showMessage("Unknown game command. Type 'exit' to quit game.");
            }
        }
    }
}