package model.entities.plant.factory;

import model.entities.plant.Plant;
import model.entities.plant.loader.PlantLoader;
import java.util.List;

public class PlantFactory {
    private static final List<Plant> templates = PlantLoader.loadPlants();

    public static Plant createPlant(String type) {
        for (Plant template : templates) {
            if (template.getName().equalsIgnoreCase(type)) {
                return new Plant(
                        template.getName(),
                        template.getCost(),
                        template.getHealth(),
                        template.getDamage(),
                        template.getTag(),
                        template.getShootBehavior(),
                        template.getCooldown(),
                        template.getSunProduce(),
                        template.getProduceInterval()
                );
            }
        }
        return null;
    }
}