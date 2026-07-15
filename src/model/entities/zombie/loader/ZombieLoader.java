package model.entities.zombie.loader;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import model.entities.zombie.Zombie;
import java.io.FileReader;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class ZombieLoader {
    private static final String FILE_PATH = "database/zombies.json";
    private static final Gson gson = new Gson();

    public static List<Zombie> loadZombies() {
        File file = new File(FILE_PATH);
        if (!file.exists()) {
            return new ArrayList<>();
        }
        try (FileReader reader = new FileReader(file)) {
            List<Map<String, Object>> rawList = gson.fromJson(reader, new TypeToken<List<Map<String, Object>>>(){}.getType());
            List<Zombie> zombies = new ArrayList<>();
            if (rawList != null) {
                for (Map<String, Object> item : rawList) {
                    List<?> aliases = (List<?>) item.get("aliases");
                    if (aliases != null && !aliases.isEmpty()) {
                        String name = aliases.get(0).toString();
                        if (name.equalsIgnoreCase("ZombieDefault")) {
                            name = "NormalZombie";
                        }
                        Map<String, Object> objdata = (Map<String, Object>) item.get("objdata");
                        int hp = 200;
                        int damage = 20;
                        double speed = 2.0;
                        if (objdata != null) {
                            if (objdata.containsKey("Hitpoints")) {
                                hp = ((Number) objdata.get("Hitpoints")).intValue();
                            }
                            if (objdata.containsKey("EatDPS")) {
                                damage = ((Number) objdata.get("EatDPS")).intValue() / 5;
                            }
                        }
                        zombies.add(new Zombie(name, hp, speed, damage));
                    }
                }
            }
            return zombies;
        } catch (IOException e) {
            e.printStackTrace();
            return new ArrayList<>();
        }
    }
}