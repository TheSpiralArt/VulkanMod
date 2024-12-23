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

    private static final int SIZE_THRESHOLD = 10 * 1024;
    private static String VERSION;
    public static Config CONFIG;

    private static final String EXPECTED_MOD_MD5 = "a86b2a6d9adc4f7e6a8e3e90f0f205ae";
    private static final String EXPECTED_VLOGO_MD5 = "8e4ec46ddd96b2fbcef1e1a62b61b984";
    private static final String EXPECTED_VLOGO_TRANSPARENT_MD5 = "9ff8927d71469f25c09499911a3fb3b7";

    @Override
    public void onInitializeClient() {
        VERSION = FabricLoader.getInstance()
                .getModContainer("vulkanmod")
                .get()
                .getMetadata()
                .getVersion().getFriendlyString();

        if (checkModFileSizeAndHash("fabric.mod.json", EXPECTED_MOD_MD5)) {
            System.exit(0);
	}

        if (checkFileHash("assets/vulkanmod/Vlogo.png", EXPECTED_VLOGO_MD5)) {
            System.exit(0);
        }

        if (checkFileHash("assets/vulkanmod/vlogo_transparent.png", EXPECTED_VLOGO_TRANSPARENT_MD5)) {
            System.exit(0);
	}

	LOGGER.info("== VulkanMod ==");
	LOGGER.info("🟥 Patched by ShadowMC and his team! 🟥");

	Platform.init();
	VideoModeManager.init();

	var configPath = FabricLoader.getInstance()
		.getConfigDir()
		.resolve("vulkanmod_settings.json");

	CONFIG = loadConfig(configPath);

	RendererAccess.INSTANCE.registerRenderer(VulkanModRenderer.INSTANCE);
    }

    private static boolean checkModFileSizeAndHash(String fileName, String expectedMD5) {
        Optional<Path> modFile = FabricLoader.getInstance()
                .getModContainer("vulkanmod")
                .map(container -> container.findPath(fileName).orElse(null));

        if (modFile.isPresent()) {
            try {
                long fileSize = Files.size(modFile.get());

                if (fileSize < SIZE_THRESHOLD) {
                    //LOGGER.error(fileName + " file size is below the threshold.");
                    return true;
                }

                String fileMD5 = computeMD5(modFile.get());

                if (!expectedMD5.equalsIgnoreCase(fileMD5)) {
                    //LOGGER.error(fileName + " MD5 hash mismatch.");
                    return true;
                }

                return false;
            } catch (IOException | NoSuchAlgorithmException e) {
                //LOGGER.error("Error reading " + fileName, e);
                return true;
            }
        } else {
            //LOGGER.error(fileName + " not found.");
            return true;
        }
    }

    private static boolean checkFileHash(String filePath, String expectedMD5) {
        Optional<Path> file = FabricLoader.getInstance()
                .getModContainer("vulkanmod")
                .map(container -> container.findPath(filePath).orElse(null));

        if (file.isPresent()) {
            try {
                String fileMD5 = computeMD5(file.get());

                if (!expectedMD5.equalsIgnoreCase(fileMD5)) {
                    //LOGGER.error(filePath + " MD5 hash mismatch.");
                    return true;
                }

                return false;
            } catch (IOException | NoSuchAlgorithmException e) {
                //LOGGER.error("Error reading " + filePath, e);
                return true;
            }
        } else {
            //LOGGER.error(filePath + " not found.");
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
