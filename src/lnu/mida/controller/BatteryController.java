package lnu.mida.controller;

import peersim.config.Configuration;
import peersim.core.Control;
import peersim.core.*;
import lnu.mida.entity.GeneralNode;
import lnu.mida.protocol.OverloadApplication;


public class BatteryController implements Control {

	/**
	 * The component assambly protocol.
	 */
	private static final String COMP_PROT = "comp_prot";

	/**
	 * The application protocol.
	 */
	private static final String APPL_PROT = "appl_prot";
	
	
	/**
	 * The name of this observer in the configuration file. Initialized by the
	 * constructor parameter.
	 */
	private final String name;

	private final int	component_assembly_pid;
	private final int	application_pid;
		
	public BatteryController(String name) {
		
		this.name = name;
		component_assembly_pid = Configuration.getPid(name + "." + COMP_PROT);
		application_pid = Configuration.getPid(name + "." + APPL_PROT);
	}
	
	
	@Override
	public boolean execute() {
		
		for (int i = 0; i < Network.size(); i++) {
			
			GeneralNode node = (GeneralNode) Network.get(i);
			OverloadApplication appl = (OverloadApplication) node.getProtocol(application_pid);

			// se il nodo è attivo
			if(node.getFailState()==0) {


				// se R>G la batteria si scarica
				if(node.getR()>node.getG()) {
					
					node.addDischargeCycle();

					double level = node.getBattery() - ( node.getR() - node.getG() );
					if(level<0)
						node.setBattery(0);
					else
						node.setBattery(level);
					
				}

				
				// se R<G la batteria si carica (il livello di carica NON può superare la capacità)
				if(node.getR()<node.getG()) {

					
					node.addChargeCycle();

					double charge = node.getG() - node.getR();
					
					if(node.getBattery()+charge<node.getCapacity())
						node.setBattery(node.getBattery()+charge);
					else
						node.setBattery(node.getCapacity());
				
				}

			}

			// se il nodo è inattivo la batteria continua a caricarsi
			if(node.getFailState()==2) {

				node.addChargeCycle();

				double charge = node.getG();
				
				if(node.getBattery()+charge<node.getCapacity())
					node.setBattery(node.getBattery()+charge);
				else
					node.setBattery(node.getCapacity());				
				
			}

			if(node.getHistoryCounter()<288)
				node.addSolarHistory(node.getG(), node.getHistoryCounter());

			if(node.getHistoryCounter()>285)
				appl.addGreenHistoryExperience(node, node.getG());

			node.addHistoryCounter();
			
		}

		return false;
	}
	

}
