package org.fogcomputing.algorithms;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.cloudbus.cloudsim.core.CloudSim;
import org.fog.application.AppModule;
import org.fog.entities.FogDevice;
import org.fog.entities.Tuple;
import org.fog.placement.ModulePlacement;
import org.fog.utils.NetworkUsageMonitor;

/**
 * Energy-Aware Task Offloading Algorithm
 * This algorithm prioritizes energy efficiency when making offloading decisions
 */
public class EnergyAwareOffloading extends ModulePlacement {

    private List<FogDevice> fogDevices;
    private Map<String, Double> deviceEnergyMap;
    private Map<String, Integer> currentLoad;

    /**
     * Constructor for Energy-Aware Offloading Algorithm
     * @param fogDevices List of fog devices in the system
     * @param application List of application modules
     */
    public EnergyAwareOffloading(List<FogDevice> fogDevices, Map<String, AppModule> application) {
        super(fogDevices, application);
        this.fogDevices = fogDevices;
        this.deviceEnergyMap = new HashMap<>();
        this.currentLoad = new HashMap<>();
        
        // Initialize energy and load maps
        for (FogDevice device : fogDevices) {
            deviceEnergyMap.put(device.getName(), device.getEnergyConsumption());
            currentLoad.put(device.getName(), 0);
        }
    }

    /**
     * Get the best device for offloading based on energy considerations
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
        double minEnergyImpact = Double.MAX_VALUE;
        String targetDeviceName = null;
        FogDevice sourceDevice = getDeviceByName(sourceDeviceName);
        
        // Skip if source device not found
        if (sourceDevice == null)
            return null;
        
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
            
            // Calculate energy impact of this offloading decision
            double targetDeviceEnergy = deviceEnergyMap.get(targetDevice.getName());
            int targetDeviceLoad = currentLoad.get(targetDevice.getName());
            double uplinkLatency = getLatency(sourceDevice, targetDevice);
            double downlinkLatency = getLatency(targetDevice, sourceDevice);
            
            // Energy impact calculation
            // Consider: target device energy consumption, network energy, and load balancing
            double energyImpact = calculateEnergyImpact(targetDeviceEnergy, targetDeviceLoad, 
                                                      uplinkLatency, downlinkLatency);
            
            // Choose device with minimum energy impact
            if (energyImpact < minEnergyImpact) {
                minEnergyImpact = energyImpact;
                targetDeviceName = targetDevice.getName();
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
     * Calculate the energy impact of offloading a task
     */
    private double calculateEnergyImpact(double deviceEnergy, int deviceLoad, 
                                      double uplinkLatency, double downlinkLatency) {
        // Energy impact is a weighted sum of:
        // 1. Device energy consumption
        // 2. Network energy (based on latencies)
        // 3. Current device load (for load balancing)
        
        double deviceEnergyFactor = deviceEnergy * 0.4; // 40% weight to device energy
        double networkEnergyFactor = (uplinkLatency + downlinkLatency) * 0.3; // 30% weight to network
        double loadFactor = deviceLoad * 0.3; // 30% weight to current load
        
        return deviceEnergyFactor + networkEnergyFactor + loadFactor;
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
                System.out.println("Energy-Aware Offloading: Task " + tuple.getCloudletId() + 
                                 " offloaded from " + sourceDevice.getName() + 
                                 " to " + targetDeviceName);
                
                // Update destination (in a real implementation this would modify tuple routing)
            }
        }
    }
}
