/*
 * Copyright (c) 2012, 2014 Moreno Marzolla
 *
 * This program is free software; you can redistribute it and/or modify
 * it under the terms of the GNU Lesser General Public License version 2 as
 * published by the Free Software Foundation.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public License
 * along with this program; if not, write to the Free Software
 * Foundation, Inc., 675 Mass Ave, Cambridge, MA 02139, USA.
 *
 */

package lnu.mida.controller.energy;

import java.util.ArrayList;

import com.lajv.location.Location;

import lnu.mida.entity.GeneralNode;
import lnu.mida.entity.Service;
import lnu.mida.protocol.OverloadApplication;
import lnu.mida.protocol.OverloadComponentAssembly;
import peersim.config.*;
import peersim.core.*;

public class EnergyController implements Control {

	// ///////////////////////////////////////////////////////////////////////
	// Constants
	// ///////////////////////////////////////////////////////////////////////

	/**
	 * The component assambly protocol.
	 */
	private static final String VIV_PROT = "viv_prot";

	/**
	 * The component assambly protocol.
	 */
	private static final String COMP_PROT = "comp_prot";

	/**
	 * The application protocol.
	 */
	private static final String APPL_PROT = "appl_prot";

	// ///////////////////////////////////////////////////////////////////////
	// Fields
	// ///////////////////////////////////////////////////////////////////////

	/**
	 * The name of this observer in the configuration file. Initialized by the
	 * constructor parameter.
	 */
	private final String name;

	private final int vivaldi_pid;
	private final int component_assembly_pid;
	private final int application_pid;

	public static int failed_connections;
	public static int number_of_experiences;
	public static double total_experiences_value;

	public static double variance;
	public static double stdDev;

	//public double Rtot;

	// ///////////////////////////////////////////////////////////////////////
	// Constructor
	// ///////////////////////////////////////////////////////////////////////
	/**
	 * Standard constructor that reads the configuration parameters. Invoked by the
	 * simulation engine.
	 * 
	 * @param name the configuration prefix for this class.
	 */
	public EnergyController(String name) { 
		this.name = name;
		vivaldi_pid = Configuration.getPid(name + "." + VIV_PROT);
		component_assembly_pid = Configuration.getPid(name + "." + COMP_PROT);
		application_pid = Configuration.getPid(name + "." + APPL_PROT);
	}

	// ///////////////////////////////////////////////////////////////////////
	// Methods
	// ///////////////////////////////////////////////////////////////////////

	@Override
	public boolean execute() {

		
		
		// non calcolo al round 0 (nessun bind)
		if (CommonState.getIntTime() == 0)
			return false;

		int alive_nodes=0;
		int fully_resolved=0;
		// For every service in a node calculates the individual energy comsumption
		double meanR=0;
		
		for (int i = 0; i < Network.size(); i++) {

			
			if (!Network.get(i).isUp()) {
				continue;
			}

			alive_nodes++;
			
			GeneralNode node = (GeneralNode) Network.get(i);
			OverloadComponentAssembly ca = (OverloadComponentAssembly) node.getProtocol(component_assembly_pid);
			OverloadApplication appl = (OverloadApplication) node.getProtocol(application_pid);
			
			
			ArrayList<Service> services = ca.getServices();
					
			double R=0;
			
			// Navigates all the services hosted on a node
			for (Service service : services) {

				service.updateLambdaTot();
				

				if (!service.isFullyResolved()) {
					continue;
				} else {
					// Consumed energy by services S
					
					fully_resolved++;
					/** 
					 * Computation
					 */
					double I_comp = node.getConsumedIndividualCPUEnergy(1);				
					double I_comp_lambda = node.getConsumedIndividualCPUEnergy(service.getLambdatoCPU());
	
					
					/** 
					 * Communication
					 */
					double I_comm = node.getConsumedIndividualCommEnergyReceiving(1);
					double I_comm_lambda = node.getConsumedIndividualCommEnergyReceiving(service.getLambda_t());

					Service[] listDepObj = service.getDependencies_obj();
					boolean[] listDep = service.getDependencies();
					
					// Navigate dependencies of a service
					for (int j = 0; j < listDep.length; j++) {

						boolean dep = listDep[j];
						if (dep == true) {

							Service depObj = listDepObj[j];
							GeneralNode receiverNode = GeneralNode.getNode(depObj.getNode_id());

							/**
							 * Control things that should not happen
							 */
							if (service.getType() == depObj.getType()) {
								System.err.println("Cannot have dependency on same type: Energy Controller");
								System.exit(0);
							}

							if (service.getService_id() == depObj.getService_id()) {
								System.err.println("Cannot have dependency on itself: Energy Controller");
								System.exit(0);
							}

							if (service.getNode_id() != node.getID()) {
								System.err.println("Service-node Id and Node must have same ID "+service.getNode_id()+" "+node.getID());
								System.exit(0);
							}

							if (depObj.getNode_id() != receiverNode.getID()) {
								System.err.println("depObj and receiverNode must have same ID " + depObj.getNode_id() + " "	+ receiverNode.getID());
								System.exit(0);
							}

							
							
							// If the the dependency of a service is NOT on the same node 
							if(service.getNode_id()!=depObj.getNode_id()) {
								
								Location senderLoc = node.getLocation();
								Location receiverLoc = receiverNode.getLocation();
								double latency = senderLoc.latency(receiverLoc);
								double lambda_to_receiver = service.getTransferFunctions()[depObj.getType()].calculate_tSd(service.getLambda_t());	
		
								
								I_comm+=node.getConsumedIndividualCommEnergySending(1, latency);
								I_comm_lambda+=node.getConsumedIndividualCommEnergySending(lambda_to_receiver, latency);

								
							}
						}
					}
					
					// single request
					service.setI_comp(I_comp);
					service.setI_comm(I_comm);
					
					
					// lambda dependent
					service.setI_comp_lambda(I_comp_lambda);
					service.setI_comm_lambda(I_comm_lambda);

					R+= I_comp_lambda+I_comm_lambda;
					
				}		
				
			}
			
			// set the energy consumption rate of node
			node.setR(R);
			meanR+=R;
			
		}

		return false;
	}
	
	

	public static double getVariance() {
		return variance;
	}

	public static double getStdDev() {
		return stdDev;
	}

}
