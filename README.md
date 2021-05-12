# Decentralized-Architecture-for-Energy-aware-Service-Assembly---D-Scenario
This repository contains the simulation code that implements a decentralized architecture to manage an energy-aware service assembly. 
This project extends the project predented in https://github.com/mi-da/Energy-Aware-Service-Assembly-Journal.

We suppose that each node of the network is powered two different energy sources: a "green" source, which means a renewable energy source, and a battery. 
In particular, we suppose that each node owns a 5x5cm solar panel and a rechargeable battery with capacity of 1200 mAh.


## **Instructions**

- Download the Java Project "Decentralized-Architecture-for-Energy-aware-Service-Assembly---D-Scenario" and import it in your IDE as a Java project
- Link the provided libraries in "ext-lib" to the project
- Input the program argument "configs/mida-assembly-config.txt" (i.e., the configuration file of PeerSim)
- The main class to run the experiments is "peersim.Simulator"

## **Configuration Parameters**

The file configs/mida-assembly-config.txt contains the configuration parameters for the simulation. The main parameters are:

NETWORK_SIZE: The number of nodes in the network

SERVICES_PER_NODE: The number of services hosted by each node

TYPES: Number of types of services

STRATEGY: The selection criteria that the nodes adopt. We implemented multiple strategies:
  1) random 
  2) green learning
  3) local energy
  4) fair energy
  5) max balance
  6) max green production


## **New Features**

The main features of this project are organized as follow:
- the initial settings of the battery are placed in src/lnu/mida/controller/init/OverloadComponentInitializer.java
- the update of the battery status is done in src/lnu/mida/controller/BatteryController.java
- the simulation of the green energy production is managed in src/lnu/mida/controller/SolarController.java
- The controller src/lnu/mida/controller/LinkController.java manages the bindings between services at the end of the gossiping procedure
- In src / lnu / mida / controller / OverloadReset.java we check the status of the nodes: nodes with no battery die (the node is set as unavailable and every link of its services is removed); dead nodes with an acceptable battery level become available again.
