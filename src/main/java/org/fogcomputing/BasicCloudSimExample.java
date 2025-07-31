package org.fogcomputing;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.text.DecimalFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.ArrayList;
import java.util.LinkedList;

import org.cloudbus.cloudsim.Cloudlet;
import org.cloudbus.cloudsim.CloudletSchedulerTimeShared;
import org.cloudbus.cloudsim.Datacenter;
import org.cloudbus.cloudsim.DatacenterBroker;
import org.cloudbus.cloudsim.DatacenterCharacteristics;
import org.cloudbus.cloudsim.Host;
import org.cloudbus.cloudsim.Log;
import org.cloudbus.cloudsim.Pe;
import org.cloudbus.cloudsim.Storage;
import org.cloudbus.cloudsim.UtilizationModel;
import org.cloudbus.cloudsim.UtilizationModelFull;
import org.cloudbus.cloudsim.Vm;
import org.cloudbus.cloudsim.VmAllocationPolicySimple;
import org.cloudbus.cloudsim.VmSchedulerTimeShared;
import org.cloudbus.cloudsim.core.CloudSim;
import org.cloudbus.cloudsim.provisioners.BwProvisionerSimple;
import org.cloudbus.cloudsim.provisioners.PeProvisionerSimple;
import org.cloudbus.cloudsim.provisioners.RamProvisionerSimple;

/**
 * A basic CloudSim example to demonstrate file output capabilities.
 */
public class BasicCloudSimExample {

    /** The cloudlet list. */
    private static List<Cloudlet> cloudletList;

    /** The vmList. */
    private static List<Vm> vmlist;

    private static final String RESULTS_DIR = "/home/wexa/CascadeProjects/FogEdgeComputing/iFogSimImplementation/results/";
    private static final DecimalFormat dft = new DecimalFormat("###.##");

    /**
     * Creates main() to run this example
     */
    public static void main(String[] args) {
        Log.printLine("Starting Basic CloudSim Example...");

        try {
            // First test file writing capability
            testFileWriting();

            // Initialize CloudSim
            int num_user = 1;
            Calendar calendar = Calendar.getInstance();
            boolean trace_flag = false;
            CloudSim.init(num_user, calendar, trace_flag);

            // Create Datacenter
            Datacenter datacenter = createDatacenter("Datacenter_0");

            // Create Broker
            DatacenterBroker broker = createBroker();
            int brokerId = broker.getId();

            // Create VMs
            vmlist = new ArrayList<Vm>();
            
            // VM description
            int vmid = 0;
            int mips = 1000;
            long size = 10000; // image size (MB)
            int ram = 512; // memory (MB)
            long bw = 1000;
            int pesNumber = 1; // number of cpus
            String vmm = "Xen"; // VMM name

            // Create two VMs
            Vm vm1 = new Vm(vmid++, brokerId, mips, pesNumber, ram, bw, size, vmm, new CloudletSchedulerTimeShared());
            Vm vm2 = new Vm(vmid++, brokerId, mips * 2, pesNumber, ram, bw, size, vmm, new CloudletSchedulerTimeShared());

            vmlist.add(vm1);
            vmlist.add(vm2);

            // Submit VM list to the broker
            broker.submitVmList(vmlist);

            // Create cloudlets
            cloudletList = new ArrayList<Cloudlet>();
            
            int cloudletId = 0;
            long length = 400000; // MI
            long fileSize = 300; // size of input file
            long outputSize = 300; // size of output file
            UtilizationModel utilizationModel = new UtilizationModelFull();

            // Create 10 cloudlets
            for (int i = 0; i < 10; i++) {
                Cloudlet cloudlet = new Cloudlet(cloudletId++, length, pesNumber, fileSize, outputSize, utilizationModel, utilizationModel, utilizationModel);
                cloudlet.setUserId(brokerId);
                
                // Alternate between VMs
                if (i % 2 == 0) {
                    cloudlet.setVmId(0);
                } else {
                    cloudlet.setVmId(1);
                }
                
                cloudletList.add(cloudlet);
            }

            // Submit cloudlet list to broker
            broker.submitCloudletList(cloudletList);

            // Start the simulation
            CloudSim.startSimulation();

            // Stop the simulation
            CloudSim.stopSimulation();

            // Process results
            List<Cloudlet> finalCloudletList = broker.getCloudletReceivedList();
            printCloudletList(finalCloudletList);

            // Save results to files
            saveResultsToCSV(finalCloudletList);

            Log.printLine("BasicCloudSimExample finished!");
        } catch (Exception e) {
            e.printStackTrace();
            Log.printLine("Unwanted errors happened");
        }
    }

    /**
     * Test method to check if file writing works properly
     */
    private static void testFileWriting() {
        System.out.println("\n===== Testing File Writing =====\n");
        
        // Print system info
        System.out.println("Current directory: " + System.getProperty("user.dir"));
        System.out.println("Java version: " + System.getProperty("java.version"));
        System.out.println("Java vendor: " + System.getProperty("java.vendor"));
        
        // Create results directory if it doesn't exist
        File resultsDir = new File(RESULTS_DIR);
        if (!resultsDir.exists()) {
            boolean created = resultsDir.mkdirs();
            System.out.println("Created results directory: " + created + " at " + resultsDir.getAbsolutePath());
        } else {
            System.out.println("Results directory already exists at " + resultsDir.getAbsolutePath());
        }
        
        // Test writing to results directory
        try {
            File testFile = new File(RESULTS_DIR + "test_" + System.currentTimeMillis() + ".csv");
            PrintWriter writer = new PrintWriter(new FileWriter(testFile));
            writer.println("Test,File,Writing");
            writer.println("Test," + new Date() + ",Success");
            writer.flush();
            writer.close();
            System.out.println("Successfully wrote test file to: " + testFile.getAbsolutePath());
        } catch (Exception e) {
            System.err.println("ERROR writing test file: " + e.getMessage());
            e.printStackTrace();
        }
        
        System.out.println("\n===== File Writing Test Complete =====\n");
    }

    private static Datacenter createDatacenter(String name) {
        // Create a list of hosts
        List<Host> hostList = new ArrayList<Host>();

        // Create PEs and add to list
        int mips = 1000;
        List<Pe> peList = new ArrayList<Pe>();
        peList.add(new Pe(0, new PeProvisionerSimple(mips))); // need to store Pe id and MIPS Rating

        int hostId = 0;
        int ram = 2048; // host memory (MB)
        long storage = 1000000; // host storage
        int bw = 10000;

        hostList.add(
            new Host(
                hostId,
                new RamProvisionerSimple(ram),
                new BwProvisionerSimple(bw),
                storage,
                peList,
                new VmSchedulerTimeShared(peList)
            )
        );

        // Create a DatacenterCharacteristics object
        String arch = "x86";      // system architecture
        String os = "Linux";      // operating system
        String vmm = "Xen";
        double time_zone = 10.0;  // time zone this resource located
        double cost = 3.0;        // the cost of using processing in this resource
        double costPerMem = 0.05; // the cost of using memory in this resource
        double costPerStorage = 0.001; // the cost of using storage in this resource
        double costPerBw = 0.0;   // the cost of using bw in this resource
        LinkedList<Storage> storageList = new LinkedList<Storage>();

        DatacenterCharacteristics characteristics = new DatacenterCharacteristics(
                arch, os, vmm, hostList, time_zone, cost, costPerMem,
                costPerStorage, costPerBw);

        // Create a Datacenter with previously created characteristics
        Datacenter datacenter = null;
        try {
            datacenter = new Datacenter(name, characteristics, new VmAllocationPolicySimple(hostList), storageList, 0);
        } catch (Exception e) {
            e.printStackTrace();
        }

        return datacenter;
    }

    /**
     * Creates the broker.
     *
     * @return the datacenter broker
     */
    private static DatacenterBroker createBroker() {
        DatacenterBroker broker = null;
        try {
            broker = new DatacenterBroker("Broker");
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
        return broker;
    }

    /**
     * Prints the Cloudlet objects.
     *
     * @param list list of Cloudlets
     */
    private static void printCloudletList(List<Cloudlet> list) {
        int size = list.size();
        Cloudlet cloudlet;

        String indent = "    ";
        Log.printLine();
        Log.printLine("========== OUTPUT ==========");
        Log.printLine("Cloudlet ID" + indent + "STATUS" + indent +
                "Data center ID" + indent + "VM ID" + indent + "Time" + indent + "Start Time" + indent + "Finish Time");

        DecimalFormat dft = new DecimalFormat("###.##");
        for (int i = 0; i < size; i++) {
            cloudlet = list.get(i);
            Log.print(indent + cloudlet.getCloudletId() + indent + indent);

            if (cloudlet.getStatus() == Cloudlet.SUCCESS) {
                Log.print("SUCCESS");

                Log.printLine(indent + indent + cloudlet.getResourceId() + indent + indent + indent + cloudlet.getVmId() +
                        indent + indent + dft.format(cloudlet.getActualCPUTime()) + indent + indent + dft.format(cloudlet.getExecStartTime()) +
                        indent + indent + dft.format(cloudlet.getFinishTime()));
            }
        }
    }

    /**
     * Save results to CSV file
     */
    private static void saveResultsToCSV(List<Cloudlet> list) {
        try {
            String fileName = RESULTS_DIR + "basic_cloudsim_results_" + System.currentTimeMillis() + ".csv";
            File file = new File(fileName);
            
            // Make sure directory exists
            file.getParentFile().mkdirs();
            
            PrintWriter writer = new PrintWriter(new FileWriter(file));
            
            writer.println("Cloudlet_ID,Status,Datacenter_ID,VM_ID,Time,Start_Time,Finish_Time");
            
            for (Cloudlet cloudlet : list) {
                if (cloudlet.getStatus() == Cloudlet.SUCCESS) {
                    writer.println(
                        cloudlet.getCloudletId() + "," +
                        "SUCCESS" + "," + 
                        cloudlet.getResourceId() + "," +
                        cloudlet.getVmId() + "," +
                        dft.format(cloudlet.getActualCPUTime()) + "," +
                        dft.format(cloudlet.getExecStartTime()) + "," +
                        dft.format(cloudlet.getFinishTime())
                    );
                }
            }
            
            writer.flush();
            writer.close();
            System.out.println("Successfully saved results to: " + file.getAbsolutePath());
        } catch (IOException e) {
            System.err.println("ERROR writing results: " + e.getMessage());
            e.printStackTrace();
        }
    }
}
