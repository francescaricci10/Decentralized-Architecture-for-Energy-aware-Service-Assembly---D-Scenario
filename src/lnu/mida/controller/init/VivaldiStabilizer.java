package lnu.mida.controller.init;

import com.lajv.location.CircleLocation;
import com.lajv.vivaldi.VivaldiProtocol;
import com.lajv.vivaldi.dim2d.Dim2DVivaldiCoordinate;

import lnu.mida.entity.GeneralNode;
import peersim.config.Configuration;
import peersim.core.Control;
import peersim.core.Network;

/**
 * This initializer stabilizes Vivaldi by setting the estimated location equal to the actual location
 * DO NOT RUN to study the effects of latency estimations
 */

public class VivaldiStabilizer implements Control {
	
	/**
	 * The protocol to operate on.
	 * 
	 * @config
	 */
	private static final String PAR_PROT = "protocol";
	
	/**
	 * Protocol identifier; obtained from config property {@link #PAR_PROT}.
	 * */
	private final int pid;
	
	
	public VivaldiStabilizer(String name) {
		pid = Configuration.getPid(name + "." + PAR_PROT);
	}

	@Override
	public boolean execute() {
		
		for (int i = 0; i < Network.size(); i++) {	
			GeneralNode n1 = (GeneralNode) Network.get(i);
			VivaldiProtocol vp1 = (VivaldiProtocol) n1.getProtocol(pid);		
			double actualXLocation = ((CircleLocation)n1.location).getX();
			double actualYLocation = ((CircleLocation)n1.location).getY();
			((Dim2DVivaldiCoordinate)vp1.vivCoord).setLocation(actualXLocation,actualYLocation);
		}
		return false;
	}
	
	
	

}
