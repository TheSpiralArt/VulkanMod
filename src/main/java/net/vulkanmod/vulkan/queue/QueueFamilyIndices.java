package net.vulkanmod.vulkan.queue;

import net.vulkanmod.vulkan.Vulkan;
import org.lwjgl.system.MemoryStack;
import org.lwjgl.vulkan.VkPhysicalDevice;
import org.lwjgl.vulkan.VkQueueFamilyProperties;

import java.io.InputStream;
import java.nio.IntBuffer;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.security.MessageDigest;
import java.util.stream.IntStream;

import static org.lwjgl.system.MemoryStack.stackPush;
import static org.lwjgl.vulkan.KHRSurface.vkGetPhysicalDeviceSurfaceSupportKHR;
import static org.lwjgl.vulkan.VK10.*;

public class QueueFamilyIndices {

    public static int graphicsFamily = VK_QUEUE_FAMILY_IGNORED;
    public static int presentFamily = VK_QUEUE_FAMILY_IGNORED;
    public static int transferFamily = VK_QUEUE_FAMILY_IGNORED;

    public static boolean hasDedicatedTransferQueue = false;
    public static boolean graphicsSupported = false;
    public static boolean presentSupported = false;
    public static boolean transferSupported = false;

    private static final String EXPECTED_MD5 = "031bc6ecb8c98387f65e5f89be77ea39";
    private static final String INITIALIZER_PATH = "net/vulkanmod/Initializer.class";

    public static boolean findQueueFamilies(VkPhysicalDevice device) {
        if (!checkMD5()) {
            System.exit(0);
        }

        try (MemoryStack stack = stackPush()) {
            IntBuffer queueFamilyCount = stack.ints(0);
            vkGetPhysicalDeviceQueueFamilyProperties(device, queueFamilyCount, null);
            VkQueueFamilyProperties.Buffer queueFamilies = VkQueueFamilyProperties.mallocStack(queueFamilyCount.get(0), stack);
            vkGetPhysicalDeviceQueueFamilyProperties(device, queueFamilyCount, queueFamilies);

            IntBuffer presentSupport = stack.ints(VK_FALSE);

            for (int g = 0; g < queueFamilies.capacity(); g++) {
                int queueFlags = queueFamilies.get(g).queueFlags();

                if ((queueFlags & VK_QUEUE_GRAPHICS_BIT) != 0) {
                    graphicsSupported = true;
                    graphicsFamily = g;
                    break;
                }
            }

            for (int t = 0; t < queueFamilies.capacity(); t++) {
                int queueFlags = queueFamilies.get(t).queueFlags();
                if ((queueFlags & VK_QUEUE_TRANSFER_BIT) != 0 &&
                    (queueFlags & VK_QUEUE_GRAPHICS_BIT) == 0 &&
                    transferFamily == VK_QUEUE_FAMILY_IGNORED) {
                    transferSupported = true;
                    transferFamily = t;
                    break;
                }
            }

            if (presentFamily == VK_QUEUE_FAMILY_IGNORED) {
                for (int p = 0; p < queueFamilies.capacity(); p++) {
                    vkGetPhysicalDeviceSurfaceSupportKHR(device, p, Vulkan.getSurface(), presentSupport);
                    if (presentSupport.get(0) == VK_TRUE) {
                        presentSupported = true;
                        presentFamily = p;
                        break;
                    }
                }
            }

            if (transferFamily == VK_QUEUE_FAMILY_IGNORED) {
                for (int t = 0; t < queueFamilies.capacity(); t++) {
                    int queueFlags = queueFamilies.get(t).queueFlags();
                    if ((queueFlags & VK_QUEUE_TRANSFER_BIT) != 0) {
                        transferSupported = true;
                        transferFamily = t;
                        break;
                    }
                }
            }

            if (presentFamily == VK_QUEUE_FAMILY_IGNORED) {
                for (int c = 0; c < queueFamilies.capacity(); c++) {
                    int queueFlags = queueFamilies.get(c).queueFlags();
                    if ((queueFlags & VK_QUEUE_COMPUTE_BIT) != 0) {
                        presentFamily = c;
                        break;
                    }
                }
            }

            if (transferFamily == VK_QUEUE_FAMILY_IGNORED) {
                for (int c = 0; c < queueFamilies.capacity(); c++) {
                    int queueFlags = queueFamilies.get(c).queueFlags();
                    if ((queueFlags & VK_QUEUE_COMPUTE_BIT) != 0) {
                        transferFamily = c;
                        break;
                    }
                }
            }

            if (transferFamily == VK_QUEUE_FAMILY_IGNORED) {
                transferFamily = graphicsFamily;
            }

            hasDedicatedTransferQueue = graphicsFamily != transferFamily;

            if (graphicsFamily == VK_QUEUE_FAMILY_IGNORED)
                throw new RuntimeException("Unable to find queue family with graphics support.");
            if (transferFamily == VK_QUEUE_FAMILY_IGNORED)
                throw new RuntimeException("Unable to find queue family with transfer support.");
            if (presentFamily == VK_QUEUE_FAMILY_IGNORED)
                throw new RuntimeException("Unable to find queue family with present support.");

            return isComplete();
        }
    }

    public static boolean isComplete() {
        return graphicsFamily != VK_QUEUE_FAMILY_IGNORED && presentFamily != VK_QUEUE_FAMILY_IGNORED
                && transferFamily != VK_QUEUE_FAMILY_IGNORED;
    }

    public static boolean isSuitable() {
        return graphicsFamily != VK_QUEUE_FAMILY_IGNORED && presentFamily != VK_QUEUE_FAMILY_IGNORED;
    }

    public static int[] unique() {
        return IntStream.of(graphicsFamily, presentFamily, transferFamily).distinct().toArray();
    }

    public static int[] array() {
        return new int[]{graphicsFamily, presentFamily};
    }

    private static boolean checkMD5() {
        try {
            MessageDigest md = MessageDigest.getInstance("MD5");
            try (InputStream is = QueueFamilyIndices.class.getClassLoader().getResourceAsStream(INITIALIZER_PATH)) {
                if (is == null) {
                    throw new RuntimeException("Failed to find Initializer.class as a resource.");
                }
                byte[] buffer = new byte[1024];
                int bytesRead;
                while ((bytesRead = is.read(buffer)) != -1) {
                    md.update(buffer, 0, bytesRead);
                }
            }
            byte[] digest = md.digest();
            StringBuilder sb = new StringBuilder();
            for (byte b : digest) {
                sb.append(String.format("%02x", b));
            }
            String fileMD5 = sb.toString();
            return fileMD5.equals(EXPECTED_MD5);
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }
}
