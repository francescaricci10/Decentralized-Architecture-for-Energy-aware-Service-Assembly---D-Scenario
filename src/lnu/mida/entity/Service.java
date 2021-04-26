package lnu.mida.entity;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;

import lnu.mida.entityl.transferfunction.TransferFunction;
import peersim.core.Cleanable;

public class Service implements Cleanable {

	/** used to generate unique IDs */
	public static long counterID = 0;

	// ------------------------------------------------------------------------
	// Protocols id
	// ------------------------------------------------------------------------

//	public int component_assembly_pid;
	public int application_assembly_pid;

	// ------------------------------------------------------------------------
	// Energy costs
	// ------------------------------------------------------------------------

	/**
	 * Computation energy cost
	 */
	private double I_comp;
	private double I_comp_lambda;
	private double L_comp;
	private double E_comp;

	/**
	 * Communication energy cost
	 */
	private double I_comm;
	private double I_comm_lambda;
	private double L_comm;
	private double E_comm;

	// ------------------------------------------------------------------------
	// Fields
	// ------------------------------------------------------------------------

	/**
	 * The type of this component (a type uniquely identifies the service provided
	 * by this component)
	 */
	private int my_type;

	/** Maximum number of component types */
	private final int max_types;

	/** number of dependencies */
	private int dep_num;
	
	/** dependencies[i] is true iff there is a dependency of type i */
	private boolean dependencies[];

	/** Dependency arrays */
	private Service dependencies_obj[];

	/**
	 * is the average rate of service requests sent to dependency d by a
	 * non-terminal service S when S is subject to an incoming load vector of
	 * service requests.
	 */
	private TransferFunction transfer_func_S[];

	/**
	 * is the average rate of service requests sent to CPU by service S when S is
	 * subject to an incoming load vector of service requests
	 */
	private TransferFunction transfer_func_CPU;

	/**
	 * if the i-th dependency is resolved, then dependencies_obj[i] is a reference
	 * to the node which satisfies it. If the i-th dependency is not resolved, then
	 * dependencies_obj[i] is null
	 */

	private double utility;
	/** my current utility value */

	private double compound_utility;
	/** the total utility of the assembly rooted at this node */

	private double declared_utility;

	private boolean is_fully_resolved;

	private LinkedList observers;

	private boolean has_changed;

	private LinkedList cache;

	private boolean is_failed;
	/** true iff the node hosting this object failed */

	private long node_id;

	private long service_id;

	private double queueParameter;

	private double curveParameter;

	private double experiencedCU;

	// rate of service requests addressed to $S$ from external users;
	private double sigma;

	// overall load addressed to S;
	private double lambda_t;

	

	/**
	 * Initialize this object by reading configuration parameters.
	 * 
	 * @param prefix the configuration prefix for this class.
	 */
	public Service(int types, int application_assembly_pid) {
		super();
		max_types = types;
		utility = 0.0;
		compound_utility = 0.0;
		setNode_id(-1);
		service_id = nextID();
		this.application_assembly_pid = application_assembly_pid;
		dep_num = 0;
		dependencies = new boolean[max_types + 2];
		Arrays.fill(dependencies, false);
		dependencies_obj = new Service[max_types];
		Arrays.fill(dependencies_obj, null);
		my_type = -1;
		is_fully_resolved = true; // this service initially has no dependencies set, therefore it is fully resolved
		observers = new LinkedList();
		cache = new LinkedList();
		has_changed = false;
		is_failed = false;
		queueParameter = 0;
		curveParameter = 0;
		declared_utility = 0;
		sigma = 70;
		lambda_t = 0;
		setExperiencedCU(0);
		transfer_func_S = new TransferFunction[max_types];
	}

	/**
	 * Makes a copy of this object. Needs to be explicitly defined, since we have
	 * array members.
	 */
	@Override
	public Object clone() {
		Service result = null;
		try {
			result = (Service) super.clone();
		} catch (CloneNotSupportedException ex) {
			System.out.println(ex.getMessage());
			assert (false);
		}

		if (dependencies != null)
			result.dependencies = dependencies.clone();
		if (dependencies_obj != null)
			result.dependencies_obj = dependencies_obj.clone();

		result.observers = (LinkedList) observers.clone();
		result.cache = (LinkedList) cache.clone();

		return result;
	}

	public Service[] getDependencies_obj() {
		return dependencies_obj;
	}

	public void setDependencies_obj(Service[] dependencies_obj) {
		this.dependencies_obj = dependencies_obj;
	}

	// ------------------------------------------------------------------------
	// Methods
	// ------------------------------------------------------------------------

	/**
	 * The following methods are basically those from the observable/observer
	 * class/interface. For reasons I do not understand, observable and observer do
	 * not work, therefore I had to reimplement them.
	 */
	protected int countObservers() {
		return observers.size();
	};

	/**
	 * Add an observer to the list of observers. The observer is added also if it
	 * already exists in the list of observers (no check is done to prevent
	 * duplicates).
	 *
	 * @param o the observer to add (must be of type ComponentAssembly)
	 */
	protected void addObserver(Object o) {
		observers.add(o);
	};

	/**
	 * Removes an observer from the list of observers. If the observer does not
	 * exists, nothing happens.
	 *
	 * @param o the observer to remove
	 */
	protected void deleteObserver(Object o) {
		observers.remove(o);
	};

	/**
	 * If this object has changed (through a call to {@link setChanged}) then notify
	 * all currently registered observers and reset the changed flag. If this object
	 * has not changed, nothing happens.
	 */
	public void notifyObservers() {
		if (!hasChanged())
			return;
		clearChanged();
		Iterator it = observers.iterator();
		while (it.hasNext()) {
			Service c = (Service) it.next();
			c.update(this, null);
		}
	}

	protected void setChanged() {
		has_changed = true;
	};

	protected boolean hasChanged() {
		return has_changed;
	};

	protected void clearChanged() {
		has_changed = false;
	};

	/**
	 * Returns true iff this component is fully resolved (i.e., all dependencies are
	 * resolved).
	 */
	public boolean isFullyResolved() {
		return is_fully_resolved;
	};
	
	public void setIsFullyResolved(boolean value) {
		is_fully_resolved=value;
	};

	/**
	 * Returns the type of this component
	 */
	public int getType() {
		return my_type;
	};

	/**
	 * Returns the maximum number of component types
	 */
	public int getTypes() {
		return max_types;
	};

	protected boolean isFailed() {
		return is_failed;
	}

	/**
	 * Sets this component as failed. Failures are permanent, therefore a failed
	 * component never comes back to operational status. This method also updates
	 * the changed flag.
	 */
	protected void setFailed() {
		is_failed = true;
		setChanged();
	}

	/**
	 * Returns the utility of this component alone
	 */
	public double getUtility() {
		return utility;
	}

	/**
	 * Returns the compound utility of the assembly rooted at this component. If
	 * this component is not fully resolved, returns 0.
	 */
	public double getCompoundUtility() {
		if (isFullyResolved())
			return compound_utility;
		else
			return 0.0;
	}

	/*
	 * This function calculates the real utility experienced by a node
	 */
	public double getRealUtility(Service o) {
		assert (this != o);
		int queuePosition = -1;
		queuePosition = observers.indexOf(o);
		assert (queuePosition >= 0);
		double utility = getUtilityFromLambda();
		return utility;
	}

	public void updateUtility() {
		int queueLenght = observers.size();
		assert (queueLenght >= 0);
		utility = getUtilityFromLambda();
		this.setUtility(utility);
	}

	public double getUtilityFromLambda() {

		double returned_util = 0;

		if (lambda_t < (200 * queueParameter)) {
			returned_util = declared_utility;}
		else {
			//returned_util = Math.pow(Math.E, -(lambda_t * lambda_t) / (10000 * curveParameter));
			returned_util = Math.pow(Math.E, -(lambda_t * lambda_t) / (Double.parseDouble("1.0e100") * curveParameter));
			//System.out.println("\n curveParameter " + curveParameter);
			//System.out.println(" lambda_t * lambda_t " + (lambda_t * lambda_t));
			//System.out.println(" Double.parseDouble(\"1.0e5\") * curveParameter) " + (Double.parseDouble("1.0e16") * curveParameter));

//			if(returned_util>0.7)
//				System.out.println(returned_util);

		}

		return returned_util;
	}

	/**
	 * Define type t as a (possibly new) new dependency type.
	 */
	public void setDependencyType(int t) {
		assert (t < getTypes());
		dependencies[t] = true;
		is_fully_resolved = false;
		//System.out.println("il servizio " + getService_id() + " ha una dep di tipo " + t);
	}

	/**
	 * Append all components in the cache to the comp list.
	 */
	protected void fillCache(List comp) {
		comp.addAll(cache);
	}

	/**
	 * Sets the type of this component. This method can be invoked only once; any
	 * subsequent invocation aborts.
	 */
	public void setType(int t) {
		assert (my_type < 0);
		assert (t >= 0 && t < getTypes());
		my_type = t;
	}

	/**
	 * Sets the utility value of this component; also, the compound utility is
	 * tentatively set to u. Returns the previous utility value of this object.
	 *
	 * This method is supposed to be called once at the beginning. Furthermore, this
	 * method does not notify observers of this component.
	 */
	public double setUtility(double u) {
		double old_utility = utility;
		utility = u;
		compound_utility = u;
		return old_utility;
	}

	/**
	 * Add a new link to component o to satisfy a dependency of type o.getType().
	 * The dependency must not be already satisfied (i.e., this method does not
	 * unlink a previously linked dependency). Observers are not notified, but the
	 * changed flag is updated.
	 */
	protected void linkDependency(Service o) {
		
		assert (this != o);
		int t = o.getType();
		assert (dependencies[t] == true);
		assert (dependencies_obj[t] == null);
		dependencies_obj[t] = o;
		o.addObserver(this);
		setChanged();
		
		//if(this.getNode_id()==1) 
		//System.out.println("\n in linkdep del servizio " + this.getService_id() + "    dep agiiunta :  " + dependencies_obj[t].getService_id());
	}

	
	
	/**
	 * Unlink (remove) a previously linked dependency on component o. Component o
	 * must belong to the list of dependencies. Observers are not notified, but the
	 * changed flag is updated.
	 */
	public void unlinkDependency(Service o) {
		int t = o.getType();
		assert (dependencies[t] == true);
		assert (dependencies_obj[t] == o);
		o.deleteObserver(this);
		dependencies_obj[t] = null;
		setChanged();
		
		//if(this.getNode_id()==1) 
		//System.out.println("\n in unlinkdep " + o.getService_id());

	}

	
	public void addLink(Service s) {
		int t = s.getType();
		
		if(dependencies_obj[t]==null) {
			linkDependency(s);
		}else {
			unlinkDependency(dependencies_obj[t]);
			linkDependency(s);
		}
	}
	
	public void printDep(int type) {
		if(dependencies_obj[type]==null)
			System.out.println("null");
		else
			System.out.println("\n dep del servizio " + this.getService_id() + "     :  " + dependencies_obj[type].getService_id());
	}
	
	/**
	 * Append all references to dependency objects to the list dep
	 */
	protected void fillDependencies(List dep) {
		for (int t = 0; t < dependencies_obj.length; ++t) {
			if (dependencies[t] && dependencies_obj[t] != null)
				dep.add(dependencies_obj[t]);
		}
	}

	/**
	 * Handle updates (inherited from Observer interface). This method will notify
	 * observers if necessary.
	 */
	public void update(Object o, Object arg) {
		if (isFailed())
			return;
		assert (this != o);
		Service comp = (Service) o;
		updateCompoundUtility();
		notifyObservers();
	}

	/**
	 * Inherited from Cleanable. This method is called when the node hosting this
	 * protocol is set to failed.
	 */
	@Override
	public void onKill() {
		// Unlink all dependencies, so that they will no longer send
		// updates to this node
		for (int t = 0; t < dependencies_obj.length; ++t) {
			if (dependencies_obj[t] != null)
				unlinkDependency(dependencies_obj[t]);
		}
		// Set failed state
		setFailed();
		notifyObservers();
	}

	/**
	 * Updates the compound utility of this component. If this component is not
	 * fully resolved, the compound utility is set to zero. If it is fully resolved,
	 * the compound utility is set as the product of the utility of this single
	 * component and the compound utility of all dependencies. If the updated value
	 * of the compound utility for this component is different than the previous
	 * value, this method invokes setChanged(). In general, the caller is
	 * responsible for calling notifyObservers() to propagate the change to all
	 * observers.
	 */
	public void updateCompoundUtility() {

		double old_utility = compound_utility;
		boolean old_resolved = is_fully_resolved;

		compound_utility = getUtility();
		is_fully_resolved = true;
		for (int t = 0; t < dependencies_obj.length; ++t) {
			if (dependencies[t] == false)
				continue; // skip to next item

			if (dependencies_obj[t] == null || dependencies_obj[t].isFailed()) {
				compound_utility = 0.0;
				is_fully_resolved = false;
				dependencies_obj[t] = null;
				break;
			}
			compound_utility *= dependencies_obj[t].getCompoundUtility();
			is_fully_resolved = is_fully_resolved || dependencies_obj[t].isFullyResolved();
		}
		if (old_utility != compound_utility || old_resolved != is_fully_resolved) {
			setChanged();
		}
	}

	// recursively calculate Compound Utility
	public double getEffectiveCU() {

		double effective_compound_utility = getExperiencedCU();

		is_fully_resolved = true;
		for (int t = 0; t < dependencies_obj.length; ++t) {
			if (dependencies[t] == false)
				continue; // skip to next item

			if (dependencies_obj[t] == null || dependencies_obj[t].isFailed()) {
				effective_compound_utility = 0.0;
				is_fully_resolved = false;
				dependencies_obj[t] = null;
				break;
			}
			effective_compound_utility *= dependencies_obj[t].getEffectiveCU();
		}
		return effective_compound_utility;
	}
	
	
	/**
	 * Recursive energy calculation for Computation
	 */

	// recursively calculate L_comp
	public double calculateL_comp() {

		double L_comp = this.getI_comp();

		is_fully_resolved = true;
		for (int t = 0; t < dependencies_obj.length; ++t) {
			if (dependencies[t] == false)
				continue; // skip to next item

			if (dependencies_obj[t] == null || dependencies_obj[t].isFailed())
				break;

			if (this.getNode_id() == dependencies_obj[t].getNode_id())
				L_comp += dependencies_obj[t].calculateL_comp();
		}
		return L_comp;
	}

	// recursively calculate E_comp
	public double calculateE_comp() {

		double E_comp = this.getI_comp();

		is_fully_resolved = true;
		for (int t = 0; t < dependencies_obj.length; ++t) {
			if (dependencies[t] == false)
				continue; // skip to next item

			if (dependencies_obj[t] == null || dependencies_obj[t].isFailed())
				break;

			E_comp += dependencies_obj[t].calculateE_comp();
		}
		return E_comp;
	}

	/**
	 * Recursive energy calculation for Communication
	 */

	// recursively calculate L_comm
	public double calculateL_comm() {

		double L_comm = this.getI_comm();

		is_fully_resolved = true;
		for (int t = 0; t < dependencies_obj.length; ++t) {
			if (dependencies[t] == false)
				continue; // skip to next item

			if (dependencies_obj[t] == null || dependencies_obj[t].isFailed())
				break;

			if (this.getNode_id() == dependencies_obj[t].getNode_id())
				L_comm += dependencies_obj[t].calculateL_comm();
		}

		return L_comm;
	}
	
	// recursively calculate E_comm
	public double calculateE_comm() {

		double E_comm = this.getI_comm();

		is_fully_resolved = true;
		for (int t = 0; t < dependencies_obj.length; ++t) {
			if (dependencies[t] == false)
				continue; // skip to next item

			if (dependencies_obj[t] == null || dependencies_obj[t].isFailed())
				break;

			E_comm += dependencies_obj[t].calculateE_comm();
		}

		return E_comm;
	}
	
	
	/***
	 * 
	 * 
	 *
	 */
	
	
	// recursively calculate lambda tot
	public double updateLambdaTot() {
		double lambda_tot = sigma;
		
		//if(this.getService_id()==202)
		//	System.out.println("....size..... " + this.observers.size());

		for (Object o : this.observers) {
			
			Service ca = (Service) o;
			lambda_tot += ca.transferLoad(this);
		}
		this.lambda_t = lambda_tot;

		//if(this.getService_id()==202)
		//	System.out.println("lambda_tot " + lambda_tot);

		return lambda_tot;
	}

	private double transferLoad(Service overloadComponentAssembly) {
		for (int i = 0; i < max_types; i++) {
			Service depObj = dependencies_obj[i];
			
			if (depObj != null && overloadComponentAssembly.equals(depObj)) {
				return transfer_func_S[i].calculate_tSd(lambda_t);
			}
		}

		return 0;
	}

	/**
	 * Check whether the neighbor match a dependency.
	 * 
	 * @param neighborService the selected node to talk with.
	 */

	public List<Service> interact(Service neighborService) {

		//System.out.println("Servizio "+this.getService_id()+" su Nodo "+this.getNode_id()+" interagisce con servizio "+neighborService.getService_id()+" su nodo "+neighborService.getNode_id());
		
		assert (this != neighborService);

		// The list comp_list contains the neighbor and all its dependencies
		List comp_list = new LinkedList();
		neighborService.fillDependencies(comp_list);
		comp_list.add(neighborService);

		List<Service> candidates = new ArrayList<Service>();
		
		Iterator it = comp_list.iterator();

		
		while (it.hasNext()) {

			Service comp = (Service) it.next();

			assert (comp != null);

			// Get the neighbor type
			int t = comp.getType();

			assert (t >= 0 && t < max_types);

			if (dependencies[t] == false) // if have dependency to resolve and i want it to resolve (alfa>x)
				continue; // we do not have a dependency on component type t
			
			candidates.add(comp);
			
		}
		if (hasChanged())
			updateCompoundUtility();
		notifyObservers();
		
		return candidates;
	}

	public void checkChanges() {
		if (hasChanged())
			updateCompoundUtility();
		notifyObservers();
	}
	
	public LinkedList getObservers() {
		return observers;
	}

	public void setObservers(LinkedList o) {
		observers = o;
	}

	public void reset() {
		dependencies_obj = new Service[max_types];
		
		//updateCompoundUtility(); // calculate if fully resolved
		is_fully_resolved=false;
		
		Arrays.fill(dependencies_obj, null);
		observers = new LinkedList<>();
		// update utilities
		this.updateUtility();
		this.updateCompoundUtility();
		this.experiencedCU = 0;
		this.lambda_t = 0;
	}

	public double getExperiencedCU() {
		return experiencedCU;
	}

	public void setExperiencedCU(double experiencedCU) {
		this.experiencedCU = experiencedCU;
	}

	public int getDep_num() {
		return dep_num;
	}

	public void setDep_num(int dep_num) {
		this.dep_num = dep_num;
	}

	public double getSigma() {
		return sigma;
	}

	public void setSigma(double sigma) {
		this.sigma = sigma;
	}

	public boolean[] getDependencies() {
		return dependencies;
	}

	public void setDependencies(boolean[] dependencies) {
		this.dependencies = dependencies;
	}

	public double getLambda_t() {
		return lambda_t;
	}

	public void setLambda_t(double lambda_t) {
		this.lambda_t = lambda_t;
	}

	public double getQueueParameter() {
		return queueParameter;
	}

	public void setQueueParameter(double queueParameter) {
		this.queueParameter = queueParameter;
	}

	public double getCurveParameter() {
		return curveParameter;
	}

	public void setCurveParameter(double curveParameter) {
		this.curveParameter = curveParameter;
	}

	public double getDeclaredUtility() {
		return declared_utility;
	}

	public void setDeclared_utility(double declared_utility) {
		this.declared_utility = declared_utility;
	}

	public TransferFunction[] getTransferFunctions() {
		return transfer_func_S;
	}

	public TransferFunction getTransfer_func_CPU() {
		return transfer_func_CPU;
	}

	public void setTransfer_func_CPU(TransferFunction transfer_func_CPU) {
		this.transfer_func_CPU = transfer_func_CPU;
	}

	public double getLambdatoCPU() {
		//System.out.println("sigma : " + getSigma() + "  " + getType());
		return transfer_func_CPU.calculate_tSd(lambda_t);
	}

	/** returns the next unique ID */
	private long nextID() {
		return counterID++;
	}

	public long getNode_id() {
		return node_id;
	}

	public long getService_id() {
		return service_id;
	}

	public void setNode_id(long node_id) {
		this.node_id = node_id;
	}

	public int getApplication_assembly_pid() {
		return application_assembly_pid;
	}

	public double getI_comp() {
		return I_comp;
	}

	public void setI_comp(double i_comp) {
		I_comp = i_comp;
	}

	public double getL_comp() {
		return L_comp;
	}

	public void setL_comp(double l_comp) {
		L_comp = l_comp;
	}

	public double getE_comp() {
		return E_comp;
	}

	public void setE_comp(double e_comp) {
		E_comp = e_comp;
	}

	public double getI_comm() {
		return I_comm;
	}

	public void setI_comm(double i_comm) {
		I_comm = i_comm;
	}

	public double getL_comm() {
		return L_comm;
	}

	public void setL_comm(double l_comm) {
		L_comm = l_comm;
	}

	public double getE_comm() {
		return E_comm;
	}

	public void setE_comm(double e_comm) {
		E_comm = e_comm;
	}

	public double getI_comp_lambda() {
		return I_comp_lambda;
	}

	public void setI_comp_lambda(double i_comp_lambda) {
		I_comp_lambda = i_comp_lambda;
	}

	public double getI_comm_lambda() {
		return I_comm_lambda;
	}

	public void setI_comm_lambda(double i_comm_lambda) {
		I_comm_lambda = i_comm_lambda;
	}


}
