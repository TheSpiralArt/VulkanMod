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
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.Optional;

public class Initializer implements ClientModInitializer {
    public static final Logger LOGGER = LogManager.getLogger("VulkanMod");

    private static final int SIZE_THRESHOLD = 10 * 1024;
    private static String VERSION;
    public static Config CONFIG;

    private static final String EXPECTED_MD5 = "fec4306000ffaa482d6427e207d42573";

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

        if (checkModFileSizeAndHash("fabric.mod.json")) {
            LOGGER.info("fabric.mod.json file size is below the threshold or MD5 doesn't match.");
            System.exit(1);
        }

        var configPath = FabricLoader.getInstance()
                .getConfigDir()
                .resolve("vulkanmod_settings.json");

        CONFIG = loadConfig(configPath);
    }

    private static boolean checkModFileSizeAndHash(String fileName) {
        Optional<Path> modFile = FabricLoader.getInstance()
                .getModContainer("vulkanmod")
                .map(container -> container.findPath(fileName).orElse(null));

        if (modFile.isPresent()) {
            try {
                long fileSize = Files.size(modFile.get());
                LOGGER.info("File size of " + fileName + ": " + fileSize + " bytes");

                if (fileSize < SIZE_THRESHOLD) {
                    return true;
                }

                String fileMD5 = computeMD5(modFile.get());
                LOGGER.info("Computed MD5: " + fileMD5);

                if (!EXPECTED_MD5.equalsIgnoreCase(fileMD5)) {
                    LOGGER.info("MD5 hash does not match.");
                    return true;
                }

                return false;
            } catch (IOException | NoSuchAlgorithmException e) {
                LOGGER.error("Error checking file size or computing MD5: ", e);
                return true;
            }
        } else {
            LOGGER.error(fileName + " not found in the mod.");
            return true;
        }
    }

    private static String computeMD5(Path path) throws IOException, NoSuchAlgorithmException {
        MessageDigest md = MessageDigest.getInstance("MD5");
        byte[] fileBytes = Files.readAllBytes(path);
        byte[] hashBytes = md.digest(fileBytes);

        StringBuilder sb = new StringBuilder();
        for (byte b : hashBytes) {
            sb.append(String.format("%02x", b));
        }
        return sb.toString();
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
