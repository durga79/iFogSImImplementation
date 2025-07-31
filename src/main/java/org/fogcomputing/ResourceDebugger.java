package org.fogcomputing;

import java.util.List;
import org.cloudbus.cloudsim.Datacenter;
import org.cloudbus.cloudsim.Host;
import org.cloudbus.cloudsim.Vm;
import org.cloudbus.cloudsim.core.SimEntity;

/**
 * A utility class to help debug resource allocation issues in CloudSim simulations
 */
public class ResourceDebugger {
    
    /**
     * Checks if a VM can be allocated to a host based on resource requirements
     * @param host The host to check allocation against
     * @param vm The VM to check for allocation
     * @return A diagnostic string describing allocation status
     */
    public static String checkAllocation(Host host, Vm vm) {
        StringBuilder result = new StringBuilder();
        
        result.append("=== Resource Allocation Check ===\n");
        result.append("VM #").append(vm.getId()).append(" -> Host #").append(host.getId()).append("\n");
        
        // Check RAM
        long hostRam = host.getRam();
        long vmRam = vm.getRam();
        boolean ramOK = hostRam >= vmRam;
        result.append("RAM Check: ").append(vmRam).append("MB needed, ")
              .append(hostRam).append("MB available - ").append(ramOK ? "OK" : "FAILED").append("\n");
        
        // Check MIPS
        double hostMips = host.getTotalMips();
        double vmMips = vm.getMips() * vm.getNumberOfPes();
        boolean mipsOK = hostMips >= vmMips;
        result.append("MIPS Check: ").append(vmMips).append(" needed, ")
              .append(hostMips).append(" available - ").append(mipsOK ? "OK" : "FAILED").append("\n");
        
        // Check PEs (Processing Elements)
        int hostPes = host.getPeList().size();
        int vmPes = vm.getNumberOfPes();
        boolean peOK = hostPes >= vmPes;
        result.append("PE Count Check: ").append(vmPes).append(" needed, ")
              .append(hostPes).append(" available - ").append(peOK ? "OK" : "FAILED").append("\n");
        
        // Check BW (Bandwidth)
        long hostBw = host.getBw();
        long vmBw = vm.getBw();
        boolean bwOK = hostBw >= vmBw;
        result.append("Bandwidth Check: ").append(vmBw).append(" needed, ")
              .append(hostBw).append(" available - ").append(bwOK ? "OK" : "FAILED").append("\n");
        
        // Check Storage
        long hostStorage = host.getStorage();
        long vmStorage = vm.getSize();
        boolean storageOK = hostStorage >= vmStorage;
        result.append("Storage Check: ").append(vmStorage).append("MB needed, ")
              .append(hostStorage).append("MB available - ").append(storageOK ? "OK" : "FAILED").append("\n");
        
        // Overall result
        boolean canAllocate = ramOK && mipsOK && peOK && bwOK && storageOK;
        result.append("Final Result: ").append(canAllocate ? "VM CAN be allocated to this host" : "VM CANNOT be allocated to this host");
        
        return result.toString();
    }
    
    /**
     * Checks if a VM can be allocated to any host in a list
     * @param hosts The list of hosts to check
     * @param vm The VM to check for allocation
     * @return A diagnostic string with allocation check results for all hosts
     */
    public static String checkAllocationAcrossHosts(List<Host> hosts, Vm vm) {
        StringBuilder result = new StringBuilder();
        
        result.append("====== ALLOCATION CHECK FOR VM #").append(vm.getId()).append(" ======\n");
        result.append("VM Specs: ").append(vm.getMips()).append(" MIPS, ")
               .append(vm.getRam()).append("MB RAM, ")
               .append(vm.getNumberOfPes()).append(" PEs, ")
               .append(vm.getBw()).append(" Bandwidth, ")
               .append(vm.getSize()).append("MB Storage\n\n");
        
        boolean canAllocateToAny = false;
        
        for (Host host : hosts) {
            String allocCheck = checkAllocation(host, vm);
            result.append(allocCheck).append("\n\n");
            
            if (allocCheck.contains("VM CAN be allocated")) {
                canAllocateToAny = true;
            }
        }
        
        result.append("===== SUMMARY =====\n");
        result.append("VM #").append(vm.getId()).append(" can ");
        if (!canAllocateToAny) {
            result.append("NOT ");
        }
        result.append("be allocated to at least one host in the list.");
        
        return result.toString();
    }
    
    /**
     * Prints detailed information about hosts in a datacenter for debugging purposes
     * @param hostList The list of hosts to debug
     */
    public static void debugHostList(List<Host> hostList) {
        System.out.println("\n===== HOST CAPABILITIES DEBUG =====");
        for (Host host : hostList) {
            System.out.println("Host #" + host.getId() + ": " + host.getPeList().size() + " cores, " +
                           (host.getTotalMips() / host.getPeList().size()) + " MIPS per core, " +
                           host.getRam() + " MB RAM, " + 
                           host.getBw() + " bandwidth, " +
                           host.getStorage() + " MB storage");
        }
        System.out.println("===== END HOST CAPABILITIES DEBUG =====\n");
    }
    
    /**
     * Checks if a list of VMs can be allocated to a datacenter
     * @param vms The list of VMs to check
     * @param entity The datacenter entity
     */
    public static void checkVmHostCompatibility(List<Vm> vms, SimEntity entity) {
        if (!(entity instanceof Datacenter)) {
            System.out.println("Error: Entity is not a Datacenter");
            return;
        }
        
        Datacenter datacenter = (Datacenter) entity;
        List<Host> hostList = datacenter.getHostList();
        
        System.out.println("Datacenter #" + datacenter.getId() + " has " + hostList.size() + " hosts");
        
        for (Vm vm : vms) {
            System.out.println("\nChecking VM #" + vm.getId() + ": " + vm.getNumberOfPes() + " cores, " + 
                           vm.getMips() + " MIPS, " + vm.getRam() + " MB RAM");
            
            boolean canBeAllocated = false;
            Host compatibleHost = null;
            
            for (Host host : hostList) {
                boolean isCompatible = true;
                
                // Check RAM
                if (host.getRam() < vm.getRam()) {
                    System.out.println("  Host #" + host.getId() + " RAM insufficient: " + 
                                   host.getRam() + " MB available vs " + vm.getRam() + " MB needed");
                    isCompatible = false;
                }
                
                // Check PE count
                if (host.getPeList().size() < vm.getNumberOfPes()) {
                    System.out.println("  Host #" + host.getId() + " PE count insufficient: " + 
                                   host.getPeList().size() + " cores available vs " + vm.getNumberOfPes() + " needed");
                    isCompatible = false;
                }
                
                // Check MIPS
                double hostMips = host.getTotalMips();
                double vmMips = vm.getMips() * vm.getNumberOfPes();
                if (hostMips < vmMips) {
                    System.out.println("  Host #" + host.getId() + " MIPS insufficient: " + 
                                   hostMips + " available vs " + vmMips + " needed");
                    isCompatible = false;
                }
                
                if (isCompatible) {
                    canBeAllocated = true;
                    compatibleHost = host;
                    break;
                }
            }
            
            if (canBeAllocated) {
                System.out.println("  VM #" + vm.getId() + " CAN be allocated to Host #" + compatibleHost.getId());
            } else {
                System.out.println("  VM #" + vm.getId() + " CANNOT be allocated to any host in this datacenter!");
            }
        }
    }
}
