package com.worldeditcui.config;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import net.minecraft.client.Minecraft;

import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.nio.file.Path;

public class Config {
    private static final Gson GSON = new GsonBuilder().setPrettyPrinting().create();
    private static ConfigData instance = new ConfigData();

    public static class ConfigData {
        public boolean invertScrollDirection = false;
        public boolean invertArrowKeys = false;
        public int selectionFillColor = 0x403366FF;
        public int selectionEdgeColor = 0xC84488FF;
        public int selectionGridColor = 0x604488FF;
        public boolean renderSelectionEdgesThroughTerrain = true;
        public boolean hideGizmoChatFeedback = true;
    }

    public static void load() {
        try {
            File configFile = getConfigFile();
            if (configFile.exists()) {
                try (FileReader reader = new FileReader(configFile)) {
                    instance = GSON.fromJson(reader, ConfigData.class);
                }
            } else {
                save();
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public static void save() {
        try {
            File configFile = getConfigFile();
            configFile.getParentFile().mkdirs();
            try (FileWriter writer = new FileWriter(configFile)) {
                GSON.toJson(instance, writer);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public static ConfigData get() {
        return instance;
    }

    private static File getConfigFile() {
        Path configDir = Minecraft.getInstance().gameDirectory.toPath().resolve("config");
        return configDir.resolve("worldeditcui.json").toFile();
    }
}
