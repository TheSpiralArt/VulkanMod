package net.vulkanmod.vulkan.queue;

import net.vulkanmod.vulkan.Vulkan;
import org.lwjgl.system.MemoryStack;
import org.lwjgl.vulkan.VkPhysicalDevice;
import org.lwjgl.vulkan.VkQueueFamilyProperties;

import java.nio.IntBuffer;
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

    public static boolean findQueueFamilies(VkPhysicalDevice device) {

        try (MemoryStack stack = stackPush()) {
            IntBuffer queueFamilyCount = stack.ints(0);

            vkGetPhysicalDeviceQueueFamilyProperties(device, queueFamilyCount, null);

            if (queueFamilyCount.get(0) == 1) {
                transferFamily = presentFamily = graphicsFamily = 0;
                return true;
            }

            IntBuffer presentSupport = stack.ints(VK_FALSE);
            VkQueueFamilyProperties.Buffer queueFamilies = VkQueueFamilyProperties.mallocStack(queueFamilyCount.get(0), stack);
            vkGetPhysicalDeviceQueueFamilyProperties(device, queueFamilyCount, queueFamilies);
        
            for (int g = 0; g < queueFamilies.capacity(); g++) {
                int queueFlags = queueFamilies.get(g).queueFlags();

                if ((queueFlags & VK_QUEUE_GRAPHICS_BIT) != 0) {
                    graphicsFamily = g;
                    graphicsSupported = true;
                    vkGetPhysicalDeviceSurfaceSupportKHR(device, g, Vulkan.getSurface(), presentSupport);
                    if (presentSupport.get(0) == VK_TRUE) {
                        presentFamily = g;
                        presentSupported = true;
                        break;
                    }
                }
            }

            for (int t = 0; t < queueFamilies.capacity(); t++) {
                int queueFlags = queueFamilies.get(t).queueFlags();
                if ((queueFlags & VK_QUEUE_TRANSFER_BIT) != 0 &&
                    (queueFlags & VK_QUEUE_GRAPHICS_BIT) == 0 &&
                    transferFamily == VK_QUEUE_FAMILY_IGNORED) {
                    transferFamily = t;
                    transferSupported = true;
                    break;
                }
            }

            if (presentFamily == VK_QUEUE_FAMILY_IGNORED) {
                for (int p = 0; p < queueFamilies.capacity(); p++) {
                    vkGetPhysicalDeviceSurfaceSupportKHR(device, p, Vulkan.getSurface(), presentSupport);
                    if (presentSupport.get(0) == VK_TRUE) {
                        presentFamily = p;
                        presentSupported = true;
                        break;
                    }
                }
            }

            if (transferFamily == VK_QUEUE_FAMILY_IGNORED) {
                for (int t = 0; t < queueFamilies.capacity(); t++) {
                    int queueFlags = queueFamilies.get(t).queueFlags();
                    if ((queueFlags & VK_QUEUE_TRANSFER_BIT) != 0) {
                        transferFamily = t;
                        transferSupported = true;
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
}
