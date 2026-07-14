package model.entities.plant;

import model.enums.PlantTag;

public class Plant {
    private String name;
    private int cost;
    private int health;
    private int damage;
    private PlantTag tag;
    private String shootBehavior;
    private double cooldown;
    private int sunProduce;
    private int produceInterval;

    public Plant(String name, int cost, int health, int damage, PlantTag tag, String shootBehavior, double cooldown, int sunProduce, int produceInterval) {
        this.name = name;
        this.cost = cost;
        this.health = health;
        this.damage = damage;
        this.tag = tag;
        this.shootBehavior = shootBehavior;
        this.cooldown = cooldown;
        this.sunProduce = sunProduce;
        this.produceInterval = produceInterval;
    }

    public void takeDamage(int amount) {
        health -= amount;
    }

    public boolean isAlive() {
        return health > 0;
    }

    public String getName() {
        return name;
    }

    public int getCost() {
        return cost;
    }

    public int getHealth() {
        return health;
    }

    public int getDamage() {
        return damage;
    }

    public PlantTag getTag() {
        return tag;
    }

    public String getShootBehavior() {
        return shootBehavior;
    }

    public double getCooldown() {
        return cooldown;
    }

    public int getSunProduce() {
        return sunProduce;
    }

    public int getProduceInterval() {
        return produceInterval;
    }
}