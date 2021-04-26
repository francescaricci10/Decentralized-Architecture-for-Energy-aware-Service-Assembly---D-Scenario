package lnu.mida.entityl.transferfunction;

public class UnityTransferFunction extends TransferFunction {

	// Returns 1 for each value of lambda_tot. Simulates a "single call/user engaged"
	@Override
	public double calculate_tSd(double lamda_tot) {
		//System.out.println("UnityTransferFunction");
		return 1;
	}

}
