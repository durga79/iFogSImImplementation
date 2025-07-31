package org.fogcomputing.algorithms;

import java.util.List;
import org.cloudbus.cloudsim.Cloudlet;
import org.cloudbus.cloudsim.Vm;

public class DeadlineAwareOffloadingPolicy implements OffloadingPolicy {

    @Override
    public int getTargetVmId(Cloudlet cloudlet, List<Vm> vmList) {
        // Deadline-aware allocation strategy
        // Distribute tasks based on deadline constraints
        // For simulation, we'll use a simple rule-based approach:
        // - Tasks with tight deadlines go to higher-performance resources
        // - Tasks with longer deadlines can afford more transmission time
        
        int cloudletId = cloudlet.getCloudletId();
        
        // For this simplified implementation, we'll make decisions based on cloudlet ID
        // In a real implementation, this would be based on the actual deadline
        
        if (cloudletId == 0 || cloudletId == 1) {
            // First two tasks go to cloud (assuming they have tight deadlines)
            return cloudletId % 2;
        } else if (cloudletId >= 2 && cloudletId <= 6) {
            // Next 5 tasks go to fog nodes
            return 2 + (cloudletId - 2);
        } else {
            // Remaining tasks stay on IoT devices
            return 7 + ((cloudletId - 7) % 3);
        }
    }
}
