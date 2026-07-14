package view;

import model.Board;
import model.Tile;

public class GameView extends View {
    public void showBoard(Board board) {
        showMessage("\n--- PvZ Battlefield ---");
        for (int r = 0; r < board.getRows(); r++) {
            StringBuilder rowStr = new StringBuilder();
            for (int c = 0; c < board.getColumns(); c++) {
                Tile tile = board.getTile(r, c);
                rowStr.append("[ ]");
            }
            showMessage(rowStr.toString());
        }
        showMessage("-----------------------\n");
    }
}