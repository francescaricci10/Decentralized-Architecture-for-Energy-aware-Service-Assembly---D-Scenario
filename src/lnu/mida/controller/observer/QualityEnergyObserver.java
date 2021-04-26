package lnu.mida.controller.observer;

import java.util.ArrayList;

import java.util.List;

import lnu.mida.entity.GeneralNode;
import lnu.mida.entity.Service;
import lnu.mida.protocol.OverloadComponentAssembly;
import peersim.cdsim.CDState;
import peersim.config.*;
import peersim.core.Control;
import peersim.core.Fallible;
import peersim.core.Network;
import peersim.util.*;

/**
 * I am an observer that prints, at each assembly step, the minimum, average and
 * maximum quality and energy of all fully resolved services.
 */
public class QualityEnergyObserver implements Control {

	// ///////////////////////////////////////////////////////////////////////
	// Constants
	// ///////////////////////////////////////////////////////////////////////

	/**
	 * The protocol to operate on.
	 * 
	 * @config
	 */
	private static final String PAR_PROT = "protocol";

	// ///////////////////////////////////////////////////////////////////////
	// Fields
	// ///////////////////////////////////////////////////////////////////////

	/**
	 * The name of this observer in the configuration file. Initialized by the
	 * constructor parameter.
	 */
	private final String name;

	/** Protocol identifier, obtained from config property {@link #PAR_PROT}. */
	private final int pid;

//	private double stopat;
//	private double minstopat;
	
	public int resolvedAssemblies=0;
	public int toResolveAssemblies=0;
	
	
	public double minEn=0;

	// ///////////////////////////////////////////////////////////////////////
	// Constructor
	// ///////////////////////////////////////////////////////////////////////

	/**
	 * Standard constructor that reads the configuration parameters. Invoked by the
	 * simulation engine.
	 * 
	 * @param name the configuration prefix for this class.
	 */
	public QualityEnergyObserver(String name) {
		this.name = name;
		pid = Configuration.getPid(name + "." + PAR_PROT);
	}

	// ///////////////////////////////////////////////////////////////////////
	// Methods
	// ///////////////////////////////////////////////////////////////////////

	@Override
	public boolean execute() {

		long time = peersim.core.CommonState.getTime();

		IncrementalStats quality = new IncrementalStats();
		IncrementalStats energy = new IncrementalStats();

		int fully_resolved_services = 0;
		int to_resolve_services=0;
		
		int nodes_up=0;
		
		double min_residual_life=Double.MAX_VALUE;
		
		
		toResolveAssemblies++;
				
		for (int i = 0; i < Network.size(); i++) {

			GeneralNode node = (GeneralNode) Network.get(i);
			OverloadComponentAssembly n = (OverloadComponentAssembly) node.getProtocol(pid);
			
			ArrayList<Service> services = n.getServices();
			
			if(Network.get(i).isUp())
				nodes_up++;

			for (Service service : services) {
				
				service.updateCompoundUtility();
				
				to_resolve_services++;
				
				service.updateCompoundUtility();
				
				if (service.isFullyResolved()) {
					fully_resolved_services++;
				}
				
				//if (service.isFullyResolved())
					quality.add(service.getEffectiveCU());

			}					

			double energyBalance = node.getG()-node.getR();

			 
			 if(energyBalance<minEn)
				 minEn=energyBalance;

			energy.add(energyBalance); 
			
			
		}
		
		
		if(to_resolve_services==fully_resolved_services)
			resolvedAssemblies++;


		int index = (int) ((time / Configuration.getInt("COMPOSITION_STEPS", 1)));

		// Quality
		FinalUtilityObserver.quality.get(index).add(quality.getAverage());
		IncrementalStats quality_jain_is = FinalUtilityObserver.quality_jain.get(index);
		// calculates the jain's fairness for quality
		double quality_jain_fairness =1-(2*quality.getStD()); // double quality_jain_fairness = Math.pow(quality.getSum(), 2) / (quality.getN() * quality.getSqrSum());
		
		quality_jain_is.add(quality_jain_fairness);

		// Energy
		FinalUtilityObserver.energy.get(index).add(energy.getAverage());
		IncrementalStats energy_jain_is = FinalUtilityObserver.energy_jain.get(index);
		
		// calculates the jain's fairness for energy
		double energy_jain_fairness =  1 - (2*energy.getStD()/8.5);   // calcola sperimentalmente il minimo che pu� raggiungere
		
		
		// double energy_jain_fairness = Math.pow(energy.getSum(), 2) / (energy.getN() * energy.getSqrSum());
		energy_jain_is.add(energy_jain_fairness);
		
		
		// Network		
		FinalUtilityObserver.networkSize.get(index).add(Network.size());
		FinalUtilityObserver.networkUpSize.get(index).add(nodes_up);
		
		// Availability		
		double availability = (double) resolvedAssemblies/(double) toResolveAssemblies;
		double availability_services = (double) fully_resolved_services/to_resolve_services;
		double availability_n1 = (double) nodes_up/Network.size();
		
		FinalUtilityObserver.availability.get(index).add(availability);
		FinalUtilityObserver.availability_s.get(index).add(availability_services);
		FinalUtilityObserver.availability_n1.get(index).add(availability_n1);

		
		return false;
	}

}