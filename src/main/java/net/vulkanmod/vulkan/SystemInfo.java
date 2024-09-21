package net.vulkanmod.vulkan;

import net.vulkanmod.vulkan.device.MobileDeviceChecker;
import oshi.hardware.CentralProcessor;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.stream.Stream;

public class SystemInfo {
    public static final String cpuInfo;

    static {
        cpuInfo = MobileDeviceChecker.isRunningOnAndroid() ? getProcessorNameForAndroid() : getProcessorNameForDesktop();
    }

    public static String getProcessorNameForAndroid() {
        try (Stream<String> lines = Files.lines(Paths.get("/proc/cpuinfo"))) {
            return lines.filter(line -> line.startsWith("Hardware") || line.startsWith("model name"))
                .reduce((f, s) -> f.startsWith("H") ? f : s)
                .map(line -> {
                    String value = line.split(":")[1].trim();
                    return line.startsWith("H") ? value + " (SoC)" : value;
                }).orElse("Unknown CPU");
        } catch (IOException e) {
            return "Unknown CPU";
        }
    }

    public static String getProcessorNameForDesktop() {
        try {
            CentralProcessor centralProcessor = new oshi.SystemInfo().getHardware().getProcessor();
            return String.format("%s", centralProcessor.getProcessorIdentifier().getName()).replaceAll("\\s+", " ");
        } catch (NoClassDefFoundError | Exception e) {
            return getProcessorNameForAndroid();
        }
    }
}
