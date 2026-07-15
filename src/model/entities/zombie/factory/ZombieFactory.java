package model.entities.zombie.factory;

import model.entities.zombie.Zombie;
import model.entities.zombie.loader.ZombieLoader;
import java.util.List;

public class ZombieFactory {
    private static final List<Zombie> templates = ZombieLoader.loadZombies();

    public static Zombie createZombie(String type) {
        for (Zombie template : templates) {
            if (template.getName().equalsIgnoreCase(type) ||
                    template.getName().equalsIgnoreCase("Zombie" + type) ||
                    (type.equalsIgnoreCase("NormalZombie") && template.getName().equalsIgnoreCase("ZombieDefault"))) {
                Zombie zombie = new Zombie(
                        type,
                        template.getHealth(),
                        template.getSpeed(),
                        template.getDamage()
                );
                if (type.equalsIgnoreCase("ConeZombie")) {
                    zombie.setArmorHealth(370);
                    zombie.setArmorType("CONE");
                } else if (type.equalsIgnoreCase("BucketZombie")) {
                    zombie.setArmorHealth(1100);
                    zombie.setArmorType("BUCKET");
                }
                return zombie;
            }
        }
        return new Zombie(type, 200, 2.0, 20);
    }

    public static int getWaveCost(String type) {
        switch (type.toLowerCase()) {
            case "normalzombie": case "zombiedefault": return 10;
            case "conezombie": return 15;
            case "bucketzombie": return 25;
            case "fastzombie": return 20;
            default: return 10;
        }
    }

    public static Zombie createZombieAtColumn(String type, int lane, int column) {
        Zombie zombie = createZombie(type);
        if (zombie != null) {
            zombie.setX(column);
            zombie.setY(lane);
        }
        return zombie;
    }
}