package controller.menu;

import model.Game;
import view.GameView;

public class GameController {
    private final Game game;
    private final GameView view;

    public GameController(Game game, GameView view) {
        this.game = game;
        this.view = view;
    }

    public void startGame() {
        game.start();
        view.showBoard(game.getBoard());
    }
}

