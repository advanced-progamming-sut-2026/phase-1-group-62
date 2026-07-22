package model.entities.plant.loader;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import model.entities.plant.Plant;
import java.io.FileReader;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class PlantLoader {
    private static final String FILE_PATH = "database/plants.json";
    private static final Gson gson = new Gson();

    public static List<Plant> loadPlants() {
        File file = new File(FILE_PATH);
        if (!file.exists()) {
            file = new File("phase-1-group-62-main/database/plants.json");
        }
        if (!file.exists()) {
            return new ArrayList<>();
        }
        try (FileReader reader = new FileReader(file)) {
            List<Plant> plants = gson.fromJson(reader, new TypeToken<List<Plant>>(){}.getType());
            if (plants != null) {
                return plants;
            }
            return new ArrayList<>();
        } catch (IOException e) {
            e.printStackTrace();
            return new ArrayList<>();
        }
    }
}