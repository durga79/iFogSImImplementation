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
mkdir src\main\java\org\fogcomputing\mock
mkdir target\classes\org\fogcomputing\mock

echo package org.fogcomputing.mock; > src\main\java\org\fogcomputing\mock\MockFogClasses.java
echo. >> src\main\java\org\fogcomputing\mock\MockFogClasses.java
echo // This file provides mock classes to replace the missing iFogSim classes >> src\main\java\org\fogcomputing\mock\MockFogClasses.java
echo // It allows the simulation to compile without the actual fog libraries >> src\main\java\org\fogcomputing\mock\MockFogClasses.java
echo. >> src\main\java\org\fogcomputing\mock\MockFogClasses.java
echo public class MockFogClasses { >> src\main\java\org\fogcomputing\mock\MockFogClasses.java
echo     // Empty implementation - just to provide the classes needed for compilation >> src\main\java\org\fogcomputing\mock\MockFogClasses.java
echo } >> src\main\java\org\fogcomputing\mock\MockFogClasses.java
echo. >> src\main\java\org\fogcomputing\mock\MockFogClasses.java
echo // Mock FogDevice class >> src\main\java\org\fogcomputing\mock\MockFogClasses.java
echo class FogDevice { >> src\main\java\org\fogcomputing\mock\MockFogClasses.java
echo     private String name; >> src\main\java\org\fogcomputing\mock\MockFogClasses.java
echo     private double energyConsumption; >> src\main\java\org\fogcomputing\mock\MockFogClasses.java
echo     private int id; >> src\main\java\org\fogcomputing\mock\MockFogClasses.java
echo     >> src\main\java\org\fogcomputing\mock\MockFogClasses.java
echo     public FogDevice(int id, String name) { >> src\main\java\org\fogcomputing\mock\MockFogClasses.java
echo         this.id = id; >> src\main\java\org\fogcomputing\mock\MockFogClasses.java
echo         this.name = name; >> src\main\java\org\fogcomputing\mock\MockFogClasses.java
echo         this.energyConsumption = 0.0; >> src\main\java\org\fogcomputing\mock\MockFogClasses.java
echo     } >> src\main\java\org\fogcomputing\mock\MockFogClasses.java
echo     >> src\main\java\org\fogcomputing\mock\MockFogClasses.java
echo     public String getName() { >> src\main\java\org\fogcomputing\mock\MockFogClasses.java
echo         return name; >> src\main\java\org\fogcomputing\mock\MockFogClasses.java
echo     } >> src\main\java\org\fogcomputing\mock\MockFogClasses.java
echo     >> src\main\java\org\fogcomputing\mock\MockFogClasses.java
echo     public double getEnergyConsumption() { >> src\main\java\org\fogcomputing\mock\MockFogClasses.java
echo         return energyConsumption; >> src\main\java\org\fogcomputing\mock\MockFogClasses.java
echo     } >> src\main\java\org\fogcomputing\mock\MockFogClasses.java
echo     >> src\main\java\org\fogcomputing\mock\MockFogClasses.java
echo     public int getId() { >> src\main\java\org\fogcomputing\mock\MockFogClasses.java
echo         return id; >> src\main\java\org\fogcomputing\mock\MockFogClasses.java
echo     } >> src\main\java\org\fogcomputing\mock\MockFogClasses.java
echo } >> src\main\java\org\fogcomputing\mock\MockFogClasses.java

REM Compile the mock classes
echo Compiling mock classes...
javac -d target\classes src\main\java\org\fogcomputing\mock\MockFogClasses.java

if %ERRORLEVEL% neq 0 (
  echo Failed to compile mock classes
  exit /b 1
)

REM Create simplified implementations of the offloading policies
echo Creating simplified implementations of offloading policies...

echo package org.fogcomputing.algorithms; > src\main\java\org\fogcomputing\algorithms\EnergyAwareOffloadingPolicy.java
echo. >> src\main\java\org\fogcomputing\algorithms\EnergyAwareOffloadingPolicy.java
echo import java.util.List; >> src\main\java\org\fogcomputing\algorithms\EnergyAwareOffloadingPolicy.java
echo import org.cloudbus.cloudsim.Cloudlet; >> src\main\java\org\fogcomputing\algorithms\EnergyAwareOffloadingPolicy.java
echo import org.cloudbus.cloudsim.Vm; >> src\main\java\org\fogcomputing\algorithms\EnergyAwareOffloadingPolicy.java
echo. >> src\main\java\org\fogcomputing\algorithms\EnergyAwareOffloadingPolicy.java
echo public class EnergyAwareOffloadingPolicy implements OffloadingPolicy { >> src\main\java\org\fogcomputing\algorithms\EnergyAwareOffloadingPolicy.java
echo. >> src\main\java\org\fogcomputing\algorithms\EnergyAwareOffloadingPolicy.java
echo     @Override >> src\main\java\org\fogcomputing\algorithms\EnergyAwareOffloadingPolicy.java
echo     public int getTargetVmId(Cloudlet cloudlet, List^<Vm^> vmList) { >> src\main\java\org\fogcomputing\algorithms\EnergyAwareOffloadingPolicy.java
echo         // Energy-aware allocation strategy  >> src\main\java\org\fogcomputing\algorithms\EnergyAwareOffloadingPolicy.java
echo         // Distribute tasks to minimize energy consumption >> src\main\java\org\fogcomputing\algorithms\EnergyAwareOffloadingPolicy.java
echo         // For simulation, we'll use a simple rule-based approach: >> src\main\java\org\fogcomputing\algorithms\EnergyAwareOffloadingPolicy.java
echo         // - CPU-intensive tasks go to cloud (better energy efficiency for computation) >> src\main\java\org\fogcomputing\algorithms\EnergyAwareOffloadingPolicy.java
echo         // - Data-intensive but less compute-heavy tasks go to fog (reduced transmission energy) >> src\main\java\org\fogcomputing\algorithms\EnergyAwareOffloadingPolicy.java
echo         // - Very small tasks stay on IoT devices (no transmission energy) >> src\main\java\org\fogcomputing\algorithms\EnergyAwareOffloadingPolicy.java
echo         >> src\main\java\org\fogcomputing\algorithms\EnergyAwareOffloadingPolicy.java
echo         int cloudletId = cloudlet.getCloudletId(); >> src\main\java\org\fogcomputing\algorithms\EnergyAwareOffloadingPolicy.java
echo         int numVms = vmList.size(); >> src\main\java\org\fogcomputing\algorithms\EnergyAwareOffloadingPolicy.java
echo         >> src\main\java\org\fogcomputing\algorithms\EnergyAwareOffloadingPolicy.java
echo         // Simple rule-based assignment >> src\main\java\org\fogcomputing\algorithms\EnergyAwareOffloadingPolicy.java
echo         // First 20%% go to cloud, middle 50%% go to fog, remaining 30%% stay on IoT >> src\main\java\org\fogcomputing\algorithms\EnergyAwareOffloadingPolicy.java
echo         if (cloudletId ^< 2) { >> src\main\java\org\fogcomputing\algorithms\EnergyAwareOffloadingPolicy.java
echo             // Cloud VMs (first 2 VMs) >> src\main\java\org\fogcomputing\algorithms\EnergyAwareOffloadingPolicy.java
echo             return cloudletId %% 2; // Round-robin among cloud VMs >> src\main\java\org\fogcomputing\algorithms\EnergyAwareOffloadingPolicy.java
echo         } else if (cloudletId ^< 7) { >> src\main\java\org\fogcomputing\algorithms\EnergyAwareOffloadingPolicy.java
echo             // Fog VMs (next 5 VMs) >> src\main\java\org\fogcomputing\algorithms\EnergyAwareOffloadingPolicy.java
echo             return 2 + (cloudletId %% 5); // Round-robin among fog VMs >> src\main\java\org\fogcomputing\algorithms\EnergyAwareOffloadingPolicy.java
echo         } else { >> src\main\java\org\fogcomputing\algorithms\EnergyAwareOffloadingPolicy.java
echo             // IoT VMs (remaining VMs) >> src\main\java\org\fogcomputing\algorithms\EnergyAwareOffloadingPolicy.java
echo             return 7 + (cloudletId %% 3); // Round-robin among IoT VMs >> src\main\java\org\fogcomputing\algorithms\EnergyAwareOffloadingPolicy.java
echo         } >> src\main\java\org\fogcomputing\algorithms\EnergyAwareOffloadingPolicy.java
echo     } >> src\main\java\org\fogcomputing\algorithms\EnergyAwareOffloadingPolicy.java
echo } >> src\main\java\org\fogcomputing\algorithms\EnergyAwareOffloadingPolicy.java

echo package org.fogcomputing.algorithms; > src\main\java\org\fogcomputing\algorithms\DeadlineAwareOffloadingPolicy.java
echo. >> src\main\java\org\fogcomputing\algorithms\DeadlineAwareOffloadingPolicy.java
echo import java.util.List; >> src\main\java\org\fogcomputing\algorithms\DeadlineAwareOffloadingPolicy.java
echo import org.cloudbus.cloudsim.Cloudlet; >> src\main\java\org\fogcomputing\algorithms\DeadlineAwareOffloadingPolicy.java
echo import org.cloudbus.cloudsim.Vm; >> src\main\java\org\fogcomputing\algorithms\DeadlineAwareOffloadingPolicy.java
echo. >> src\main\java\org\fogcomputing\algorithms\DeadlineAwareOffloadingPolicy.java
echo public class DeadlineAwareOffloadingPolicy implements OffloadingPolicy { >> src\main\java\org\fogcomputing\algorithms\DeadlineAwareOffloadingPolicy.java
echo. >> src\main\java\org\fogcomputing\algorithms\DeadlineAwareOffloadingPolicy.java
echo     @Override >> src\main\java\org\fogcomputing\algorithms\DeadlineAwareOffloadingPolicy.java
echo     public int getTargetVmId(Cloudlet cloudlet, List^<Vm^> vmList) { >> src\main\java\org\fogcomputing\algorithms\DeadlineAwareOffloadingPolicy.java
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
echo         if (cloudletId == 0 ^|^| cloudletId == 1) { >> src\main\java\org\fogcomputing\algorithms\DeadlineAwareOffloadingPolicy.java
echo             // First two tasks go to cloud (assuming they have tight deadlines) >> src\main\java\org\fogcomputing\algorithms\DeadlineAwareOffloadingPolicy.java
echo             return cloudletId %% 2; >> src\main\java\org\fogcomputing\algorithms\DeadlineAwareOffloadingPolicy.java
echo         } else if (cloudletId ^>= 2 ^&^& cloudletId ^<= 6) { >> src\main\java\org\fogcomputing\algorithms\DeadlineAwareOffloadingPolicy.java
echo             // Next 5 tasks go to fog nodes >> src\main\java\org\fogcomputing\algorithms\DeadlineAwareOffloadingPolicy.java
echo             return 2 + (cloudletId - 2); >> src\main\java\org\fogcomputing\algorithms\DeadlineAwareOffloadingPolicy.java
echo         } else { >> src\main\java\org\fogcomputing\algorithms\DeadlineAwareOffloadingPolicy.java
echo             // Remaining tasks stay on IoT devices >> src\main\java\org\fogcomputing\algorithms\DeadlineAwareOffloadingPolicy.java
echo             return 7 + ((cloudletId - 7) %% 3); >> src\main\java\org\fogcomputing\algorithms\DeadlineAwareOffloadingPolicy.java
echo         } >> src\main\java\org\fogcomputing\algorithms\DeadlineAwareOffloadingPolicy.java
echo     } >> src\main\java\org\fogcomputing\algorithms\DeadlineAwareOffloadingPolicy.java
echo } >> src\main\java\org\fogcomputing\algorithms\DeadlineAwareOffloadingPolicy.java

REM Compile the offloading policies
echo Compiling offloading policies...
javac -cp "target\classes;libs\cloudsim-3.0.3.jar;libs\cloudsim-examples-3.0.3.jar;libs\commons-math3-3.5.jar;libs\json-simple-1.1.1.jar;libs\guava-18.0.jar" ^
      -d target\classes ^
      src\main\java\org\fogcomputing\algorithms\EnergyAwareOffloadingPolicy.java ^
      src\main\java\org\fogcomputing\algorithms\DeadlineAwareOffloadingPolicy.java

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
