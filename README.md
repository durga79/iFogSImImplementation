# Fog Edge Task Offloading Simulation using CloudSim/iFogSim

This project implements a proof-of-concept prototype for task offloading in fog/edge computing environments using the CloudSim/iFogSim frameworks. It simulates IoT devices offloading computational tasks to fog nodes and cloud servers based on different offloading strategies, evaluating performance metrics such as execution time, energy consumption, and cost.

## Project Structure

```
iFogSimImplementation
├── libs/ (CloudSim and supporting libraries)
│   ├── cloudsim-3.0.3.jar (CloudSim core library)
│   ├── cloudsim-examples-3.0.3.jar (CloudSim examples)
│   ├── json-simple-1.1.1.jar (JSON processing library)
│   └── guava-18.0.jar (Google Guava library)
├── src/main/java/org/fogcomputing/
│   ├── CloudSimTaskOffloadingSimulation.java (Main simulation class)
│   ├── algorithms/
│   │   ├── OffloadingPolicy.java (Interface for offloading strategies)
│   │   ├── EnergyAwareOffloadingPolicy.java (Energy-efficient offloading strategy)
│   │   └── DeadlineAwareOffloadingPolicy.java (Deadline-aware offloading strategy)
├── target/classes/ (Compiled classes)
└── README.md (This file)
```

## Setup Instructions

### Prerequisites

- Java Development Kit (JDK) 8 or higher
- Apache Maven (optional, for dependency management)

### Installing CloudSim/iFogSim

1. Clone this repository:
   ```bash
   git clone https://github.com/yourusername/fog-edge-offloading.git
   cd FogEdgeComputing/iFogSimImplementation
   ```

2. Verify the required JAR files are in the libs directory:
   - cloudsim-3.0.3.jar
   - cloudsim-examples-3.0.3.jar
   - json-simple-1.1.1.jar
   - guava-18.0.jar

3. Create necessary directories if they don't exist:
   ```bash
   mkdir -p target/classes
   ```

### Compiling the Simulation

1. Compile the source files using the CloudSim libraries:
   ```bash
   javac -cp "libs/cloudsim-3.0.3.jar:libs/cloudsim-examples-3.0.3.jar:libs/json-simple-1.1.1.jar:libs/guava-18.0.jar:target/classes" -d target/classes src/main/java/org/fogcomputing/algorithms/OffloadingPolicy.java src/main/java/org/fogcomputing/algorithms/EnergyAwareOffloadingPolicy.java src/main/java/org/fogcomputing/algorithms/DeadlineAwareOffloadingPolicy.java src/main/java/org/fogcomputing/CloudSimTaskOffloadingSimulation.java
   ```

   For Windows:
   ```batch
   javac -cp "libs/cloudsim-3.0.3.jar;libs/cloudsim-examples-3.0.3.jar;libs/json-simple-1.1.1.jar;libs/guava-18.0.jar;target/classes" -d target/classes src/main/java/org/fogcomputing/algorithms/OffloadingPolicy.java src/main/java/org/fogcomputing/algorithms/EnergyAwareOffloadingPolicy.java src/main/java/org/fogcomputing/algorithms/DeadlineAwareOffloadingPolicy.java src/main/java/org/fogcomputing/CloudSimTaskOffloadingSimulation.java
   ```

### Running the Simulation

1. Run the simulation with the CloudSim framework:
   ```bash
   java -cp "target/classes:libs/cloudsim-3.0.3.jar:libs/cloudsim-examples-3.0.3.jar:libs/json-simple-1.1.1.jar:libs/guava-18.0.jar" org.fogcomputing.CloudSimTaskOffloadingSimulation
   ```

   For Windows:
   ```batch
   java -cp "target/classes;libs/cloudsim-3.0.3.jar;libs/cloudsim-examples-3.0.3.jar;libs/json-simple-1.1.1.jar;libs/guava-18.0.jar" org.fogcomputing.CloudSimTaskOffloadingSimulation
   ```

2. To run with a specific offloading policy (the simulation automatically uses both policies):
   ```bash
   # The simulation will run with both policies by default
   # Results for both will be displayed sequentially
   java -cp "target/classes:libs/cloudsim-3.0.3.jar:libs/cloudsim-examples-3.0.3.jar:libs/json-simple-1.1.1.jar:libs/guava-18.0.jar" org.fogcomputing.CloudSimTaskOffloadingSimulation
   ```

### For iFogSim Implementation (Alternative)

If you want to use iFogSim instead of CloudSim:

1. Download iFogSim from: https://github.com/Cloudslab/iFogSim

2. Replace the libs with iFogSim libraries:
   - iFogSim.jar
   - cloudsim-3.0.3.jar
   - commons-math3-3.5.jar
   - json-simple-1.1.1.jar

3. Compile using iFogSim libraries:
   ```bash
   javac -cp "libs/iFogSim.jar:libs/cloudsim-3.0.3.jar:libs/commons-math3-3.5.jar:libs/json-simple-1.1.1.jar:target/classes" -d target/classes src/main/java/org/fogcomputing/iFogTaskOffloadingSimulation.java
   ```

4. Run the iFogSim simulation:
   ```bash
   java -cp "target/classes:libs/iFogSim.jar:libs/cloudsim-3.0.3.jar:libs/commons-math3-3.5.jar:libs/json-simple-1.1.1.jar" org.fogcomputing.iFogTaskOffloadingSimulation
   ```

## Implemented Offloading Algorithms

### 1. Energy-Aware Offloading Algorithm

This algorithm prioritizes energy efficiency when making offloading decisions. It considers:
- Device energy consumption profiles
- Network transmission energy costs
- Current load on potential target devices
- Link latencies and bandwidth

It aims to minimize the total energy consumption while maintaining acceptable performance levels.

### 2. Deadline-Aware Offloading Algorithm

This algorithm prioritizes meeting task deadlines when making offloading decisions. It considers:
- Task computation requirements and deadlines
- Processing capabilities of available devices
- Network transmission times
- Current device loads

It aims to ensure tasks are completed before their deadlines while optimizing resource utilization.

## Simulation Environment

The simulation includes:
- **IoT Devices**: Resource-constrained end devices that generate tasks
- **Fog Nodes**: Mid-tier computing devices with moderate resources
- **Cloud Servers**: High-performance remote computing resources

The network topology is hierarchical with configurable latencies and bandwidths between different layers.

## Simulation Results and Output Interpretation

When you run the simulation, you'll see output similar to the following:

```
----- Simulation Results -----
Offloading Algorithm: Deadline-Aware Offloading
    Total Tasks Generated: 10
    Total Tasks Completed: 10
    Success Rate: 100.00%

    ========== Tasks ==========
        Task ID: 5, Status: SUCCESS, VM: IoT-12, Time: 13.87 ms, Transmission: 0 ms, Energy: 0.01 J
        Task ID: 6, Status: SUCCESS, VM: IoT-13, Time: 14.65 ms, Transmission: 0 ms, Energy: 0.01 J
        Task ID: 7, Status: SUCCESS, VM: IoT-14, Time: 16.01 ms, Transmission: 0 ms, Energy: 0.02 J
        Task ID: 0, Status: SUCCESS, VM: IoT-7, Time: 16.88 ms, Transmission: 0 ms, Energy: 0.02 J
        Task ID: 4, Status: SUCCESS, VM: IoT-11, Time: 17.62 ms, Transmission: 0 ms, Energy: 0.02 J
        Task ID: 2, Status: SUCCESS, VM: IoT-9, Time: 21.66 ms, Transmission: 0 ms, Energy: 0.02 J
        Task ID: 8, Status: SUCCESS, VM: IoT-15, Time: 24.47 ms, Transmission: 0 ms, Energy: 0.02 J
        Task ID: 1, Status: SUCCESS, VM: IoT-8, Time: 28.01 ms, Transmission: 0 ms, Energy: 0.03 J
        Task ID: 9, Status: SUCCESS, VM: IoT-16, Time: 31.05 ms, Transmission: 0 ms, Energy: 0.03 J
        Task ID: 3, Status: SUCCESS, VM: IoT-10, Time: 36.34 ms, Transmission: 0 ms, Energy: 0.04 J

    ========== Summary ==========
    Average Execution Time: 22.06 ms
    Average Transmission Time: 0 ms
    Average Energy Consumption: 0.02 J
    Total Cost: $21.82

    Task Distribution by Device Type:
    IoT: 10 tasks
```

### Output Explanation:

- **Total Tasks Generated/Completed**: Number of tasks created and successfully executed
- **Success Rate**: Percentage of tasks that completed successfully
- **Individual Task Results**:
  - **Task ID**: Unique identifier for the task
  - **Status**: Success or failure
  - **VM**: The virtual machine that executed the task
  - **Time**: Total execution time in milliseconds
  - **Transmission**: Time spent transmitting data (0 if executed locally)
  - **Energy**: Energy consumed in Joules
- **Summary Metrics**:
  - **Average Execution Time**: Mean time to execute all tasks
  - **Average Transmission Time**: Mean time spent on data transmission
  - **Average Energy Consumption**: Mean energy used per task
  - **Total Cost**: Estimated monetary cost of the computation
- **Task Distribution**: How tasks were distributed across device types

## Task Model

Tasks in the simulation are characterized by:
- Input data size (for transmission calculations)
- Output data size (for result transmission calculations)
- Computational length (in MI - Million Instructions)
- Deadline requirements
- Priority level

## Troubleshooting

### Common Issues and Solutions

1. **VM Allocation Failures**
   - *Problem*: VMs fail to be created with errors like "Allocation of VM failed by RAM/MIPS/storage"
   - *Solution*: Adjust host resource specifications (RAM, MIPS, storage) in the datacenter creation methods to be sufficient for the VM requirements

2. **Task Execution Failures**
   - *Problem*: Tasks show "Postponing execution: bound VM not available"
   - *Solution*: Ensure the offloading policies assign tasks only to VMs that were successfully created

3. **Compilation Errors**
   - *Problem*: "Cannot find symbol" or "package does not exist" errors
   - *Solution*: Verify all required libraries are in the classpath and the path syntax is correct for your OS

4. **Zero Success Rate**
   - *Problem*: Simulation completes but no tasks are successfully executed
   - *Solution*: Check VM allocation logs and ensure the offloading policy assigns tasks to valid VM IDs

## Conclusion

This simulation provides valuable insights into task offloading strategies in fog/edge computing environments. By comparing different offloading policies, we can evaluate the trade-offs between energy efficiency and meeting task deadlines.

The modular design allows for easy extension with new offloading policies and network topologies. Future work could explore machine learning-based offloading strategies, multi-objective optimization approaches, and more realistic network models.

## Author

This proof-of-concept implementation was developed as part of an academic project on fog edge computing systems.

## License

This project is provided for educational purposes.
# iFogSImImplementation
