package lnu.mida.entity;

public class QOSReputation implements Cloneable  {
	
	
	// service id for which the experience is evaluated
	private long serviceID;
	
	
	// last declared utility
	private double declared_utility;
	// number of time the experience is done
	private int k;
	private int counter;
	// current average (influenced by the window period of the learner)
	private double Qk;
	// trust value
	private double Tk;
	
	// window period of the learner
	private static int M;
	
	// approach to challenge
	private double ee;
	
	public QOSReputation(long id) {
		this.setServiceID(id);
		declared_utility=-1;
		k=0;
		Qk=1; // was 0
		Tk=1;
		ee=0;
	}
	
	public void addExperiencedUtility(double experienced_utility) {
		
		// Updating the Average
        double beta = (k%M)+1;
		double Qk_new = Qk + ((experienced_utility-Qk)/beta);	

		Qk = Qk_new;
		
		//if(serviceID==10)
		//	System.out.println("    experienced_utility  " + experienced_utility);
		
		
		// Updating the trust value
		
		double efficiency = (1 - ( Math.abs(declared_utility - experienced_utility) / Math.abs(declared_utility) ));
		
		// trust update
		double Tk_new = Tk + ((efficiency-Tk)/beta); 
		// step trust update
//		double Tk_old=Tk;
//		if(experienced_utility<declared_utility) {
//			if(Tk>0)
//				Tk-=0.1;
//		}
//		else {
//			if(Tk<1)
//				Tk+=0.1;
//		}
		Tk=Tk_new;		

	
			
		// Approach to Challenge - Shaerf quality	
		double W = 0.2 + (0.8/k);
		if(k==0)
			W=1;
		
		double ee_new = W*experienced_utility + ( (1-W)*ee );	
		
		
		ee = ee_new;
		
		k++;
		counter++;
	
		int nodeID = (int)serviceID/5;
		//System.out.println("Servizio: " + serviceID  + "     nodo " + nodeID);
		
		GeneralNode n = GeneralNode.getNode(nodeID);
		n.setQosCounter(k, serviceID);
		n.setEeQos(ee, serviceID);
		n.setTk(Tk, serviceID);
		n.setWindow(Qk, serviceID);
		
		/*
		if(serviceID==10) {
			System.out.println("	Tk  :" + Tk);
			System.out.println("	 k  :" + k);
			System.out.println("	 c  :" + counter);
		}
		*/
	}
	
	public double getWindowAverage() {
        return Qk;
	}
	
	@Override
	public Object clone() {	
		QOSReputation result = null;
		try {
			result = (QOSReputation) super.clone();
		} catch (CloneNotSupportedException ex) {
			System.out.println(ex.getMessage());
			assert(false);
		}
		return result;
	}

	public int getK() {
		return k;
	}
	

	public static int getM() {
		return M;
	}

	public static void setM(int m) {
		M = m;
	}

	public double getDeclared_utility() {
		return declared_utility;
	}

	public void setDeclared_utility(double declared_utility) {
		this.declared_utility = declared_utility;
	}

	public double getTk() {
		return Tk;
	}

	public void setTk(double tk) {
		Tk = tk;                                                              
	}                                                                                

	public void setQk(double qk) {
		Qk = qk;
	}
	
	public double getQk() {
		return Qk;
	}

	public double getEe() {
		return ee;
	}

	public long getServiceID() {
		return serviceID;
	}

	public void setServiceID(long serviceID) {
		this.serviceID = serviceID;
	}

}
