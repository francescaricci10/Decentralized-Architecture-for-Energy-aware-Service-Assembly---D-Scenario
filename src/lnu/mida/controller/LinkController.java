package lnu.mida.controller;

import java.util.ArrayList;
import lnu.mida.entity.CandidateServices;
import lnu.mida.entity.GeneralNode;
import lnu.mida.entity.Service;
import lnu.mida.protocol.OverloadApplication;
import lnu.mida.protocol.OverloadComponentAssembly;
import peersim.config.Configuration;
import peersim.core.Control;
import peersim.core.Network;



public class LinkController implements Control {
	
	
	
	
	/**
	 * The component assambly protocol.
	 */
	private static final String COMP_PROT = "comp_prot";

	/**
	 * The application protocol.
	 */
	private static final String APPL_PROT = "appl_prot";


	private final String name;

	private final int component_assembly_pid;
	private final int application_pid;

	private int services_per_node;

	
	
	
	public LinkController(String name) {
		this.name = name;
		component_assembly_pid = Configuration.getPid(name + "." + COMP_PROT);
		application_pid = Configuration.getPid(name + "." + APPL_PROT);
		services_per_node = Configuration.getInt("SERVICES_PER_NODE", 0);
	}
	
	
	
	@Override
	public boolean execute() {
							
		for (int i = 0; i < Network.size(); i++) {
			
			if (!Network.get(i).isUp()) {
				continue;
			}
			
			GeneralNode node = (GeneralNode) Network.get(i);
			OverloadComponentAssembly ca = (OverloadComponentAssembly) node.getProtocol(component_assembly_pid);
			OverloadApplication appl = (OverloadApplication) node.getProtocol(application_pid);
			
			
			ArrayList<Service> services = ca.getServices();
			CandidateServices candidates = ((GeneralNode) Network.get(i)).getCandidateServices();
						
			
			for (Service service : services) {
				
				boolean[] listDep = service.getDependencies();
				
				for (int j = 0; j < listDep.length; j++) {
					
					boolean dep = listDep[j];

					if (dep == true) {

						if(candidates.getCandidateServices(j).isEmpty())
							continue;
	
						Service to_link = appl.chooseByStrategy(candidates.getCandidateServices(j), node);
						
						if(to_link!=null) {						
							service.addLink(to_link);	

						service.checkChanges();
						
						}					
					}
				}
								
				service.updateCompoundUtility();
				
			}		
		}

		return false;
	}
	

}
