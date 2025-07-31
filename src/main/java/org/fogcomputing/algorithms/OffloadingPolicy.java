package org.fogcomputing.algorithms;

import java.util.List;
import org.cloudbus.cloudsim.Cloudlet;
import org.cloudbus.cloudsim.Vm;

/**
 * Interface defining an offloading policy that decides which VM should execute a task
 */
public interface OffloadingPolicy {
    
    /**
     * Determines the target VM for a task based on the policy's decision criteria
     * 
     * @param cloudlet the task to be offloaded
     * @param vmList the list of available VMs
     * @return the ID of the target VM
     */
    public int getTargetVmId(Cloudlet cloudlet, List<Vm> vmList);
}
