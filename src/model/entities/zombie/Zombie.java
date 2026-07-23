package model.entities.zombie;

public class Zombie {
    private String name;
    private int health;
    private int maxHealth;
    private double speed;
    private int damage;
    private int waveCost;
    private double x;
    private int y;
    private int armorHealth;
    private int maxArmorHealth;
    private String armorType;
    private boolean isGlowing;
    private boolean isHypnotized;
    private double chilledDuration;
    private double frozenDuration;
    private int stolenSuns;
    private boolean isAngry;
    private boolean isTorchLit;
    private double dynamiteTimer;
    private int frozenIceHealth;
    private boolean immuneToFreeze;

    private boolean isTransformedToSheep;
    private boolean hasThrownImp;
    private boolean isCharging;
    private boolean isSubmerged;
    private boolean isSpinning;
    private int iceHitsTaken;
    private int wizardTimer;
    private int kingTimer;
    private int raStealTimer;
    private int tombraiserTimer;
    private int fishermanTimer;
    private int octopusTimer;
    private int turquoiseLaserTimer;
    private int pianoPlayTimer;

    private int zombotanyJalapenoTimer;
    private int izombieSunProductionTicks;

    public Zombie(String name, int health, double speed, int damage, int waveCost) {
        this.name = name;
        this.health = health;
        this.maxHealth = health;
        this.speed = speed;
        this.damage = damage;
        this.waveCost = waveCost;
        this.x = 8.0;
        this.y = 0;
        this.armorHealth = 0;
        this.maxArmorHealth = 0;
        this.armorType = "none";
        this.isGlowing = false;
        this.isHypnotized = false;
        this.chilledDuration = 0.0;
        this.frozenDuration = 0.0;
        this.stolenSuns = 0;
        this.isAngry = false;
        this.isTorchLit = true;
        this.dynamiteTimer = 10.0;
        this.frozenIceHealth = 0;
        this.immuneToFreeze = false;

        this.isTransformedToSheep = false;
        this.hasThrownImp = false;
        this.isCharging = false;
        this.isSubmerged = false;
        this.isSpinning = false;
        this.iceHitsTaken = 0;
        this.wizardTimer = 0;
        this.kingTimer = 0;
        this.raStealTimer = 0;
        this.tombraiserTimer = 0;
        this.fishermanTimer = 0;
        this.octopusTimer = 0;
        this.turquoiseLaserTimer = 0;
        this.pianoPlayTimer = 0;

        this.zombotanyJalapenoTimer = 0;
        this.izombieSunProductionTicks = 0;
    }

    public Zombie(String name, int health, double speed, int damage) {
        this(name, health, speed, damage, 100);
    }

    public int getWaveCost() {
        return waveCost;
    }

    public void takeDamage(int amount, boolean bypassArmor) {
        if (frozenIceHealth > 0) {
            frozenIceHealth -= amount;
            if (frozenIceHealth < 0) {
                frozenIceHealth = 0;
            }
            return;
        }
        if (bypassArmor || armorHealth <= 0) {
            health -= amount;
        } else {
            armorHealth -= amount;
            if (armorHealth < 0) {
                health += armorHealth;
                armorHealth = 0;
            }
        }
        if (health < 0) {
            health = 0;
        }
    }

    public boolean isAlive() {
        return health > 0;
    }

    public void applyChilled(double duration) {
        if (!immuneToFreeze) {
            this.chilledDuration = Math.max(this.chilledDuration, duration);
        }
    }

    public void applyFrozen(double duration) {
        if (!immuneToFreeze) {
            this.frozenDuration = Math.max(this.frozenDuration, duration);
        }
    }

    // متد اضافه شده برای رفع خطای کامپایل
    public void removeEffect(ZombieEffect effect) {
        if (effect == ZombieEffect.FROZEN) {
            this.frozenDuration = 0.0;
            this.frozenIceHealth = 0;
        } else if (effect == ZombieEffect.CHILLED) {
            this.chilledDuration = 0.0;
        }
    }

    public void updateEffects(double deltaSeconds) {
        if (chilledDuration > 0) {
            chilledDuration -= deltaSeconds;
            if (chilledDuration < 0) chilledDuration = 0;
        }
        if (frozenDuration > 0) {
            frozenDuration -= deltaSeconds;
            if (frozenDuration < 0) frozenDuration = 0;
        }
        if (dynamiteTimer > 0) {
            dynamiteTimer -= deltaSeconds;
            if (dynamiteTimer < 0) dynamiteTimer = 0;
        }
    }

    public void updateEffects() {
        updateEffects(1.0);
    }

    public void updateCooldown() {
    }

    public boolean hasEffect(ZombieEffect effect) {
        if (effect == ZombieEffect.FROZEN && (frozenDuration > 0 || frozenIceHealth > 0)) {
            return true;
        }
        if (effect == ZombieEffect.CHILLED && chilledDuration > 0) {
            return true;
        }
        return false;
    }

    public void move() {
        double currentSpeed = getEffectiveSpeed();
        if (name.equalsIgnoreCase("SquashZombie")) {
            this.x -= (currentSpeed * 2.5);
        } else if (isCharging) {
            this.x -= (currentSpeed * 3.0);
        } else {
            this.x -= currentSpeed;
        }
        if (this.x < 0) {
            this.x = 0;
        }
    }

    public void setEnraged(boolean enraged) {
        this.isAngry = enraged;
    }

    public boolean isBoss() {
        return this.name.equalsIgnoreCase("gargantuar") || this.name.equalsIgnoreCase("ZombieGargantuar");
    }

    public String getType() {
        return this.name;
    }

    public double getEffectiveSpeed() {
        if (frozenDuration > 0 || frozenIceHealth > 0) {
            return 0.0;
        }

        double currentSpeed = speed;
        if (currentSpeed <= 0) {
            currentSpeed = 1.0;
        }

        double speedPerTick = currentSpeed / 10.0;
        if (chilledDuration > 0) {
            speedPerTick /= 2.0;
        }
        if (isAngry) {
            speedPerTick *= 2.0;
        }
        return speedPerTick;
    }

    public boolean isDodoRider() {
        return name != null && (name.equalsIgnoreCase("dodo rider") || name.equalsIgnoreCase("ZombieIceAgeDodo"));
    }

    public String getName() { return name; }
    public int getHealth() { return health; }
    public int getMaxHealth() { return maxHealth; }
    public double getSpeed() { return speed; }
    public int getDamage() { return damage; }
    public double getX() { return x; }
    public void setX(double x) { this.x = x; }
    public int getY() { return y; }
    public void setY(int y) { this.y = y; }
    public int getArmorHealth() { return armorHealth; }
    public void setArmorHealth(int armorHealth) { this.armorHealth = armorHealth; this.maxArmorHealth = armorHealth; }
    public int getMaxArmorHealth() { return maxArmorHealth; }
    public void setMaxArmorHealth(int maxArmorHealth) { this.maxArmorHealth = maxArmorHealth; }
    public String getArmorType() { return armorType; }
    public void setArmorType(String armorType) { this.armorType = armorType; }
    public boolean isGlowing() { return isGlowing; }
    public void setGlowing(boolean glowing) { isGlowing = glowing; }
    public boolean isHypnotized() { return isHypnotized; }
    public void setHypnotized(boolean hypnotized) { isHypnotized = hypnotized; }
    public double getChilledDuration() { return chilledDuration; }
    public double getFrozenDuration() { return frozenDuration; }
    public int getStolenSuns() { return stolenSuns; }
    public void setStolenSuns(int stolenSuns) { this.stolenSuns = stolenSuns; }
    public boolean isAngry() { return isAngry; }
    public void setAngry(boolean angry) { this.isAngry = angry; }
    public boolean isTorchLit() { return isTorchLit; }
    public void setTorchLit(boolean torchLit) { this.isTorchLit = torchLit; }
    public double getDynamiteTimer() { return dynamiteTimer; }
    public void setDynamiteTimer(double dynamiteTimer) { this.dynamiteTimer = dynamiteTimer; }
    public int getFrozenIceHealth() { return frozenIceHealth; }
    public void setFrozenIceHealth(int frozenIceHealth) { this.frozenIceHealth = frozenIceHealth; }
    public boolean isImmuneToFreeze() { return immuneToFreeze; }
    public void setImmuneToFreeze(boolean immuneToFreeze) { this.immuneToFreeze = immuneToFreeze; }

    public boolean isTransformedToSheep() { return isTransformedToSheep; }
    public void setTransformedToSheep(boolean transformedToSheep) { isTransformedToSheep = transformedToSheep; }
    public boolean isHasThrownImp() { return hasThrownImp; }
    public void setHasThrownImp(boolean hasThrownImp) { this.hasThrownImp = hasThrownImp; }
    public boolean isCharging() { return isCharging; }
    public void setCharging(boolean charging) { isCharging = charging; }
    public boolean isSubmerged() { return isSubmerged; }
    public void setSubmerged(boolean submerged) { this.isSubmerged = submerged; }
    public boolean isSpinning() { return isSpinning; }
    public void setSpinning(boolean spinning) { this.isSpinning = spinning; }
    public int getIceHitsTaken() { return iceHitsTaken; }
    public void incrementIceHitsTaken() { this.iceHitsTaken++; }
    public void resetIceHitsTaken() { this.iceHitsTaken = 0; }

    public int getWizardTimer() { return wizardTimer; }
    public void incrementWizardTimer() { this.wizardTimer++; }
    public void resetWizardTimer() { this.wizardTimer = 0; }
    public int getKingTimer() { return kingTimer; }
    public void incrementKingTimer() { this.kingTimer++; }
    public void resetKingTimer() { this.kingTimer = 0; }
    public int getRaStealTimer() { return raStealTimer; }
    public void incrementRaStealTimer() { this.raStealTimer++; }
    public void resetRaStealTimer() { this.raStealTimer = 0; }
    public int getTombraiserTimer() { return tombraiserTimer; }
    public void incrementTombraiserTimer() { this.tombraiserTimer++; }
    public void resetTombraiserTimer() { this.tombraiserTimer = 0; }
    public int getFishermanTimer() { return fishermanTimer; }
    public void incrementFishermanTimer() { this.fishermanTimer++; }
    public void resetFishermanTimer() { this.fishermanTimer = 0; }
    public int getOctopusTimer() { return octopusTimer; }
    public void incrementOctopusTimer() { this.octopusTimer++; }
    public void resetOctopusTimer() { this.octopusTimer = 0; }
    public int getTurquoiseLaserTimer() { return turquoiseLaserTimer; }
    public void incrementTurquoiseLaserTimer() { this.turquoiseLaserTimer++; }
    public void resetTurquoiseLaserTimer() { this.turquoiseLaserTimer = 0; }
    public int getPianoPlayTimer() { return pianoPlayTimer; }
    public void incrementPianoPlayTimer() { this.pianoPlayTimer++; }
    public void resetPianoPlayTimer() { this.pianoPlayTimer = 0; }

    public int getZombotanyJalapenoTimer() { return zombotanyJalapenoTimer; }
    public void incrementJalapenoTimer() { this.zombotanyJalapenoTimer++; }
    public int getIzombieSunProductionTicks() { return izombieSunProductionTicks; }
    public void incrementIzombieSunTicks() { this.izombieSunProductionTicks++; }
}
