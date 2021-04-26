package lnu.mida.controller.init;

import peersim.config.*;
import peersim.core.*;

import java.util.ArrayList;

import lnu.mida.entity.Service;
import lnu.mida.protocol.OverloadComponentAssembly;

/**
 * I am an initializer which sets the dependencies for each node. I must be
 * activated <strong>after</strong> a type initializer has been applied. In
 * other words, I require that all components have already been assigned a type.
 * When I am applied to a component of type t, I define a dependency on types
 * {i+1, i+2, ... } with probability {@link prob} each.
 */
public class ProbDependencyInitializer implements Control {

	// ------------------------------------------------------------------------
	// Parameters
	// ------------------------------------------------------------------------

	/**
	 * The probability to add a given type as a dependency
	 *
	 * @config
	 */
	private static final String PAR_PROB = "prob";

	/**
	 * The protocol to operate on.
	 * 
	 * @config
	 */
	private static final String PAR_PROT = "comp_prot";

	// ------------------------------------------------------------------------
	// Fields
	// ------------------------------------------------------------------------

	/** Protocol identifier, obtained from config property {@link #PAR_PROT}. */
	private final int protocolID;

	/**
	 * Probability that a type is a dependency, obtained from config proberty
	 * {@link #PAR_PROB}
	 */
	private static double prob;

	public static double getProb() {
		return prob;
	}

	public static void setProb(double prob) {
		ProbDependencyInitializer.prob = prob;
	}

	// ------------------------------------------------------------------------
	// Initialization
	// ------------------------------------------------------------------------
	/**
	 * Standard constructor that reads the configuration parameters. Invoked by the
	 * simulation engine.
	 * 
	 * @param prefix the configuration prefix for this class.
	 */
	public ProbDependencyInitializer(String prefix) {
		protocolID = Configuration.getPid(prefix + "." + PAR_PROT);
		prob = Configuration.getDouble(prefix + "." + PAR_PROB);
	}

	// ------------------------------------------------------------------------
	// Methods
	// ------------------------------------------------------------------------

	@Override
	public boolean execute() {

		OverloadComponentAssembly comp;

		// for each node
		
		int totDependencies = 0;
				
		for (int i = 0; i < Network.size(); ++i) {
			
			comp = (OverloadComponentAssembly) Network.get(i).getProtocol(protocolID);
			ArrayList<Service> services = comp.getServices();
			
			//System.out.println("Nodo " + Network.get(i).getID());
			// for each service inside a node
			for (Service service : services) {
				
				//System.out.println("Servizio " + service.getService_id() + "  dep: ");
				//System.out.println(" servizio " + service.getService_id() + " type " + service.getType());
				
	            int dep_num = 0;

				for (int t = service.getType() + 1; t < comp.getTypes(); ++t) {
					double val = CommonState.r.nextDouble();
					//if(service.getService_id()==261) {
						//System.out.println(" val =  " + val + "     prob = " + prob);
					//}
					if (val <= prob) {

						totDependencies++;
						service.setDependencyType(t);
						dep_num++;					
						if(t==service.getType()) {
							System.err.println("Cannot set recursive dependencies");
							System.exit(0);
						}					
					}
				}
				
			}
			
			/*
			for (Service service : services) {
				boolean[] listDep = service.getDependencies();

				for (int j = 0; j < listDep.length; j++) {
					System.out.println(" dep " + j + "  :  " + listDep[j]);
				}
			}
			*/
								
		}
		
		System.out.println("totDependencies "+totDependencies);
//		System.exit(0);

		return false;
	}
}
