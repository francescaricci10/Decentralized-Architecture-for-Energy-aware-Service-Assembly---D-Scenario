package lnu.mida.controller;

import peersim.config.Configuration;
import peersim.core.*;

/**
 * This class removes randomly chosen nodes from the network.
 */
public class OverloadComponentChangeL implements Control {

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

	// ------------------------------------------------------------------------
	// Parameters
	// ------------------------------------------------------------------------

	/**
	 * Minimum number of services to remove
	 */
	protected static final String PAR_NUM = "num";

	// ------------------------------------------------------------------------
	// Fields
	// ------------------------------------------------------------------------
	private final int num;
	private final String name;
	private final int component_assembly_pid;
	private final int application_pid;
	private int failure_injected;

	// ------------------------------------------------------------------------
	// Initialization
	// ------------------------------------------------------------------------
	public OverloadComponentChangeL(String name) {
		this.name = name;
		num = Configuration.getInt(name + "." + PAR_NUM);
		component_assembly_pid = Configuration.getPid(name + "." + COMP_PROT);
		application_pid = Configuration.getPid(name + "." + APPL_PROT);
	}

	// ------------------------------------------------------------------------
	// Methods
	// ------------------------------------------------------------------------
	@Override
	public boolean execute() {

//		int i;
//		// long time = CommonState.getTime();
//		for (i = 0; i < num; ++i) {
//
//			int j = CommonState.r.nextInt(Network.size());
//			Node n = Network.get(j);
//			OverloadComponentAssembly comp = (OverloadComponentAssembly) n.getProtocol(component_assembly_pid);
//
//			// queue parameter
//			comp.setDeclared_utility(1);
//			comp.setQueueParameter(0.1);
//			comp.setCurveParameter(0.1);			
//
//		}
		
		return false;

	}

}
