package org.fogcomputing;

import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.LinkedList;
import java.util.List;

import org.cloudbus.cloudsim.Cloudlet;
import org.cloudbus.cloudsim.CloudletSchedulerTimeShared;
import org.cloudbus.cloudsim.Datacenter;
import org.cloudbus.cloudsim.DatacenterBroker;
import org.cloudbus.cloudsim.DatacenterCharacteristics;
import org.cloudbus.cloudsim.Host;
import org.cloudbus.cloudsim.Log;
import org.cloudbus.cloudsim.NetworkTopology;
import org.cloudbus.cloudsim.Pe;
import org.cloudbus.cloudsim.Storage;
import org.cloudbus.cloudsim.Vm;
import org.cloudbus.cloudsim.VmAllocationPolicySimple;
import org.cloudbus.cloudsim.VmSchedulerTimeShared;
import org.cloudbus.cloudsim.core.CloudSim;
import org.cloudbus.cloudsim.provisioners.BwProvisionerSimple;
import org.cloudbus.cloudsim.provisioners.PeProvisionerSimple;
import org.cloudbus.cloudsim.provisioners.RamProvisionerSimple;
import org.cloudbus.cloudsim.util.WorkloadFileReader;
import org.fogcomputing.algorithms.*;

/**
 * Enhanced Policy-based simulation that combines the robust VM creation 
 * approach with policy-based task allocation.
 */
public class EnhancedPolicySimulation {
    /** The cloudlet list. */
    private static List<Cloudlet> cloudletList;

    /** The vm list. */
    private static List<Vm> vmList;

    /** Constants for VM counts and task distribution */
    private static final int NUM_CLOUD_VMS = 2;  // 20% of tasks (2 of 10)
    private static final int NUM_FOG_VMS = 5;    // 50% of tasks (5 of 10)
    private static final int NUM_IOT_VMS = 3;    // 30% of tasks (3 of 10)
    private static final int TOTAL_VMS = NUM_CLOUD_VMS + NUM_FOG_VMS + NUM_IOT_VMS;
    private static final int TOTAL_TASKS = 10;
    
    /** Network latencies */
    private static final double IOT_TO_FOG_LATENCY = 3.0;
    private static final double FOG_TO_CLOUD_LATENCY = 20.0;

    /**
     * Main method to run the simulation
     */
    public static void main(String[] args) {
        String policyName = "Deadline-Aware Offloading";
        if (args.length > 0 && args[0].equals("energy")) {
            policyName = "Energy-Aware Offloading";
        }
        runSimulation(policyName);
    }

    /**
     * Run simulation with the specified offloading policy.
     */
    public static void runSimulation(String policyName) {
        try {
            // Initialize CloudSim
            int num_user = 1;
            Calendar calendar = Calendar.getInstance();
            boolean trace_flag = false;
            CloudSim.init(num_user, calendar, trace_flag);

            // Create datacenters
            Datacenter cloudDatacenter = createDatacenter("CloudDatacenter", 
                                                          NUM_CLOUD_VMS, 8192, 20000, 16);
            Datacenter fogDatacenter = createDatacenter("FogDatacenter", 
                                                       NUM_FOG_VMS, 4096, 4000, 4);
            Datacenter iotDatacenter = createDatacenter("IoTDatacenter", 
                                                       NUM_IOT_VMS, 2048, 2000, 1);

            // Create broker
            DatacenterBroker broker = createBroker("MainBroker");
            int brokerId = broker.getId();

            // Create VMs for each tier
            createVMs(brokerId);

            // Select policy and create cloudlets
            OffloadingPolicy policy;
            if (policyName.equals("Energy-Aware Offloading")) {
                policy = new EnergyAwareOffloadingPolicy();
            } else {
                policy = new DeadlineAwareOffloadingPolicy();
            }
            
            // Create cloudlets and assign them to VMs using the policy
            createCloudlets(brokerId, policy);
            
            // Submit VMs and cloudlets to broker
            broker.submitVmList(vmList);
            broker.submitCloudletList(cloudletList);
            
            // Configure network topology
            configureNetworkTopology();
            
            // Start simulation
            CloudSim.startSimulation();
            
            // Stop simulation
            CloudSim.stopSimulation();
            
            // Print results
            List<Cloudlet> completedCloudlets = broker.getCloudletReceivedList();
            
            // Print results to console
            printCloudletList(completedCloudlets, policyName);
            
            // Process detailed results with SimulationResultProcessor
            SimulationResultProcessor.processResults(completedCloudlets, policyName);
            
            System.out.println("Simulation completed.");
            System.out.println("Check the 'results' directory for output files.");
            
        } catch (Exception e) {
            e.printStackTrace();
            Log.printLine("Simulation error: " + e.getMessage());
        }
    }
    
    /**
     * Creates a datacenter with the specified parameters.
     */
    private static Datacenter createDatacenter(String name, int numHosts, int ramPerHost, 
                                              int mipsPerCore, int coresPerHost) {
        List<Host> hostList = new ArrayList<Host>();
        
        for (int hostId = 0; hostId < numHosts; hostId++) {
            List<Pe> peList = new ArrayList<Pe>();
            
            // Create PEs/cores
            for (int i = 0; i < coresPerHost; i++) {
                peList.add(new Pe(i, new PeProvisionerSimple(mipsPerCore)));
            }
            
            // Host parameters
            int ram = ramPerHost;       // Host memory
            long storage = 1000000;     // Host storage
            int bw = 10000;            // Bandwidth
            
            // Create the host
            Host host = new Host(
                hostId,
                new RamProvisionerSimple(ram),
                new BwProvisionerSimple(bw),
                storage,
                peList,
                new VmSchedulerTimeShared(peList)
            );
            
            hostList.add(host);
        }
        
        // Datacenter characteristics
        String arch = "x86";
        String os = "Linux";
        String vmm = "Xen";
        double timeZone = 10.0;
        double costPerSec = name.contains("Cloud") ? 3.0 : 
                           (name.contains("Fog") ? 1.0 : 0.1);
        double costPerMem = name.contains("Cloud") ? 0.05 : 
                           (name.contains("Fog") ? 0.03 : 0.01);
        double costPerStorage = name.contains("Cloud") ? 0.001 : 
                               (name.contains("Fog") ? 0.0005 : 0.0001);
        double costPerBw = name.contains("Cloud") ? 0.1 : 
                          (name.contains("Fog") ? 0.05 : 0.01);
        
        LinkedList<Storage> storageList = new LinkedList<Storage>();
        
        DatacenterCharacteristics characteristics = new DatacenterCharacteristics(
            arch, os, vmm, hostList, timeZone, costPerSec, costPerMem, 
            costPerStorage, costPerBw
        );
        
        // Create Datacenter
        Datacenter datacenter = null;
        try {
            datacenter = new Datacenter(name, characteristics, 
                          new VmAllocationPolicySimple(hostList), storageList, 0);
        } catch (Exception e) {
            e.printStackTrace();
        }
        
        return datacenter;
    }
    
    /**
     * Creates a broker
     */
    private static DatacenterBroker createBroker(String name) {
        DatacenterBroker broker = null;
        try {
            broker = new DatacenterBroker(name);
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
        return broker;
    }
    
    /**
     * Configure network topology with appropriate latencies
     */
    private static void configureNetworkTopology() {
        // Initialize topology
        NetworkTopology.buildNetworkTopology("topology.brite");
        
        // Set latencies between datacenters
        // IoT to Fog
        NetworkTopology.addLink(3, 2, IOT_TO_FOG_LATENCY, 1000);
        // Fog to Cloud
        NetworkTopology.addLink(2, 1, FOG_TO_CLOUD_LATENCY, 10000);
    }
    
    /**
     * Creates VMs for each tier
     */
    private static void createVMs(int brokerId) {
        vmList = new ArrayList<Vm>();
        
        // Cloud VMs
        for (int i = 0; i < NUM_CLOUD_VMS; i++) {
            Vm vm = new Vm(
                i,                          // VM ID
                brokerId,                   // User ID
                10000,                      // MIPS
                8,                          // Number of CPUs
                8192,                       // RAM
                1000,                       // Bandwidth
                50000,                      // Size
                "Xen",                      // VMM
                new CloudletSchedulerTimeShared()
            );
            vmList.add(vm);
            System.out.println("Created Cloud VM #" + i);
        }
        
        // Fog VMs
        for (int i = NUM_CLOUD_VMS; i < NUM_CLOUD_VMS + NUM_FOG_VMS; i++) {
            Vm vm = new Vm(
                i,                          // VM ID
                brokerId,                   // User ID
                2000,                       // MIPS
                4,                          // Number of CPUs
                4096,                       // RAM
                500,                        // Bandwidth
                20000,                      // Size
                "Xen",                      // VMM
                new CloudletSchedulerTimeShared()
            );
            vmList.add(vm);
            System.out.println("Created Fog VM #" + i);
        }
        
        // IoT VMs
        for (int i = NUM_CLOUD_VMS + NUM_FOG_VMS; i < TOTAL_VMS; i++) {
            Vm vm = new Vm(
                i,                          // VM ID
                brokerId,                   // User ID
                1000,                       // MIPS
                1,                          // Number of CPUs
                1024,                       // RAM
                100,                        // Bandwidth
                5000,                       // Size
                "Embedded",                 // VMM
                new CloudletSchedulerTimeShared()
            );
            vmList.add(vm);
            System.out.println("Created IoT VM #" + i);
        }
    }
    
    /**
     * Creates cloudlets and assigns them to VMs using the specified policy
     */
    private static void createCloudlets(int brokerId, OffloadingPolicy policy) {
        cloudletList = new ArrayList<Cloudlet>();
        
        // Initialize the task/cloudlet parameters
        long length = 10000;      // Task length
        long fileSize = 300;      // Input file size
        long outputSize = 300;    // Output size
        int pesNumber = 1;        // Number of CPUs needed
        
        // Create cloudlets
        for (int i = 0; i < TOTAL_TASKS; i++) {
            // Vary task length slightly
            long taskLength = length + (i * 500);
            
            // Create cloudlet with utilization model
            Cloudlet cloudlet = new Cloudlet(
                i,                              // ID
                taskLength,                     // Length
                pesNumber,                      // Number of CPUs needed
                fileSize,                       // Input size
                outputSize,                     // Output size
                new org.cloudbus.cloudsim.UtilizationModelFull(),   // CPU utilization
                new org.cloudbus.cloudsim.UtilizationModelFull(),   // RAM utilization
                new org.cloudbus.cloudsim.UtilizationModelFull()    // BW utilization
            );
            
            cloudlet.setUserId(brokerId);
            
            // Get target VM ID using the policy
            int vmId = policy.getTargetVmId(cloudlet, vmList);
            cloudlet.setVmId(vmId);
            
            // Add to list
            cloudletList.add(cloudlet);
            System.out.println("Created Task " + i + " assigned to VM #" + vmId);
        }
    }
    
    /**
     * Prints the Cloudlet objects
     */
    private static void printCloudletList(List<Cloudlet> list, String policyName) {
        int size = list.size();
        Cloudlet cloudlet;

        String indent = "    ";
        Log.printLine();
        Log.printLine("----- Simulation Results -----");
        Log.printLine("Offloading Algorithm: " + policyName);
        Log.printLine(indent + "Total Tasks Generated: " + TOTAL_TASKS);
        Log.printLine(indent + "Total Tasks Completed: " + size);
        
        // Success rate
        double successRate = (double)size / TOTAL_TASKS * 100;
        Log.printLine(indent + "Success Rate: " + new DecimalFormat("0.00").format(successRate) + "%");
        
        // Count tasks by tier
        int cloudTasks = 0;
        int fogTasks = 0;
        int iotTasks = 0;
        
        // Print task details
        Log.printLine();
        Log.printLine(indent + "========== Tasks ==========");
        
        for (int i = 0; i < size; i++) {
            cloudlet = list.get(i);
            int vmId = cloudlet.getVmId();
            
            String tier = "Unknown";
            if (vmId < NUM_CLOUD_VMS) {
                tier = "Cloud";
                cloudTasks++;
            } else if (vmId < NUM_CLOUD_VMS + NUM_FOG_VMS) {
                tier = "Fog";
                fogTasks++;
            } else {
                tier = "IoT";
                iotTasks++;
            }
            
            double execTime = cloudlet.getActualCPUTime();
            double transmission = tier.equals("Cloud") ? 20.0 : (tier.equals("Fog") ? 3.0 : 0.0);
            double energy = execTime * 0.1; // Simple energy model
            
            Log.printLine(indent + "    Task ID: " + cloudlet.getCloudletId() + 
                         ", Status: SUCCESS, VM: " + tier + "-" + vmId + 
                         ", Time: " + new DecimalFormat("0.00").format(execTime) + 
                         " ms, Transmission: " + transmission + 
                         " ms, Energy: " + new DecimalFormat("0.00").format(energy) + " J");
        }
        
        // Print summary
        Log.printLine();
        Log.printLine(indent + "========== Summary ==========");
        
        // Only print task distribution if we have completed tasks
        if (size > 0) {
            Log.printLine(indent + "Task Distribution by Device Type:");
            Log.printLine(indent + "Cloud: " + cloudTasks + " tasks");
            Log.printLine(indent + "Fog: " + fogTasks + " tasks");
            Log.printLine(indent + "IoT: " + iotTasks + " tasks");
        }
    }
}
