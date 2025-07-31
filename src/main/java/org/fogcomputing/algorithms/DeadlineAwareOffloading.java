package org.fogcomputing.algorithms;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.cloudbus.cloudsim.core.CloudSim;
import org.fog.application.AppModule;
import org.fog.entities.FogDevice;
import org.fog.entities.Tuple;
import org.fog.placement.ModulePlacement;

/**
 * Deadline-Aware Task Offloading Algorithm
 * This algorithm prioritizes meeting task deadlines when making offloading decisions
 */
public class DeadlineAwareOffloading extends ModulePlacement {

    private List<FogDevice> fogDevices;
    private Map<String, Double> deviceProcessingMap;
    private Map<String, Integer> currentLoad;
    private Map<String, Double> deadlines;

    /**
     * Constructor for Deadline-Aware Offloading Algorithm
     * @param fogDevices List of fog devices in the system
     * @param application List of application modules
     */
    public DeadlineAwareOffloading(List<FogDevice> fogDevices, Map<String, AppModule> application) {
        super(fogDevices, application);
        this.fogDevices = fogDevices;
        this.deviceProcessingMap = new HashMap<>();
        this.currentLoad = new HashMap<>();
        this.deadlines = new HashMap<>();
        
        // Initialize processing speed and load maps
        for (FogDevice device : fogDevices) {
            deviceProcessingMap.put(device.getName(), device.getMips());
            currentLoad.put(device.getName(), 0);
        }
        
        // Set default deadlines for different module types (in milliseconds)
        deadlines.put("iot_module", 1000.0);  // 1 second
        deadlines.put("fog_module", 5000.0);  // 5 seconds
        deadlines.put("cloud_module", 10000.0); // 10 seconds
    }

    /**
     * Get the best device for offloading based on deadline considerations
     * @param moduleType The module type to be offloaded
     * @param sourceDeviceName The source device name
     * @return The target device name for offloading
     */
    public String getBestDeviceForTask(String moduleType, String sourceDeviceName) {
        // Skip offloading decision for IoT modules - they always run on IoT devices
        if (moduleType.equals("iot_module")) {
            return sourceDeviceName;
        }
        
        // Parameters to consider for offloading
        double minExecutionTime = Double.MAX_VALUE;
        String targetDeviceName = null;
        FogDevice sourceDevice = getDeviceByName(sourceDeviceName);
        
        // Skip if source device not found
        if (sourceDevice == null)
            return null;
        
        // Get deadline for this module type
        double taskDeadline = deadlines.get(moduleType);
        
        // Consider each device as potential target for offloading
        for (FogDevice targetDevice : fogDevices) {
            // Skip invalid devices
            if (targetDevice == null || targetDevice.equals(sourceDevice)) {
                continue;
            }
            
            // For fog_module, consider only fog nodes
            if (moduleType.equals("fog_module") && !targetDevice.getName().startsWith("fog-")) {
                continue;
            }
            
            // For cloud_module, consider only cloud
            if (moduleType.equals("cloud_module") && !targetDevice.getName().startsWith("cloud")) {
                continue;
            }
            
            // Calculate execution time on this target device
            double targetDeviceMips = deviceProcessingMap.get(targetDevice.getName());
            int targetDeviceLoad = currentLoad.get(targetDevice.getName());
            double uplinkLatency = getLatency(sourceDevice, targetDevice);
            double downlinkLatency = getLatency(targetDevice, sourceDevice);
            
            // Task execution time calculation
            AppModule module = getApplication().getModuleByName(moduleType);
            double taskLength = module.getMips(); // Task length in MI
            
            // Execution time = computation time + transmission time
            double computationTime = (taskLength / targetDeviceMips) * 1000; // convert to ms
            double transmissionTime = uplinkLatency + downlinkLatency;
            double totalExecutionTime = computationTime + transmissionTime;
            
            // Apply load factor (more load = slower execution)
            totalExecutionTime *= (1 + (targetDeviceLoad * 0.1));
            
            // Choose device with minimum execution time that meets deadline
            if (totalExecutionTime < minExecutionTime && totalExecutionTime < taskDeadline) {
                minExecutionTime = totalExecutionTime;
                targetDeviceName = targetDevice.getName();
            }
        }
        
        // If no device meets deadline, choose the fastest one
        if (targetDeviceName == null) {
            minExecutionTime = Double.MAX_VALUE;
            for (FogDevice targetDevice : fogDevices) {
                // Skip invalid devices
                if (targetDevice == null || targetDevice.equals(sourceDevice)) {
                    continue;
                }
                
                // Filter by device type as before
                if ((moduleType.equals("fog_module") && !targetDevice.getName().startsWith("fog-")) ||
                    (moduleType.equals("cloud_module") && !targetDevice.getName().startsWith("cloud"))) {
                    continue;
                }
                
                // Calculate execution time
                double targetDeviceMips = deviceProcessingMap.get(targetDevice.getName());
                int targetDeviceLoad = currentLoad.get(targetDevice.getName());
                double uplinkLatency = getLatency(sourceDevice, targetDevice);
                double downlinkLatency = getLatency(targetDevice, sourceDevice);
                
                AppModule module = getApplication().getModuleByName(moduleType);
                double taskLength = module.getMips();
                double computationTime = (taskLength / targetDeviceMips) * 1000;
                double transmissionTime = uplinkLatency + downlinkLatency;
                double totalExecutionTime = computationTime + transmissionTime;
                totalExecutionTime *= (1 + (targetDeviceLoad * 0.1));
                
                if (totalExecutionTime < minExecutionTime) {
                    minExecutionTime = totalExecutionTime;
                    targetDeviceName = targetDevice.getName();
                }
            }
        }
        
        // Update the load of the chosen device
        if (targetDeviceName != null) {
            int currentDeviceLoad = currentLoad.get(targetDeviceName);
            currentLoad.put(targetDeviceName, currentDeviceLoad + 1);
        }
        
        return targetDeviceName;
    }
    
    /**
     * Get fog device by name
     */
    private FogDevice getDeviceByName(String name) {
        for (FogDevice device : fogDevices) {
            if (device.getName().equals(name)) {
                return device;
            }
        }
        return null;
    }
    
    /**
     * Calculate network latency between two devices
     */
    private double getLatency(FogDevice sourceDevice, FogDevice targetDevice) {
        if (sourceDevice.getParentId() == targetDevice.getId()) {
            // Child to parent latency
            return sourceDevice.getUplinkLatency();
        } else if (targetDevice.getParentId() == sourceDevice.getId()) {
            // Parent to child latency
            return targetDevice.getUplinkLatency();
        } else {
            // Devices not directly connected, use sum of latencies
            return sourceDevice.getUplinkLatency() + targetDevice.getUplinkLatency();
        }
    }
    
    /**
     * Override methods from ModulePlacement
     * This isn't actually used in our implementation but needs to be defined
     */
    @Override
    public void processEvent(int eventType, Object data) {
        // No specific event processing in this implementation
    }
    
    /**
     * Process module submission
     */
    public void processAppSubmit() {
        // Called by the controller
        for (String moduleType : getApplication().getModules()) {
            for (FogDevice device : getFogDevices()) {
                // Make offloading decision for this module and device
                String targetDeviceName = getBestDeviceForTask(moduleType, device.getName());
                if (targetDeviceName != null) {
                    // Create mapping to deploy module on target device
                    createModuleInstanceOnDevice(getApplication().getModuleByName(moduleType), 
                                              getDeviceByName(targetDeviceName));
                }
            }
        }
    }
    
    /**
     * Required override from ModulePlacement
     */
    @Override
    protected void mapModules() {
        // This method will be called during initialization to place modules
        for (String moduleType : getApplication().getModules()) {
            for (FogDevice device : getFogDevices()) {
                // Initial placement of modules
                if ((moduleType.equals("iot_module") && device.getName().startsWith("iot-")) ||
                    (moduleType.equals("fog_module") && device.getName().startsWith("fog-")) ||
                    (moduleType.equals("cloud_module") && device.getName().startsWith("cloud"))) {
                    // Create mapping to deploy module on target device
                    createModuleInstanceOnDevice(getApplication().getModuleByName(moduleType), device);
                }
            }
        }
    }
    
    /**
     * Process a tuple arrival and potentially make dynamic offloading decisions
     */
    public void processTuple(Tuple tuple, FogDevice sourceDevice) {
        // If this is a task offloading tuple, make a decision
        if (tuple.getTupleType().equals("OFFLOAD-TASK")) {
            String moduleToOffload = tuple.getDestModuleName();
            String targetDeviceName = getBestDeviceForTask(moduleToOffload, sourceDevice.getName());
            
            // Update the tuple's destination
            if (targetDeviceName != null && !targetDeviceName.equals(sourceDevice.getName())) {
                // Log the offloading decision
                System.out.println("Deadline-Aware Offloading: Task " + tuple.getCloudletId() + 
                                 " offloaded from " + sourceDevice.getName() + 
                                 " to " + targetDeviceName);
                
                // Update destination (in a real implementation this would modify tuple routing)
            }
        }
    }
}
