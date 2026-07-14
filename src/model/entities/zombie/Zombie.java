package model.entities.zombie;

public class Zombie {
    private String name;
    private int health;
    private int speed;
    private int damage;


    public Zombie(String name, int health, int speed, int damage) {
        this.name = name;
        this.health = health;
        this.speed = speed;
        this.damage = damage;

    }

    public String getName() {
        return name;
    }

    public int getHealth() {
        return health;
    }

    public int getSpeed() {
        return speed;
    }

    public int getDamage() {
        return damage;
    }


}