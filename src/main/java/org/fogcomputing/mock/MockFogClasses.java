package org.fogcomputing.mock;

// This file provides mock classes to replace the missing iFogSim classes
// It allows the simulation to compile without the actual fog libraries

public class MockFogClasses {
    // Empty implementation - just to provide the classes needed for compilation
}

// Mock FogDevice class
class FogDevice {
    private String name;
    private double energyConsumption;
    private int id;
    
    public FogDevice(int id, String name) {
        this.id = id;
        this.name = name;
        this.energyConsumption = 0.0;
    }
    
    public String getName() {
        return name;
    }
    
    public double getEnergyConsumption() {
        return energyConsumption;
    }
    
    public int getId() {
        return id;
    }
}
