package org.fogcomputing;

import org.cloudbus.cloudsim.DatacenterBroker;
import org.cloudbus.cloudsim.Vm;
import org.cloudbus.cloudsim.core.CloudSim;
import org.cloudbus.cloudsim.core.CloudSimTags;
import org.cloudbus.cloudsim.core.SimEvent;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * A specialized DatacenterBroker that handles VM allocation to specific datacenter tiers.
 * This ensures that Cloud VMs are created in Cloud datacenters, Fog VMs in Fog datacenters,
 * and IoT VMs in IoT datacenters.
 */
public class TieredDatacenterBroker extends DatacenterBroker {

    // Maps VM IDs to specific datacenter IDs
    private Map<Integer, Integer> vmToDatacenterMap;
    
    /**
     * Creates a new TieredDatacenterBroker
     * @param name Name of the broker
     * @throws Exception If the broker cannot be created
     */
    public TieredDatacenterBroker(String name) throws Exception {
        super(name);
        vmToDatacenterMap = new HashMap<>();
    }
    
    /**
     * Associates a VM with a specific datacenter
     * @param vmId The VM ID
     * @param datacenterId The datacenter ID where this VM should be created
     */
    public void mapVmToDatacenter(int vmId, int datacenterId) {
        vmToDatacenterMap.put(vmId, datacenterId);
        System.out.println("DEBUG TieredDatacenterBroker: Explicitly mapped VM #" + vmId + " to datacenter #" + datacenterId);
    }
    
    /**
     * Maps a list of VMs to a specific datacenter
     * @param startVmId The starting VM ID in the range
     * @param endVmId The ending VM ID in the range (inclusive)
     * @param datacenterId The datacenter ID to map these VMs to
     */
    public void mapVmRangeToDatacenter(int startVmId, int endVmId, int datacenterId) {
        for (int i = startVmId; i <= endVmId; i++) {
            mapVmToDatacenter(i, datacenterId);
        }
    }

    /**
     * Process a request for VM creation
     * Override to direct VM creation requests to specific datacenters
     */
    @Override
    protected void processVmCreate(SimEvent ev) {
        int[] data = (int[]) ev.getData();
        int datacenterId = data[0];
        int vmId = data[1];
        
        System.out.println("DEBUG TieredDatacenterBroker: Processing VM creation for VM #" + vmId + " on datacenter #" + datacenterId);
        
        // Check if this VM has a specific datacenter mapping
        if (vmToDatacenterMap.containsKey(vmId)) {
            int targetDatacenterId = vmToDatacenterMap.get(vmId);
            System.out.println("Redirecting VM #" + vmId + " from datacenter #" + datacenterId + " to datacenter #" + targetDatacenterId);
            datacenterId = targetDatacenterId;
            
            // Replace the datacenter ID in the data array
            data[0] = targetDatacenterId;
            
            // Validate VM-to-datacenter mapping but don't restrict by ID ranges anymore
            // Just log what type of VM we think this is based on ID
            if (vmId >= 0 && vmId <= 1) {
                System.out.println("VM #" + vmId + " is a Cloud VM and is mapped to datacenter #" + targetDatacenterId);
            } else if (vmId >= 2 && vmId <= 6) {
                System.out.println("VM #" + vmId + " is a Fog VM and is mapped to datacenter #" + targetDatacenterId);
            } else if (vmId >= 7 && vmId <= 16) {
                System.out.println("VM #" + vmId + " is an IoT VM and is mapped to datacenter #" + targetDatacenterId);
            } else {
                System.out.println("WARNING: VM #" + vmId + " has an unknown type but is mapped to datacenter #" + targetDatacenterId);
            }
        } else {
            // If no mapping exists, use a fallback strategy based on VM resource requirements
            // First try the default datacenter, if that fails we'll handle it elsewhere
            System.out.println("INFO: VM #" + vmId + " has no datacenter mapping, using datacenter #" + datacenterId);
        }
        super.processVmCreate(ev);
    }
}
