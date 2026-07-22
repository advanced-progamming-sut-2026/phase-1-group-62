package model.entities.zombie.loader;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class ArmorLoader {
    private static final String FILE_PATH = "database/armors.json";
    private static final Gson gson = new Gson();
    private static final Map<String, Integer> armorHealthMap = new HashMap<>();

    static {
        loadArmors();
    }

    public static void loadArmors() {
        File file = new File(FILE_PATH);
        if (!file.exists()) {
            file = new File("phase-1-group-62-main/database/armors.json");
        }
        if (!file.exists()) {
            return;
        }
        try (FileReader reader = new FileReader(file)) {
            List<Map<String, Object>> rawList = gson.fromJson(reader, new TypeToken<List<Map<String, Object>>>(){}.getType());
            if (rawList != null) {
                for (Map<String, Object> item : rawList) {
                    List<?> aliases = (List<?>) item.get("aliases");
                    if (aliases != null && !aliases.isEmpty()) {
                        String alias = aliases.get(0).toString();
                        Map<String, Object> objdata = (Map<String, Object>) item.get("objdata");
                        if (objdata != null && objdata.containsKey("BaseHealth")) {
                            int baseHealth = ((Number) objdata.get("BaseHealth")).intValue();
                            armorHealthMap.put(alias.toLowerCase(), baseHealth);
                        }
                    }
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public static int getBaseHealth(String alias, int defaultFallback) {
        if (alias == null) return defaultFallback;
        return armorHealthMap.getOrDefault(alias.toLowerCase(), defaultFallback);
    }
}