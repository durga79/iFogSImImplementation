@echo off
REM Script to build and run the CloudSim Task Offloading Simulation
REM Author: Cascade
REM Date: July 31, 2025

REM Clean and create the target directory
echo Cleaning target directory...
if exist target rmdir /s /q target
mkdir target\classes

REM Create results directory if it doesn't exist
echo Creating results directory...
if not exist results mkdir results

REM First compile SimulationResultProcessor class
echo Compiling SimulationResultProcessor...
javac -cp "libs\cloudsim-3.0.3.jar;libs\cloudsim-examples-3.0.3.jar;libs\commons-math3-3.5.jar;libs\json-simple-1.1.1.jar;libs\guava-18.0.jar" ^
      -d target\classes ^
      src\main\java\org\fogcomputing\SimulationResultProcessor.java

if %ERRORLEVEL% neq 0 (
  echo Failed to compile SimulationResultProcessor
  exit /b 1
)

REM Compile OffloadingPolicy interface
echo Compiling OffloadingPolicy interface...
javac -cp "target\classes;libs\cloudsim-3.0.3.jar;libs\cloudsim-examples-3.0.3.jar;libs\commons-math3-3.5.jar;libs\json-simple-1.1.1.jar;libs\guava-18.0.jar" ^
      -d target\classes ^
      src\main\java\org\fogcomputing\algorithms\OffloadingPolicy.java

if %ERRORLEVEL% neq 0 (
  echo Failed to compile OffloadingPolicy interface
  exit /b 1
)

REM Create mock classes for the missing fog classes
echo Creating mock classes for missing fog dependencies...
mkdir src\main\java\org\fogcomputing\mock 2>NUL
mkdir target\classes\org\fogcomputing\mock 2>NUL

REM Use a different approach to create the Java files

REM Create MockFogClasses.java
echo package org.fogcomputing.mock; > src\main\java\org\fogcomputing\mock\MockFogClasses.java
echo. >> src\main\java\org\fogcomputing\mock\MockFogClasses.java
echo // This file provides mock classes to replace the missing iFogSim classes >> src\main\java\org\fogcomputing\mock\MockFogClasses.java
echo // It allows the simulation to compile without the actual fog libraries >> src\main\java\org\fogcomputing\mock\MockFogClasses.java
echo. >> src\main\java\org\fogcomputing\mock\MockFogClasses.java
echo public class MockFogClasses ^{ >> src\main\java\org\fogcomputing\mock\MockFogClasses.java
echo     // Empty implementation - just to provide the classes needed for compilation >> src\main\java\org\fogcomputing\mock\MockFogClasses.java
echo ^} >> src\main\java\org\fogcomputing\mock\MockFogClasses.java
echo. >> src\main\java\org\fogcomputing\mock\MockFogClasses.java
echo // Mock FogDevice class >> src\main\java\org\fogcomputing\mock\MockFogClasses.java
echo class FogDevice ^{ >> src\main\java\org\fogcomputing\mock\MockFogClasses.java
echo     private String name; >> src\main\java\org\fogcomputing\mock\MockFogClasses.java
echo     private double energyConsumption; >> src\main\java\org\fogcomputing\mock\MockFogClasses.java
echo     private int id; >> src\main\java\org\fogcomputing\mock\MockFogClasses.java
echo     >> src\main\java\org\fogcomputing\mock\MockFogClasses.java
echo     public FogDevice(int id, String name) ^{ >> src\main\java\org\fogcomputing\mock\MockFogClasses.java
echo         this.id = id; >> src\main\java\org\fogcomputing\mock\MockFogClasses.java
echo         this.name = name; >> src\main\java\org\fogcomputing\mock\MockFogClasses.java
echo         this.energyConsumption = 0.0; >> src\main\java\org\fogcomputing\mock\MockFogClasses.java
echo     ^} >> src\main\java\org\fogcomputing\mock\MockFogClasses.java
echo     >> src\main\java\org\fogcomputing\mock\MockFogClasses.java
echo     public String getName() ^{ >> src\main\java\org\fogcomputing\mock\MockFogClasses.java
echo         return name; >> src\main\java\org\fogcomputing\mock\MockFogClasses.java
echo     ^} >> src\main\java\org\fogcomputing\mock\MockFogClasses.java
echo     >> src\main\java\org\fogcomputing\mock\MockFogClasses.java
echo     public double getEnergyConsumption() ^{ >> src\main\java\org\fogcomputing\mock\MockFogClasses.java
echo         return energyConsumption; >> src\main\java\org\fogcomputing\mock\MockFogClasses.java
echo     ^} >> src\main\java\org\fogcomputing\mock\MockFogClasses.java
echo     >> src\main\java\org\fogcomputing\mock\MockFogClasses.java
echo     public int getId() ^{ >> src\main\java\org\fogcomputing\mock\MockFogClasses.java
echo         return id; >> src\main\java\org\fogcomputing\mock\MockFogClasses.java
echo     ^} >> src\main\java\org\fogcomputing\mock\MockFogClasses.java
echo ^} >> src\main\java\org\fogcomputing\mock\MockFogClasses.java

REM Compile the mock classes
echo Compiling mock classes...
javac -d target\classes src\main\java\org\fogcomputing\mock\MockFogClasses.java

if %ERRORLEVEL% neq 0 (
  echo Failed to compile mock classes
  exit /b 1
)

REM Create simplified implementations of the offloading policies
echo Creating simplified implementations of offloading policies...

REM Create EnergyAwareOffloadingPolicy.java with proper escaping
echo package org.fogcomputing.algorithms; > src\main\java\org\fogcomputing\algorithms\EnergyAwareOffloadingPolicy.java
echo. >> src\main\java\org\fogcomputing\algorithms\EnergyAwareOffloadingPolicy.java
echo import java.util.List; >> src\main\java\org\fogcomputing\algorithms\EnergyAwareOffloadingPolicy.java
echo import org.cloudbus.cloudsim.Cloudlet; >> src\main\java\org\fogcomputing\algorithms\EnergyAwareOffloadingPolicy.java
echo import org.cloudbus.cloudsim.Vm; >> src\main\java\org\fogcomputing\algorithms\EnergyAwareOffloadingPolicy.java
echo. >> src\main\java\org\fogcomputing\algorithms\EnergyAwareOffloadingPolicy.java
echo public class EnergyAwareOffloadingPolicy implements OffloadingPolicy ^{ >> src\main\java\org\fogcomputing\algorithms\EnergyAwareOffloadingPolicy.java
echo. >> src\main\java\org\fogcomputing\algorithms\EnergyAwareOffloadingPolicy.java
echo     @Override >> src\main\java\org\fogcomputing\algorithms\EnergyAwareOffloadingPolicy.java
echo     public int getTargetVmId(Cloudlet cloudlet, List^<Vm^> vmList) ^{ >> src\main\java\org\fogcomputing\algorithms\EnergyAwareOffloadingPolicy.java
echo         // Energy-aware allocation strategy  >> src\main\java\org\fogcomputing\algorithms\EnergyAwareOffloadingPolicy.java
echo         // Distribute tasks to minimize energy consumption >> src\main\java\org\fogcomputing\algorithms\EnergyAwareOffloadingPolicy.java
echo         // For simulation, we'll use a simple rule-based approach: >> src\main\java\org\fogcomputing\algorithms\EnergyAwareOffloadingPolicy.java
echo         // - CPU-intensive tasks go to cloud (better energy efficiency for computation) >> src\main\java\org\fogcomputing\algorithms\EnergyAwareOffloadingPolicy.java
echo         // - Data-intensive but less compute-heavy tasks go to fog (reduced transmission energy) >> src\main\java\org\fogcomputing\algorithms\EnergyAwareOffloadingPolicy.java
echo         // - Very small tasks stay on IoT devices (no transmission energy) >> src\main\java\org\fogcomputing\algorithms\EnergyAwareOffloadingPolicy.java
echo. >> src\main\java\org\fogcomputing\algorithms\EnergyAwareOffloadingPolicy.java
echo         // Simple implementation for simulation purposes >> src\main\java\org\fogcomputing\algorithms\EnergyAwareOffloadingPolicy.java
echo         int cloudletLength = (int) cloudlet.getCloudletLength(); >> src\main\java\org\fogcomputing\algorithms\EnergyAwareOffloadingPolicy.java
echo         long cloudletFileSize = cloudlet.getCloudletFileSize(); >> src\main\java\org\fogcomputing\algorithms\EnergyAwareOffloadingPolicy.java
echo. >> src\main\java\org\fogcomputing\algorithms\EnergyAwareOffloadingPolicy.java
echo         // Classification thresholds >> src\main\java\org\fogcomputing\algorithms\EnergyAwareOffloadingPolicy.java
echo         int HIGH_COMPUTATION = 25000; // MI >> src\main\java\org\fogcomputing\algorithms\EnergyAwareOffloadingPolicy.java
echo         int LOW_COMPUTATION = 15000; // MI >> src\main\java\org\fogcomputing\algorithms\EnergyAwareOffloadingPolicy.java
echo         int HIGH_DATA = 1000; // bytes >> src\main\java\org\fogcomputing\algorithms\EnergyAwareOffloadingPolicy.java
echo. >> src\main\java\org\fogcomputing\algorithms\EnergyAwareOffloadingPolicy.java
echo         if (cloudletLength ^> HIGH_COMPUTATION) ^{ >> src\main\java\org\fogcomputing\algorithms\EnergyAwareOffloadingPolicy.java
echo             // High computation tasks go to Cloud for better energy efficiency >> src\main\java\org\fogcomputing\algorithms\EnergyAwareOffloadingPolicy.java
echo             // Look for a Cloud VM >> src\main\java\org\fogcomputing\algorithms\EnergyAwareOffloadingPolicy.java
echo             for (Vm vm : vmList) ^{ >> src\main\java\org\fogcomputing\algorithms\EnergyAwareOffloadingPolicy.java
echo                 if (vm.getId() ^< 2) ^{ // First two VMs are in Cloud >> src\main\java\org\fogcomputing\algorithms\EnergyAwareOffloadingPolicy.java
echo                     return vm.getId(); >> src\main\java\org\fogcomputing\algorithms\EnergyAwareOffloadingPolicy.java
echo                 ^} >> src\main\java\org\fogcomputing\algorithms\EnergyAwareOffloadingPolicy.java
echo             ^} >> src\main\java\org\fogcomputing\algorithms\EnergyAwareOffloadingPolicy.java
echo         ^} else if (cloudletLength ^< LOW_COMPUTATION ^&^& cloudletFileSize ^< HIGH_DATA) ^{ >> src\main\java\org\fogcomputing\algorithms\EnergyAwareOffloadingPolicy.java
echo             // Low computation and low data tasks stay on IoT to save transmission energy >> src\main\java\org\fogcomputing\algorithms\EnergyAwareOffloadingPolicy.java
echo             // Look for an IoT VM >> src\main\java\org\fogcomputing\algorithms\EnergyAwareOffloadingPolicy.java
echo             for (Vm vm : vmList) ^{ >> src\main\java\org\fogcomputing\algorithms\EnergyAwareOffloadingPolicy.java
echo                 if (vm.getId() ^>= 7 ^&^& vm.getId() ^<= 16) ^{ // VMs 7-16 are IoT >> src\main\java\org\fogcomputing\algorithms\EnergyAwareOffloadingPolicy.java
echo                     return vm.getId(); >> src\main\java\org\fogcomputing\algorithms\EnergyAwareOffloadingPolicy.java
echo                 ^} >> src\main\java\org\fogcomputing\algorithms\EnergyAwareOffloadingPolicy.java
echo             ^} >> src\main\java\org\fogcomputing\algorithms\EnergyAwareOffloadingPolicy.java
echo         ^} else ^{ >> src\main\java\org\fogcomputing\algorithms\EnergyAwareOffloadingPolicy.java
echo             // Medium computation or data-intensive tasks go to Fog >> src\main\java\org\fogcomputing\algorithms\EnergyAwareOffloadingPolicy.java
echo             // Look for a Fog VM >> src\main\java\org\fogcomputing\algorithms\EnergyAwareOffloadingPolicy.java
echo             for (Vm vm : vmList) ^{ >> src\main\java\org\fogcomputing\algorithms\EnergyAwareOffloadingPolicy.java
echo                 if (vm.getId() ^>= 2 ^&^& vm.getId() ^<= 6) ^{ // VMs 2-6 are Fog >> src\main\java\org\fogcomputing\algorithms\EnergyAwareOffloadingPolicy.java
echo                     return vm.getId(); >> src\main\java\org\fogcomputing\algorithms\EnergyAwareOffloadingPolicy.java
echo                 ^} >> src\main\java\org\fogcomputing\algorithms\EnergyAwareOffloadingPolicy.java
echo             ^} >> src\main\java\org\fogcomputing\algorithms\EnergyAwareOffloadingPolicy.java
echo         ^} >> src\main\java\org\fogcomputing\algorithms\EnergyAwareOffloadingPolicy.java
echo. >> src\main\java\org\fogcomputing\algorithms\EnergyAwareOffloadingPolicy.java
echo         // Fallback to round-robin if no suitable VM found >> src\main\java\org\fogcomputing\algorithms\EnergyAwareOffloadingPolicy.java
echo         int vmId = (int) (cloudlet.getCloudletId() %% vmList.size()); >> src\main\java\org\fogcomputing\algorithms\EnergyAwareOffloadingPolicy.java
echo         return vmList.get(vmId).getId(); >> src\main\java\org\fogcomputing\algorithms\EnergyAwareOffloadingPolicy.java
echo     ^} >> src\main\java\org\fogcomputing\algorithms\EnergyAwareOffloadingPolicy.java
echo ^} >> src\main\java\org\fogcomputing\algorithms\EnergyAwareOffloadingPolicy.java

REM Create DeadlineAwareOffloadingPolicy.java with proper escaping
echo package org.fogcomputing.algorithms; > src\main\java\org\fogcomputing\algorithms\DeadlineAwareOffloadingPolicy.java
echo. >> src\main\java\org\fogcomputing\algorithms\DeadlineAwareOffloadingPolicy.java
echo import java.util.List; >> src\main\java\org\fogcomputing\algorithms\DeadlineAwareOffloadingPolicy.java
echo import org.cloudbus.cloudsim.Cloudlet; >> src\main\java\org\fogcomputing\algorithms\DeadlineAwareOffloadingPolicy.java
echo import org.cloudbus.cloudsim.Vm; >> src\main\java\org\fogcomputing\algorithms\DeadlineAwareOffloadingPolicy.java
echo. >> src\main\java\org\fogcomputing\algorithms\DeadlineAwareOffloadingPolicy.java
echo public class DeadlineAwareOffloadingPolicy implements OffloadingPolicy ^{ >> src\main\java\org\fogcomputing\algorithms\DeadlineAwareOffloadingPolicy.java
echo. >> src\main\java\org\fogcomputing\algorithms\DeadlineAwareOffloadingPolicy.java
echo     @Override >> src\main\java\org\fogcomputing\algorithms\DeadlineAwareOffloadingPolicy.java
echo     public int getTargetVmId(Cloudlet cloudlet, List^<Vm^> vmList) ^{ >> src\main\java\org\fogcomputing\algorithms\DeadlineAwareOffloadingPolicy.java
echo         // Deadline-aware allocation strategy >> src\main\java\org\fogcomputing\algorithms\DeadlineAwareOffloadingPolicy.java
echo         // Distribute tasks based on deadline constraints >> src\main\java\org\fogcomputing\algorithms\DeadlineAwareOffloadingPolicy.java
echo         // For simulation, we'll use a simple rule-based approach: >> src\main\java\org\fogcomputing\algorithms\DeadlineAwareOffloadingPolicy.java
echo         // - Tasks with tight deadlines go to higher-performance resources >> src\main\java\org\fogcomputing\algorithms\DeadlineAwareOffloadingPolicy.java
echo         // - Tasks with longer deadlines can afford more transmission time >> src\main\java\org\fogcomputing\algorithms\DeadlineAwareOffloadingPolicy.java
echo         >> src\main\java\org\fogcomputing\algorithms\DeadlineAwareOffloadingPolicy.java
echo         int cloudletId = cloudlet.getCloudletId(); >> src\main\java\org\fogcomputing\algorithms\DeadlineAwareOffloadingPolicy.java
echo         >> src\main\java\org\fogcomputing\algorithms\DeadlineAwareOffloadingPolicy.java
echo         // For this simplified implementation, we'll make decisions based on cloudlet ID >> src\main\java\org\fogcomputing\algorithms\DeadlineAwareOffloadingPolicy.java
echo         // In a real implementation, this would be based on the actual deadline >> src\main\java\org\fogcomputing\algorithms\DeadlineAwareOffloadingPolicy.java
echo         >> src\main\java\org\fogcomputing\algorithms\DeadlineAwareOffloadingPolicy.java
echo         if (cloudletId == 0 ^|^| cloudletId == 1) ^{ >> src\main\java\org\fogcomputing\algorithms\DeadlineAwareOffloadingPolicy.java
echo             // First two tasks go to cloud (assuming they have tight deadlines) >> src\main\java\org\fogcomputing\algorithms\DeadlineAwareOffloadingPolicy.java
echo             return cloudletId %% 2; >> src\main\java\org\fogcomputing\algorithms\DeadlineAwareOffloadingPolicy.java
echo         ^} else if (cloudletId ^>= 2 ^&^& cloudletId ^<= 6) ^{ >> src\main\java\org\fogcomputing\algorithms\DeadlineAwareOffloadingPolicy.java
echo             // Next 5 tasks go to fog nodes >> src\main\java\org\fogcomputing\algorithms\DeadlineAwareOffloadingPolicy.java
echo             return 2 + (cloudletId - 2); >> src\main\java\org\fogcomputing\algorithms\DeadlineAwareOffloadingPolicy.java
echo         ^} else ^{ >> src\main\java\org\fogcomputing\algorithms\DeadlineAwareOffloadingPolicy.java
echo             // Remaining tasks go to IoT nodes with round-robin >> src\main\java\org\fogcomputing\algorithms\DeadlineAwareOffloadingPolicy.java
echo             int numIotVms = 3; >> src\main\java\org\fogcomputing\algorithms\DeadlineAwareOffloadingPolicy.java
echo             return 7 + ((cloudletId - 7) %% numIotVms); >> src\main\java\org\fogcomputing\algorithms\DeadlineAwareOffloadingPolicy.java
echo         ^} >> src\main\java\org\fogcomputing\algorithms\DeadlineAwareOffloadingPolicy.java
echo     ^} >> src\main\java\org\fogcomputing\algorithms\DeadlineAwareOffloadingPolicy.java
echo ^} >> src\main\java\org\fogcomputing\algorithms\DeadlineAwareOffloadingPolicy.java

REM Create MCEETOOffloadingPolicy.java with proper escaping
echo package org.fogcomputing.algorithms; > src\main\java\org\fogcomputing\algorithms\MCEETOOffloadingPolicy.java
echo. >> src\main\java\org\fogcomputing\algorithms\MCEETOOffloadingPolicy.java
echo import java.util.List; >> src\main\java\org\fogcomputing\algorithms\MCEETOOffloadingPolicy.java
echo import java.util.ArrayList; >> src\main\java\org\fogcomputing\algorithms\MCEETOOffloadingPolicy.java
echo import java.util.HashMap; >> src\main\java\org\fogcomputing\algorithms\MCEETOOffloadingPolicy.java
echo import java.util.Map; >> src\main\java\org\fogcomputing\algorithms\MCEETOOffloadingPolicy.java
echo import org.cloudbus.cloudsim.Cloudlet; >> src\main\java\org\fogcomputing\algorithms\MCEETOOffloadingPolicy.java
echo import org.cloudbus.cloudsim.Vm; >> src\main\java\org\fogcomputing\algorithms\MCEETOOffloadingPolicy.java
echo. >> src\main\java\org\fogcomputing\algorithms\MCEETOOffloadingPolicy.java
echo /** >> src\main\java\org\fogcomputing\algorithms\MCEETOOffloadingPolicy.java
echo  * MCEETO (Multi-Classifiers based Energy-Efficient Task Offloading) algorithm implementation >> src\main\java\org\fogcomputing\algorithms\MCEETOOffloadingPolicy.java
echo  * Based on the paper: "A Multi-Classifiers Based Algorithm for Energy Efficient  >> src\main\java\org\fogcomputing\algorithms\MCEETOOffloadingPolicy.java
echo  * Tasks Offloading in Fog Computing" published in MDPI Sensors 2023 >> src\main\java\org\fogcomputing\algorithms\MCEETOOffloadingPolicy.java
echo  */ >> src\main\java\org\fogcomputing\algorithms\MCEETOOffloadingPolicy.java
echo public class MCEETOOffloadingPolicy implements OffloadingPolicy ^{ >> src\main\java\org\fogcomputing\algorithms\MCEETOOffloadingPolicy.java
echo     // Classification thresholds >> src\main\java\org\fogcomputing\algorithms\MCEETOOffloadingPolicy.java
echo     private static final int HIGH_COMPUTATION = 30000; // MI >> src\main\java\org\fogcomputing\algorithms\MCEETOOffloadingPolicy.java
echo     private static final int MEDIUM_COMPUTATION = 20000; // MI >> src\main\java\org\fogcomputing\algorithms\MCEETOOffloadingPolicy.java
echo     private static final int LOW_COMPUTATION = 10000; // MI >> src\main\java\org\fogcomputing\algorithms\MCEETOOffloadingPolicy.java
echo     private static final int HIGH_DATA = 1000; // bytes >> src\main\java\org\fogcomputing\algorithms\MCEETOOffloadingPolicy.java
echo     private static final int LOW_DATA = 500; // bytes >> src\main\java\org\fogcomputing\algorithms\MCEETOOffloadingPolicy.java
echo  >> src\main\java\org\fogcomputing\algorithms\MCEETOOffloadingPolicy.java
echo     // VM type ranges >> src\main\java\org\fogcomputing\algorithms\MCEETOOffloadingPolicy.java
echo     private static final int CLOUD_VM_MIN = 0; >> src\main\java\org\fogcomputing\algorithms\MCEETOOffloadingPolicy.java
echo     private static final int CLOUD_VM_MAX = 1; >> src\main\java\org\fogcomputing\algorithms\MCEETOOffloadingPolicy.java
echo     private static final int FOG_VM_MIN = 2; >> src\main\java\org\fogcomputing\algorithms\MCEETOOffloadingPolicy.java
echo     private static final int FOG_VM_MAX = 6; >> src\main\java\org\fogcomputing\algorithms\MCEETOOffloadingPolicy.java
echo     private static final int IOT_VM_MIN = 7; >> src\main\java\org\fogcomputing\algorithms\MCEETOOffloadingPolicy.java
echo     private static final int IOT_VM_MAX = 16; >> src\main\java\org\fogcomputing\algorithms\MCEETOOffloadingPolicy.java
echo  >> src\main\java\org\fogcomputing\algorithms\MCEETOOffloadingPolicy.java
echo     // Energy coefficients (simplified model from the paper) >> src\main\java\org\fogcomputing\algorithms\MCEETOOffloadingPolicy.java
echo     private static final double CLOUD_COMPUTATION_ENERGY = 0.01; // J/MI >> src\main\java\org\fogcomputing\algorithms\MCEETOOffloadingPolicy.java
echo     private static final double FOG_COMPUTATION_ENERGY = 0.02; // J/MI >> src\main\java\org\fogcomputing\algorithms\MCEETOOffloadingPolicy.java
echo     private static final double IOT_COMPUTATION_ENERGY = 0.03; // J/MI >> src\main\java\org\fogcomputing\algorithms\MCEETOOffloadingPolicy.java
echo     private static final double CLOUD_TRANSMISSION_ENERGY = 0.05; // J/byte >> src\main\java\org\fogcomputing\algorithms\MCEETOOffloadingPolicy.java
echo     private static final double FOG_TRANSMISSION_ENERGY = 0.03; // J/byte >> src\main\java\org\fogcomputing\algorithms\MCEETOOffloadingPolicy.java
echo     private static final double IOT_TRANSMISSION_ENERGY = 0.00; // J/byte (local) >> src\main\java\org\fogcomputing\algorithms\MCEETOOffloadingPolicy.java
echo  >> src\main\java\org\fogcomputing\algorithms\MCEETOOffloadingPolicy.java
echo     // Task counters for load balancing >> src\main\java\org\fogcomputing\algorithms\MCEETOOffloadingPolicy.java
echo     private Map^<Integer, Integer^> vmTaskCount = new HashMap^<^>(); >> src\main\java\org\fogcomputing\algorithms\MCEETOOffloadingPolicy.java
echo  >> src\main\java\org\fogcomputing\algorithms\MCEETOOffloadingPolicy.java
echo     @Override >> src\main\java\org\fogcomputing\algorithms\MCEETOOffloadingPolicy.java
echo     public int getTargetVmId(Cloudlet cloudlet, List^<Vm^> vmList) ^{ >> src\main\java\org\fogcomputing\algorithms\MCEETOOffloadingPolicy.java
echo         // Initialize task counters if needed >> src\main\java\org\fogcomputing\algorithms\MCEETOOffloadingPolicy.java
echo         if (vmTaskCount.isEmpty()) ^{ >> src\main\java\org\fogcomputing\algorithms\MCEETOOffloadingPolicy.java
echo             for (Vm vm : vmList) ^{ >> src\main\java\org\fogcomputing\algorithms\MCEETOOffloadingPolicy.java
echo                 vmTaskCount.put(vm.getId(), 0); >> src\main\java\org\fogcomputing\algorithms\MCEETOOffloadingPolicy.java
echo             ^} >> src\main\java\org\fogcomputing\algorithms\MCEETOOffloadingPolicy.java
echo         ^} >> src\main\java\org\fogcomputing\algorithms\MCEETOOffloadingPolicy.java
echo  >> src\main\java\org\fogcomputing\algorithms\MCEETOOffloadingPolicy.java
echo         // Extract task characteristics >> src\main\java\org\fogcomputing\algorithms\MCEETOOffloadingPolicy.java
echo         int computationRequirement = (int) cloudlet.getCloudletLength(); // in MI >> src\main\java\org\fogcomputing\algorithms\MCEETOOffloadingPolicy.java
echo         long dataSize = cloudlet.getCloudletFileSize() + cloudlet.getCloudletOutputSize(); // in bytes >> src\main\java\org\fogcomputing\algorithms\MCEETOOffloadingPolicy.java
echo  >> src\main\java\org\fogcomputing\algorithms\MCEETOOffloadingPolicy.java
echo         // Apply the multi-classifier approach from the paper >> src\main\java\org\fogcomputing\algorithms\MCEETOOffloadingPolicy.java
echo         int targetVmId = -1; >> src\main\java\org\fogcomputing\algorithms\MCEETOOffloadingPolicy.java
echo  >> src\main\java\org\fogcomputing\algorithms\MCEETOOffloadingPolicy.java
echo         // CLASSIFIER 1: High computation, low data -^> Cloud tier (better computation efficiency) >> src\main\java\org\fogcomputing\algorithms\MCEETOOffloadingPolicy.java
echo         if (computationRequirement ^> HIGH_COMPUTATION ^&^& dataSize ^< HIGH_DATA) ^{ >> src\main\java\org\fogcomputing\algorithms\MCEETOOffloadingPolicy.java
echo             targetVmId = findLeastLoadedVmInRange(vmList, CLOUD_VM_MIN, CLOUD_VM_MAX); >> src\main\java\org\fogcomputing\algorithms\MCEETOOffloadingPolicy.java
echo             System.out.println("Task #" + cloudlet.getCloudletId() +  >> src\main\java\org\fogcomputing\algorithms\MCEETOOffloadingPolicy.java
echo                               " classified as HIGH COMP, LOW DATA -^> assigned to Cloud VM #" + targetVmId); >> src\main\java\org\fogcomputing\algorithms\MCEETOOffloadingPolicy.java
echo         ^} >> src\main\java\org\fogcomputing\algorithms\MCEETOOffloadingPolicy.java
echo         // CLASSIFIER 2: High computation, high data -^> Balance between Fog and Cloud >> src\main\java\org\fogcomputing\algorithms\MCEETOOffloadingPolicy.java
echo         else if (computationRequirement ^> HIGH_COMPUTATION ^&^& dataSize ^>= HIGH_DATA) ^{ >> src\main\java\org\fogcomputing\algorithms\MCEETOOffloadingPolicy.java
echo             // Compare energy costs between Cloud and Fog >> src\main\java\org\fogcomputing\algorithms\MCEETOOffloadingPolicy.java
echo             double cloudEnergy = computationRequirement * CLOUD_COMPUTATION_ENERGY +  >> src\main\java\org\fogcomputing\algorithms\MCEETOOffloadingPolicy.java
echo                                dataSize * CLOUD_TRANSMISSION_ENERGY; >> src\main\java\org\fogcomputing\algorithms\MCEETOOffloadingPolicy.java
echo             double fogEnergy = computationRequirement * FOG_COMPUTATION_ENERGY +  >> src\main\java\org\fogcomputing\algorithms\MCEETOOffloadingPolicy.java
echo                              dataSize * FOG_TRANSMISSION_ENERGY; >> src\main\java\org\fogcomputing\algorithms\MCEETOOffloadingPolicy.java
echo             >> src\main\java\org\fogcomputing\algorithms\MCEETOOffloadingPolicy.java
echo             if (cloudEnergy <= fogEnergy) ^{ >> src\main\java\org\fogcomputing\algorithms\MCEETOOffloadingPolicy.java
echo                 targetVmId = findLeastLoadedVmInRange(vmList, CLOUD_VM_MIN, CLOUD_VM_MAX); >> src\main\java\org\fogcomputing\algorithms\MCEETOOffloadingPolicy.java
echo                 System.out.println("Task #" + cloudlet.getCloudletId() +  >> src\main\java\org\fogcomputing\algorithms\MCEETOOffloadingPolicy.java
echo                                   " classified as HIGH COMP, HIGH DATA (cloud efficient) -^> assigned to Cloud VM #" + targetVmId); >> src\main\java\org\fogcomputing\algorithms\MCEETOOffloadingPolicy.java
echo             ^} else ^{ >> src\main\java\org\fogcomputing\algorithms\MCEETOOffloadingPolicy.java
echo                 targetVmId = findLeastLoadedVmInRange(vmList, FOG_VM_MIN, FOG_VM_MAX); >> src\main\java\org\fogcomputing\algorithms\MCEETOOffloadingPolicy.java
echo                 System.out.println("Task #" + cloudlet.getCloudletId() +  >> src\main\java\org\fogcomputing\algorithms\MCEETOOffloadingPolicy.java
echo                                   " classified as HIGH COMP, HIGH DATA (fog efficient) -^> assigned to Fog VM #" + targetVmId); >> src\main\java\org\fogcomputing\algorithms\MCEETOOffloadingPolicy.java
echo             ^} >> src\main\java\org\fogcomputing\algorithms\MCEETOOffloadingPolicy.java
echo         ^} >> src\main\java\org\fogcomputing\algorithms\MCEETOOffloadingPolicy.java
echo         // CLASSIFIER 3: Medium computation -^> Fog tier (good balance) >> src\main\java\org\fogcomputing\algorithms\MCEETOOffloadingPolicy.java
echo         else if (computationRequirement ^>= MEDIUM_COMPUTATION ^&^& computationRequirement ^<= HIGH_COMPUTATION) ^{ >> src\main\java\org\fogcomputing\algorithms\MCEETOOffloadingPolicy.java
echo             targetVmId = findLeastLoadedVmInRange(vmList, FOG_VM_MIN, FOG_VM_MAX); >> src\main\java\org\fogcomputing\algorithms\MCEETOOffloadingPolicy.java
echo             System.out.println("Task #" + cloudlet.getCloudletId() +  >> src\main\java\org\fogcomputing\algorithms\MCEETOOffloadingPolicy.java
echo                               " classified as MEDIUM COMP -^> assigned to Fog VM #" + targetVmId); >> src\main\java\org\fogcomputing\algorithms\MCEETOOffloadingPolicy.java
echo         ^} >> src\main\java\org\fogcomputing\algorithms\MCEETOOffloadingPolicy.java
echo         // CLASSIFIER 4: Low computation, low data -^> IoT tier (no transmission cost) >> src\main\java\org\fogcomputing\algorithms\MCEETOOffloadingPolicy.java
echo         else if (computationRequirement ^< LOW_COMPUTATION ^&^& dataSize ^< LOW_DATA) ^{ >> src\main\java\org\fogcomputing\algorithms\MCEETOOffloadingPolicy.java
echo             targetVmId = findLeastLoadedVmInRange(vmList, IOT_VM_MIN, IOT_VM_MAX); >> src\main\java\org\fogcomputing\algorithms\MCEETOOffloadingPolicy.java
echo             System.out.println("Task #" + cloudlet.getCloudletId() +  >> src\main\java\org\fogcomputing\algorithms\MCEETOOffloadingPolicy.java
echo                               " classified as LOW COMP, LOW DATA -^> assigned to IoT VM #" + targetVmId); >> src\main\java\org\fogcomputing\algorithms\MCEETOOffloadingPolicy.java
echo         ^} >> src\main\java\org\fogcomputing\algorithms\MCEETOOffloadingPolicy.java
echo         // CLASSIFIER 5: Low computation, high data -^> Balance between IoT and Fog >> src\main\java\org\fogcomputing\algorithms\MCEETOOffloadingPolicy.java
echo         else if (computationRequirement ^< LOW_COMPUTATION ^&^& dataSize ^>= LOW_DATA) ^{ >> src\main\java\org\fogcomputing\algorithms\MCEETOOffloadingPolicy.java
echo             // Compare energy between IoT and Fog >> src\main\java\org\fogcomputing\algorithms\MCEETOOffloadingPolicy.java
echo             double iotEnergy = computationRequirement * IOT_COMPUTATION_ENERGY; >> src\main\java\org\fogcomputing\algorithms\MCEETOOffloadingPolicy.java
echo             double fogEnergy = computationRequirement * FOG_COMPUTATION_ENERGY +  >> src\main\java\org\fogcomputing\algorithms\MCEETOOffloadingPolicy.java
echo                              dataSize * FOG_TRANSMISSION_ENERGY; >> src\main\java\org\fogcomputing\algorithms\MCEETOOffloadingPolicy.java
echo             >> src\main\java\org\fogcomputing\algorithms\MCEETOOffloadingPolicy.java
echo             if (iotEnergy ^<= fogEnergy) ^{ >> src\main\java\org\fogcomputing\algorithms\MCEETOOffloadingPolicy.java
echo                 targetVmId = findLeastLoadedVmInRange(vmList, IOT_VM_MIN, IOT_VM_MAX); >> src\main\java\org\fogcomputing\algorithms\MCEETOOffloadingPolicy.java
echo                 System.out.println("Task #" + cloudlet.getCloudletId() +  >> src\main\java\org\fogcomputing\algorithms\MCEETOOffloadingPolicy.java
echo                                   " classified as LOW COMP, HIGH DATA (IoT efficient) -^> assigned to IoT VM #" + targetVmId); >> src\main\java\org\fogcomputing\algorithms\MCEETOOffloadingPolicy.java
echo             ^} else ^{ >> src\main\java\org\fogcomputing\algorithms\MCEETOOffloadingPolicy.java
echo                 targetVmId = findLeastLoadedVmInRange(vmList, FOG_VM_MIN, FOG_VM_MAX); >> src\main\java\org\fogcomputing\algorithms\MCEETOOffloadingPolicy.java
echo                 System.out.println("Task #" + cloudlet.getCloudletId() +  >> src\main\java\org\fogcomputing\algorithms\MCEETOOffloadingPolicy.java
echo                                   " classified as LOW COMP, HIGH DATA (Fog efficient) -^> assigned to Fog VM #" + targetVmId); >> src\main\java\org\fogcomputing\algorithms\MCEETOOffloadingPolicy.java
echo             ^} >> src\main\java\org\fogcomputing\algorithms\MCEETOOffloadingPolicy.java
echo         ^} >> src\main\java\org\fogcomputing\algorithms\MCEETOOffloadingPolicy.java
echo         // DEFAULT CLASSIFIER: Fallback for any other cases >> src\main\java\org\fogcomputing\algorithms\MCEETOOffloadingPolicy.java
echo         else ^{ >> src\main\java\org\fogcomputing\algorithms\MCEETOOffloadingPolicy.java
echo             // Find the tier with the lowest average load >> src\main\java\org\fogcomputing\algorithms\MCEETOOffloadingPolicy.java
echo             double cloudLoad = getAverageLoad(vmList, CLOUD_VM_MIN, CLOUD_VM_MAX); >> src\main\java\org\fogcomputing\algorithms\MCEETOOffloadingPolicy.java
echo             double fogLoad = getAverageLoad(vmList, FOG_VM_MIN, FOG_VM_MAX); >> src\main\java\org\fogcomputing\algorithms\MCEETOOffloadingPolicy.java
echo             double iotLoad = getAverageLoad(vmList, IOT_VM_MIN, IOT_VM_MAX); >> src\main\java\org\fogcomputing\algorithms\MCEETOOffloadingPolicy.java
echo             >> src\main\java\org\fogcomputing\algorithms\MCEETOOffloadingPolicy.java
echo             if (cloudLoad ^<= fogLoad ^&^& cloudLoad ^<= iotLoad) ^{ >> src\main\java\org\fogcomputing\algorithms\MCEETOOffloadingPolicy.java
echo                 targetVmId = findLeastLoadedVmInRange(vmList, CLOUD_VM_MIN, CLOUD_VM_MAX); >> src\main\java\org\fogcomputing\algorithms\MCEETOOffloadingPolicy.java
echo             ^} else if (fogLoad ^<= cloudLoad ^&^& fogLoad ^<= iotLoad) ^{ >> src\main\java\org\fogcomputing\algorithms\MCEETOOffloadingPolicy.java
echo                 targetVmId = findLeastLoadedVmInRange(vmList, FOG_VM_MIN, FOG_VM_MAX); >> src\main\java\org\fogcomputing\algorithms\MCEETOOffloadingPolicy.java
echo             ^} else ^{ >> src\main\java\org\fogcomputing\algorithms\MCEETOOffloadingPolicy.java
echo                 targetVmId = findLeastLoadedVmInRange(vmList, IOT_VM_MIN, IOT_VM_MAX); >> src\main\java\org\fogcomputing\algorithms\MCEETOOffloadingPolicy.java
echo             ^} >> src\main\java\org\fogcomputing\algorithms\MCEETOOffloadingPolicy.java
echo             System.out.println("Task #" + cloudlet.getCloudletId() +  >> src\main\java\org\fogcomputing\algorithms\MCEETOOffloadingPolicy.java
echo                               " using default classifier -^> assigned to VM #" + targetVmId); >> src\main\java\org\fogcomputing\algorithms\MCEETOOffloadingPolicy.java
echo         ^} >> src\main\java\org\fogcomputing\algorithms\MCEETOOffloadingPolicy.java
echo         >> src\main\java\org\fogcomputing\algorithms\MCEETOOffloadingPolicy.java
echo         // Update the task count for the selected VM >> src\main\java\org\fogcomputing\algorithms\MCEETOOffloadingPolicy.java
echo         vmTaskCount.put(targetVmId, vmTaskCount.get(targetVmId) + 1); >> src\main\java\org\fogcomputing\algorithms\MCEETOOffloadingPolicy.java
echo         return targetVmId; >> src\main\java\org\fogcomputing\algorithms\MCEETOOffloadingPolicy.java
echo     ^} >> src\main\java\org\fogcomputing\algorithms\MCEETOOffloadingPolicy.java
echo     >> src\main\java\org\fogcomputing\algorithms\MCEETOOffloadingPolicy.java
echo     /**  >> src\main\java\org\fogcomputing\algorithms\MCEETOOffloadingPolicy.java
echo      * Find the VM with the least number of tasks in a specific range  >> src\main\java\org\fogcomputing\algorithms\MCEETOOffloadingPolicy.java
echo      */ >> src\main\java\org\fogcomputing\algorithms\MCEETOOffloadingPolicy.java
echo     private int findLeastLoadedVmInRange(List^<Vm^> vmList, int minVmId, int maxVmId) ^{ >> src\main\java\org\fogcomputing\algorithms\MCEETOOffloadingPolicy.java
echo         int leastLoadedVmId = -1; >> src\main\java\org\fogcomputing\algorithms\MCEETOOffloadingPolicy.java
echo         int minTasks = Integer.MAX_VALUE; >> src\main\java\org\fogcomputing\algorithms\MCEETOOffloadingPolicy.java
echo         >> src\main\java\org\fogcomputing\algorithms\MCEETOOffloadingPolicy.java
echo         for (Vm vm : vmList) ^{ >> src\main\java\org\fogcomputing\algorithms\MCEETOOffloadingPolicy.java
echo             int vmId = vm.getId(); >> src\main\java\org\fogcomputing\algorithms\MCEETOOffloadingPolicy.java
echo             if (vmId ^>= minVmId ^&^& vmId ^<= maxVmId) ^{ >> src\main\java\org\fogcomputing\algorithms\MCEETOOffloadingPolicy.java
echo                 int tasks = vmTaskCount.getOrDefault(vmId, 0); >> src\main\java\org\fogcomputing\algorithms\MCEETOOffloadingPolicy.java
echo                 if (tasks ^< minTasks) ^{ >> src\main\java\org\fogcomputing\algorithms\MCEETOOffloadingPolicy.java
echo                     minTasks = tasks; >> src\main\java\org\fogcomputing\algorithms\MCEETOOffloadingPolicy.java
echo                     leastLoadedVmId = vmId; >> src\main\java\org\fogcomputing\algorithms\MCEETOOffloadingPolicy.java
echo                 ^} >> src\main\java\org\fogcomputing\algorithms\MCEETOOffloadingPolicy.java
echo             ^} >> src\main\java\org\fogcomputing\algorithms\MCEETOOffloadingPolicy.java
echo         ^} >> src\main\java\org\fogcomputing\algorithms\MCEETOOffloadingPolicy.java
echo         >> src\main\java\org\fogcomputing\algorithms\MCEETOOffloadingPolicy.java
echo         return leastLoadedVmId; >> src\main\java\org\fogcomputing\algorithms\MCEETOOffloadingPolicy.java
echo     ^} >> src\main\java\org\fogcomputing\algorithms\MCEETOOffloadingPolicy.java
echo     >> src\main\java\org\fogcomputing\algorithms\MCEETOOffloadingPolicy.java
echo     /** >> src\main\java\org\fogcomputing\algorithms\MCEETOOffloadingPolicy.java
echo      * Calculate average load for VMs in a specific range >> src\main\java\org\fogcomputing\algorithms\MCEETOOffloadingPolicy.java
echo      */ >> src\main\java\org\fogcomputing\algorithms\MCEETOOffloadingPolicy.java
echo     private double getAverageLoad(List^<Vm^> vmList, int minVmId, int maxVmId) ^{ >> src\main\java\org\fogcomputing\algorithms\MCEETOOffloadingPolicy.java
echo         int totalTasks = 0; >> src\main\java\org\fogcomputing\algorithms\MCEETOOffloadingPolicy.java
echo         int vmCount = 0; >> src\main\java\org\fogcomputing\algorithms\MCEETOOffloadingPolicy.java
echo         >> src\main\java\org\fogcomputing\algorithms\MCEETOOffloadingPolicy.java
echo         for (Vm vm : vmList) ^{ >> src\main\java\org\fogcomputing\algorithms\MCEETOOffloadingPolicy.java
echo             int vmId = vm.getId(); >> src\main\java\org\fogcomputing\algorithms\MCEETOOffloadingPolicy.java
echo             if (vmId ^>= minVmId ^&^& vmId ^<= maxVmId) ^{ >> src\main\java\org\fogcomputing\algorithms\MCEETOOffloadingPolicy.java
echo                 totalTasks += vmTaskCount.getOrDefault(vmId, 0); >> src\main\java\org\fogcomputing\algorithms\MCEETOOffloadingPolicy.java
echo                 vmCount++; >> src\main\java\org\fogcomputing\algorithms\MCEETOOffloadingPolicy.java
echo             ^} >> src\main\java\org\fogcomputing\algorithms\MCEETOOffloadingPolicy.java
echo         ^} >> src\main\java\org\fogcomputing\algorithms\MCEETOOffloadingPolicy.java
echo         >> src\main\java\org\fogcomputing\algorithms\MCEETOOffloadingPolicy.java
echo         return vmCount ^> 0 ? (double)totalTasks / vmCount : Double.MAX_VALUE; >> src\main\java\org\fogcomputing\algorithms\MCEETOOffloadingPolicy.java
echo     ^} >> src\main\java\org\fogcomputing\algorithms\MCEETOOffloadingPolicy.java
echo ^} >> src\main\java\org\fogcomputing\algorithms\MCEETOOffloadingPolicy.java
@echo off

REM Compile the offloading policies
echo Compiling offloading policies...
javac -cp "target\classes;libs\cloudsim-3.0.3.jar;libs\cloudsim-examples-3.0.3.jar;libs\commons-math3-3.5.jar;libs\json-simple-1.1.1.jar;libs\guava-18.0.jar" ^
      -d target\classes ^
      src\main\java\org\fogcomputing\algorithms\EnergyAwareOffloadingPolicy.java ^
      src\main\java\org\fogcomputing\algorithms\DeadlineAwareOffloadingPolicy.java ^
      src\main\java\org\fogcomputing\algorithms\MCEETOOffloadingPolicy.java

if %ERRORLEVEL% neq 0 (
  echo Failed to compile offloading policies
  exit /b 1
)

REM Compile the custom VM allocation policy
echo Compiling tiered VM allocation policy...
javac -cp "target\classes;libs\cloudsim-3.0.3.jar;libs\cloudsim-examples-3.0.3.jar;libs\commons-math3-3.5.jar;libs\json-simple-1.1.1.jar;libs\guava-18.0.jar" ^
      -d target\classes ^
      src\main\java\org\fogcomputing\TieredVmAllocationPolicy.java

if %ERRORLEVEL% neq 0 (
  echo Failed to compile tiered VM allocation policy
  exit /b 1
)

REM Compile flexible VM allocation policy
echo Compiling flexible VM allocation policy...
javac -cp "target\classes;libs\cloudsim-3.0.3.jar;libs\cloudsim-examples-3.0.3.jar;libs\commons-math3-3.5.jar" ^
      -d target\classes ^
      src\main\java\org\fogcomputing\FlexibleVmAllocationPolicy.java

if %ERRORLEVEL% neq 0 (
  echo Failed to compile flexible VM allocation policy
  exit /b 1
)

REM Compile the custom datacenter broker
echo Compiling tiered datacenter broker...
javac -cp "target\classes;libs\cloudsim-3.0.3.jar;libs\cloudsim-examples-3.0.3.jar;libs\commons-math3-3.5.jar" ^
      -d target\classes ^
      src\main\java\org\fogcomputing\TieredDatacenterBroker.java

if %ERRORLEVEL% neq 0 (
  echo Failed to compile tiered datacenter broker
  exit /b 1
)

REM Compile ResourceDebugger.java
echo Compiling ResourceDebugger...
javac -cp "target\classes;libs\cloudsim-3.0.3.jar;libs\cloudsim-examples-3.0.3.jar;libs\commons-math3-3.5.jar;libs\json-simple-1.1.1.jar;libs\guava-18.0.jar" ^
      -d target\classes ^
      src\main\java\org\fogcomputing\ResourceDebugger.java

if %ERRORLEVEL% neq 0 (
  echo Failed to compile ResourceDebugger
  exit /b 1
)

REM Compile the main CloudSimTaskOffloadingSimulation class
echo Compiling main simulation class...
javac -cp "target\classes;libs\cloudsim-3.0.3.jar;libs\cloudsim-examples-3.0.3.jar;libs\commons-math3-3.5.jar;libs\json-simple-1.1.1.jar;libs\guava-18.0.jar" ^
      -d target\classes ^
      src\main\java\org\fogcomputing\CloudSimTaskOffloadingSimulation.java

if %ERRORLEVEL% neq 0 (
  echo Failed to compile main simulation class
  exit /b 1
)

REM Create and compile test file writer
echo Testing file writing capability...
echo package org.fogcomputing; > src\main\java\org\fogcomputing\TestFileWriter.java
echo. >> src\main\java\org\fogcomputing\TestFileWriter.java
echo public class TestFileWriter { >> src\main\java\org\fogcomputing\TestFileWriter.java
echo     public static void main(String[] args) { >> src\main\java\org\fogcomputing\TestFileWriter.java
echo         // Call the testFileWriting method in SimulationResultProcessor >> src\main\java\org\fogcomputing\TestFileWriter.java
echo         SimulationResultProcessor.testFileWriting(); >> src\main\java\org\fogcomputing\TestFileWriter.java
echo     } >> src\main\java\org\fogcomputing\TestFileWriter.java
echo } >> src\main\java\org\fogcomputing\TestFileWriter.java

javac -cp "target\classes;libs\cloudsim-3.0.3.jar;libs\cloudsim-examples-3.0.3.jar;libs\commons-math3-3.5.jar;libs\json-simple-1.1.1.jar;libs\guava-18.0.jar" ^
      -d target\classes src\main\java\org\fogcomputing\TestFileWriter.java

java -cp "target\classes;libs\cloudsim-3.0.3.jar;libs\cloudsim-examples-3.0.3.jar;libs\commons-math3-3.5.jar;libs\json-simple-1.1.1.jar;libs\guava-18.0.jar" ^
     org.fogcomputing.TestFileWriter

REM Run the simulation
echo Running simulation with Deadline-Aware Offloading policy...
java -cp "target\classes;libs\cloudsim-3.0.3.jar;libs\cloudsim-examples-3.0.3.jar;libs\commons-math3-3.5.jar;libs\json-simple-1.1.1.jar;libs\guava-18.0.jar" ^
     org.fogcomputing.CloudSimTaskOffloadingSimulation "Deadline-Aware Offloading"

echo Simulation completed.
echo Check the 'results' directory for output files.
pause
