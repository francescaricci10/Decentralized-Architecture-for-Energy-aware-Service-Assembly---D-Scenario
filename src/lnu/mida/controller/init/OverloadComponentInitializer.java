/*
 * Copyright (c) 2012 Moreno Marzolla
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
 */

package lnu.mida.controller.init;

import java.util.ArrayList;
import lnu.mida.entity.EnergyReputation;
import lnu.mida.entity.GeneralNode;
import lnu.mida.entity.GreenReputation;
import lnu.mida.entity.QOSReputation;
import lnu.mida.entity.Service;
import lnu.mida.entityl.transferfunction.CustomTransferFunction;
import lnu.mida.entityl.transferfunction.TransferFunction;
import lnu.mida.protocol.OverloadApplication;
import lnu.mida.protocol.OverloadComponentAssembly;
import peersim.config.*;
import peersim.core.*;

public class OverloadComponentInitializer implements Control {

	// ------------------------------------------------------------------------
	// Parameters
	// ------------------------------------------------------------------------

	private static final String COMP_PROT = "comp_prot";

	private static final String APPL_PROT = "appl_prot";


	// ------------------------------------------------------------------------
	// Fields
	// ------------------------------------------------------------------------

	private final int component_assembly_pid;
	private final int application_assembly_pid;

	public static int lastTypeInjected;


	// ------------------------------------------------------------------------
	// Initialization
	// ------------------------------------------------------------------------
	/**
	 * Standard constructor that reads the configuration parameters. Invoked by the
	 * simulation engine.
	 * 
	 * @param prefix the configuration prefix for this class.
	 */
	public OverloadComponentInitializer(String prefix) {
		component_assembly_pid = Configuration.getPid(prefix + "." + COMP_PROT);
		application_assembly_pid = Configuration.getPid(prefix + "." + APPL_PROT);
		lastTypeInjected = 0;
	}

	// ------------------------------------------------------------------------
	// Methods
	// ------------------------------------------------------------------------

	@Override
	public boolean execute() {

		System.out.println("--- OverloadComponentInitizlizer--- ");

		int m = Configuration.getInt("M", 50000);
		QOSReputation.setM(m); // set the parameter m for the learner

		int types = Configuration.getInt("TYPES", 0);
		int services_per_node = Configuration.getInt("SERVICES_PER_NODE", 0);
		
		int max_services_per_type = Network.size()*services_per_node/types;
		
		
		System.out.println("types="+types+" max_services_per_type="+max_services_per_type);

		
		
		ArrayList<Integer> availableTypes = new ArrayList<Integer>();
		for (int i = 0; i < types; i++) {
			availableTypes.add(max_services_per_type);
		}
		
		
		for (int i = 0; i < Network.size(); i++) {


			GeneralNode n = (GeneralNode) Network.get(i);
			OverloadComponentAssembly ca = (OverloadComponentAssembly) n.getProtocol(component_assembly_pid);
			OverloadApplication appl = (OverloadApplication) n.getProtocol(application_assembly_pid);
					
			appl.reset();
			ca.reset();

			n.initCand();
			n.initRep();
			
			ArrayList<Integer> new_list = new ArrayList<Integer>();
			n.setMTBF(new_list);
			
			ArrayList<Integer> new_list2 = new ArrayList<Integer>();
			n.setMTTF(new_list2);
			
			ArrayList<Integer> new_list3 = new ArrayList<Integer>();
			n.setDowntimePeriods(new_list3);
			
			
			// Initialize the number of services with max_types
			for (int j = 0; j < services_per_node; j++) {

				Service s = new Service(types,application_assembly_pid);
				

				// set type of service
				int randomType = getRandomType(availableTypes);
				//System.out.println(randomType);
				s.setType(randomType);
				s.setNode_id((int) n.getID());
				//System.out.println(randomType);

				// sigma: type 0 get requests from external users
				if (randomType == 0) {
					s.setSigma(1.0);
					s.setLambda_t(1.0);
				}
				
				
				s.setTransfer_func_CPU(new CustomTransferFunction(15*Math.random()));

				/**
				 * Quality parameters
				 */

				// queue parameter
				s.setQueueParameter(CommonState.r.nextDouble());
				

				// curve parameter between 0.2 and 1
				double curveParameter = (CommonState.r.nextDouble() * 0.8) + 0.2; // double curveParameter = (Math.random()*0.8)+0.2;
				s.setCurveParameter(curveParameter);

				// declared utility
				s.setDeclared_utility(1);
				
				// setup transfer functions
				TransferFunction transfer_func[] = s.getTransferFunctions();
				for (int k = 0; k < types; k++) {
					transfer_func[k] = new CustomTransferFunction(15*CommonState.r.nextDouble()); // transfer_func[j] = new
																				  // CustomTransferFunction(0.2);
                                                                                  // transfer_func[j] = new UnityTransferFunction();
				}
				
				// add service to list of services in the node
				ca.getServices().add(s);

			}
	

			/**
			 * Energy parameters
			 */

			// set green energy generation rate (for Journal)
			double G = (1440 * CommonState.r.nextDouble());
			n.setG(0);
			//n.setG(0);
			
			
	        // set the Battery 
			
			int max = 6480; // batteria AAA da 1200 mAh
			int min = 6480;
			
			double capacity = min + (max - min) * CommonState.r.nextDouble();
			n.setBattery(capacity);
			n.setCapacity(capacity);
			
			//System.out.println(n.getBattery());

			n.setCPUConsumptionFactor(0.5+(1.5*CommonState.r.nextDouble()));
			n.setCommunicationConsumptionFactor(0.5+(1.5*CommonState.r.nextDouble()));
			
			/**
			 * Construct parameters
			 */
			
			

			for(long serv_num=0;serv_num<500;serv_num++)
				appl.getQoSReputations().add(new QOSReputation(serv_num));
			
			for(long nodes_num=0;nodes_num<100;nodes_num++)
				appl.getEnergyReputations().add(new EnergyReputation(nodes_num));

			for(long nodes_num=0;nodes_num<100;nodes_num++)
				appl.getGreenReputations().add(new GreenReputation(nodes_num));


		}
		
		
		return false;
	}

	public static int getRandomType(ArrayList<Integer> list) {

		int rnd = CommonState.r.nextInt(list.size());
		
		if(list.get(rnd)==0)
			return getRandomType(list);
		
		else
			list.set(rnd, list.get(rnd)-1);
	    return rnd;
	}

}
