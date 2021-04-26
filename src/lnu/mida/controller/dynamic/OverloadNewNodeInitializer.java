package lnu.mida.controller.dynamic;

import java.util.ArrayList;
import java.util.Random;
import com.lajv.location.CircleLocation;
import com.lajv.vivaldi.VivaldiProtocol;
import com.lajv.vivaldi.dim2d.Dim2DVivaldiCoordinate;
import lnu.mida.controller.init.ProbDependencyInitializer;
import lnu.mida.entity.EnergyReputation;
import lnu.mida.entity.GeneralNode;
import lnu.mida.entity.GreenReputation;
import lnu.mida.entity.QOSReputation;
import lnu.mida.entity.Service;
import lnu.mida.entityl.transferfunction.CustomTransferFunction;
import lnu.mida.entityl.transferfunction.TransferFunction;
import lnu.mida.protocol.OverloadApplication;
import lnu.mida.protocol.OverloadComponentAssembly;
import peersim.config.Configuration;
import peersim.core.CommonState;
import peersim.core.Network;
import peersim.core.Node;
import peersim.dynamics.NodeInitializer;

public class OverloadNewNodeInitializer implements NodeInitializer {

	private static final String COMP_PROT = "comp_prot";
	private static final String APP_PROT = "app_prot";
	private static final String VIV_PROT = "viv_prot";

	private final int component_assembly_pid;
	private final int application_assembly_pid;
	private final int vivaldi_assembly_pid;

	public OverloadNewNodeInitializer(String prefix) {
		component_assembly_pid = Configuration.getPid(prefix + "." + COMP_PROT);
		application_assembly_pid = Configuration.getPid(prefix + "." + APP_PROT);		
		vivaldi_assembly_pid = Configuration.getPid(prefix + "." + VIV_PROT);
	}

	@Override
	public void initialize(Node n) {
		
		
		GeneralNode node = (GeneralNode)n;
	
		node.initCand();
		node.initRep();
				
		int m = Configuration.getInt("M", 50000);
		QOSReputation.setM(m); // set the parameter m for the learner

		int types = Configuration.getInt("TYPES", 0);
		int services_per_node = Configuration.getInt("SERVICES_PER_NODE", 0);

		OverloadComponentAssembly ca = (OverloadComponentAssembly) node.getProtocol(component_assembly_pid);
		OverloadApplication appl = (OverloadApplication) node.getProtocol(application_assembly_pid);

		appl.reset();
		ca.reset();
		
		ArrayList<Integer> new_list = new ArrayList<Integer>();
		node.setMTBF(new_list);
		
		ArrayList<Integer> new_list2 = new ArrayList<Integer>();
		node.setDowntimePeriods(new_list2);
		
		
		appl.getQoSReputations().add(new QOSReputation(500));
		appl.getEnergyReputations().add(new EnergyReputation(100));	
		appl.getGreenReputations().add(new GreenReputation(100));

		// Initialize the number of services with max_types
		for (int j = 0; j < services_per_node; j++) {

			Service s = new Service(types,application_assembly_pid);
			

			// set type of service
			int randomType = CommonState.r.nextInt(10);			
						
			s.setType(randomType);
			s.setNode_id((int) node.getID());
			        
			
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
			
			// Probability dependency attachment
            int dep_num = 0;         
			for (int t = s.getType() + 1; t < ca.getTypes(); t++) {
				double val = CommonState.r.nextDouble();
				if (val <= 0.6) {
					s.setDependencyType(t);
					dep_num++;					
					if(t==s.getType()) {
						System.err.println("Cannot set recursive dependencies");
						System.exit(0);
					}					
				}
			}
			s.setDep_num(dep_num);	

		}

		/**
		 * Energy parameters
		 */

		// set green energy generation rate (for Journal)
		double G = (1440 * CommonState.r.nextDouble());
		node.setG(0);
		
        // set the Battery 
		int max = 6480; // batteria AAA da 1200 mAh
		int min = 6480;
		
		double capacity = min + (max - min) * CommonState.r.nextDouble();
		node.setBattery(capacity);
		node.setCapacity(capacity);		

		node.setCPUConsumptionFactor(0.5+(1.5*CommonState.r.nextDouble()));
		node.setCommunicationConsumptionFactor(0.5+(1.5*CommonState.r.nextDouble()));
		
		/**
		 * Construct parameters
		 */
				

		for(long serv_num=0;serv_num<500;serv_num++)
			appl.getQoSReputations().add(new QOSReputation(serv_num));
		
		for(long nodes_num=0;nodes_num<100;nodes_num++)
			appl.getEnergyReputations().add(new EnergyReputation(nodes_num));

		for(long nodes_num=0;nodes_num<100;nodes_num++)
			appl.getGreenReputations().add(new GreenReputation(nodes_num));

		
		// random location		
		node.location.randomize();
		
		// stabilize location		
		VivaldiProtocol vp1 = (VivaldiProtocol) node.getProtocol(vivaldi_assembly_pid);		
		double actualXLocation = ((CircleLocation)node.location).getX();
		double actualYLocation = ((CircleLocation)node.location).getY();
		((Dim2DVivaldiCoordinate)vp1.vivCoord).setLocation(actualXLocation,actualYLocation);	
		
	}

}
