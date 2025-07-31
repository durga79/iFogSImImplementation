    /**
     * Creates the IoT datacenter with very constrained resources.
     *
     * @param name the datacenter name
     * @return the datacenter
     */
    private static Datacenter createIoTDatacenter(String name) {
        List<Host> hostList = new ArrayList<Host>();

        int mips = 1000; // MIPS for IoT - simplified from FixedTierSimulation
        int ram = 2048; // 2GB RAM for IoT devices - simplified from FixedTierSimulation
        long storage = 1000000; // Storage (MB)
        int bw = 1000; // Low bandwidth for IoT (MB/s)

        for (int hostId = 0; hostId < NUM_IOT_DEVICES; hostId++) {
            List<Pe> peList = new ArrayList<Pe>();
            
            // Single core per host as in FixedTierSimulation
            peList.add(new Pe(0, new PeProvisionerSimple(mips)));
            
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
            // Use TieredVmAllocationPolicy for IoT tier (VMs NUM_CLOUD_HOSTS+NUM_FOG_NODES to NUM_CLOUD_HOSTS+NUM_FOG_NODES+NUM_IOT_DEVICES-1)
            datacenter = new Datacenter(name, characteristics,
                new TieredVmAllocationPolicy(hostList, NUM_CLOUD_HOSTS+NUM_FOG_NODES, NUM_CLOUD_HOSTS+NUM_FOG_NODES+NUM_IOT_DEVICES-1, "IoT"),
                storageList, 0);
        } catch (Exception e) {
            e.printStackTrace();
        }

        return datacenter;
    }
