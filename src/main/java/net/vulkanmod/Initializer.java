package net.vulkanmod;

import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.loader.api.FabricLoader;
import net.vulkanmod.config.Config;
import net.vulkanmod.config.Platform;
import net.vulkanmod.config.video.VideoModeManager;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Path;

public class Initializer implements ClientModInitializer {
    public static final Logger LOGGER = LogManager.getLogger("VulkanMod");

    private static final int SIZE_THRESHOLD = 4 * 1024;
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

        if (checkModFileSize("/fabric.mod.json")) {
            LOGGER.info("fabric.mod.json file size is below the threshold.");
            System.exit(1);
        }

        var configPath = FabricLoader.getInstance()
                .getConfigDir()
                .resolve("vulkanmod_settings.json");

        CONFIG = loadConfig(configPath);
    }

    private static boolean checkModFileSize(String filePath) {
        try (InputStream inputStream = Initializer.class.getResourceAsStream(filePath)) {
            if (inputStream == null) {
                LOGGER.error("fabric.mod.json not found.");
                return false;
            }

            long fileSize = getFileSize(inputStream);
            return fileSize < SIZE_THRESHOLD;
        } catch (IOException e) {
            LOGGER.error("Error checking file size: ", e);
            return false;
        }
    }

    private static long getFileSize(InputStream inputStream) throws IOException {
        ByteArrayOutputStream buffer = new ByteArrayOutputStream();
        byte[] data = new byte[1024];
        int length;
        while ((length = inputStream.read(data, 0, data.length)) != -1) {
            buffer.write(data, 0, length);
        }
        buffer.flush();
        return buffer.size();
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
