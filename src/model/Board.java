package model;

import model.enums.TileType;

public class Board {
    private final int rows;
    private final int columns;
    private final Tile[][] tiles;

    public Board(int rows, int columns) {
        this.rows = rows;
        this.columns = columns;
        this.tiles = new Tile[rows][columns];
        initializeTiles();
    }

    private void initializeTiles() {
        for (int row = 0; row < rows; row++) {
            for (int column = 0; column < columns; column++) {
                tiles[row][column] = new Tile(row, column);
            }
        }
    }

    public Tile getTile(int row, int column) {
        if (row < 0 || row >= rows || column < 0 || column >= columns) {
            return null;
        }
        return tiles[row][column];
    }

    public void setTileType(int row, int column, TileType type) {
        Tile tile = getTile(row, column);
        if (tile != null) {
            tile.setType(type);
        }
    }

    public boolean isTileWater(int row, int column) {
        Tile tile = getTile(row, column);
        return tile != null && tile.getType() == TileType.WATER;
    }

    public boolean isTileIce(int row, int column) {
        Tile tile = getTile(row, column);
        return tile != null && tile.getType() == TileType.ICE;
    }

    public boolean isTileGrave(int row, int column) {
        Tile tile = getTile(row, column);
        return tile != null && tile.getType() == TileType.GRAVE;
    }

    public boolean isTileGrass(int row, int column) {
        Tile tile = getTile(row, column);
        return tile != null && tile.getType() == TileType.GRASS;
    }

    public int getRows() { return rows; }
    public int getColumns() { return columns; }

    public boolean isInBounds(int row, int column) {
        return row >= 0 && row < rows && column >= 0 && column < columns;
    }
}
