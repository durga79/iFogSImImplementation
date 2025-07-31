package org.fogcomputing;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.LinkedList;
import java.util.List;
import java.util.Random;

import org.cloudbus.cloudsim.Host;
import org.cloudbus.cloudsim.Log;
import org.cloudbus.cloudsim.Pe;
import org.cloudbus.cloudsim.Storage;
import org.cloudbus.cloudsim.core.CloudSim;
import org.cloudbus.cloudsim.power.PowerHost;
import org.cloudbus.cloudsim.provisioners.RamProvisionerSimple;
import org.cloudbus.cloudsim.sdn.overbooking.BwProvisionerOverbooking;
import org.cloudbus.cloudsim.sdn.overbooking.PeProvisionerOverbooking;
import org.fog.application.AppEdge;
import org.fog.application.AppLoop;
import org.fog.application.Application;
import org.fog.application.selectivity.FractionalSelectivity;
import org.fog.entities.Actuator;
import org.fog.entities.FogBroker;
import org.fog.entities.FogDevice;
import org.fog.entities.FogDeviceCharacteristics;
import org.fog.entities.Sensor;
import org.fog.entities.Tuple;
import org.fog.placement.Controller;
import org.fog.placement.ModuleMapping;
import org.fog.placement.ModulePlacementEdgewards;
import org.fog.placement.ModulePlacementMapping;
import org.fogcomputing.algorithms.EnergyAwareOffloading;
import org.fogcomputing.algorithms.DeadlineAwareOffloading;
import org.fog.policy.AppModuleAllocationPolicy;
import org.fog.scheduler.StreamOperatorScheduler;
import org.fog.utils.FogLinearPowerModel;
import org.fog.utils.FogUtils;
import org.fog.utils.TimeKeeper;
import org.fog.utils.distribution.DeterministicDistribution;

/**
 * Simulation of Task Offloading in Fog/Edge Computing Environment using iFogSim
 * This simulation demonstrates task offloading from IoT devices to fog nodes and cloud
 */
public class TaskOffloadingFogSimulation {
    
    // Lists to store fog devices, sensors, actuators, etc.
    private static List<FogDevice> fogDevices = new ArrayList<>();
    private static List<Sensor> sensors = new ArrayList<>();
    private static List<Actuator> actuators = new ArrayList<>();
    private static Random rand = new Random(123);
    
    // Number of devices in the simulation
    private static int numOfIoTDevices = 10;
    private static int numOfFogNodes = 5;
    private static int numOfCloudServers = 2;
    
    // Latency between different layers (in milliseconds)
    private static double iotToFogLatency = 2;
    private static double fogToCloudLatency = 20;
    
    // Application parameters
    private static final String appId = "TaskOffloadingApp";
    
    // Task parameters
    private static final int taskLength = 10000; // in MIPS
    private static final int taskDataSize = 1000; // in bytes
    
    /**
     * Main method that starts the simulation
     */
    public static void main(String[] args) {
        Log.printLine("Starting Task Offloading Simulation using iFogSim...");
        
        try {
            // Run first simulation with Energy-Aware Offloading algorithm
            runSimulation("Energy-Aware Offloading");
            
            // Reset simulation state
            resetSimulation();
            
            // Run second simulation with Deadline-Aware Offloading algorithm
            runSimulation("Deadline-Aware Offloading");
            
            Log.printLine("All Task Offloading Simulations completed successfully.");
            
        } catch (Exception e) {
            e.printStackTrace();
            Log.printLine("Simulation terminated due to an unexpected error");
        }
    }
    
    /**
     * Run a simulation with the specified offloading algorithm
     */
    private static void runSimulation(String algorithm) throws Exception {
        Log.printLine("\n==================================\n" + 
                    "Running simulation with " + algorithm + 
                    "\n==================================\n");
        
        // Initialize CloudSim
        int numUsers = 1;
        Calendar calendar = Calendar.getInstance();
        boolean traceFlag = false;
        CloudSim.init(numUsers, calendar, traceFlag);
        
        // Create fog broker
        FogBroker broker = new FogBroker("broker");
        
        // Create application
        Application application = createApplication(appId, broker.getId());
        application.setUserId(broker.getId());
        
        // Create fog devices (IoT devices, fog nodes, cloud)
        createFogDevices(broker.getId(), appId);
        
        // Connect the application to the sensors and actuators
        createSensorsAndActuators(broker.getId(), appId);
        
        // Create module mapping
        ModuleMapping moduleMapping = createModuleMapping();
        
        // Create a controller
        Controller controller = new Controller("controller", fogDevices, sensors, actuators);
        
        // Select and set module placement strategy based on algorithm
        if (algorithm.equals("Energy-Aware Offloading")) {
            // Use Energy-Aware Offloading algorithm
            controller.submitApplication(application, 
                new ModulePlacementMapping(fogDevices, application, moduleMapping, 
                new EnergyAwareOffloading(fogDevices, application.getModules())));
        } else {
            // Use Deadline-Aware Offloading algorithm
            controller.submitApplication(application, 
                new ModulePlacementMapping(fogDevices, application, moduleMapping, 
                new DeadlineAwareOffloading(fogDevices, application.getModules())));
        }
        
        // Start simulation
        Log.printLine("Starting simulation with " + algorithm);
        TimeKeeper.getInstance().setSimulationStartTime(Calendar.getInstance().getTimeInMillis());
        CloudSim.startSimulation();
        CloudSim.stopSimulation();
        
        // Print results
        Log.printLine("Simulation with " + algorithm + " finished!");
        printResults(algorithm);
    }
    
    /**
     * Reset simulation state between runs
     */
    private static void resetSimulation() {
        fogDevices.clear();
        sensors.clear();
        actuators.clear();
        TimeKeeper.getInstance().resetKeeper();
    }
    
    /**
     * Creates the fog devices in the simulation (IoT devices, fog nodes, cloud)
     */
    private static void createFogDevices(int userId, String appId) {
        FogDevice cloud = createCloud(userId, appId, "cloud");
        fogDevices.add(cloud);
        
        // Create fog nodes
        for (int i = 0; i < numOfFogNodes; i++) {
            FogDevice fogNode = createFogNode(userId, appId, "fog-" + i, cloud.getId());
            fogDevices.add(fogNode);
            
            // Create IoT devices connected to this fog node
            int iotsPerFogNode = numOfIoTDevices / numOfFogNodes;
            int startIndex = i * iotsPerFogNode;
            int endIndex = (i == numOfFogNodes - 1) ? numOfIoTDevices : (i + 1) * iotsPerFogNode;
            
            for (int j = startIndex; j < endIndex; j++) {
                FogDevice iot = createIoTDevice(userId, appId, "iot-" + j, fogNode.getId());
                fogDevices.add(iot);
            }
        }
    }
    
    /**
     * Creates a cloud data center
     */
    private static FogDevice createCloud(int userId, String appId, String name) {
        // Cloud characteristics
        FogDeviceCharacteristics characteristics = new FogDeviceCharacteristics(
                "x86",          // architecture
                16000,          // CPU (MIPS)
                64000,          // RAM (MB)
                100000,         // Uplink Bandwidth
                100000,         // Downlink Bandwidth
                16,             // Level of hierarchy
                0.01,           // Scheduling interval
                1000000,        // Storage size (MB)
                0.0,            // CPU busy power
                0.0             // CPU idle power
        );
        
        // Host configuration for cloud (RAM, BW, Storage)
        List<Pe> peList = new ArrayList<>();
        int mips = 16000;
        for (int i = 0; i < 8; i++)  // 8 CPU cores
            peList.add(new Pe(i, new PeProvisionerOverbooking(mips)));
        
        // Create host with its characteristics
        Host host = new PowerHost(
                FogUtils.generateEntityId(),
                new RamProvisionerSimple(65536),
                new BwProvisionerOverbooking(1000000),
                1000000,
                peList,
                new StreamOperatorScheduler(peList),
                new FogLinearPowerModel(107.339, 83.4333)
        );
        
        List<Host> hostList = new ArrayList<>();
        hostList.add(host);
        
        // Create cloud data center
        String arch = "x86";
        String os = "Linux";
        String vmm = "Xen";
        double time_zone = 10.0;
        double cost = 0.1;
        double costPerMem = 0.05;
        double costPerStorage = 0.001;
        double costPerBw = 0.0;
        LinkedList<Storage> storageList = new LinkedList<>();
        
        FogDevice cloud = new FogDevice(
                name, characteristics, 
                new AppModuleAllocationPolicy(hostList),
                storageList, 10.0, 0.01, 16, 16000,
                0, 1000000, 100000, 100000, 0.01, 
                FogUtils.generateEntityId()
        );
        cloud.setParentId(-1);  // Cloud has no parent
        
        return cloud;
    }
    
    /**
     * Creates a fog node
     */
    private static FogDevice createFogNode(int userId, String appId, String name, int parentId) {
        // Define fog node characteristics (middle-tier)
        FogDeviceCharacteristics characteristics = new FogDeviceCharacteristics(
                "ARM",          // architecture
                4000,           // CPU (MIPS)
                8000,           // RAM (MB)
                10000,          // Uplink Bandwidth
                10000,          // Downlink Bandwidth
                8,              // Level of hierarchy
                0.01,           // Scheduling interval
                100000,         // Storage size (MB)
                25.0,           // CPU busy power
                15.0            // CPU idle power
        );
        
        // Host configuration for fog node
        List<Pe> peList = new ArrayList<>();
        int mips = 4000;
        for (int i = 0; i < 4; i++)  // 4 CPU cores
            peList.add(new Pe(i, new PeProvisionerOverbooking(mips)));
        
        // Create host with its characteristics
        Host host = new PowerHost(
                FogUtils.generateEntityId(),
                new RamProvisionerSimple(8000),
                new BwProvisionerOverbooking(10000),
                100000,
                peList,
                new StreamOperatorScheduler(peList),
                new FogLinearPowerModel(107.339, 83.4333)
        );
        
        List<Host> hostList = new ArrayList<>();
        hostList.add(host);
        
        // Create fog node
        String arch = "ARM";
        String os = "Linux";
        String vmm = "Xen";
        double time_zone = 10.0;
        double cost = 0.0;
        double costPerMem = 0.0;
        double costPerStorage = 0.0;
        double costPerBw = 0.0;
        LinkedList<Storage> storageList = new LinkedList<>();
        
        FogDevice fogNode = new FogDevice(
                name, characteristics, 
                new AppModuleAllocationPolicy(hostList),
                storageList, 10.0, 0.01, 8, 4000,
                25.0, 100000, 10000, 10000, 0.01, 
                FogUtils.generateEntityId()
        );
        fogNode.setParentId(parentId);
        fogNode.setUplinkLatency(fogToCloudLatency);  // latency to cloud
        
        return fogNode;
    }
    
    /**
     * Creates an IoT device
     */
    private static FogDevice createIoTDevice(int userId, String appId, String name, int parentId) {
        // Define IoT device characteristics (resource-constrained)
        FogDeviceCharacteristics characteristics = new FogDeviceCharacteristics(
                "ARM",          // architecture
                500,            // CPU (MIPS)
                1000,           // RAM (MB)
                1000,           // Uplink Bandwidth
                1000,           // Downlink Bandwidth
                1,              // Level of hierarchy
                0.01,           // Scheduling interval
                10000,          // Storage size (MB)
                10.0,           // CPU busy power
                5.0             // CPU idle power
        );
        
        // Host configuration for IoT device
        List<Pe> peList = new ArrayList<>();
        int mips = 500;
        peList.add(new Pe(0, new PeProvisionerOverbooking(mips)));
        
        // Create host with its characteristics
        Host host = new PowerHost(
                FogUtils.generateEntityId(),
                new RamProvisionerSimple(1000),
                new BwProvisionerOverbooking(1000),
                10000,
                peList,
                new StreamOperatorScheduler(peList),
                new FogLinearPowerModel(87.53, 82.44)
        );
        
        List<Host> hostList = new ArrayList<>();
        hostList.add(host);
        
        // Create IoT device
        LinkedList<Storage> storageList = new LinkedList<>();
        
        FogDevice iot = new FogDevice(
                name, characteristics, 
                new AppModuleAllocationPolicy(hostList),
                storageList, 10.0, 0.01, 1, 500,
                10.0, 10000, 1000, 1000, 0.01, 
                FogUtils.generateEntityId()
        );
        iot.setParentId(parentId);
        iot.setUplinkLatency(iotToFogLatency);  // latency to fog node
        
        return iot;
    }
    
    /**
     * Creates sensors and actuators for the devices
     */
    private static void createSensorsAndActuators(int userId, String appId) {
        // Attach a sensor to each IoT device
        for (FogDevice device : fogDevices) {
            if (device.getName().startsWith("iot-")) {
                String sensorName = "sensor-" + device.getName().substring(4);  // e.g., sensor-0
                Sensor sensor = new Sensor(sensorName, "SENSOR", userId, appId, 
                                         new DeterministicDistribution(5000));  // 5-second intervals
                sensor.setGatewayDeviceId(device.getId());
                sensor.setLatency(1.0);  // 1ms latency
                sensors.add(sensor);
                
                // Create an actuator for the device
                String actuatorName = "actuator-" + device.getName().substring(4);  // e.g., actuator-0
                Actuator actuator = new Actuator(actuatorName, userId, appId, 
                                               "ACTUATOR");
                actuator.setGatewayDeviceId(device.getId());
                actuator.setLatency(1.0);  // 1ms latency
                actuators.add(actuator);
            }
        }
    }
    
    /**
     * Creates the application with modules and edges
     */
    private static Application createApplication(String appId, int userId) {
        // Create application
        Application application = Application.createApplication(appId, userId);
        
        // Add modules
        application.addAppModule("iot_module", 10, 1000, 1000, 100);
        application.addAppModule("fog_module", 500, 1500, 4000, 800);
        application.addAppModule("cloud_module", 1000, 2000, 10000, 1000);
        
        // Add edges
        // Data flow from sensor to IoT module
        application.addAppEdge("SENSOR", "iot_module", taskDataSize, 
                             taskLength, "SENSOR-DATA", Tuple.UP, AppEdge.SENSOR);
        
        // Data flow from IoT module to fog module (offloading task)
        application.addAppEdge("iot_module", "fog_module", taskDataSize, 
                             taskLength, "OFFLOAD-TASK", Tuple.UP, AppEdge.MODULE);
        
        // Data flow from fog module to cloud module (further offloading)
        application.addAppEdge("fog_module", "cloud_module", taskDataSize/2, 
                             taskLength/2, "CLOUD-TASK", Tuple.UP, AppEdge.MODULE);
        
        // Results flow from cloud to fog
        application.addAppEdge("cloud_module", "fog_module", taskDataSize/10, 
                             taskLength/10, "CLOUD-RESULT", Tuple.DOWN, AppEdge.MODULE);
        
        // Results flow from fog to IoT
        application.addAppEdge("fog_module", "iot_module", taskDataSize/5, 
                             taskLength/5, "FOG-RESULT", Tuple.DOWN, AppEdge.MODULE);
        
        // Results flow from IoT to actuator
        application.addAppEdge("iot_module", "ACTUATOR", taskDataSize/20, 
                             taskLength/20, "ACTUATOR-COMMAND", Tuple.DOWN, AppEdge.ACTUATOR);
        
        // Set selectivity (chance of producing output tuple for input tuple)
        // IoT processing: 90% selectivity
        application.addTupleMapping("iot_module", "SENSOR-DATA", "OFFLOAD-TASK", 
                                  new FractionalSelectivity(0.9));
        
        // Fog processing: 80% selectivity for cloud offload, 20% processed locally
        application.addTupleMapping("fog_module", "OFFLOAD-TASK", "CLOUD-TASK", 
                                  new FractionalSelectivity(0.8));
        application.addTupleMapping("fog_module", "OFFLOAD-TASK", "FOG-RESULT", 
                                  new FractionalSelectivity(0.2));
        
        // Cloud processing: 100% produces results
        application.addTupleMapping("cloud_module", "CLOUD-TASK", "CLOUD-RESULT", 
                                  new FractionalSelectivity(1.0));
        
        // Fog forwards cloud results to IoT
        application.addTupleMapping("fog_module", "CLOUD-RESULT", "FOG-RESULT", 
                                  new FractionalSelectivity(1.0));
        
        // IoT forwards results to actuator
        application.addTupleMapping("iot_module", "FOG-RESULT", "ACTUATOR-COMMAND", 
                                  new FractionalSelectivity(1.0));
        
        // Define application loops (for latency tracking)
        final AppLoop loop1 = new AppLoop(new ArrayList<String>(){{
            add("SENSOR");
            add("iot_module");
            add("fog_module");
            add("iot_module");
            add("ACTUATOR");
        }});
        
        final AppLoop loop2 = new AppLoop(new ArrayList<String>(){{
            add("SENSOR");
            add("iot_module");
            add("fog_module");
            add("cloud_module");
            add("fog_module");
            add("iot_module");
            add("ACTUATOR");
        }});
        
        List<AppLoop> loops = new ArrayList<AppLoop>(){{
            add(loop1);
            add(loop2);
        }};
        
        application.setLoops(loops);
        
        return application;
    }
    
    /**
     * Creates module mappings for placement strategy
     */
    private static ModuleMapping createModuleMapping() {
        ModuleMapping moduleMapping = ModuleMapping.createModuleMapping();
        
        // Place IoT modules on IoT devices
        for (FogDevice device : fogDevices) {
            if (device.getName().startsWith("iot-")) {
                moduleMapping.addModuleToDevice("iot_module", device.getName());
            }
        }
        
        // Place fog modules on fog nodes
        for (FogDevice device : fogDevices) {
            if (device.getName().startsWith("fog-")) {
                moduleMapping.addModuleToDevice("fog_module", device.getName());
            }
        }
        
        // Place cloud modules on cloud
        moduleMapping.addModuleToDevice("cloud_module", "cloud");
        
        return moduleMapping;
    }
    
    /**
     * Print results of the simulation
     * @param algorithm The offloading algorithm used
     */
    private static void printResults(String algorithm) {
        // Print loop delays
        System.out.println("\n----- Simulation Results -----");
        System.out.println("Offloading Algorithm: " + algorithm);
        System.out.println("Application loops delays:");
        
        for (AppLoop loop : TimeKeeper.getInstance().getLoopIdToCurrentAverage().keySet()) {
            double delay = TimeKeeper.getInstance().getLoopIdToCurrentAverage().get(loop);
            System.out.println(loop.getLoopId() + " : " + delay + " ms");
        }
        
        // Calculate total task count and completion rate
        int totalTasks = sensors.size();
        int completedTasks = 0;
        for (AppLoop loop : TimeKeeper.getInstance().getLoopIdToCurrentAverage().keySet()) {
            completedTasks++;
        }
        double successRate = (double)completedTasks / totalTasks * 100;
        
        System.out.println("Total Tasks Generated: " + totalTasks);
        System.out.println("Total Tasks Completed: " + completedTasks);
        System.out.println("Success Rate: " + String.format("%.2f", successRate) + "%");
        
        // Print energy consumption
        double totalEnergy = 0;
        System.out.println("\nEnergy consumption per device:");
        for (FogDevice device : fogDevices) {
            System.out.println(device.getName() + " : " + String.format("%.2f", device.getEnergyConsumption()) + " J");
            totalEnergy += device.getEnergyConsumption();
        }
        System.out.println("Total Energy Consumption: " + String.format("%.2f", totalEnergy) + " J");
        
        // Print network usage
        long totalNetworkUsage = 0;
        System.out.println("\nNetwork usage per device:");
        for (FogDevice device : fogDevices) {
            long deviceNetworkUsage = device.getNetworkUsageUp() + device.getNetworkUsageDown();
            System.out.println(device.getName() + 
                             " : Up (KB): " + String.format("%.2f", device.getNetworkUsageUp()/1024.0) + 
                             " | Down (KB): " + String.format("%.2f", device.getNetworkUsageDown()/1024.0));
            totalNetworkUsage += deviceNetworkUsage;
        }
        System.out.println("Total Network Usage: " + String.format("%.2f", totalNetworkUsage/1024.0) + " KB");
        
        // Print average execution time
        double totalExecutionTime = 0;
        int deviceCount = 0;
        System.out.println("\nExecution times per device:");
        for (FogDevice device : fogDevices) {
            if (device.getTotalExecutionTime() > 0) {
                System.out.println(device.getName() + " : " + device.getTotalExecutionTime() + " ms");
                totalExecutionTime += device.getTotalExecutionTime();
                deviceCount++;
            }
        }
        
        if (deviceCount > 0) {
            double avgExecutionTime = totalExecutionTime / deviceCount;
            System.out.println("Average Execution Time: " + String.format("%.2f", avgExecutionTime) + " ms");
        }
    }
}
