package org.fogcomputing.algorithms;

import java.util.*;
import org.cloudbus.cloudsim.Cloudlet;
import org.cloudbus.cloudsim.Vm;

/**
 * Implementation of the MCEETO (Multi-Classifiers based Energy-Efficient Task Offloading) algorithm
 * Based on the paper: "A Multi-Classifiers Based Algorithm for Energy Efficient Tasks Offloading in Fog Computing"
 * Published in MDPI Sensors 2023, Vol. 23, Issue 16
 * 
 * This algorithm uses a multi-classifier approach to make energy-efficient task offloading decisions
 * in a hybrid IoT-Fog-Cloud environment.
 */
public class MCEETOOffloadingPolicy implements OffloadingPolicy {
    
    // Energy consumption coefficients for different tiers (in J/MI)
    private static final double CLOUD_ENERGY_COEFF = 0.0005;  // Energy consumed per MI in cloud
    private static final double FOG_ENERGY_COEFF = 0.0003;    // Energy consumed per MI in fog
    private static final double IOT_ENERGY_COEFF = 0.0001;    // Energy consumed per MI in IoT devices
    
    // Network transmission energy coefficients (J/byte)
    private static final double IOT_TO_FOG_TRANS_ENERGY = 0.00001;
    private static final double FOG_TO_CLOUD_TRANS_ENERGY = 0.00002;
    
    // Network latencies (ms)
    private static final double IOT_TO_FOG_LATENCY = 2.0;
    private static final double FOG_TO_CLOUD_LATENCY = 20.0;
    
    // VM tier identification
    private static final int CLOUD_VM_START = 0;
    private static final int CLOUD_VM_END = 1;
    private static final int FOG_VM_START = 2;
    private static final int FOG_VM_END = 6;
    private static final int IOT_VM_START = 7;
    private static final int IOT_VM_END = 16;

    // Task properties thresholds
    private static final int HIGH_MI_THRESHOLD = 30000;      // High computation tasks (MI)
    private static final int MEDIUM_MI_THRESHOLD = 20000;    // Medium computation tasks (MI)
    private static final int LARGE_DATA_THRESHOLD = 1000;    // Large data tasks (bytes)
    private static final int MEDIUM_DATA_THRESHOLD = 500;    // Medium data tasks (bytes)
    
    // Cache for decisions to improve performance
    private Map<Integer, Integer> taskDecisionCache = new HashMap<>();
    
    /**
     * Get the target VM ID for offloading a cloudlet based on the MCEETO algorithm
     *
     * @param cloudlet The cloudlet (task) to be offloaded
     * @param vmList List of available VMs
     * @return The ID of the target VM
     */
    @Override
    public int getTargetVmId(Cloudlet cloudlet, List<Vm> vmList) {
        int cloudletId = cloudlet.getCloudletId();
        
        // Check if we've already made a decision for this task
        if (taskDecisionCache.containsKey(cloudletId)) {
            return taskDecisionCache.get(cloudletId);
        }
        
        // Get task characteristics
        long taskLength = cloudlet.getCloudletLength(); // MI
        long taskDataSize = cloudlet.getCloudletFileSize() + cloudlet.getCloudletOutputSize(); // bytes
        
        // Step 1: Feature extraction and classification of the task
        TaskCategory category = classifyTask(taskLength, taskDataSize);
        
        // Step 2: Multi-classifier decision process
        int targetVmId = applyMultiClassifierDecision(category, cloudlet, vmList);
        
        // Cache the decision
        taskDecisionCache.put(cloudletId, targetVmId);
        
        System.out.println("MCEETO: Task #" + cloudletId + 
                          " (Length: " + taskLength + " MI, Data: " + taskDataSize + 
                          " bytes) classified as " + category + 
                          " -> assigned to VM #" + targetVmId);
        
        return targetVmId;
    }
    
    /**
     * Classify the task based on its computation and data requirements
     * 
     * @param taskLength Task computation requirement in MI
     * @param taskDataSize Task data size in bytes
     * @return TaskCategory classification
     */
    private TaskCategory classifyTask(long taskLength, long taskDataSize) {
        // High computation, low data -> CLOUD preferred
        if (taskLength >= HIGH_MI_THRESHOLD && taskDataSize < MEDIUM_DATA_THRESHOLD) {
            return TaskCategory.HIGH_COMPUTE_LOW_DATA;
        }
        // High computation, high data -> FOG preferred
        else if (taskLength >= HIGH_MI_THRESHOLD && taskDataSize >= MEDIUM_DATA_THRESHOLD) {
            return TaskCategory.HIGH_COMPUTE_HIGH_DATA;
        }
        // Medium computation, medium data -> FOG preferred
        else if (taskLength >= MEDIUM_MI_THRESHOLD && taskLength < HIGH_MI_THRESHOLD && 
                taskDataSize >= MEDIUM_DATA_THRESHOLD && taskDataSize < LARGE_DATA_THRESHOLD) {
            return TaskCategory.MEDIUM_COMPUTE_MEDIUM_DATA;
        }
        // Medium computation, high data -> FOG preferred
        else if (taskLength >= MEDIUM_MI_THRESHOLD && taskLength < HIGH_MI_THRESHOLD && 
                taskDataSize >= LARGE_DATA_THRESHOLD) {
            return TaskCategory.MEDIUM_COMPUTE_HIGH_DATA;
        }
        // Low computation, low data -> IOT preferred
        else if (taskLength < MEDIUM_MI_THRESHOLD && taskDataSize < MEDIUM_DATA_THRESHOLD) {
            return TaskCategory.LOW_COMPUTE_LOW_DATA;
        }
        // Low computation, high data -> IOT or FOG preferred
        else {
            return TaskCategory.LOW_COMPUTE_HIGH_DATA;
        }
    }
    
    /**
     * Apply the multi-classifier decision process to determine the target VM
     * 
     * @param category Task category from classification
     * @param cloudlet The cloudlet to be processed
     * @param vmList List of available VMs
     * @return Target VM ID
     */
    private int applyMultiClassifierDecision(TaskCategory category, Cloudlet cloudlet, List<Vm> vmList) {
        int cloudletId = cloudlet.getCloudletId();
        long taskLength = cloudlet.getCloudletLength();
        long taskDataSize = cloudlet.getCloudletFileSize() + cloudlet.getCloudletOutputSize();
        
        // Apply the ensemble decision based on the category
        switch (category) {
            case HIGH_COMPUTE_LOW_DATA:
                // For high computation, low data tasks, prefer cloud for better processing capability
                // Select among cloud VMs (VM 0-1) using round-robin
                return CLOUD_VM_START + (cloudletId % (CLOUD_VM_END - CLOUD_VM_START + 1));
                
            case HIGH_COMPUTE_HIGH_DATA:
                // For high computation, high data tasks, consider energy tradeoff between computation and transmission
                // Calculate energy cost for cloud vs. fog
                double cloudEnergyEstimate = (taskLength * CLOUD_ENERGY_COEFF) + 
                                            (taskDataSize * (IOT_TO_FOG_TRANS_ENERGY + FOG_TO_CLOUD_TRANS_ENERGY));
                double fogEnergyEstimate = (taskLength * FOG_ENERGY_COEFF) + 
                                          (taskDataSize * IOT_TO_FOG_TRANS_ENERGY);
                
                // Choose based on energy efficiency
                if (cloudEnergyEstimate <= fogEnergyEstimate) {
                    return CLOUD_VM_START + (cloudletId % (CLOUD_VM_END - CLOUD_VM_START + 1));
                } else {
                    return FOG_VM_START + (cloudletId % (FOG_VM_END - FOG_VM_START + 1));
                }
                
            case MEDIUM_COMPUTE_MEDIUM_DATA:
            case MEDIUM_COMPUTE_HIGH_DATA:
                // For medium computation tasks, prefer fog nodes for balance
                return FOG_VM_START + (cloudletId % (FOG_VM_END - FOG_VM_START + 1));
                
            case LOW_COMPUTE_LOW_DATA:
                // For low computation, low data tasks, prefer IoT devices to minimize transmission
                return IOT_VM_START + (cloudletId % (IOT_VM_END - IOT_VM_START + 1));
                
            case LOW_COMPUTE_HIGH_DATA:
                // For low computation but high data, calculate if it's worth transmitting
                if (taskDataSize > LARGE_DATA_THRESHOLD) {
                    // If data is very large, process locally to avoid transmission costs
                    return IOT_VM_START + (cloudletId % (IOT_VM_END - IOT_VM_START + 1));
                } else {
                    // Otherwise, use fog for better processing
                    return FOG_VM_START + (cloudletId % (FOG_VM_END - FOG_VM_START + 1));
                }
                
            default:
                // Fallback to round-robin across all VMs if classification fails
                return cloudletId % vmList.size();
        }
    }
    
    /**
     * Task categories used by the classifier
     */
    private enum TaskCategory {
        HIGH_COMPUTE_LOW_DATA,
        HIGH_COMPUTE_HIGH_DATA,
        MEDIUM_COMPUTE_MEDIUM_DATA,
        MEDIUM_COMPUTE_HIGH_DATA,
        LOW_COMPUTE_LOW_DATA,
        LOW_COMPUTE_HIGH_DATA
    }
}
