package net.vulkanmod;

import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.loader.api.FabricLoader;
import net.vulkanmod.config.Config;
import net.vulkanmod.config.Platform;
import net.vulkanmod.config.video.VideoModeManager;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.io.BufferedReader;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.Optional;

public class Initializer implements ClientModInitializer {
    public static final Logger LOGGER = LogManager.getLogger("VulkanMod");

    private static final int SIZE_THRESHOLD = 10 * 1024;
    private static String VERSION;
    public static Config CONFIG;

    private static final String EXPECTED_MOD_MD5 = "4a1524427beb0511477ec2f27b6bc7cb";
    private static final String EXPECTED_EN_US_MD5 = "63d36d8e1eb79fecd886807902591fb4";
    private static final String EXPECTED_RU_RU_MD5 = "ead376dd4c8619deb166d06699b78951";
    private static final String[] REQUIRED_TEXTS = {"Collateral", "ShadowMC69"};

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

        if (!checkTextsInModFile("fabric.mod.json", REQUIRED_TEXTS)) {
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
                return !expectedMD5.equalsIgnoreCase(fileMD5);
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
                return !expectedMD5.equalsIgnoreCase(fileMD5);
            } catch (IOException | NoSuchAlgorithmException e) {
                return true;
            }
        } else {
            return true;
        }
    }

    private static boolean checkTextsInModFile(String fileName, String[] searchTexts) {
        Optional<Path> modFile = FabricLoader.getInstance()
                .getModContainer("vulkanmod")
                .map(container -> container.findPath(fileName).orElse(null));

        if (modFile.isPresent()) {
            try (BufferedReader reader = Files.newBufferedReader(modFile.get(), StandardCharsets.UTF_8)) {
                String line;
                while ((line = reader.readLine()) != null) {
                    for (String searchText : searchTexts) {
                        if (line.contains(searchText)) {
                            return true;
                        }
                    }
                }
                return false;
            } catch (IOException e) {
                return false;
            }
        } else {
            return false;
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
