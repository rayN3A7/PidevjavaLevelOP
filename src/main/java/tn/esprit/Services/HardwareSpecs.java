package tn.esprit.Services;

import oshi.SystemInfo;
import oshi.hardware.CentralProcessor;
import oshi.hardware.GlobalMemory;
import oshi.hardware.GraphicsCard;

import java.util.List;
import java.util.stream.Collectors;

public class HardwareSpecs {

    /**
     * Retrieves hardware specifications (CPU, RAM, GPUs) of the system.
     * @return A formatted string containing hardware specs in a JSON-like format, or an error message if retrieval fails.
     */
    public static String getHardwareSpecs() {
        try {
            SystemInfo systemInfo = new SystemInfo();

            // Get CPU information
            // Get CPU information
            CentralProcessor processor = systemInfo.getHardware().getProcessor();
            String cpuName = processor.getProcessorIdentifier().getName();

            if (cpuName != null) {
                if (cpuName.contains("Intel")) {
                    cpuName = cpuName.replace("GenuineIntel", "").trim();
                } else if (cpuName.contains("AMD") || cpuName.contains("Ryzen")) {
                    cpuName = cpuName.replace("AuthenticAMD", "").trim();
                }
                cpuName = cleanupCpuName(cpuName);
            }

            if (cpuName == null || cpuName.isEmpty()) cpuName = "Unknown CPU";


            // Get RAM information
            GlobalMemory memory = systemInfo.getHardware().getMemory();
            long totalRamBytes = memory.getTotal();
            double totalRamGB = totalRamBytes / (1024.0 * 1024 * 1024);
            String ramInfo = String.format("%.2f GB", totalRamGB);

            // Get all GPU information
            List<GraphicsCard> graphicsCards = systemInfo.getHardware().getGraphicsCards();
            String gpuNames = graphicsCards.stream()
                    .map(GraphicsCard::getName)
                    .map(HardwareSpecs::escapeJsonString)
                    .collect(Collectors.joining("\",\""));
            String gpuArray = graphicsCards.isEmpty() ? "\"Unknown GPU\"" : "\"" + gpuNames + "\"";

            // Build JSON-like string
            String specs = String.format(
                    "{\"cpu\": \"%s\", \"ram\": \"%s\", \"gpus\": [%s]}",
                    escapeJsonString(cpuName),
                    escapeJsonString(ramInfo),
                    gpuArray
            );

            return specs;

        } catch (Exception e) {
            System.err.println("Error retrieving hardware specs: " + e.getMessage());
            return "{\"error\": \"Failed to retrieve hardware specifications: " + e.getMessage() + "\"}";
        }
    }

    private static String cleanupCpuName(String rawCpuName) {
        if (rawCpuName == null || rawCpuName.isEmpty()) return "Unknown CPU";
        if (rawCpuName.contains("Family 6 Model 165")) {
            return "Intel Core i5-9400F (Estimated)";
        }
        return rawCpuName.replace("GenuineIntel", "").replaceAll("\\s+", " ").trim();
    }

    private static String escapeJsonString(String input) {
        if (input == null) return "Unknown";
        return input.replace("\"", "\\\"")
                .replace("\n", "\\n")
                .replace("\r", "\\r");
    }
}