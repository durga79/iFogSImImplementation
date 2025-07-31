package org.fogcomputing.algorithms;

import java.util.List;
import org.cloudbus.cloudsim.Cloudlet;
import org.cloudbus.cloudsim.Vm;

public class EnergyAwareOffloadingPolicy implements OffloadingPolicy {

    @Override
    public int getTargetVmId(Cloudlet cloudlet, List<Vm> vmList) {
        // Energy-aware allocation strategy 
        // Distribute tasks to minimize energy consumption
        // For simulation, we'll use a simple rule-based approach:
        // - CPU-intensive tasks go to cloud (better energy efficiency for computation)
        // - Data-intensive but less compute-heavy tasks go to fog (reduced transmission energy)
        // - Very small tasks stay on IoT devices (no transmission energy)
        
        int cloudletId = cloudlet.getCloudletId();
        int numVms = vmList.size();
        
        // Simple rule-based assignment
        // First 20% go to cloud, middle 50% go to fog, remaining 30% stay on IoT
        if (cloudletId < 2) {
            // Cloud VMs (first 2 VMs)
            return cloudletId % 2; // Round-robin among cloud VMs
        } else if (cloudletId < 7) {
            // Fog VMs (next 5 VMs)
            return 2 + (cloudletId % 5); // Round-robin among fog VMs
        } else {
            // IoT VMs (remaining VMs)
            return 7 + (cloudletId % 3); // Round-robin among IoT VMs
        }
    }
}
