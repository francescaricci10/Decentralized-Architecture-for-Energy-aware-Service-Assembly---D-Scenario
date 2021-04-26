package lnu.mida.controller.observer;

import java.io.PrintStream;
import java.util.ArrayList;
import lnu.mida.controller.init.OverloadFileInitializer;
import peersim.config.Configuration;
import peersim.core.*;
import peersim.util.*;

/**
 * I am an observer that prints, at each timestep, the minimum, average and
 * maximum utility of all fully resolved services.
 */
public class FinalUtilityObserver implements Control {

	// ///////////////////////////////////////////////////////////////////////
	// Fields
	// ///////////////////////////////////////////////////////////////////////

	/**
	 * The name of this observer in the configuration file. Initialized by the
	 * constructor parameter.
	 */
	private final String name;

	// Quality
	public static ArrayList<IncrementalStats> quality;	
	public static ArrayList<IncrementalStats> quality_jain;
	
	
	// Energy	
	public static ArrayList<IncrementalStats> energy;
	public static ArrayList<IncrementalStats> energy_jain;
	
	// Network	
	public static ArrayList<IncrementalStats> networkSize;
	public static ArrayList<IncrementalStats> networkUpSize;
	
	// Availability	
	public static ArrayList<IncrementalStats> availability;	// in relazione alle assemblies completamente risolte
	public static ArrayList<IncrementalStats> availability_s;	// in relazione ai servizi completamente risolti
	public static ArrayList<IncrementalStats> availability_n1;	// in relazione al numero di nodi attivi


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
	public FinalUtilityObserver(String name) {
		this.name = name;
	}

	// ///////////////////////////////////////////////////////////////////////
	// Methods
	// ///////////////////////////////////////////////////////////////////////

	@Override
	public boolean execute() {
		
		int exp_number = OverloadFileInitializer.experiment_number;
		int total_exps = Configuration.getInt("simulation.experiments",1);

		
		// print data from last experiment
		if(exp_number==total_exps) {
			

			PrintStream ps = OverloadFileInitializer.getPs_final();
			
			int n = 1;
			for (IncrementalStats incrementalStats : quality.subList(1, quality.size() ) ) {
					
				int index = quality.indexOf(incrementalStats);
				
				// Quality
				double finalQuality = incrementalStats.getAverage();
				IncrementalStats quality_jain_is = quality_jain.get(index);		
				double finalQualityFairness = quality_jain_is.getAverage();
				
				// Energy
				IncrementalStats energy_is = energy.get(index);	
				IncrementalStats energy_jain_is = energy_jain.get(index);	
				
				double finalEnergy = energy_is.getAverage();
				double finalEnergyFairness = energy_jain_is.getAverage();
				
				// Network
				IncrementalStats nodesAlive_is = networkSize.get(index);	
				double nodesAlive = nodesAlive_is.getAverage();
				
				IncrementalStats nodesUp_is = networkUpSize.get(index);	
				double nodesUp = nodesUp_is.getAverage();
				
				// Availability
				IncrementalStats avail_is = availability.get(index);	
				double availability = avail_is.getAverage();
				
				IncrementalStats avail_s_is = availability_s.get(index);	
				double availability_s = avail_s_is.getAverage();
				
				IncrementalStats avail_n1_is = availability_n1.get(index);	
				double availability_n1 = avail_n1_is.getAverage();

				//System.out.println(finalQuality);
				/*1*/ps.print(n+" ");
				/*2*/ps.print(finalQuality+" ");
				/*3*/ps.print(finalQualityFairness+" ");
				/*4*/ps.print(finalEnergy+" ");
				/*5*/ps.print(finalEnergyFairness+" ");
				/*6*/ps.print(nodesAlive+" ");
				/*7*/ps.print(nodesUp+" ");
				/*8*/ps.print(availability+" ");	// assemblies fully resolved
				/*9*/ps.print(availability_s+" ");	// services fully resolved/tot services
				/*10*/ps.print(availability_n1+"\n");	// nodes up/Network size
	
				
				n+=1; // learning step

			}						
		}
		
		return false;
	}	
	
}