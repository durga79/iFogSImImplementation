package org.fogcomputing;

import org.cloudbus.cloudsim.Host;
import org.cloudbus.cloudsim.Log;
import org.cloudbus.cloudsim.Vm;
import org.cloudbus.cloudsim.VmAllocationPolicy;
import org.cloudbus.cloudsim.core.CloudSim;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * A more flexible VM allocation policy for multi-tier environments.
 * This policy doesn't restrict VMs by ID range and is more permissive with resource checks.
 */
public class FlexibleVmAllocationPolicy extends VmAllocationPolicy {

    /** The VM table. */
    private Map<String, Host> vmTable;
    
    /** A name identifier for the datacenter tier */
    private String tierName;

    /**
     * Creates a new FlexibleVmAllocationPolicy.
     * 
     * @param list the list of hosts
     * @param tierName the name of the datacenter tier (for debugging)
     */
    public FlexibleVmAllocationPolicy(List<Host> list, String tierName) {
        super(list);
        setVmTable(new HashMap<String, Host>());
        this.tierName = tierName;
    }

    /**
     * Allocates a host for a given VM without ID restrictions.
     * 
     * @param vm the VM to allocate
     * @return true if the host allocation was successful, false otherwise
     */
    @Override
    public boolean allocateHostForVm(Vm vm) {
        int vmId = vm.getId();
        
        System.out.println("\n======== DEBUG: " + tierName + " datacenter trying to allocate VM #" + vmId + " ========");
        
        // Print VM specifications for debugging
        System.out.println("VM #" + vmId + " specs: " + 
                           vm.getNumberOfPes() + " cores, " + 
                           vm.getMips() + " MIPS per core, " + 
                           vm.getRam() + "MB RAM, " + 
                           vm.getBw() + " BW, " + 
                           vm.getSize() + "MB storage");
        
        // Check if we have any hosts
        if (getHostList().isEmpty()) {
            System.out.println("ERROR: " + tierName + " datacenter has NO HOSTS!");
            return false;
        }
        
        System.out.println(tierName + " datacenter has " + getHostList().size() + " hosts");
        
        // Try all hosts one by one
        boolean result = false;
        int tries = 0;
        
        // First try to find a host that can accommodate the VM
        for (Host host : getHostList()) {
            tries++;
            // Try to create VM on this host
            result = host.vmCreate(vm);
            if (result) {
                // Success! VM created on host
                System.out.println("SUCCESS: VM #" + vmId + " allocated to host #" + host.getId() + 
                                 " in " + tierName + " datacenter (attempt #" + tries + ")");
                getVmTable().put(vm.getUid(), host);
                return true;
            }
            
            // Output detailed diagnostics for why allocation failed
            System.out.println("Host #" + host.getId() + " failed to create VM #" + vmId);
            System.out.println("  - Host has " + host.getNumberOfPes() + " PEs, VM needs " + vm.getNumberOfPes());
            System.out.println("  - Host has " + host.getTotalMips() + " total MIPS, VM needs " + vm.getMips() + " per PE");
            System.out.println("  - Host has " + host.getRamProvisioner().getAvailableRam() + "MB RAM, VM needs " + vm.getRam() + "MB");
            System.out.println("  - Host has " + host.getBwProvisioner().getAvailableBw() + " BW, VM needs " + vm.getBw());
            System.out.println("  - Host has " + host.getStorage() + "MB storage, VM needs " + vm.getSize() + "MB");
            System.out.println("  - Host uses " + host.getVmScheduler().getClass().getSimpleName() + " scheduler");
        }
        
        System.out.println("FAILURE: Could not allocate VM #" + vmId + " to any host in " + tierName + " datacenter");
        return false;
    }

    /**
     * Allocates a specific host for a given VM.
     * 
     * @param vm the VM to allocate
     * @param host the host to allocate
     * @return true if the allocation was successful, false otherwise
     */
    @Override
    public boolean allocateHostForVm(Vm vm, Host host) {
        if (host.vmCreate(vm)) {
            getVmTable().put(vm.getUid(), host);
            System.out.println("VM #" + vm.getId() + " allocated to host #" + host.getId());
            return true;
        }
        System.out.println("Failed to allocate VM #" + vm.getId() + " to host #" + host.getId());
        return false;
    }

    /**
     * Releases the host used by a VM.
     * 
     * @param vm the vm
     */
    @Override
    public void deallocateHostForVm(Vm vm) {
        Host host = getVmTable().remove(vm.getUid());
        if (host != null) {
            host.vmDestroy(vm);
        }
    }

    /**
     * Get the host that is executing the given VM.
     * 
     * @param vm the vm
     * @return the Host with the given vm
     */
    @Override
    public Host getHost(Vm vm) {
        return getVmTable().get(vm.getUid());
    }

    /**
     * Get the host that is executing the given VM.
     * 
     * @param vmId the vm id
     * @param userId the user id
     * @return the Host with the given vm
     */
    @Override
    public Host getHost(int vmId, int userId) {
        return getVmTable().get(Vm.getUid(userId, vmId));
    }

    /**
     * Gets the vm table.
     * 
     * @return the vm table
     */
    public Map<String, Host> getVmTable() {
        return vmTable;
    }

    /**
     * Sets the vm table.
     * 
     * @param vmTable the vm table
     */
    protected void setVmTable(Map<String, Host> vmTable) {
        this.vmTable = vmTable;
    }
    
    /**
     * Optimize allocation of the VMs according to current utilization.
     * This implementation does no optimization and simply returns null.
     *
     * @param vmList the vm list to be optimized
     * @return null, as no optimization is done
     */
    @Override
    public List<Map<String, Object>> optimizeAllocation(List<? extends Vm> vmList) {
        // No optimization in this allocation policy
        return null;
    }
}
