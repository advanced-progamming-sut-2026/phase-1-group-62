package model;

public class Bullet {
    private int damage;
    private int row;
    private int column;
    private BulletType type;
    private boolean pierce;
    private boolean explosive;
    private int splashRadius;
    private int targetRow; // For homing/burst bullets
    private boolean active;

    public enum BulletType {
        NORMAL,
        ICE,        // Slows zombies
        FIRE,       // Double damage, melts ice
        POISON,     // Damage over time, ignores armor
        LASER,      // Pierces through all zombies
        LOB,        // Lobbed over obstacles
        HOMING,     // Tracks zombies
        STRIKE_THROUGH, // Hits all in lane
        ELECTRIC,   // Chain lightning
        MAGIC       // Special effects
    }

    public Bullet(int damage, int row, int column) {
        this(damage, row, column, BulletType.NORMAL, false, false, 0);
    }

    public Bullet(int damage, int row, int column, BulletType type, boolean pierce, boolean explosive, int splashRadius) {
        this.damage = damage;
        this.row = row;
        this.column = column;
        this.type = type;
        this.pierce = pierce;
        this.explosive = explosive;
        this.splashRadius = splashRadius;
        this.targetRow = row;
        this.active = true;
    }

    public void move() {
        if (type == BulletType.LOB) {
            // Lobbed bullets move slower and have arc
            column += 0.5;
        } else {
            column++;
        }
    }

    public int getDamage() { return damage; }
    public int getRow() { return row; }
    public int getColumn() { return column; }
    public void setColumn(int column) { this.column = column; }
    public BulletType getType() { return type; }
    public boolean isPierce() { return pierce; }
    public boolean isExplosive() { return explosive; }
    public int getSplashRadius() { return splashRadius; }
    public int getTargetRow() { return targetRow; }
    public void setTargetRow(int targetRow) { this.targetRow = targetRow; }
    public boolean isActive() { return active; }
    public void setActive(boolean active) { this.active = active; }

    public boolean isOutOfBounds(int maxColumns) {
        return column > maxColumns || column < 0;
    }
}
