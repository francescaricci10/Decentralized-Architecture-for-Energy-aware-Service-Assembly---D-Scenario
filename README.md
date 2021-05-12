# Decentralized-Architecture-for-Energy-aware-Service-Assembly---D-Scenario
This repository contains the simulation code that implements a decentralized architecture to manage an energy-aware service assembly. 
We suppose that each node of the network is powered two different energy sources: a "green" source, which means a renewable energy source, and a battery. 
In particular, we suppose that each node owns a 5x5cm solar panel and a rechargeable battery with capacity of 1200 mAh.

The main features of this project are organized as follow:
- the initial settings of the battery are placed in src/lnu/mida/controller/init/OverloadComponentInitializer.java
- the update of the battery status is done in src/lnu/mida/controller/BatteryController.java
- the simulation of the green energy production is managed in src/lnu/mida/controller/SolarController.java

The controller src/lnu/mida/controller/LinkController.java manages the bindings between services at the end of the gossiping procedure.

In src / lnu / mida / controller / OverloadReset.java we check the status of the nodes: nodes with no battery die (the node is set as unavailable and every link of its services is removed); 
dead nodes with an acceptable battery level become available again.
