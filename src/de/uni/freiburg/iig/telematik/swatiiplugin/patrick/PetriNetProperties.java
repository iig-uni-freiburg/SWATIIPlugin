package de.uni.freiburg.iig.telematik.swatiiplugin.patrick;


import java.util.Vector;

import de.uni.freiburg.iig.telematik.sepia.exception.PNValidationException;
import de.uni.freiburg.iig.telematik.sepia.graphic.AbstractGraphicalPN;
import de.uni.freiburg.iig.telematik.sepia.petrinet.properties.PropertyCheckingResult;
import de.uni.freiburg.iig.telematik.sewol.accesscontrol.rbac.RBACModel;

/**
 * This class validates the status of a Petri net, RBAC model, BoDs and SoDs
 * 
 * @author Patrick Notz
 *
 */
public class PetriNetProperties {
	public PropertyCheckingResult net_status = PropertyCheckingResult.UNKNOWN;
	public PropertyCheckingResult rbac_status = PropertyCheckingResult.UNKNOWN;
	public PropertyCheckingResult bod_status = PropertyCheckingResult.UNKNOWN;
	public PropertyCheckingResult sod_status = PropertyCheckingResult.UNKNOWN;

	// PN properties are checked, when the class is initialized.
	// (net available? RBAC model available? BoDs and SoDs available?)
	
	/**
	 * After initialization of this class functions are called to check, if the input parameters are not empty. 
	 * @param net Stores a Petri net.
	 * @param acmodel Stores a RBAC Model.
	 * @param BoDs Stores BoD constraints.
	 * @param SoDs Stores SoD constraints.
	 */
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

	
	/**
	 * Examines if the given Petri net is not empty.<br>
	 * No resilience checks are performed, if there is no net.
	 * @param net Stores a Petri net.
	 * @return {@code true} if the net is not null, {@code false} otherwise.
	 * @throws PNValidationException
	 */
	public PropertyCheckingResult validateNet(AbstractGraphicalPN net) throws PNValidationException {
		PropertyCheckingResult result = PropertyCheckingResult.UNKNOWN;
		if (net == null)
			throw new PNValidationException("Petri Net is necessary.");
		else
			result = PropertyCheckingResult.TRUE;
		return result;
	}

	
	/**
	 * Examines if the given RBAC model is not empty.<br>
	 * No resilience checks are performed, if there is no access control model.
	 * @param acmodel Stores a RBAC model.
	 * @return {@code true} if the RBAC model is not null, {@code false} otherwise.
	 * @throws PNValidationException
	 */
	public PropertyCheckingResult validateRbac(RBACModel acmodel) throws PNValidationException {
		PropertyCheckingResult result = PropertyCheckingResult.UNKNOWN;
		if (acmodel == null)
			throw new PNValidationException("Access Control Model is necessary.");
		else
			result = PropertyCheckingResult.TRUE;
		return result;

	}

	
	/**
	 * Examines if the given BoD constraints are not empty.<br>
	 * Resilience checks are performed, disregarding the return value of this function.
	 * @param BoDs Stores BoD constraints
	 * @return {@code true} if BoD constraints are not null, {@code false} otherwise.
	 */
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

	
	/**
	 * Examines if the given SoD constraints are not empty.<br>
	 * Resilience checks are performed, disregarding the return value of this function.
	 * @param SoDs Stores SoD constraints
	 * @return {@code true} if SoD constraints are not null, {@code false} otherwise.
	 */
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
