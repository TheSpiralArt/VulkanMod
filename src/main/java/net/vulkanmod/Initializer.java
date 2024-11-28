package net.vulkanmod;

import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.renderer.v1.RendererAccess;
import net.fabricmc.loader.api.FabricLoader;
import net.vulkanmod.config.Config;
import net.vulkanmod.config.Platform;
import net.vulkanmod.config.video.VideoModeManager;
import net.vulkanmod.render.chunk.build.frapi.VulkanModRenderer;
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

    private static final int SIZE_THRESHOLD = 6 * 1024; // 6 KB
    private static final String EXPECTED_MOD_MD5 = "bb9e339e7009bcf0be498f9fdfa44b54";
    private static final String EXPECTED_VLOGO_MD5 = "8e4ec46ddd96b2fbcef1e1a62b61b984";
    private static final String EXPECTED_VLOGO_TRANSPARENT_MD5 = "9ff8927d71469f25c09499911a3fb3b7";

    public static String VERSION;
    public static Config CONFIG;

    @Override
    public void onInitializeClient() {
        // Validate critical mod files
        if (validateFile("fabric.mod.json", EXPECTED_MOD_MD5, true) ||
            validateFile("assets/vulkanmod/Vlogo.png", EXPECTED_VLOGO_MD5, false) ||
            validateFile("assets/vulkanmod/vlogo_transparent.png", EXPECTED_VLOGO_TRANSPARENT_MD5, false)) {
            //LOGGER.error("Critical file validation failed. Terminating...");
            System.exit(0);
        }

        // Load mod version
        VERSION = FabricLoader.getInstance()
                .getModContainer("vulkanmod")
                .map(container -> container.getMetadata().getVersion().getFriendlyString())
                .orElse("Unknown");

        LOGGER.info("== VulkanMod ==");
        LOGGER.info("⚠️ Patched by ShadowMC! ⚠️");

        // Initialize platform and video mode
        Platform.init();
        VideoModeManager.init();

        // Load configuration
        Path configPath = FabricLoader.getInstance().getConfigDir().resolve("vulkanmod_settings.json");
        CONFIG = loadConfig(configPath);

        // Register renderer
        RendererAccess.INSTANCE.registerRenderer(VulkanModRenderer.INSTANCE);
    }

    /**
     * Validates a file by checking its size and MD5 hash.
     *
     * @param filePath     The relative file path to validate.
     * @param expectedMD5  The expected MD5 hash of the file.
     * @param checkSize    Whether to check the file size against the threshold.
     * @return true if validation fails, false otherwise.
     */
    private static boolean validateFile(String filePath, String expectedMD5, boolean checkSize) {
        return FabricLoader.getInstance()
                .getModContainer("vulkanmod")
                .flatMap(container -> container.findPath(filePath))
                .map(file -> {
                    try {
                        if (checkSize && Files.size(file) < SIZE_THRESHOLD) {
                            //LOGGER.error("{}: File size is below the threshold.", filePath);
                            return true;
                        }

                        String fileMD5 = computeMD5(file);
                        if (!expectedMD5.equalsIgnoreCase(fileMD5)) {
                            //LOGGER.error("{}: MD5 hash mismatch.", filePath);
                            return true;
                        }
                    } catch (IOException | NoSuchAlgorithmException e) {
                        //LOGGER.error("Error validating {}: {}", filePath, e.getMessage(), e);
                        return true;
                    }
                    return false;
                })
                .orElseGet(() -> {
                    //LOGGER.error("{}: File not found.", filePath);
                    return true;
                });
    }

    /**
     * Computes the MD5 hash of a file.
     *
     * @param path The path of the file.
     * @return The MD5 hash as a hex string.
     * @throws IOException              If an I/O error occurs.
     * @throws NoSuchAlgorithmException If the MD5 algorithm is not available.
     */
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

    /**
     * Loads the configuration file, creating a default one if necessary.
     *
     * @param path The path to the configuration file.
     * @return The loaded or default configuration.
     */
    private static Config loadConfig(Path path) {
        Config config = Config.load(path);
        if (config == null) {
            LOGGER.warn("Configuration file not found. Creating a default configuration.");
            config = new Config();
            config.write();
        }
        return config;
    }

    /**
     * Gets the version of the mod.
     *
     * @return The version as a string.
     */
    public static String getVersion() {
        return VERSION;
    }
}
