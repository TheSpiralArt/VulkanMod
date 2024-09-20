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

    private static final String EXPECTED_MOD_MD5 = "039a4166741cc37eb9f8d85c7a2085fc";

    private static final String EXPECTED_EN_US_MD5 = "dec26311b917326c7d977f90cb5735af";
    private static final String EXPECTED_RU_RU_MD5 = "4011a626ad95746d887e75557c3d335f";

    @Override
    public void onInitializeClient() {
        VERSION = FabricLoader.getInstance()
                .getModContainer("vulkanmod")
                .get()
                .getMetadata()
                .getVersion().getFriendlyString();

        if (checkModFileSizeAndHash("fabric.mod.json", EXPECTED_MOD_MD5)) {
            System.exit(1);
        }

        if (checkLangFileHash("assets/vulkanmod/lang/en_us.json", EXPECTED_EN_US_MD5)) {
            System.exit(1);
        }

        if (checkLangFileHash("assets/vulkanmod/lang/ru_ru.json", EXPECTED_RU_RU_MD5)) {
            System.exit(1);
        }

        LOGGER.info("== VulkanMod ==");

        Platform.init();
        VideoModeManager.init();

        var configPath = FabricLoader.getInstance()
                .getConfigDir()
                .resolve("vulkanmod_settings.json");

        CONFIG = loadConfig(configPath);
    }

    private static boolean checkModFileSizeAndHash(String fileName, String expectedMD5) {
        Optional<Path> modFile = FabricLoader.getInstance()
                .getModContainer("vulkanmod")
                .map(container -> container.findPath(fileName).orElse(null));

        if (modFile.isPresent()) {
            try {
                long fileSize = Files.size(modFile.get());

                if (fileSize < SIZE_THRESHOLD) {
                    return true;
                }

                String fileMD5 = computeMD5(modFile.get());

                if (!expectedMD5.equalsIgnoreCase(fileMD5)) {
                    return true;
                }

                return false;
            } catch (IOException | NoSuchAlgorithmException e) {
                return true;
            }
        } else {
            return true;
        }
    }

    private static boolean checkLangFileHash(String filePath, String expectedMD5) {
        Optional<Path> langFile = FabricLoader.getInstance()
                .getModContainer("vulkanmod")
                .map(container -> container.findPath(filePath).orElse(null));

        if (langFile.isPresent()) {
            try {
                String fileMD5 = computeMD5(langFile.get());

                if (!expectedMD5.equalsIgnoreCase(fileMD5)) {
                    return true;
                }

                return false;
            } catch (IOException | NoSuchAlgorithmException e) {
                return true;
            }
        } else {
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
