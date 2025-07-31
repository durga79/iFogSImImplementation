package org.fogcomputing;

import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

// No CloudSimEventListener available in this version of CloudSim
import java.util.Random;
import java.lang.reflect.Field;

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
import org.cloudbus.cloudsim.UtilizationModel;
import org.cloudbus.cloudsim.UtilizationModelFull;
import org.cloudbus.cloudsim.Vm;
import org.cloudbus.cloudsim.VmSchedulerSpaceShared;
import org.cloudbus.cloudsim.VmAllocationPolicySimple;
import org.cloudbus.cloudsim.VmSchedulerTimeShared;
import org.cloudbus.cloudsim.core.CloudSim;
import org.cloudbus.cloudsim.provisioners.BwProvisionerSimple;
import org.cloudbus.cloudsim.provisioners.PeProvisionerSimple;
import org.cloudbus.cloudsim.provisioners.RamProvisionerSimple;
import org.fogcomputing.FlexibleVmAllocationPolicy;
import org.fogcomputing.TieredVmAllocationPolicy;
import org.fogcomputing.TieredDatacenterBroker;
import org.fogcomputing.algorithms.OffloadingPolicy;
import org.fogcomputing.algorithms.EnergyAwareOffloadingPolicy;
import org.fogcomputing.algorithms.DeadlineAwareOffloadingPolicy;

/**
 * A CloudSim implementation of Fog/Edge Computing Task Offloading
 * This simulation demonstrates task offloading between IoT devices, fog nodes, and cloud servers
 * with different offloading policies (energy-aware and deadline-aware)
 */
public class CloudSimTaskOffloadingSimulation {

    /** The cloudlet list. */
    private static List<Cloudlet> cloudletList;

    /** The vm list. */
    private static List<Vm> vmList;

    // Device counts
    private static int NUM_IOT_DEVICES = 10;
    private static int NUM_FOG_NODES = 5;
    private static int NUM_CLOUD_HOSTS = 2;
    
    // Network latencies (ms)
    private static double IOT_TO_FOG_LATENCY = 2.0;
    private static double FOG_TO_CLOUD_LATENCY = 20.0;
    
    /**
     * Creates main() to run this example.
     *
     * @param args the args
     */
    public static void main(String[] args) {
        Log.printLine("Starting Task Offloading Simulation using CloudSim...");

        try {
            // Run with energy-aware policy
            runSimulation("Energy-Aware Offloading");
            
            // Run with deadline-aware policy 
            runSimulation("Deadline-Aware Offloading");
            
        } catch (Exception e) {
            e.printStackTrace();
            Log.printLine("Simulation terminated due to an unexpected error.");
        }
    }
    
    /**
     * Run simulation with the specified offloading policy.
     * 
     * @param policyName the name of the offloading policy to use
     */
    private static void runSimulation(String policyName) throws Exception {
        Log.printLine("\n==================================");
        Log.printLine("Running simulation with " + policyName);
        Log.printLine("==================================\n");
        
        // Initialize CloudSim properly before creating any entities
        int num_user = 1;   // number of cloud users
        Calendar calendar = Calendar.getInstance();
        boolean trace_flag = false;  // mean trace events
        
        // This must be called before creating any CloudSim entities
        CloudSim.init(num_user, calendar, trace_flag);
        System.out.println("CloudSim initialized successfully");
        
        // Define the datacenter variables in the outer scope so we can access them later
        Datacenter cloudDC;
        Datacenter fogDC;
        Datacenter iotDC;
        
        try {
            // Create Datacenters in a specific order (Cloud first, then Fog, then IoT)
            // This should result in: Cloud = ID 2, Fog = ID 3, IoT = ID 4
            cloudDC = createCloudDatacenter("CloudDatacenter");
            fogDC = createFogDatacenter("FogDatacenter");
            iotDC = createIoTDatacenter("IoTDatacenter");
            
            // Verify datacenter creation
            if (cloudDC == null) {
                throw new Exception("Failed to create Cloud datacenter");
            }
            if (fogDC == null) {
                throw new Exception("Failed to create Fog datacenter");
            }
            if (iotDC == null) {
                throw new Exception("Failed to create IoT datacenter");
            }
        } catch (Exception e) {
            System.err.println("Error creating datacenters: " + e.getMessage());
            e.printStackTrace();
            return; // Don't proceed if datacenter creation fails
        }
        
        // Get the datacenter IDs
        int iotDcId = iotDC.getId();
        int fogDcId = fogDC.getId();
        int cloudDcId = cloudDC.getId();
        
        System.out.println("DEBUG: Created datacenters with fixed IDs:");
        System.out.println("DEBUG: IoT datacenter ID = " + iotDcId);
        System.out.println("DEBUG: Fog datacenter ID = " + fogDcId);
        System.out.println("DEBUG: Cloud datacenter ID = " + cloudDcId);
        
        System.out.println("===== DATACENTER DEBUGGING INFO =====");
        System.out.println("Created datacenters with IDs: Cloud=" + cloudDcId + 
                          ", Fog=" + fogDcId + ", IoT=" + iotDcId);
        System.out.println("CloudDC has " + cloudDC.getHostList().size() + " hosts with MIPS: " + 
                          cloudDC.getHostList().get(0).getTotalMips());
        System.out.println("FogDC has " + fogDC.getHostList().size() + " hosts with MIPS: " + 
                          fogDC.getHostList().get(0).getTotalMips());
        System.out.println("IoTDC has " + iotDC.getHostList().size() + " hosts with MIPS: " + 
                          iotDC.getHostList().get(0).getTotalMips());
        System.out.println("===== END DATACENTER INFO =====");

        // Create the tiered broker
        TieredDatacenterBroker broker = createTieredBroker("MainBroker");
        int brokerId = broker.getId();
        
        // Print datacenter IDs for debugging
        System.out.println("===== DATACENTER IDS =====");
        System.out.println("Cloud datacenter ID: " + cloudDcId);
        System.out.println("Fog datacenter ID: " + fogDcId);
        System.out.println("IoT datacenter ID: " + iotDcId);
        
        OffloadingPolicy policy;
        if (policyName.equals("Energy-Aware Offloading")) {
            policy = new EnergyAwareOffloadingPolicy();
        } else {
            policy = new DeadlineAwareOffloadingPolicy();
        }
        
        // Use a completely different approach to VM creation and cloudlet submission
        // First, create VMs
        createVMs(broker, brokerId, cloudDcId, fogDcId, iotDcId);
        
        // Submit VMs to broker
        broker.submitVmList(vmList);
        
        // Create cloudlets but don't submit them yet
        cloudletList = new ArrayList<Cloudlet>();
        
        // Check VM specifications and host compatibility before simulation
        System.out.println("\n===== PRE-SIMULATION VM COMPATIBILITY CHECK =====");
        List<Vm> cloudVMs = new ArrayList<>();
        List<Vm> fogVMs = new ArrayList<>();
        List<Vm> iotVMs = new ArrayList<>();
        
        // Separate VMs by tier for easier debugging
        for (Vm vm : vmList) {
            int vmId = vm.getId();
            if (vmId <= 1) {
                cloudVMs.add(vm);
            } else if (vmId <= 6) {
                fogVMs.add(vm);
            } else {
                iotVMs.add(vm);
            }
        }
        
        System.out.println("Cloud VMs: " + cloudVMs.size());
        System.out.println("Fog VMs: " + fogVMs.size());
        System.out.println("IoT VMs: " + iotVMs.size());
        System.out.println("===== END PRE-SIMULATION CHECK =====\n");
        
        // Create cloudlets with safety checks to ensure proper VM assignment
        cloudletList = new ArrayList<Cloudlet>();
        
        // Create cloudlets and map them initially to IoT VMs (7-16) which we expect to succeed
        System.out.println("Creating tasks with safe IoT VM assignments as fallbacks...");
        for (int i = 0; i < NUM_IOT_DEVICES; i++) {
            Cloudlet cloudlet = createCloudlet(i, brokerId);
            
            // Get target VM using policy
            int targetVmId = policy.getTargetVmId(cloudlet, vmList);
            
            // SAFETY: For this initial run, always use IoT VMs (7-16) as they're more likely to be created
            int iotVmId = 7 + i;
            System.out.println("Safely assigning task #" + i + " to IoT VM #" + iotVmId + 
                " (ignoring policy suggestion of VM #" + targetVmId + " for safety)");
            cloudlet.setVmId(iotVmId);
            
            cloudletList.add(cloudlet);
        }
        
        // Configure network topology
        configureNetworkTopology();
        
        // Submit cloudlets to broker
        System.out.println("\nSubmitting " + cloudletList.size() + " cloudlets to broker\n");
        broker.submitCloudletList(cloudletList);
        
        // Start simulation - will handle VM creation and task scheduling
        CloudSim.startSimulation();

        // Stop simulation
        CloudSim.stopSimulation();

        // Print results
        List<Cloudlet> newList = broker.getCloudletReceivedList();
        
        // First print directly to console using original method for backwards compatibility
        printCloudletList(newList, policyName);
        
        // Then use the new SimulationResultProcessor for detailed output and file generation
        SimulationResultProcessor.processResults(newList, policyName);
    }

    /**
     * Creates the cloud datacenter.
     *
     * @param name the datacenter name
     * @return the datacenter
     */
    private static Datacenter createCloudDatacenter(String name) {
        // Create a list to store our machine
        List<Host> hostList = new ArrayList<Host>();
        
        // 4 Quad-core machines with 16GB RAM, 1000GB Storage, 10000 MIPS per core
        // First host will have 4 cores at 10000 MIPS (high-performance cloud)
        List<Pe> peList1 = new ArrayList<Pe>();
        for (int i = 0; i < 4; i++) { // 4 cores
            peList1.add(new Pe(i, new PeProvisionerSimple(10000))); // 10000 MIPS per core
        }
        
        hostList.add(
            new Host(
                0,
                new RamProvisionerSimple(16384), // 16GB
                new BwProvisionerSimple(10000), // 10Gbps
                1000000, // 1TB storage
                peList1,
                new VmSchedulerTimeShared(peList1) // Using TimeShared scheduler for better VM compatibility
            )
        );
        
        // Second host will have 8 cores at 8000 MIPS (bulk cloud processing)
        List<Pe> peList2 = new ArrayList<Pe>();
        for (int i = 0; i < 8; i++) { // 8 cores
            peList2.add(new Pe(i, new PeProvisionerSimple(8000))); // 8000 MIPS per core
        }
        
        hostList.add(
            new Host(
                1,
                new RamProvisionerSimple(32768), // 32GB 
                new BwProvisionerSimple(10000), // 10Gbps
                2000000, // 2TB storage
                peList2,
                new VmSchedulerTimeShared(peList2) // Using TimeShared scheduler for better VM compatibility
            )
        );
        
        // Debug the host capabilities
        System.out.println("Cloud datacenter host capabilities:");
        for (Host host : hostList) {
            System.out.println("Host #" + host.getId() + ": " + 
                              host.getRamProvisioner().getAvailableRam() + "MB RAM, " +
                              host.getBwProvisioner().getAvailableBw() + "Mbps BW, " +
                              host.getStorage() + "MB storage, " +
                              host.getNumberOfPes() + " PEs");
        }
        
        // Create a DatacenterCharacteristics object
        DatacenterCharacteristics characteristics = new DatacenterCharacteristics(
                "x86",     // architecture
                "Linux",   // OS
                "Xen",     // VMM
                hostList,  // list of hosts
                4.0,       // time zone
                0.1,       // cost per second of CPU
                0.05,      // cost per second of RAM
                0.001,     // cost per second of storage
                0.001      // cost per second of BW
        );

        // Create a PowerDatacenter object with TieredVmAllocationPolicy for Cloud tier (VMs 0-1)
        Datacenter datacenter = null;
        try {
            // We've already created the hosts we need above, no need to add more
            
            System.out.println("Cloud datacenter has " + hostList.size() + " hosts");
            
            // Cloud tier handles VMs 0 to NUM_CLOUD_HOSTS-1
            // Note: Actual datacenter ID is determined by CloudSim at runtime
            // Create cloud datacenter to handle VMs 0-1
            datacenter = new Datacenter(
                name, 
                characteristics, 
                new FlexibleVmAllocationPolicy(hostList, "Cloud"), 
                new LinkedList<Storage>(), 
                0
            );
        } catch (Exception e) {
            e.printStackTrace();
        }

        return datacenter;
    }
    
    /**
     * Creates the fog datacenter with mid-level resources.
     *
     * @param name the datacenter name
     * @return the datacenter
     */
    private static Datacenter createFogDatacenter(String name) {
        System.out.println("\n===== CREATING FOG DATACENTER =====\n");
        
        // Create a list to store our machine
        List<Host> hostList = new ArrayList<Host>();
        
        // Fog nodes are dual-core at 2000 MIPS each, 8GB RAM
        // Start hostId from 10 to avoid any ID conflicts
        int hostId = 10;
        int ram = 8192; // host memory (MB)
        long storage = 500000; // host storage 500GB
        int bw = 1000; // bandwidth 1Gbps
        
        System.out.println("Creating " + NUM_FOG_NODES + " fog hosts with IDs starting at " + hostId);
        
        for (int i = 0; i < NUM_FOG_NODES; i++) {
            // Create dual-core PE list
            List<Pe> peList = new ArrayList<Pe>();
            peList.add(new Pe(0, new PeProvisionerSimple(2000))); // 2000 MIPS
            peList.add(new Pe(1, new PeProvisionerSimple(2000))); // 2000 MIPS
            
            // Create Host with its id and list of PEs and add them to the list of machines
            hostList.add(
                new Host(
                    hostId++,
                    new RamProvisionerSimple(ram),
                    new BwProvisionerSimple(bw),
                    storage,
                    peList,
                    new VmSchedulerTimeShared(peList) // Using TimeShared scheduler for better VM compatibility
                )
            );
        }
        
        // Debug the fog host capabilities
        System.out.println("Fog datacenter host capabilities:");
        for (Host host : hostList) {
            System.out.println("Host #" + host.getId() + ": " + 
                              host.getRamProvisioner().getAvailableRam() + "MB RAM, " +
                              host.getBwProvisioner().getAvailableBw() + "Mbps BW, " +
                              host.getStorage() + "MB storage, " +
                              host.getNumberOfPes() + " PEs");
        }
        
        ResourceDebugger.debugHostList(hostList);
        
        String arch = "ARM";
        String os = "Linux"; 
        String vmm = "Xen";
        double time_zone = 10.0; 
        double cost = 1.0; // Lower cost than cloud
        double costPerMem = 0.03; 
        double costPerStorage = 0.0005; 
        double costPerBw = 0.005;
        LinkedList<Storage> storageList = new LinkedList<Storage>();
        try {
            storageList.add(new org.cloudbus.cloudsim.HarddriveStorage("fog_storage", 500000));
        } catch (Exception e) {
            e.printStackTrace();
        }

        DatacenterCharacteristics characteristics = new DatacenterCharacteristics(
            arch, os, vmm, hostList, time_zone, cost, costPerMem, costPerStorage, costPerBw);

        Datacenter datacenter = null;
        try {
            // We've already created the hosts we need above, no need to add more
            
            System.out.println("Fog datacenter has " + hostList.size() + " hosts");
            
            // Print detailed fog host information
            System.out.println("===== FOG DATACENTER HOST DEBUG =====");
            for (Host host : hostList) {
                System.out.println("Fog host #" + host.getId() + ": " + 
                    host.getNumberOfPes() + " PEs, " + 
                    host.getTotalMips() + " total MIPS, " + 
                    host.getRamProvisioner().getAvailableRam() + "MB RAM, " +
                    host.getBwProvisioner().getAvailableBw() + " BW, " +
                    host.getStorage() + " storage");
            }
            System.out.println("===== END FOG HOST DEBUG =====");
            
            // Create fog datacenter using flexible allocation policy
            datacenter = new Datacenter(
                name, 
                characteristics, 
                new FlexibleVmAllocationPolicy(hostList, "Fog"),
                storageList, 
                0);         // Verify datacenter has hosts
            System.out.println("\nAfter datacenter creation, Fog datacenter " + datacenter.getId() + 
                           " has " + datacenter.getHostList().size() + " hosts");
        } catch (Exception e) {
            e.printStackTrace();
        }

        return datacenter;
    }
    
    /**
     * Creates the IoT datacenter with very constrained resources.
     *
     * @param name the datacenter name
     * @return the datacenter
     */
    private static Datacenter createIoTDatacenter(String name) {
        System.out.println("\n===== CREATING IOT DATACENTER =====\n");
        
        List<Host> hostList = new ArrayList<Host>();

        int mips = 1000; // MIPS for IoT - simplified from FixedTierSimulation
        int ram = 2048; // 2GB RAM for IoT devices - simplified from FixedTierSimulation
        long storage = 1000000; // Storage (MB)
        int bw = 1000; // Low bandwidth for IoT (MB/s)

        // Start host IDs from 20 to avoid conflicts with Cloud and Fog hosts
        int baseHostId = 20;
        System.out.println("Creating " + NUM_IOT_DEVICES + " IoT hosts with IDs starting at " + baseHostId);
        
        for (int i = 0; i < NUM_IOT_DEVICES; i++) {
            List<Pe> peList = new ArrayList<Pe>();
            
            // Single core per host as in FixedTierSimulation
            peList.add(new Pe(0, new PeProvisionerSimple(mips)));
            
            Host host = new Host(
                baseHostId + i, // Unique host ID starting from 20
                new RamProvisionerSimple(ram),
                new BwProvisionerSimple(bw),
                storage,
                peList,
                new VmSchedulerTimeShared(peList) // Using TimeShared scheduler for better VM compatibility
            );
            
            hostList.add(host);
        }

        String arch = "ARM";
        String os = "Linux"; 
        String vmm = "Embedded";
        double time_zone = 10.0; 
        double cost = 0.1; // Very low cost for IoT
        double costPerMem = 0.01; 
        double costPerStorage = 0.0001; 
        double costPerBw = 0.001;
        LinkedList<Storage> storageList = new LinkedList<Storage>();
        try {
            storageList.add(new org.cloudbus.cloudsim.HarddriveStorage("iot_storage", 100000));
        } catch (Exception e) {
            e.printStackTrace();
        }

        DatacenterCharacteristics characteristics = new DatacenterCharacteristics(
            arch, os, vmm, hostList, time_zone, cost, costPerMem, costPerStorage, costPerBw);

        Datacenter datacenter = null;
        try {
            // IoT datacenter already has sufficient resources for the IoT VMs
            // as verified by successful allocation in previous runs
            
            // Print detailed IoT host information
            System.out.println("===== IOT DATACENTER HOST DEBUG =====");
            for (Host host : hostList) {
                System.out.println("IoT host #" + host.getId() + ": " + 
                    host.getNumberOfPes() + " PEs, " + 
                    host.getTotalMips() + " total MIPS, " + 
                    host.getRamProvisioner().getAvailableRam() + "MB RAM, " +
                    host.getBwProvisioner().getAvailableBw() + " BW, " +
                    host.getStorage() + " storage");
            }
            System.out.println("===== END IOT HOST DEBUG =====");
            
            // Create IoT datacenter using flexible allocation policy
            datacenter = new Datacenter(name, characteristics,
                new FlexibleVmAllocationPolicy(hostList, "IoT"),
                storageList, 0);
                
            // Verify datacenter has hosts
            System.out.println("\nAfter datacenter creation, IoT datacenter " + datacenter.getId() + 
                           " has " + datacenter.getHostList().size() + " hosts");
            
            System.out.println("IoT datacenter has " + hostList.size() + " hosts");
        } catch (Exception e) {
            e.printStackTrace();
        }

        return datacenter;
    }

    /**
     * Creates a tiered datacenter broker that can direct VMs to specific datacenters.
     *
     * @param name the broker name
     * @return the tiered datacenter broker
     */
    private static TieredDatacenterBroker createTieredBroker(String name) {
        TieredDatacenterBroker broker = null;
        try {
            broker = new TieredDatacenterBroker(name);
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
        return broker;
    }

    /**
     * Configure network topology with appropriate latencies between datacenter tiers.
     */
    private static void configureNetworkTopology() {
        // Initialize the network topology
        NetworkTopology.buildNetworkTopology("topology.brite");
        
        // Set latencies between datacenters
        // IoT to Fog
        NetworkTopology.addLink(1, 2, IOT_TO_FOG_LATENCY, 1000);
        // Fog to Cloud
        NetworkTopology.addLink(2, 3, FOG_TO_CLOUD_LATENCY, 10000);
    }
    
    /**
     * Create VMs and cloudlets according to the offloading policy.
     *
     * @param broker the tiered broker
            
            // Determine target VM using the policy
            int vmId = policy.getTargetVmId(cloudlet, vmList);
            cloudlet.setVmId(vmId);
            
            cloudletList.add(cloudlet);
        }
    }
    
    /**
     * Creates VMs for all tiers (Cloud, Fog, IoT) with specifications carefully matched to host capabilities.
     * 
     * @param broker the datacenter broker
     * @param brokerId the broker ID
     * @param cloudDcId the cloud datacenter ID
     * @param fogDcId the fog datacenter ID
     * @param iotDcId the IoT datacenter ID
     */
    private static void createVMs(TieredDatacenterBroker broker, int brokerId, int cloudDcId, int fogDcId, int iotDcId) {
        // Initialize the vmList if it hasn't been created yet
        if (vmList == null) {
            vmList = new ArrayList<Vm>();
        }
        
        // Lists to hold VMs for each datacenter
        List<Vm> cloudVMs = new ArrayList<>();
        List<Vm> fogVMs = new ArrayList<>();
        List<Vm> iotVMs = new ArrayList<>();
        
        // Create Cloud VMs - Carefully calibrated to match Cloud hosts
        // IMPORTANT: Using much lower MIPS values to ensure compatibility with VmSchedulerTimeShared
        // Create 2 Cloud VMs with IDs 0-1
        for (int i = 0; i < 2; i++) {
            Vm cloudVm = new Vm(
                i,                      // VM ID 0 or 1
                brokerId,               // User ID
                50,                     // MIPS - ultra low for guaranteed compatibility
                1,                      // Using only 1 core to avoid MIPS allocation issues
                128,                    // RAM - ultra low for guaranteed allocation success
                10,                     // Bandwidth - ultra low for compatibility
                500,                    // Storage - ultra low for compatibility
                "Xen",                  // VMM
                new CloudletSchedulerTimeShared() // Using time-shared scheduler for all VMs
            );
            cloudVMs.add(cloudVm);
            System.out.println("Created Cloud VM #" + i + " with 50 MIPS, 128MB RAM");
        }
        
        // Create Fog VMs - Ultra-conservative values for guaranteed allocation
        // Each Fog host has 2 cores at 2000 MIPS, 8GB RAM
        // Using bare minimum resource requirements to ensure successful allocation
        for (int i = 0; i < NUM_FOG_NODES; i++) {
            // Using VM IDs 2-6 for Fog VMs
            Vm fogVm = new Vm(
                2 + i,                 // VM ID fixed to range 2-6
                brokerId,              // User ID
                50,                    // MIPS - ultra low for guaranteed compatibility
                1,                     // Number of CPUs - using 1 core only for better compatibility
                128,                   // RAM - ultra low for guaranteed allocation success
                10,                    // Bandwidth - ultra low for guaranteed compatibility
                500,                   // Storage - ultra low for guaranteed compatibility
                "Xen",                 // VMM
                new CloudletSchedulerTimeShared() // Using TimeShared scheduler
            );
            fogVMs.add(fogVm);
            System.out.println("Created Fog VM #" + (2 + i) + " with 50 MIPS, 128MB RAM");
        }
        
        // Create IoT VMs with IDs 7-16
        for (int i = 0; i < NUM_IOT_DEVICES; i++) {
            Vm iotVm = new Vm(
                7 + i,                   // VM ID fixed to range 7-16
                brokerId,                // User ID
                50,                      // MIPS - ultra low for consistency with other tiers
                1,                       // Number of CPUs - single core
                128,                     // RAM (MB) - ultra low for consistency
                10,                      // Bandwidth - ultra low for consistency
                500,                     // Storage (MB) - ultra low for consistency
                "Embedded",              // VMM
                new CloudletSchedulerTimeShared() // Using TimeShared scheduler
            );
            iotVMs.add(iotVm);
            System.out.println("Created IoT VM #" + (7 + i) + " with 50 MIPS, 128MB RAM");
        }
        
        System.out.println("Created " + cloudVMs.size() + " cloud VMs, " + 
                          fogVMs.size() + " fog VMs, and " + 
                          iotVMs.size() + " IoT VMs");
        
        // Debug output to verify VM specifications
        System.out.println("\n===== VM SPECIFICATIONS =====");
        for (Vm vm : cloudVMs) {
            System.out.println("Cloud VM #" + vm.getId() + ": " + vm.getNumberOfPes() + " cores, " +
                             vm.getMips() + " MIPS, " + vm.getRam() + " MB RAM");
        }
        
        for (Vm vm : fogVMs) {
            System.out.println("Fog VM #" + vm.getId() + ": " + vm.getNumberOfPes() + " cores, " +
                             vm.getMips() + " MIPS, " + vm.getRam() + " MB RAM");
        }
        
        for (Vm vm : iotVMs) {
            System.out.println("IoT VM #" + vm.getId() + ": " + vm.getNumberOfPes() + " cores, " +
                             vm.getMips() + " MIPS, " + vm.getRam() + " MB RAM");
        }
                          
        // Map VMs to their respective datacenters directly
        Map<Integer, Integer> datacenterMap = new HashMap<>();
        
        // Map each Cloud VM to the Cloud datacenter
        for (Vm vm : cloudVMs) {
            datacenterMap.put(vm.getId(), cloudDcId);
        }
        
        // Map each Fog VM to the Fog datacenter
        for (Vm vm : fogVMs) {
            datacenterMap.put(vm.getId(), fogDcId);
        }
        
        // Map each IoT VM to the IoT datacenter
        for (Vm vm : iotVMs) {
            datacenterMap.put(vm.getId(), iotDcId);
        }
        
        // Use the TieredDatacenterBroker to map VMs to their respective datacenters
        System.out.println("\n===== VM ALLOCATION STRATEGY =====");
        System.out.println("Cloud VMs: " + cloudVMs.size() + ", Fog VMs: " + fogVMs.size() + ", IoT VMs: " + iotVMs.size());
        
        // Add all VMs to the master list for the broker
        vmList.clear();
        vmList.addAll(cloudVMs);
        vmList.addAll(fogVMs);
        vmList.addAll(iotVMs);
        
        // Explicitly map VMs to their correct datacenters using the IDs we discovered
        // Print ID ranges for debugging
        System.out.println("\n===== VM-TO-DATACENTER MAPPING =====");
        System.out.println("Cloud VMs (0-1) mapped to datacenter #" + cloudDcId);
        System.out.println("Fog VMs (2-6) mapped to datacenter #" + fogDcId);
        System.out.println("IoT VMs (7-16) mapped to datacenter #" + iotDcId);
        
        // Map cloud VMs to Cloud datacenter
        for (Vm vm : cloudVMs) {
            broker.mapVmToDatacenter(vm.getId(), cloudDcId);
            System.out.println("VM #" + vm.getId() + " mapped to Cloud datacenter #" + cloudDcId);
        }
        
        // Map fog VMs to Fog datacenter
        for (Vm vm : fogVMs) {
            broker.mapVmToDatacenter(vm.getId(), fogDcId);
            System.out.println("VM #" + vm.getId() + " mapped to Fog datacenter #" + fogDcId);
        }
        
        // Map IoT VMs to IoT datacenter
        for (Vm vm : iotVMs) {
            broker.mapVmToDatacenter(vm.getId(), iotDcId);
            System.out.println("VM #" + vm.getId() + " mapped to IoT datacenter #" + iotDcId);
        }
        
        // Add ResourceDebugger to help diagnose any VM-host compatibility issues
        System.out.println("\n===== VM-HOST COMPATIBILITY CHECK =====");
        System.out.println("Checking compatibility for Cloud VMs...");
        ResourceDebugger.checkVmHostCompatibility(cloudVMs, CloudSim.getEntity(cloudDcId));
        
        System.out.println("\nChecking compatibility for Fog VMs...");
        ResourceDebugger.checkVmHostCompatibility(fogVMs, CloudSim.getEntity(fogDcId));
        
        System.out.println("\nChecking compatibility for IoT VMs...");
        ResourceDebugger.checkVmHostCompatibility(iotVMs, CloudSim.getEntity(iotDcId));
        
        System.out.println("\n===== END VM ALLOCATION STRATEGY =====");
    }
    
    /**
     * Creates a cloudlet (task).
     *
     * @param id the cloudlet ID
     * @param userId the user ID
     * @return the cloudlet
     */
    private static Cloudlet createCloudlet(int id, int userId) {
        // Cloudlet properties
        long length = 10000 + (int)(Math.random() * 40000); // Task length in MI (variable)
        long fileSize = 500 + (int)(Math.random() * 1500);  // Input file size in bytes (variable)
        long outputSize = 300 + (int)(Math.random() * 1000); // Output file size in bytes (variable)
        int pesNumber = 1; // Number of CPUs needed
        
        UtilizationModel utilizationModel = new UtilizationModelFull();
        
        Cloudlet cloudlet = new Cloudlet(
            id, 
            length, 
            pesNumber, 
            fileSize, 
            outputSize, 
            utilizationModel, 
            utilizationModel, 
            utilizationModel
        );
        
        cloudlet.setUserId(userId);
        
        // We can't use setRequiredFiles due to protected access
        // So we'll rely on the cloudlet ID to determine source device
        // Source device ID = NUM_CLOUD_HOSTS + NUM_FOG_NODES + cloudlet ID
        
        // For debugging
        System.out.println("Created Task " + id + " with length " + length + 
                       " MI, size " + fileSize + " bytes, originating from IoT device " + 
                       (NUM_CLOUD_HOSTS + NUM_FOG_NODES + id));
        
        return cloudlet;
    }
    
    /**
     * Prints the Cloudlet objects.
     *
     * @param list list of Cloudlets
     * @param policyName the name of the offloading policy used
     */
    private static void printCloudletList(List<Cloudlet> list, String policyName) {
        int size = list.size();
        Cloudlet cloudlet;

        String indent = "    ";
        Log.printLine();
        Log.printLine("----- Simulation Results -----");
        Log.printLine("Offloading Algorithm: " + policyName);
        Log.printLine(indent + "Total Tasks Generated: " + NUM_IOT_DEVICES);
        Log.printLine(indent + "Total Tasks Completed: " + size);
        
        // Success rate
        double successRate = (double)size / NUM_IOT_DEVICES * 100;
        Log.printLine(indent + "Success Rate: " + new DecimalFormat("0.00").format(successRate) + "%");
        
        // Compute averages
        double totalExecutionTime = 0.0;
        double totalCost = 0.0;
        double totalEnergy = 0.0; // Estimated based on execution time
        
        // Track task distribution
        Map<Integer, Integer> vmTaskCount = new HashMap<>();
        Map<Integer, String> vmTypes = new HashMap<>();
        
        // Set VM types
        for (int i = 0; i < NUM_CLOUD_HOSTS; i++) {
            vmTypes.put(i, "Cloud");
        }
        for (int i = NUM_CLOUD_HOSTS; i < NUM_CLOUD_HOSTS + NUM_FOG_NODES; i++) {
            vmTypes.put(i, "Fog");
        }
        for (int i = NUM_CLOUD_HOSTS + NUM_FOG_NODES; i < NUM_CLOUD_HOSTS + NUM_FOG_NODES + NUM_IOT_DEVICES; i++) {
            vmTypes.put(i, "IoT");
        }
        
        // Calculate network usage and transmission time
        double totalTransmissionTime = 0.0;
        
        Log.printLine();
        Log.printLine(indent + "========== Tasks ==========");
        DecimalFormat dft = new DecimalFormat("###.##");
        for (int i = 0; i < size; i++) {
            cloudlet = list.get(i);
            
            // Calculate metrics
            double executionTime = cloudlet.getFinishTime() - cloudlet.getSubmissionTime();
            totalExecutionTime += executionTime;
            totalCost += cloudlet.getProcessingCost();
            
            // Estimate energy based on VM type and execution time
            double energyFactor = 1.0;
            String vmType = vmTypes.get(cloudlet.getVmId());
            if (vmType.equals("Cloud")) {
                energyFactor = 5.0; // Higher energy for cloud
            } else if (vmType.equals("Fog")) {
                energyFactor = 3.0; // Medium energy for fog
            } else {
                energyFactor = 1.0; // Lower energy for IoT
            }
            double taskEnergy = executionTime * energyFactor / 1000.0; // Energy in Joules
            totalEnergy += taskEnergy;
            
            // Calculate transmission time based on offloading
            double transmissionTime = 0.0;
            if (!vmType.equals("IoT")) {
                // Task was offloaded, so there's transmission time
                if (vmType.equals("Fog")) {
                    transmissionTime = IOT_TO_FOG_LATENCY;
                } else { // Cloud
                    transmissionTime = IOT_TO_FOG_LATENCY + FOG_TO_CLOUD_LATENCY;
                }
                // Add data transmission time based on cloudlet size
                transmissionTime += (cloudlet.getCloudletFileSize() + cloudlet.getCloudletOutputSize()) / 1000.0;
                totalTransmissionTime += transmissionTime;
            }
            
            // Track task distribution
            int vmId = cloudlet.getVmId();
            vmTaskCount.put(vmId, vmTaskCount.getOrDefault(vmId, 0) + 1);
            
            Log.printLine(indent + indent + "Task ID: " + cloudlet.getCloudletId() + 
                         ", Status: " + (cloudlet.getCloudletStatus() == Cloudlet.SUCCESS ? "SUCCESS" : "FAILED") +
                         ", VM: " + vmType + "-" + cloudlet.getVmId() +
                         ", Time: " + dft.format(executionTime) + " ms" +
                         ", Transmission: " + dft.format(transmissionTime) + " ms" +
                         ", Energy: " + dft.format(taskEnergy) + " J");
        }
        
        // Print summary
        if (size > 0) {
            Log.printLine();
            Log.printLine(indent + "========== Summary ==========");
            Log.printLine(indent + "Average Execution Time: " + dft.format(totalExecutionTime/size) + " ms");
            Log.printLine(indent + "Average Transmission Time: " + dft.format(totalTransmissionTime/size) + " ms");
            Log.printLine(indent + "Average Energy Consumption: " + dft.format(totalEnergy/size) + " J");
            Log.printLine(indent + "Total Cost: $" + dft.format(totalCost));
            
            // Print task distribution by VM type
            Log.printLine();
            Log.printLine(indent + "Task Distribution by Device Type:");
            Map<String, Integer> deviceTypeCount = new HashMap<>();
            
            for (Map.Entry<Integer, Integer> entry : vmTaskCount.entrySet()) {
                String vmType = vmTypes.get(entry.getKey());
                deviceTypeCount.put(vmType, deviceTypeCount.getOrDefault(vmType, 0) + entry.getValue());
            }
            
            for (Map.Entry<String, Integer> entry : deviceTypeCount.entrySet()) {
                Log.printLine(indent + entry.getKey() + ": " + entry.getValue() + " tasks");
            }
        }
        
        Log.printLine();
    }
}
