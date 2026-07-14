package model.entities.zombie.factory;

import model.entities.zombie.Zombie;
import model.entities.zombie.loader.ZombieLoader;
import java.util.List;

public class ZombieFactory {
    private static final List<Zombie> templates = ZombieLoader.loadZombies();

    public static Zombie createZombie(String type) {
        for (Zombie template : templates) {
            if (template.getName().equalsIgnoreCase(type)) {
                return new Zombie(
                        template.getName(),
                        template.getHealth(),
                        template.getSpeed(),
                        template.getDamage()
                );
            }
        }
        return null;
    }
}