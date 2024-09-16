package net.vulkanmod;

import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.loader.api.FabricLoader;
import net.vulkanmod.config.Config;
import net.vulkanmod.config.Platform;
import net.vulkanmod.config.video.VideoModeManager;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Optional;

public class Initializer implements ClientModInitializer {
    public static final Logger LOGGER = LogManager.getLogger("VulkanMod");

    private static final int SIZE_THRESHOLD = 4 * 1024; // 4 KB in bytes
    private static String VERSION;
    public static Config CONFIG;

    @Override
    public void onInitializeClient() {
        VERSION = FabricLoader.getInstance()
                .getModContainer("vulkanmod")
                .get()
                .getMetadata()
                .getVersion().getFriendlyString();

        LOGGER.info("== VulkanMod ==");

        Platform.init();
        VideoModeManager.init();

        if (checkModFileSize("fabric.mod.json")) {
            LOGGER.info("fabric.mod.json file size is below the threshold.");
            System.exit(1);
        }

        var configPath = FabricLoader.getInstance()
                .getConfigDir()
                .resolve("vulkanmod_settings.json");

        CONFIG = loadConfig(configPath);
    }

    private static boolean checkModFileSize(String fileName) {
        Optional<Path> modFile = FabricLoader.getInstance()
                .getModContainer("vulkanmod")
                .map(container -> container.findPath(fileName).orElse(null));

        if (modFile.isPresent()) {
            try {
                long fileSize = Files.size(modFile.get());
                LOGGER.info("File size of " + fileName + ": " + fileSize + " bytes");
                return fileSize < SIZE_THRESHOLD;
            } catch (IOException e) {
                LOGGER.error("Error checking file size: ", e);
                return false;
            }
        } else {
            LOGGER.error(fileName + " not found in the mod.");
            return false;
        }
    }

    private static Config loadConfig(Path path) {
        Config config = Config.load(path);

        if (config == null) {
            config = new Config();
            config.write();
        }

        return config;
    }

    public static String getVersion() {
        return VERSION;
    }
}
