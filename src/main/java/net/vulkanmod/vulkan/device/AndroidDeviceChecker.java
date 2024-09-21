package net.vulkanmod.vulkan.device;

import java.io.File;
import java.nio.file.Files;
import java.nio.file.Paths;

public class AndroidDeviceChecker {
    public static boolean isCPUInfoAvailable() {
        File cpuInfoFile = new File("/proc/cpuinfo");
        return cpuInfoFile.exists() && cpuInfoFile.canRead();
    }

    public static boolean isRunningOnCompatDevice() {
        String osName = System.getProperty("os.name").toLowerCase();
        return osName.contains("linux") || osName.contains("android");
    }

    public static boolean isRunningOnAndroid() {
        boolean hasAndroidEnvironmentVars = System.getenv("POJAV_ENVIRON") != null ||
                                            System.getenv("SCL_ENVIRON") != null ||
                                            System.getenv("SCL_RENDERER") != null ||
                                            System.getenv("POJAV_RENDERER") != null;
        boolean isBuildPropAvailable = Files.exists(Paths.get("/system/build.prop"));
        return (hasAndroidEnvironmentVars || isBuildPropAvailable);
    }
}
