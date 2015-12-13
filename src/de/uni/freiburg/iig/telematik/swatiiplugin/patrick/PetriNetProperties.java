package de.uni.freiburg.iig.telematik.swatiiplugin.patrick;


import java.util.Vector;

import de.uni.freiburg.iig.telematik.sepia.exception.PNValidationException;
import de.uni.freiburg.iig.telematik.sepia.graphic.AbstractGraphicalPN;
import de.uni.freiburg.iig.telematik.sepia.petrinet.properties.PropertyCheckingResult;
import de.uni.freiburg.iig.telematik.sewol.accesscontrol.rbac.RBACModel;

public class PetriNetProperties {
	public PropertyCheckingResult net_status = PropertyCheckingResult.UNKNOWN;
	public PropertyCheckingResult rbac_status = PropertyCheckingResult.UNKNOWN;
	public PropertyCheckingResult bod_status = PropertyCheckingResult.UNKNOWN;
	public PropertyCheckingResult sod_status = PropertyCheckingResult.UNKNOWN;

	// PN properties are checked, when the class is initialized.
	// (net available? RBAC model available? BoDs and SoDs available?)
	
	public PetriNetProperties(AbstractGraphicalPN net, RBACModel acmodel, Vector<BoDSoD> BoDs, Vector<BoDSoD> SoDs) {

		try {
			net_status = validateNet(net);
		} catch (PNValidationException e) {
			net_status = PropertyCheckingResult.FALSE;
			System.out.println(e);
		}

		try {
			rbac_status = validateRbac(acmodel);
		} catch (PNValidationException e) {
			rbac_status = PropertyCheckingResult.FALSE;
			System.out.println(e);
		}

		bod_status = validateBod(BoDs);
		sod_status = validateSod(SoDs);

	}

	public PropertyCheckingResult validateNet(AbstractGraphicalPN net) throws PNValidationException {
		PropertyCheckingResult result = PropertyCheckingResult.UNKNOWN;
		if (net == null)
			throw new PNValidationException("Petri Net is necessary.");
		else
			result = PropertyCheckingResult.TRUE;
		return result;
	}

	public PropertyCheckingResult validateRbac(RBACModel acmodel) throws PNValidationException {
		PropertyCheckingResult result = PropertyCheckingResult.UNKNOWN;
		if (acmodel == null)
			throw new PNValidationException("Access Control Model is necessary.");
		else
			result = PropertyCheckingResult.TRUE;
		return result;

	}

	public PropertyCheckingResult validateBod(Vector<BoDSoD> BoDs) {
		PropertyCheckingResult result = PropertyCheckingResult.UNKNOWN;
		if (BoDs == null)
			result = PropertyCheckingResult.FALSE;
		else if (BoDs.isEmpty())
			result = PropertyCheckingResult.FALSE;
		else
			result = PropertyCheckingResult.TRUE;
		return result;
	}

	public PropertyCheckingResult validateSod(Vector<BoDSoD> SoDs) {
		PropertyCheckingResult result = PropertyCheckingResult.UNKNOWN;
		if (SoDs == null)
			result = PropertyCheckingResult.FALSE;
		else if (SoDs.isEmpty())
			result = PropertyCheckingResult.FALSE;
		else
			result = PropertyCheckingResult.TRUE;
		return result;
	}

}
