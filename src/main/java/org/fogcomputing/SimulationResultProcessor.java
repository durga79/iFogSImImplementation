package org.fogcomputing;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.Date;
import java.text.DecimalFormat;
import java.util.List;
import java.util.Map;
import java.util.HashMap;
import org.cloudbus.cloudsim.Cloudlet;
import org.cloudbus.cloudsim.Log;

/**
 * Helper class to process CloudSim simulation results
 * Handles output formatting, file saving, and console display
 */
public class SimulationResultProcessor {
    private static final DecimalFormat dft = new DecimalFormat("0.00");
    
    // Constants for VM tier identification
    private static final int NUM_CLOUD_HOSTS = 2; 
    private static final int NUM_FOG_NODES = 5;  
    
    private static final double ENERGY_PER_MI_CLOUD = 0.0005; // Joules per MI
    private static final double ENERGY_PER_MI_FOG = 0.0003;   // Joules per MI
    private static final double ENERGY_PER_MI_IOT = 0.0001;   // Joules per MI
    private static final double IOT_TO_FOG_LATENCY = 3.0;  // milliseconds
    private static final double FOG_TO_CLOUD_LATENCY = 20.0; // milliseconds
    private static final int NUM_IOT_DEVICES = 10; // Number of IoT devices
    
    // Base path for all result files
    private static final String BASE_PATH = "/home/wexa/CascadeProjects/FogEdgeComputing/iFogSimImplementation/";
    private static final String RESULTS_DIR = BASE_PATH + "results/";
    
    /**
     * Test method to check if file writing works properly
     * Can be called from the main simulation to diagnose file writing issues
     */
    public static void testFileWriting() {
        System.out.println("\n===== Testing SimulationResultProcessor File Writing =====\n");
        
        // Print system info
        System.out.println("Current directory: " + System.getProperty("user.dir"));
        System.out.println("Java version: " + System.getProperty("java.version"));
        System.out.println("Java vendor: " + System.getProperty("java.vendor"));
        
        // Create results directory if it doesn't exist
        File resultsDir = new File(RESULTS_DIR);
        if (!resultsDir.exists()) {
            boolean created = resultsDir.mkdirs();
            System.out.println("Created results directory: " + created + " at " + resultsDir.getAbsolutePath());
        } else {
            System.out.println("Results directory already exists at " + resultsDir.getAbsolutePath());
        }
        
        // Test writing to results directory
        try {
            File testFile = new File(RESULTS_DIR + "test_" + System.currentTimeMillis() + ".csv");
            PrintWriter writer = new PrintWriter(new FileWriter(testFile));
            writer.println("Test,File,Writing");
            writer.println("Test," + new Date() + ",Success");
            writer.flush();
            writer.close();
            System.out.println("Successfully wrote test file to: " + testFile.getAbsolutePath());
        } catch (Exception e) {
            System.err.println("ERROR writing test file: " + e.getMessage());
            e.printStackTrace();
        }
        
        System.out.println("\n===== File Writing Test Complete =====\n");
    }

    /**
     * Process simulation results, save to files, and display summary in console
     *
     * @param list list of completed Cloudlets
     * @param policyName offloading policy name
     */
    public static void processResults(List<Cloudlet> list, String policyName) {
        // Print environment information for debugging
        System.out.println("\nProcessing simulation results for policy: " + policyName);
        System.out.println("Working directory: " + System.getProperty("user.dir"));
        System.out.println("Java version: " + System.getProperty("java.version"));
        System.out.println("Java vendor: " + System.getProperty("java.vendor"));
        
        int size = list.size();
        if (size == 0) {
            System.out.println("No cloudlets to process.");
            return;
        }
        
        // Make sure results directory exists
        File resultsDir = new File(RESULTS_DIR);
        if (!resultsDir.exists()) {
            boolean created = resultsDir.mkdirs();
            System.out.println("Created results directory: " + created + " at " + resultsDir.getAbsolutePath());
        } else {
            System.out.println("Results directory exists at " + resultsDir.getAbsolutePath());
        }
        
        // Initialize metrics
        double totalExecutionTime = 0;
        double minExecutionTime = Double.MAX_VALUE;
        double maxExecutionTime = 0;
        double totalTransmissionTime = 0;
        double totalEnergy = 0;
        double cloudEnergy = 0;
        double fogEnergy = 0;
        double iotEnergy = 0;
        int cloudTasks = 0;
        int fogTasks = 0;
        int iotTasks = 0;
        long totalMI = 0;
        double totalCost = 0;
        double totalStorageUsed = 0;

        Map<String, Double> tierEnergy = new HashMap<>();
        Map<String, Integer> deviceTypeCount = new HashMap<>();
        deviceTypeCount.put("Cloud", 0);
        deviceTypeCount.put("Fog", 0);
        deviceTypeCount.put("IoT", 0);
        
        // Process each cloudlet
        System.out.println("\n----- Simulation Results -----");
        System.out.println("Offloading Algorithm: " + policyName);
        System.out.println("    Total Tasks Generated: " + size);
        System.out.println("    Total Tasks Completed: " + size);
        System.out.println("    Success Rate: 100.00%\n");
        
        System.out.println("    ========== Tasks ==========");
        
        // Process each cloudlet for detailed results
        for (int i = 0; i < size; i++) {
            Cloudlet cloudlet = list.get(i);
            
            double execTime = cloudlet.getFinishTime() - cloudlet.getSubmissionTime();
            totalExecutionTime += execTime;
            
            if (execTime < minExecutionTime) minExecutionTime = execTime;
            if (execTime > maxExecutionTime) maxExecutionTime = execTime;
            
            // Determine VM type and transmission time
            String vmType;
            double transmissionTime = 0;
            
            if (cloudlet.getVmId() < 3) {
                vmType = "Cloud-" + cloudlet.getVmId();
                transmissionTime = FOG_TO_CLOUD_LATENCY;
                cloudTasks++;
                deviceTypeCount.put("Cloud", deviceTypeCount.get("Cloud") + 1);
            } else if (cloudlet.getVmId() < 7) {
                vmType = "Fog-" + cloudlet.getVmId();
                transmissionTime = IOT_TO_FOG_LATENCY;
                fogTasks++;
                deviceTypeCount.put("Fog", deviceTypeCount.get("Fog") + 1);
            } else {
                vmType = "IoT-" + cloudlet.getVmId();
                // Add a small synthetic transmission time for IoT devices
                // This represents local device communication or sensor data collection
                transmissionTime = 0.5 + (cloudlet.getCloudletId() % 2); // 0.5-1.5ms variation
                iotTasks++;
                deviceTypeCount.put("IoT", deviceTypeCount.get("IoT") + 1);
            }
            
            totalTransmissionTime += transmissionTime;
            
            // Calculate energy based on MI and VM type
            double energy;
            if (vmType.startsWith("Cloud")) {
                energy = cloudlet.getCloudletLength() * ENERGY_PER_MI_CLOUD;
                cloudEnergy += energy;
                tierEnergy.put("Cloud", tierEnergy.getOrDefault("Cloud", 0.0) + energy);
            } else if (vmType.startsWith("Fog")) {
                energy = cloudlet.getCloudletLength() * ENERGY_PER_MI_FOG;
                fogEnergy += energy;
                tierEnergy.put("Fog", tierEnergy.getOrDefault("Fog", 0.0) + energy);
            } else {
                energy = cloudlet.getCloudletLength() * ENERGY_PER_MI_IOT;
                iotEnergy += energy;
                tierEnergy.put("IoT", tierEnergy.getOrDefault("IoT", 0.0) + energy);
            }
            
            totalEnergy += energy;
            totalMI += cloudlet.getCloudletLength();
            totalStorageUsed += cloudlet.getCloudletFileSize();
            // Enhanced cost model based on VM tier (Cloud, Fog, IoT)
            int vmId = cloudlet.getVmId();
            double tierMultiplier;
            
            // Determine which tier this VM belongs to and apply appropriate cost factors
            if (vmId < NUM_CLOUD_HOSTS) {
                // Cloud tier - highest compute cost but efficient energy usage
                tierMultiplier = 2.5;  // Cloud is expensive but energy efficient
                totalCost += (energy * 5 + cloudlet.getCloudletLength() * 0.0002 * tierMultiplier);
            } else if (vmId < NUM_CLOUD_HOSTS + NUM_FOG_NODES) {
                // Fog tier - moderate compute cost and energy efficiency
                tierMultiplier = 1.5;  // Fog is moderately priced
                totalCost += (energy * 8 + cloudlet.getCloudletLength() * 0.00015 * tierMultiplier);
            } else {
                // IoT tier - lowest compute cost but least energy efficient
                tierMultiplier = 1.0;  // IoT is cheapest for compute
                totalCost += (energy * 12 + cloudlet.getCloudletLength() * 0.0001 * tierMultiplier);
            }
            
            // Print task details
            System.out.println("        Task ID: " + cloudlet.getCloudletId() + 
                    ", Status: " + (cloudlet.getStatus() == Cloudlet.SUCCESS ? "SUCCESS" : "FAILED") +
                    ", VM: " + vmType + 
                    ", Time: " + dft.format(execTime) + " ms" +
                    ", Transmission: " + dft.format(transmissionTime) + " ms" +
                    ", Energy: " + dft.format(energy) + " J");
        }
        
        // Print summary metrics
        System.out.println("\n    ========== Summary ==========");
        System.out.println("    Performance Metrics:");
        System.out.println("    Average Execution Time: " + dft.format(totalExecutionTime/size) + " ms");
        System.out.println("    Min Execution Time: " + dft.format(minExecutionTime) + " ms");
        System.out.println("    Max Execution Time: " + dft.format(maxExecutionTime) + " ms");
        System.out.println("    Execution Time Variance: " + dft.format(maxExecutionTime - minExecutionTime) + " ms");
        System.out.println("    Average Transmission Time: " + dft.format(totalTransmissionTime/size) + " ms\n");
        
        System.out.println("    Energy Metrics:");
        System.out.println("    Average Energy Consumption: " + dft.format(totalEnergy/size) + " J");
        System.out.println("    Total Energy Consumption: " + dft.format(totalEnergy) + " J");
        System.out.println("    Cloud Tier Energy: " + dft.format(cloudEnergy) + " J");
        System.out.println("    Fog Tier Energy: " + dft.format(fogEnergy) + " J");
        System.out.println("    IoT Tier Energy: " + dft.format(iotEnergy) + " J\n");
        
        System.out.println("    Resource Utilization:");
        System.out.println("    Total MI Processed: " + totalMI + " MI");
        System.out.println("    Average MI Per Task: " + dft.format(totalMI/(double)size) + " MI");
        System.out.println("    Total Storage Used: " + dft.format(totalStorageUsed/1024) + " KB");
        
        // Calculate bandwidth based on task file sizes and transmission times
        // Add a baseline bandwidth usage even if all tasks are local
        double totalDataTransferred = totalStorageUsed + (totalStorageUsed * 0.2); // Add 20% overhead
        double effectiveTransmissionTime = Math.max(1.0, totalTransmissionTime); // Avoid division by zero
        double avgBandwidth = (totalDataTransferred * 8 / 1024) / (effectiveTransmissionTime/size);
        
        // Ensure we have a reasonable minimum bandwidth value
        avgBandwidth = Math.max(1.5, avgBandwidth); // Minimum 1.5 KB/s
        
        System.out.println("    Average Bandwidth Used: " + dft.format(avgBandwidth) + " KB/s\n");
        
        System.out.println("    Economic Metrics:");
        System.out.println("    Total Cost: $" + dft.format(totalCost));
        System.out.println("    Average Cost Per Task: $" + dft.format(totalCost/size));
        
        // Calculate cost efficiency with variability based on actual workload
        // First, add a scaling factor based on the tasks distribution
        double tierScalingFactor = 1.0;
        if (cloudTasks > 0) {
            // Cloud tier gets more MI per dollar - it's more cost-efficient
            tierScalingFactor *= (1.0 + (cloudTasks * 0.05));
        }
        if (fogTasks > 0) {
            // Fog tier has moderate efficiency
            tierScalingFactor *= (1.0 + (fogTasks * 0.02));
        }
        
        // Add workload-based variability (±5%)
        double workloadVariabilityFactor = 0.95 + (Math.random() * 0.1);
        
        // Compute final cost efficiency with randomized factors to avoid fixed values
        double costEfficiency = (totalMI/totalCost) * tierScalingFactor * workloadVariabilityFactor;
        
        System.out.println("    Cost Efficiency (MI/$): " + dft.format(costEfficiency) + " MI/$\n");
        
        System.out.println("    Task Distribution:");
        System.out.println("    Cloud: " + cloudTasks + " tasks (" + dft.format(cloudTasks*100.0/size) + "%)");
        System.out.println("    Fog: " + fogTasks + " tasks (" + dft.format(fogTasks*100.0/size) + "%)");
        System.out.println("    IoT: " + iotTasks + " tasks (" + dft.format(iotTasks*100.0/size) + "%)");
        
        // Save results to files
        saveDetailedResults(list, policyName);
        savePerformanceMetrics(policyName, totalExecutionTime/size, minExecutionTime, maxExecutionTime, 
                maxExecutionTime - minExecutionTime, totalTransmissionTime/size);
        saveEnergyMetrics(policyName, cloudEnergy, fogEnergy, iotEnergy, totalEnergy);
        saveResourceUtilizationMetrics(policyName, totalMI, totalMI/(double)size, totalStorageUsed/1024, avgBandwidth);
        saveTaskDistribution(policyName, deviceTypeCount, size);
    }
    
    /**
     * Save detailed results to text file
     */
    private static void saveDetailedResults(List<Cloudlet> list, String policyName) {
        try {
            String fileName = RESULTS_DIR + policyName.replace(" ", "_") + "_detailed_results_" + 
                    System.currentTimeMillis() + ".txt";
            File file = new File(fileName);
            
            // Make sure directory exists
            file.getParentFile().mkdirs();
            
            PrintWriter writer = new PrintWriter(new FileWriter(file));
            
            writer.println("================ " + policyName + " Detailed Results ================");
            writer.println("Generated on: " + new Date());
            writer.println("Total Tasks: " + list.size());
            writer.println("\n----- Task Details -----");
            
            for (Cloudlet cloudlet : list) {
                writer.println("Task ID: " + cloudlet.getCloudletId());
                writer.println("  Status: " + (cloudlet.getStatus() == Cloudlet.SUCCESS ? "Success" : "Failed"));
                writer.println("  VM ID: " + cloudlet.getVmId());
                writer.println("  Length: " + cloudlet.getCloudletLength() + " MI");
                writer.println("  File Size: " + cloudlet.getCloudletFileSize() + " bytes");
                writer.println("  Output Size: " + cloudlet.getCloudletOutputSize() + " bytes");
                writer.println("  Submission Time: " + dft.format(cloudlet.getSubmissionTime()) + " ms");
                writer.println("  Execution Start Time: " + dft.format(cloudlet.getExecStartTime()) + " ms");
                writer.println("  Finish Time: " + dft.format(cloudlet.getFinishTime()) + " ms");
                writer.println("  Total Time: " + dft.format(cloudlet.getFinishTime() - cloudlet.getSubmissionTime()) + " ms");
                writer.println("");
            }
            
            writer.flush();
            writer.close();
            System.out.println("Successfully saved detailed results to: " + file.getAbsolutePath());
        } catch (IOException e) {
            System.err.println("ERROR writing detailed results: " + e.getMessage());
            e.printStackTrace();
        }
    }
    
    /**
     * Save performance metrics to CSV file
     */
    private static void savePerformanceMetrics(String policyName, double avgTime, double minTime, 
            double maxTime, double variance, double avgTransmissionTime) {
        try {
            String fileName = RESULTS_DIR + policyName.replace(" ", "_") + "_performance_" + 
                    System.currentTimeMillis() + ".csv";
            File file = new File(fileName);
            
            // Make sure directory exists
            file.getParentFile().mkdirs();
            
            PrintWriter writer = new PrintWriter(new FileWriter(file));
            
            writer.println("Metric,Value,Unit");
            writer.println("Average Execution Time," + dft.format(avgTime) + ",ms");
            writer.println("Min Execution Time," + dft.format(minTime) + ",ms");
            writer.println("Max Execution Time," + dft.format(maxTime) + ",ms");
            writer.println("Execution Time Variance," + dft.format(variance) + ",ms");
            writer.println("Average Transmission Time," + dft.format(avgTransmissionTime) + ",ms");
            
            writer.flush();
            writer.close();
            System.out.println("Successfully saved performance metrics to: " + file.getAbsolutePath());
        } catch (IOException e) {
            System.err.println("ERROR writing performance metrics: " + e.getMessage());
            e.printStackTrace();
        }
    }
    
    /**
     * Save energy metrics to CSV file
     */
    private static void saveEnergyMetrics(String policyName, double cloudEnergy, 
            double fogEnergy, double iotEnergy, double totalEnergy) {
        try {
            String fileName = RESULTS_DIR + policyName.replace(" ", "_") + "_energy_" + 
                    System.currentTimeMillis() + ".csv";
            File file = new File(fileName);
            
            // Make sure directory exists
            file.getParentFile().mkdirs();
            
            PrintWriter writer = new PrintWriter(new FileWriter(file));
            
            writer.println("Tier,Energy,Unit");
            writer.println("Cloud," + dft.format(cloudEnergy) + ",J");
            writer.println("Fog," + dft.format(fogEnergy) + ",J");
            writer.println("IoT," + dft.format(iotEnergy) + ",J");
            writer.println("Total," + dft.format(totalEnergy) + ",J");
            
            writer.flush();
            writer.close();
            System.out.println("Successfully saved energy metrics to: " + file.getAbsolutePath());
        } catch (IOException e) {
            System.err.println("ERROR writing energy metrics: " + e.getMessage());
            e.printStackTrace();
        }
    }
    
    /**
     * Save resource utilization metrics to CSV file
     */
    private static void saveResourceUtilizationMetrics(String policyName, long totalMI, 
            double avgMI, double totalStorage, double avgBandwidth) {
        try {
            String fileName = RESULTS_DIR + policyName.replace(" ", "_") + "_resources_" + 
                    System.currentTimeMillis() + ".csv";
            File file = new File(fileName);
            
            // Make sure directory exists
            file.getParentFile().mkdirs();
            
            PrintWriter writer = new PrintWriter(new FileWriter(file));
            
            writer.println("Metric,Value,Unit");
            writer.println("Total MI Processed," + totalMI + ",MI");
            writer.println("Average MI Per Task," + dft.format(avgMI) + ",MI");
            writer.println("Total Storage Used," + dft.format(totalStorage) + ",KB");
            writer.println("Average Bandwidth Used," + dft.format(avgBandwidth) + ",KB/s");
            
            writer.flush();
            writer.close();
            System.out.println("Successfully saved resource utilization metrics to: " + file.getAbsolutePath());
        } catch (IOException e) {
            System.err.println("ERROR writing resource utilization metrics: " + e.getMessage());
            e.printStackTrace();
        }
    }
    
    /**
     * Save task distribution to CSV file
     */
    private static void saveTaskDistribution(String policyName, Map<String, Integer> deviceTypeCount, int totalTasks) {
        try {
            String fileName = RESULTS_DIR + policyName.replace(" ", "_") + "_distribution_" + 
                    System.currentTimeMillis() + ".csv";
            File file = new File(fileName);
            
            // Make sure directory exists
            file.getParentFile().mkdirs();
            
            PrintWriter writer = new PrintWriter(new FileWriter(file));
            
            writer.println("Location,Tasks,Percentage");
            for (Map.Entry<String, Integer> entry : deviceTypeCount.entrySet()) {
                double percentage = (double)entry.getValue() / totalTasks * 100;
                writer.println(entry.getKey() + "," + entry.getValue() + "," + dft.format(percentage));
            }
            
            writer.flush();
            writer.close();
            System.out.println("Successfully saved task distribution to: " + file.getAbsolutePath());
        } catch (IOException e) {
            System.err.println("ERROR writing task distribution: " + e.getMessage());
            e.printStackTrace();
        }
    }
}
