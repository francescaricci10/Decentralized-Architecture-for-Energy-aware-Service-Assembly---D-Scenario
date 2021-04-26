package com.lajv.location;

import lnu.mida.entity.GeneralNode;
import peersim.core.Control;
import peersim.core.Network;
import peersim.core.Node;
import peersim.dynamics.NodeInitializer;

/**
 * Initializes the network with network coordinates
 */
public class LocationInitializer implements NodeInitializer, Control {


	private String prefix;

	// --------------------------------------------------------------------------
	// Initialization
	// --------------------------------------------------------------------------

	/**
	 * Standard constructor that reads the configuration parameters. Invoked by the simulation
	 * engine.
	 * 
	 * @param prefix
	 *            the configuration prefix for this class
	 */
	public LocationInitializer(String prefix) {
		this.prefix = prefix;
	}

	// --------------------------------------------------------------------------
	// Methods
	// --------------------------------------------------------------------------

	/**
	 * 
	 * @return always false
	 */
	@Override
	public boolean execute() {

		int numOfDistances = 0;
		double sumOfDistances = 0;
		for (int i = 0; i < Network.size(); i++) {
			GeneralNode node = (GeneralNode) Network.get(i);
			initialize(node);
			for (int j = i - 1; j >= 0; j--) {
				GeneralNode otherNode = (GeneralNode) Network.get(j);
				sumOfDistances += node.location.latency(otherNode.location);
				numOfDistances++;
			}
		}
		double avgDistance = sumOfDistances / numOfDistances;
		System.out.println(prefix + " : Average distance between nodes: " + avgDistance);
		return false;
	}

	/**
	 * @see peersim.dynamics.NodeInitializer#initialize(peersim.core.Node)
	 */
	@Override
	public void initialize(Node n) {
		GeneralNode netNode = (GeneralNode) n;
		netNode.location.randomize();
	}
}
