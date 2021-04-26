package lnu.mida.entity;

public class EnergyReputation implements Cloneable  {
	
	
	// service id for which the experience is evaluated
	private long nodeID;
	
	// number of time the experience is done
	private int k;
	// window period of the learner
	private static int M;
	
	// approach to challenge
	private double ee;
	
	
	public EnergyReputation(long nodeID) {
		this.setNodeID(nodeID);
		k=0;
		ee=0;
	}
	
	public void addDeclaredEnergy(double declaredEnergy) {		
		
		
		double W = 0.1 + (0.9/k);
		if(k==0)
			W=1;
		
		//double W = 0.7;
				
		double ee_new = W*declaredEnergy + ( (1-W)*ee );		
		ee = ee_new;
		
		k++;
		
		GeneralNode node = GeneralNode.getNode(nodeID);
		node.setEeEnergy(ee);
		node.setEeCounter(k);
		
	}
	
	@Override
	public Object clone() {	
		EnergyReputation result = null;
		try {
			result = (EnergyReputation) super.clone();
		} catch (CloneNotSupportedException ex) {
			System.out.println(ex.getMessage());
			assert(false);
		}
		return result;
	}

	public long getK() {
		return k;
	}
	
	public void setK(int k) {
		this.k=k;
	}

	public static int getM() {
		return M;
	}

	public static void setM(int m) {
		M = m;
	}

	public double getEe() {
		return ee;
	}

	public long getNodeId() {
		return nodeID;
	}

	public void setNodeID(long nodeID) {
		this.nodeID = nodeID;
	}

}
