@echo off
REM Script to build and run the CloudSim Task Offloading Simulation
REM Author: Cascade
REM Date: July 31, 2025
REM Modified: July 31, 2025 - Fixed batch script echo issues

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

REM Create MockFogClasses.java by writing the raw content line by line
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
echo     public FogDevice^(int id, String name^) ^{ >> src\main\java\org\fogcomputing\mock\MockFogClasses.java
echo         this.id = id; >> src\main\java\org\fogcomputing\mock\MockFogClasses.java
echo         this.name = name; >> src\main\java\org\fogcomputing\mock\MockFogClasses.java
echo         this.energyConsumption = 0.0; >> src\main\java\org\fogcomputing\mock\MockFogClasses.java
echo     ^} >> src\main\java\org\fogcomputing\mock\MockFogClasses.java
echo     >> src\main\java\org\fogcomputing\mock\MockFogClasses.java
echo     public String getName^(^) ^{ >> src\main\java\org\fogcomputing\mock\MockFogClasses.java
echo         return name; >> src\main\java\org\fogcomputing\mock\MockFogClasses.java
echo     ^} >> src\main\java\org\fogcomputing\mock\MockFogClasses.java
echo     >> src\main\java\org\fogcomputing\mock\MockFogClasses.java
echo     public double getEnergyConsumption^(^) ^{ >> src\main\java\org\fogcomputing\mock\MockFogClasses.java
echo         return energyConsumption; >> src\main\java\org\fogcomputing\mock\MockFogClasses.java
echo     ^} >> src\main\java\org\fogcomputing\mock\MockFogClasses.java
echo     >> src\main\java\org\fogcomputing\mock\MockFogClasses.java
echo     public int getId^(^) ^{ >> src\main\java\org\fogcomputing\mock\MockFogClasses.java
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

REM Create EnergyAwareOffloadingPolicy.java with proper escaping - using grouped echo approach
(echo package org.fogcomputing.algorithms;
 echo.
 echo import java.util.List;
 echo import org.cloudbus.cloudsim.Cloudlet;
 echo import org.cloudbus.cloudsim.Vm;
 echo.
 echo public class EnergyAwareOffloadingPolicy implements OffloadingPolicy {
 echo.
 echo     @Override
 echo     public int getTargetVmId(Cloudlet cloudlet, List<Vm> vmList) {
 echo         // Energy-aware allocation strategy 
 echo         // Distribute tasks to minimize energy consumption
 echo         // For simulation, we'll use a simple rule-based approach:
 echo         // - CPU-intensive tasks go to cloud (better energy efficiency for computation)
 echo         // - Data-intensive but less compute-heavy tasks go to fog (reduced transmission energy)
 echo         // Simple heuristic: check if the task has high MI/size ratio
 echo         double computeRatio = cloudlet.getCloudletLength() / (cloudlet.getCloudletFileSize() + 1);
 echo         
 echo         for (Vm vm : vmList) {
 echo             // Assign to cloud if compute-intensive
 echo             if (computeRatio > 100 && vm.getId() < 3) {
 echo                 return vm.getId();
 echo             }
 echo             
 echo             // Assign to fog if data-intensive
 echo             if (computeRatio < 50 && vm.getId() >= 3 && vm.getId() < 6) {
 echo                 return vm.getId();
 echo             }
 echo         }
 echo.
 echo         // Fallback to round-robin if no suitable VM found
 echo         int vmId = (int) (cloudlet.getCloudletId() % vmList.size());
 echo         return vmList.get(vmId).getId();
 echo     }
 echo }) > src\main\java\org\fogcomputing\algorithms\EnergyAwareOffloadingPolicy.java

REM Create DeadlineAwareOffloadingPolicy.java using grouped echo to prevent ECHO is off errors
(echo package org.fogcomputing.algorithms;
 echo.
 echo import java.util.List;
 echo import org.cloudbus.cloudsim.Cloudlet;
 echo import org.cloudbus.cloudsim.Vm;
 echo.
 echo public class DeadlineAwareOffloadingPolicy implements OffloadingPolicy {
 echo.
 echo     @Override
 echo     public int getTargetVmId(Cloudlet cloudlet, List<Vm> vmList) {
 echo         // Deadline-aware allocation strategy
 echo         // Distribute tasks based on deadline constraints
 echo         // For simulation, we'll use a simple rule:
 echo         // - Deadline-sensitive tasks go to more powerful VMs
 echo.
 echo         // Simple implementation for simulation purposes
 echo         long deadline = cloudlet.getDeadlineTime();
 echo         long length = cloudlet.getCloudletLength();
 echo.
 echo         // Mock priority based on cloudlet ID
 echo         // In a real implementation, this would be based on actual deadline
 echo         int priority = (int) (cloudlet.getCloudletId() % 3); // 0=high, 1=medium, 2=low
 echo.
 echo         for (Vm vm : vmList) {
 echo             // High priority to cloud (fastest)
 echo             if (priority == 0 && vm.getId() < 2) {
 echo                 return vm.getId();
 echo             }
 echo.
 echo             // Medium priority to fog
 echo             if (priority == 1 && vm.getId() >= 2 && vm.getId() < 7) {
 echo                 return vm.getId();
 echo             }
 echo.
 echo             // Low priority to IoT
 echo             if (priority == 2 && vm.getId() >= 7) {
 echo                 return vm.getId();
 echo             }
 echo         }
 echo.
 echo         // Fallback to round-robin if no suitable VM found
 echo         int vmId = (int) (cloudlet.getCloudletId() % vmList.size());
 echo         return vmList.get(vmId).getId();
 echo     }
 echo }) > src\main\java\org\fogcomputing\algorithms\DeadlineAwareOffloadingPolicy.java

REM Create MCEETOOffloadingPolicy.java using grouped echo approach to prevent ECHO is off errors
(echo package org.fogcomputing.algorithms;
 echo.
 echo import java.util.List;
 echo import java.util.ArrayList;
 echo import java.util.HashMap;
 echo import java.util.Map;
 echo import org.cloudbus.cloudsim.Cloudlet;
 echo import org.cloudbus.cloudsim.Vm;
 echo.
 echo /**
 echo  * MCEETO (Multi-Classifiers based Energy-Efficient Task Offloading) algorithm implementation
 echo  * Based on the paper: "A Multi-Classifiers Based Algorithm for Energy Efficient 
 echo  * Tasks Offloading in Fog Computing" published in MDPI Sensors 2023
 echo  */
 echo public class MCEETOOffloadingPolicy implements OffloadingPolicy {
 echo     // Classification thresholds
 echo     private static final int HIGH_COMPUTATION = 30000; // MI
 echo     private static final int MEDIUM_COMPUTATION = 20000; // MI
 echo     private static final int LOW_COMPUTATION = 10000; // MI
 echo     private static final int HIGH_DATA = 1000; // bytes
 echo     private static final int LOW_DATA = 500; // bytes
 echo 
 echo     // VM type ranges
 echo     private static final int CLOUD_VM_MIN = 0;
 echo     private static final int CLOUD_VM_MAX = 1;
 echo     private static final int FOG_VM_MIN = 2;
 echo     private static final int FOG_VM_MAX = 6;
 echo     private static final int IOT_VM_MIN = 7;
 echo     private static final int IOT_VM_MAX = 16;
 echo 
 echo     // Energy coefficients (simplified model from the paper)
 echo     private static final double CLOUD_COMPUTATION_ENERGY = 0.01; // J/MI
 echo     private static final double FOG_COMPUTATION_ENERGY = 0.02; // J/MI
 echo     private static final double IOT_COMPUTATION_ENERGY = 0.03; // J/MI
 echo     private static final double CLOUD_TRANSMISSION_ENERGY = 0.05; // J/byte
 echo     private static final double FOG_TRANSMISSION_ENERGY = 0.03; // J/byte
 echo     private static final double IOT_TRANSMISSION_ENERGY = 0.00; // J/byte (local)
 echo 
 echo     // Task counters for load balancing
 echo     private Map<Integer, Integer> vmTaskCount = new HashMap<>();
 echo 
 echo     @Override
 echo     public int getTargetVmId(Cloudlet cloudlet, List<Vm> vmList) {
 echo         // Initialize task counters if needed
 echo         if (vmTaskCount.isEmpty()) {
 echo             for (Vm vm : vmList) {
 echo                 vmTaskCount.put(vm.getId(), 0);
 echo             }
 echo         }
 echo 
 echo         // Extract task characteristics
 echo         int computationRequirement = (int) cloudlet.getCloudletLength(); // in MI
 echo         long dataSize = cloudlet.getCloudletFileSize() + cloudlet.getCloudletOutputSize(); // in bytes
 echo 
 echo         // Apply the multi-classifier approach from the paper
 echo         int targetVmId = -1;
 echo 
 echo         // CLASSIFIER 1: High computation, low data -> Cloud tier (better computation efficiency)
 echo         if (computationRequirement > HIGH_COMPUTATION && dataSize < HIGH_DATA) {
 echo             targetVmId = findLeastLoadedVmInRange(vmList, CLOUD_VM_MIN, CLOUD_VM_MAX);
 echo             System.out.println("Task #" + cloudlet.getCloudletId() + 
 echo                               " classified as HIGH COMP, LOW DATA -> assigned to Cloud VM #" + targetVmId);
 echo         }
 echo         // CLASSIFIER 2: High computation, high data -> Balance between Fog and Cloud
 echo         else if (computationRequirement > HIGH_COMPUTATION && dataSize >= HIGH_DATA) {
 echo             // Compare energy costs between Cloud and Fog
 echo             double cloudEnergy = computationRequirement * CLOUD_COMPUTATION_ENERGY + 
 echo                                dataSize * CLOUD_TRANSMISSION_ENERGY;
 echo             double fogEnergy = computationRequirement * FOG_COMPUTATION_ENERGY + 
 echo                              dataSize * FOG_TRANSMISSION_ENERGY;
 echo             
 echo             if (cloudEnergy <= fogEnergy) {
 echo                 targetVmId = findLeastLoadedVmInRange(vmList, CLOUD_VM_MIN, CLOUD_VM_MAX);
 echo                 System.out.println("Task #" + cloudlet.getCloudletId() + 
 echo                                   " classified as HIGH COMP, HIGH DATA (cloud efficient) -> assigned to Cloud VM #" + targetVmId);
 echo             } else {
 echo                 targetVmId = findLeastLoadedVmInRange(vmList, FOG_VM_MIN, FOG_VM_MAX);
 echo                 System.out.println("Task #" + cloudlet.getCloudletId() + 
 echo                                   " classified as HIGH COMP, HIGH DATA (fog efficient) -> assigned to Fog VM #" + targetVmId);
 echo             }
 echo         }
 echo         // CLASSIFIER 3: Medium computation -> Fog tier (good balance)
 echo         else if (computationRequirement >= MEDIUM_COMPUTATION && computationRequirement <= HIGH_COMPUTATION) {
 echo             targetVmId = findLeastLoadedVmInRange(vmList, FOG_VM_MIN, FOG_VM_MAX);
 echo             System.out.println("Task #" + cloudlet.getCloudletId() + 
 echo                               " classified as MEDIUM COMP -> assigned to Fog VM #" + targetVmId);
 echo         }
 echo         // CLASSIFIER 4: Low computation, low data -> IoT tier (no transmission cost)
 echo         else if (computationRequirement < LOW_COMPUTATION && dataSize < LOW_DATA) {
 echo             targetVmId = findLeastLoadedVmInRange(vmList, IOT_VM_MIN, IOT_VM_MAX);
 echo             System.out.println("Task #" + cloudlet.getCloudletId() + 
 echo                               " classified as LOW COMP, LOW DATA -> assigned to IoT VM #" + targetVmId);
 echo         }
 echo         // CLASSIFIER 5: Low computation, high data -> Balance between IoT and Fog
 echo         else if (computationRequirement < LOW_COMPUTATION && dataSize >= LOW_DATA) {
 echo             // Compare energy between IoT and Fog
 echo             double iotEnergy = computationRequirement * IOT_COMPUTATION_ENERGY;
 echo             double fogEnergy = computationRequirement * FOG_COMPUTATION_ENERGY + 
 echo                              dataSize * FOG_TRANSMISSION_ENERGY;
 echo             
 echo             if (iotEnergy <= fogEnergy) {
 echo                 targetVmId = findLeastLoadedVmInRange(vmList, IOT_VM_MIN, IOT_VM_MAX);
 echo                 System.out.println("Task #" + cloudlet.getCloudletId() + 
 echo                                   " classified as LOW COMP, HIGH DATA (IoT efficient) -> assigned to IoT VM #" + targetVmId);
 echo             } else {
 echo                 targetVmId = findLeastLoadedVmInRange(vmList, FOG_VM_MIN, FOG_VM_MAX);
 echo                 System.out.println("Task #" + cloudlet.getCloudletId() + 
 echo                                   " classified as LOW COMP, HIGH DATA (Fog efficient) -> assigned to Fog VM #" + targetVmId);
 echo             }
 echo         }
 echo         // DEFAULT CLASSIFIER: Fallback for any other cases
 echo         else {
 echo             // Find the tier with the lowest average load
 echo             double cloudLoad = getAverageLoad(vmList, CLOUD_VM_MIN, CLOUD_VM_MAX);
 echo             double fogLoad = getAverageLoad(vmList, FOG_VM_MIN, FOG_VM_MAX);
 echo             double iotLoad = getAverageLoad(vmList, IOT_VM_MIN, IOT_VM_MAX);
 echo             
 echo             if (cloudLoad <= fogLoad && cloudLoad <= iotLoad) {
 echo                 targetVmId = findLeastLoadedVmInRange(vmList, CLOUD_VM_MIN, CLOUD_VM_MAX);
 echo             } else if (fogLoad <= cloudLoad && fogLoad <= iotLoad) {
 echo                 targetVmId = findLeastLoadedVmInRange(vmList, FOG_VM_MIN, FOG_VM_MAX);
 echo             } else {
 echo                 targetVmId = findLeastLoadedVmInRange(vmList, IOT_VM_MIN, IOT_VM_MAX);
 echo             }
 echo             
 echo             System.out.println("Task #" + cloudlet.getCloudletId() + 
 echo                               " using default classification -> assigned to VM #" + targetVmId);
 echo         }
 echo         // Update the task count for the selected VM
 echo         vmTaskCount.put(targetVmId, vmTaskCount.get(targetVmId) + 1);
 echo         return targetVmId;
 echo     }
 echo 
 echo     /**
 echo      * Find the VM with the least number of tasks in a specific range
 echo      */
 echo     private int findLeastLoadedVmInRange(List<Vm> vmList, int minVmId, int maxVmId) {
 echo         int leastLoadedVmId = -1;
 echo         int minTasks = Integer.MAX_VALUE;
 echo         for (Vm vm : vmList) {
 echo             int vmId = vm.getId();
 echo             if (vmId >= minVmId && vmId <= maxVmId) {
 echo                 int tasks = vmTaskCount.getOrDefault(vmId, 0);
 echo                 if (tasks < minTasks) {
 echo                     minTasks = tasks;
 echo                     leastLoadedVmId = vmId;
 echo                 }
 echo             }
 echo         }
 echo         return leastLoadedVmId;
 echo     }
 echo 
 echo     /**
 echo      * Calculate average load for VMs in a specific range
 echo      */
 echo     private double getAverageLoad(List<Vm> vmList, int minVmId, int maxVmId) {
 echo         int totalTasks = 0;
 echo         int vmCount = 0;
 echo         for (Vm vm : vmList) {
 echo             int vmId = vm.getId();
 echo             if (vmId >= minVmId && vmId <= maxVmId) {
 echo                 totalTasks += vmTaskCount.getOrDefault(vmId, 0);
 echo                 vmCount++;
 echo             }
 echo         }
 echo         return vmCount > 0 ? (double)totalTasks / vmCount : Double.MAX_VALUE;
 echo     }
 echo }) > src\main\java\org\fogcomputing\algorithms\MCEETOOffloadingPolicy.java
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
echo public class TestFileWriter ^{ >> src\main\java\org\fogcomputing\TestFileWriter.java
echo     public static void main^(String[] args^) ^{ >> src\main\java\org\fogcomputing\TestFileWriter.java
echo         // Call the testFileWriting method in SimulationResultProcessor >> src\main\java\org\fogcomputing\TestFileWriter.java
echo         SimulationResultProcessor.testFileWriting^(^); >> src\main\java\org\fogcomputing\TestFileWriter.java
echo     ^} >> src\main\java\org\fogcomputing\TestFileWriter.java
echo ^} >> src\main\java\org\fogcomputing\TestFileWriter.java

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
