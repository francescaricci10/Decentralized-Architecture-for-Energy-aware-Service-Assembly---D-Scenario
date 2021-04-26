package lnu.mida.entityl.transferfunction;

public class CustomTransferFunction extends TransferFunction {
	
	// Transfer rate parameter
	private double transfer_rate; 
	
	public CustomTransferFunction(double transfer_rate) {
		this.transfer_rate=transfer_rate;
	}
	

	// Returns 1 for each value of lambda_tot. Simulates a "single call/user engaged"
	@Override
	public double calculate_tSd(double lambda_tot) {
		return transfer_rate*lambda_tot+1;
	}

}
