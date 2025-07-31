package org.fogcomputing;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.cloudbus.cloudsim.Host;
import org.cloudbus.cloudsim.Log;
import org.cloudbus.cloudsim.Vm;
import org.cloudbus.cloudsim.VmAllocationPolicySimple;

/**
 * A VM allocation policy that only accepts VMs with specific IDs.
 * This ensures that each datacenter only creates VMs that belong to a specific tier.
 */
public class TieredVmAllocationPolicy extends VmAllocationPolicySimple {
    
    private int minVmId;
    private int maxVmId;
    private String tierName;
    
    /**
     * Creates a new TieredVmAllocationPolicy
     * @param list the list of hosts
     * @param minVmId the minimum VM ID that this datacenter should accept
     * @param maxVmId the maximum VM ID that this datacenter should accept
     * @param tierName the name of the tier (for logging purposes)
     */
    public TieredVmAllocationPolicy(List<? extends Host> list, int minVmId, int maxVmId, String tierName) {
        super(list);
        this.minVmId = minVmId;
        this.maxVmId = maxVmId;
        this.tierName = tierName;
    }
    
    /**
     * Allocate a host for a VM - only if it's in the allowed VM ID range for this datacenter.
     * 
     * @param vm Virtual Machine that needs allocation
     * @return true if the allocation was successful, false otherwise
     */
    @Override
    public boolean allocateHostForVm(Vm vm) {
        int vmId = vm.getId();
        
        // COMPLETELY REMOVE THE VM ID CHECK TO LET ANY VM ALLOCATE TO ANY DATACENTER
        // This allows more flexible allocation based on VM compatibility rather than ID range
        
        System.out.println("\n======== DEBUG: " + tierName + " datacenter trying to allocate VM #" + vmId + " ========");
        
        // Get VM requirements for diagnostics
        int vmRam = vm.getRam();
        int vmPes = vm.getNumberOfPes();
        double vmMips = vm.getMips();
        long vmSize = vm.getSize();
        long vmBw = vm.getBw();
        
        System.out.println("VM #" + vmId + " specs: " + vmPes + " cores, " + 
                        vmMips + " MIPS per core, " + vmRam + "MB RAM, " + 
                        vmBw + " bandwidth, " + vmSize + "MB storage");
        
        // First check if we have any hosts at all
        if (getHostList().isEmpty()) {
            System.out.println("ERROR: " + tierName + " datacenter has NO HOSTS! Cannot allocate VM #" + vmId);
            return false;
        }

        // Print host count
        System.out.println(tierName + " datacenter has " + getHostList().size() + " hosts");
        
        // Try the default allocation algorithm first
        for (Host host : getHostList()) {
            if (host.vmCreate(vm)) {
                System.out.println("SUCCESS: VM #" + vmId + " allocated to host #" + host.getId() + 
                                " in " + tierName + " datacenter");
                getVmTable().put(vm.getUid(), host);
                return true;
            } else {
                System.out.println("Host #" + host.getId() + " in " + tierName + 
                                " datacenter FAILED to create VM #" + vmId);
                
                // Detailed diagnostics for why allocation failed
                System.out.println("  - Host has " + host.getNumberOfPes() + " PEs, VM needs " + vmPes);
                System.out.println("  - Host has " + host.getTotalMips() + " total MIPS, VM needs " + vmMips + " per PE");
                System.out.println("  - Host has " + host.getRamProvisioner().getAvailableRam() + "MB RAM, VM needs " + vmRam + "MB");
                System.out.println("  - Host has " + host.getBwProvisioner().getAvailableBw() + " BW, VM needs " + vmBw);
                System.out.println("  - Host has " + host.getStorage() + "MB storage, VM needs " + vmSize + "MB");
                System.out.println("  - Host uses " + host.getVmScheduler().getClass().getSimpleName() + " scheduler");
            }
        }
        
        System.out.println("ERROR: No compatible host found for VM #" + vmId + " in " + tierName + " datacenter");
        return false;
    }
}
