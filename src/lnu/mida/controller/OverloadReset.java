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

package lnu.mida.controller;

import java.util.ArrayList;

import lnu.mida.entity.*;
import lnu.mida.entity.GeneralNode;
import lnu.mida.protocol.OverloadComponentAssembly;
import peersim.cdsim.CDState;
import peersim.config.*;
import peersim.core.*;


/**
 * I am an observer that observe
 */
public class OverloadReset implements Control {

	// ///////////////////////////////////////////////////////////////////////
	// Constants
	// ///////////////////////////////////////////////////////////////////////

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

	private final int	component_assembly_pid;
	private final int	application_pid;
	

	// ///////////////////////////////////////////////////////////////////////
	// Constructor
	// ///////////////////////////////////////////////////////////////////////
	/**
	 * Standard constructor that reads the configuration parameters. Invoked by
	 * the simulation engine.
	 * 
	 * @param name
	 *            the configuration prefix for this class.
	 */
	public OverloadReset(String name) {
		
		this.name = name;
		component_assembly_pid = Configuration.getPid(name + "." + COMP_PROT);
		application_pid = Configuration.getPid(name + "." + APPL_PROT);
	}

	// ///////////////////////////////////////////////////////////////////////
	// Methods
	// ///////////////////////////////////////////////////////////////////////

	@Override
	public boolean execute() {
		
		
			
		// reset the dependencies for the new round of composition
		for (int i = 0; i < Network.size(); i++) {	
			
			GeneralNode n = (GeneralNode) Network.get(i);		
			OverloadComponentAssembly ca = (OverloadComponentAssembly) n.getProtocol(component_assembly_pid);		
						
			n.resetCandidateServices();
			
		}
		
		

		
		ArrayList<Node> to_remove = new ArrayList<Node>();
		
		
		for (int i = 0; i < Network.size(); i++) {	
			
			GeneralNode n = (GeneralNode) Network.get(i);
			OverloadComponentAssembly ca = (OverloadComponentAssembly) n.getProtocol(component_assembly_pid);		

			// Nodes with no battery die
			if((n.getBattery()<0 || n.getBattery()==0) && n.getFailState()==0) {	
				
				n.setBattery(0);
				n.setR(0); 
				
				n.addMTBF(CDState.getCycle()-n.getLastFailure());
				n.addMTTF(CDState.getCycle()-n.getLastFailureEnd());
				
				
				n.setLastFailure(CDState.getCycle());
				
				to_remove.add(Network.get(i)); 

				
			}
			
			// down nodes with battery live
			if(n.getFailState()==2 && n.getBattery()>(0.2*n.getCapacity())) {

				n.setFailState(0);
				n.setLastFailureEnd(CDState.getCycle());
				n.addDowntimePeriod(CDState.getCycle()-n.getLastFailure());
				
			}
				
			double availability = (double) n.getChargeCycles()/(n.getChargeCycles()+n.getDischargeCycles());
			n.setAvailability(availability);

		}


		for(int j=0; j<to_remove.size(); j++) {
			Node to_del = to_remove.get(j);
			long id_to_del = to_del.getID();
			
			for (int i = 0; i < Network.size(); i++) {
				if(Network.get(i).getID()==id_to_del) {
					// si rimuovono tutte le dipendenze
					remove_links(i);
					Network.get(i).setFailState(2);
				}
			}
		}
		
		
		return false;
	}
	
	
	
	
	
	
	
	public void remove_links(int node_index) {
		
		// se qualche servizio ospitato dal nodo morto risolveva delle dipendenze, questi link vanno rimossi
		GeneralNode n = (GeneralNode) Network.get(node_index);		
		OverloadComponentAssembly ca = (OverloadComponentAssembly) n.getProtocol(component_assembly_pid);		
		
		ArrayList<Service> services_to_del = ca.getServices();
		for (Service service2 : services_to_del) {
			service2.reset();
		}				

		// si scorre tutta la rete
		for (int i = 0; i < Network.size(); i++) {
			GeneralNode node_to_check = (GeneralNode) Network.get(i);		
						
			// si scorrono tutti i servizi
			OverloadComponentAssembly ca_to_check = (OverloadComponentAssembly) node_to_check.getProtocol(component_assembly_pid);		
			ArrayList<Service> services_to_check = ca_to_check.getServices();

			for (Service service : services_to_check) {
				Service[] listDepObj = service.getDependencies_obj();
				boolean[] listDep = service.getDependencies();
				
				if(listDepObj==null)
					continue;
				
				// si scorre tutta la lista dei providers
				for (int j = 0; j < listDep.length; j++) {
					
					boolean dep = listDep[j];
					if (dep == true) {
						Service depObj = listDepObj[j];

						if(depObj==null)
							continue;
						
						// si cerca se ci sono link con il nodo morto
						for (Service s : services_to_del) {
							if(depObj==s) {
								service.unlinkDependency(s);
							}
						}
					}
				}
			}
			
		}
				
	}
	
	
}
