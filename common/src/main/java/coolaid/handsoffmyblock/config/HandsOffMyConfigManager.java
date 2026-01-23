package coolaid.handsoffmyblock.config;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import net.fabricmc.loader.api.FabricLoader;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

public final class HandsOffMyConfigManager {

    private static final Gson GSON = new GsonBuilder()
            .setPrettyPrinting()
            .create();

    private static final Path CONFIG_PATH =
            FabricLoader.getInstance().getConfigDir().resolve("handsoffmyblock.json");

    private static HandsOffMyConfig CONFIG;

    public static void load() {
        if (Files.exists(CONFIG_PATH)) {
            try {
                CONFIG = GSON.fromJson(
                        Files.readString(CONFIG_PATH),
                        HandsOffMyConfig.class
                );
            } catch (Exception e) {
                System.err.println("Failed to load config, defaults");
                e.printStackTrace();
                CONFIG = new HandsOffMyConfig();
                save();
            }
        } else {
            CONFIG = new HandsOffMyConfig();
            save();
        }
    }

    public static synchronized void save() {
        try {
            Files.createDirectories(CONFIG_PATH.getParent());
            Files.writeString(CONFIG_PATH, GSON.toJson(CONFIG));
        } catch (IOException e) {
            System.err.println("Failed to save config");
            e.printStackTrace();
        }
    }

    public static HandsOffMyConfig get() {
        if (CONFIG == null) {
            load(); // safety net
        }
        return CONFIG;
    }
}
