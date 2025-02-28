package tn.esprit.Services;

import oshi.SystemInfo;
import oshi.hardware.CentralProcessor;
import oshi.hardware.GlobalMemory;
import oshi.hardware.GraphicsCard;

import java.util.List;
import java.util.Optional;

public class HardwareSpecs {

    /**
     * Retrieves hardware specifications (CPU, RAM, GPU) of the system.
     * @return A formatted string containing hardware specs in a JSON-like format, or an error message if retrieval fails.
     */
    public static String getHardwareSpecs() {
        try {
            // Initialize SystemInfo to access hardware information
            SystemInfo systemInfo = new SystemInfo();

            // Get CPU information
            CentralProcessor processor = systemInfo.getHardware().getProcessor();
            String cpuName = Optional.ofNullable(processor)
                    .map(CentralProcessor::getProcessorIdentifier)
                    .map(pi -> {
                        String name = pi.getName();
                        // Try to clean up or enhance the CPU name for better readability
                        if (name != null && name.contains("Intel")) {
                            name = name.replace("GenuineIntel", "").trim();
                            // Try to map raw identifiers to known models (optional, needs a lookup table or database)
                            name = cleanupCpuName(name);
                        }
                        return name.isEmpty() ? "Unknown CPU" : name;
                    })
                    .orElse("Unknown CPU");

            // Get RAM information (total memory in GB, rounded to 2 decimal places)
            GlobalMemory memory = systemInfo.getHardware().getMemory();
            long totalRamBytes = Optional.ofNullable(memory)
                    .map(GlobalMemory::getTotal)
                    .orElse(0L);
            double totalRamGB = totalRamBytes / (1024.0 * 1024 * 1024); // Convert to GB
            String ramInfo = String.format("%.2f GB", totalRamGB);

            // Debug: Print raw RAM value to console
            System.out.println("Raw RAM (bytes): " + totalRamBytes + " | Converted RAM (GB): " + totalRamGB);

            // Get GPU information (first graphics card, if available)
            List<GraphicsCard> graphicsCards = systemInfo.getHardware().getGraphicsCards();
          //list of gpu
            String gpuName = graphicsCards.stream()
                    .findFirst()
                    .map(GraphicsCard::getName)
                    .orElse("Unknown GPU");

            // Build a JSON-like string with the hardware specs
            String specs = String.format(
                    "{\"cpu\": \"%s\", \"ram\": \"%s\", \"gpu\": \"%s\"}",
                    escapeJsonString(cpuName),
                    escapeJsonString(ramInfo),
                    escapeJsonString(gpuName)
            );

            return specs;

        } catch (Exception e) {
            System.err.println("Error retrieving hardware specs: " + e.getMessage());
            return "{\"error\": \"Failed to retrieve hardware specifications: " + e.getMessage() + "\"}";
        }
    }

    /**
     * Attempts to clean up or enhance the CPU name for better readability.
     * This is a simple placeholder; you might need a lookup table or external database for accurate mapping.
     * @param rawCpuName The raw CPU name from the system.
     * @return A cleaned or enhanced CPU name.
     */
    private static String cleanupCpuName(String rawCpuName) {
        if (rawCpuName == null || rawCpuName.isEmpty()) return "Unknown CPU";

        // Example: Try to map "Intel64 Family 6 Model 165 Stepping 3" to a known model
        if (rawCpuName.contains("Family 6 Model 165")) {
            // This is a guess; you’d need a mapping or external lookup for Model 165
            return "Intel Core i5-9400F (Estimated)"; // Placeholder; adjust based on actual model
        }

        // Remove unnecessary parts and trim
        return rawCpuName.replace("GenuineIntel", "").replaceAll("\\s+", " ").trim();
    }

    /**
     * Escapes special characters in a string for safe JSON output.
     * @param input The input string to escape.
     * @return The escaped string, or "Unknown" if input is null.
     */
    private static String escapeJsonString(String input) {
        if (input == null) return "Unknown";
        return input.replace("\"", "\\\"") // Escape double quotes
                .replace("\n", "\\n")    // Escape newlines
                .replace("\r", "\\r");   // Escape carriage returns
    }
}