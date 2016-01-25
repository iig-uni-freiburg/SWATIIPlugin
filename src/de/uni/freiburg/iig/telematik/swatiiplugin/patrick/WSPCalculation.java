package de.uni.freiburg.iig.telematik.swatiiplugin.patrick;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Vector;

import org.sat4j.minisat.SolverFactory;
import org.sat4j.reader.DimacsReader;
import org.sat4j.reader.ParseFormatException;
import org.sat4j.reader.Reader;
import org.sat4j.specs.ContradictionException;
import org.sat4j.specs.IProblem;
import org.sat4j.specs.ISolver;
import org.sat4j.specs.TimeoutException;

import de.uni.freiburg.iig.telematik.sepia.petrinet.properties.PropertyCheckingResult;

/**
 * This class contains functions about examining a workflow regarding satisfiability.<br>
 * It can be seen as a necessary extension of {@link RobustnessChecker}.
 * 
 * @author Patrick Notz
 *
 */
public class WSPCalculation {
	
	public static Vector<WSPTransition> DimacsTransitions = new Vector<WSPTransition>();
	public static RobustnessProperties result = new RobustnessProperties();
	public static boolean WSP = false;
	public static int amount_constraints = 0;
	public static List<String> constraints = new ArrayList<String>();

	/**
	 * This is the main function of {@link WSPCalculation}.
	 * It calls sub-functions for calculations regarding the Workflow Satisfiability Problem.
	 * 
	 * @param sod_status Contains information, if the workflow contains Sod-statements.
	 * @param SoDConstraints Contains all relevant SoD-constraints concerning the sequence.
	 * @param summed_up_BoDs Contains all relevant BoD-constraints concerning the sequence.
	 * @param sequence Stores the sequence which needs to be examined.
	 * @throws IOException
	 */
	public void calculateWSP(PropertyCheckingResult sod_status, Vector<BoDSoD> SoDConstraints, Vector<Vector<ArrayList<String>>> summed_up_BoDs, Vector<WSPTransition> sequence) throws IOException {
		initializeWSPCalculation();
		if (sod_status == PropertyCheckingResult.TRUE && !SoDConstraints.isEmpty()) 
			convertToDimacs(sequence, SoDConstraints);
		else {
			Vector<BoDSoD> emptyVector = new Vector<BoDSoD>();
			convertToDimacs(sequence, emptyVector);
		}
		
		createSAT4JFile(DimacsTransitions.size());
		if (runSolver()) {
			WSP = false;
		}
		else {
			WSP = true;
		}
	}
	
	/**
	 * Deletes CNF file if it exists already and initializes all global variables
	 */
	private static void initializeWSPCalculation() {
		File CNFfile = new File("src/de/uni/freiburg/iig/telematik/swatiiplugin/patrick/ressources/DimacsFile.cnf");
		if (CNFfile.exists()) {
			CNFfile.delete();
		}

		DimacsTransitions.clear();
		amount_constraints = 0;
		constraints.clear();	
	}
	
	
	/**
	 * This function converts a sequence with SoD constraints to the DIMACS format.<br>
	 * This is necessary for checking the workflow regarding the WSP later.<br>
	 * The variable DimacsTransitions contains a WSPNumber for each user of a transition and the user itself (WSPUser)<br>
	 * E.g.: currentTransition = t1 [Alice, Bob, Claire]<br>
	 * 		 DimacsTransitions[0] = 1 Alice<br>
	 * 		 DimacsTransitions[1] = 2 Bob<br>
	 * 		 DimacsTransitions[2] = 3 Claire<br>
	 * 		 <br>	
	 * 		 currentTransition = t2 [Claire]<br>
	 *       DimacsTransitions[3] = 4 Claire<br>
	 *       
	 * This is passed to convertXOR()<br>
	 * Subsequently the transitions are checked, if they are in a SoD constraint.<br>
	 * The WSPNumber of the WSPUsers which belong to both SoD transitions are passed to convertNAND().<br>
	 * <br>
	 * E.g.: SoD t1 [Alice(1), Bob(2)] <--> t2 [Alice(3), Bob(4), Claire(5)]<br>
	 * convertNAND(1, 3)<br>
	 * convertNAND(2, 4)<br>
	 * @param onesequence Stores the sequence which needs to be converted
	 * @param SoDConstraints_for_sequence Stores SoD constraints for the sequence which needs to be converted
	 * @throws IOException
	 */
	static void convertToDimacs(Vector<WSPTransition> onesequence, Vector<BoDSoD> SoDConstraints_for_sequence) throws IOException {
		Integer count = 0;
		for (int i = 0; i < onesequence.size(); i++) {
			WSPTransition currentTransition = onesequence.get(i);
			for (int j = 0; j < currentTransition.getAuthorizedUsers().size(); j++) {
				count++;
				currentTransition.setWSPuser(currentTransition.getAuthorizedUsers().get(j));
				currentTransition.setWSPNumber(count);
				DimacsTransitions.addElement(currentTransition.clone());
			}
			convertXOR(count, currentTransition.getAuthorizedUsers().size());
		}
			
		for (int i = 0; i < DimacsTransitions.size(); i++) {
			for (int z = 0; z < DimacsTransitions.size(); z++) {
				if (DimacsTransitions.elementAt(i).getWSPuser().contentEquals(DimacsTransitions.elementAt(z).getWSPuser()) && DimacsTransitions.elementAt(i).getName().compareTo(DimacsTransitions.elementAt(z).getName()) < 0) {
					for (int j = 0; j < SoDConstraints_for_sequence.size(); j++) {
						if (SoDConstraints_for_sequence.get(j).getT1().getName().toString().contentEquals(DimacsTransitions.elementAt(i).getName())&& SoDConstraints_for_sequence.get(j).getT2().getName().contentEquals(DimacsTransitions.elementAt(z).getName())) {
							constraints.add(convertNAND((i + 1) + " " + (z + 1)) + " 0");
							amount_constraints++;
						}
						if (SoDConstraints_for_sequence.get(j).getT2().getName().contentEquals(DimacsTransitions.elementAt(i).getName())&& SoDConstraints_for_sequence.get(j).getT1().getName().contentEquals(DimacsTransitions.elementAt(z).getName())) {
							constraints.add(convertNAND((i + 1) + " " + (z + 1)) + " 0");
							amount_constraints++;
						}
					}
				}
			}
		}
	}
	
	
	/**
	 * This function creates DIMACS lines to enforce, that exactly one user is firing a transition at once.<br>
	 * It's important to distinguish between the number of variables (saved in variable part.<br>
	 * The line has to be converted different.<br>
	 * <br>
	 * E.g:<br>
	 * part = 1; overall = 3<br>
	 * Line in File for part = 1: 3 0<br>
	 * Meaning: 3 has to be true --> only user 3 is allowed to fire this transition<br>
	 * <br>
	 * part = 2; overall = 9<br>
	 * Line in File for part = 2: 8 9 0 <br>
	 *                           -8 -9 0<br>
	 * Meaning: 8 or 9 has to be true and 8 or 9 has to be false --> User 8 or 9 is allowed to fire this transition<br>
	 * <br>
	 * part = 4; overall = 21<br>
	 * Line in File for part > 2: -18 -19 -20 -21 0<br>
	 *                             18 -19 -20 -21 0<br>
	 *                            -18 19 -20 -21 0<br>
	 *                            -18 -19 20 -21 0<br>
	 *                            -18 -19 -20 21 0<br>
	 * Meaning: One of the variables 18, 19, 20 or 21 has to be true and the rest has to be false. One of the users 18-21 is allowed to fire this transition
	 * @param overall Stores the highest WSPNumber, which is passed to this function. 
	 * @param part Number of variables (users) for a transition
	 * @throws IOException
	 */
	static void convertXOR(int overall, int part)throws IOException {

		String constraint = "";
		String line = "";

		// check how many variables are needed and name them from 1 .. i
		// E.g.: overall = 7, part = 3 --> constraint = "5 6 7"
		for (int i = overall - (part - 1); i <= overall; i++) {
			constraint = constraint + i + " ";
		}

		// delete last space from String constraint and add a zero to complete it
		constraint = constraint.substring(0, constraint.length() - 1);
		if (part > 1) {
			constraint = constraint + " 0";
			constraints.add(constraint);
			amount_constraints++;
			constraint = constraint.substring(0, constraint.length() - 2);
		}

		// set a minus before each variable in constraint
		constraint = convertNAND(constraint);

		constraint = constraint + " 0";

		// If there is only one variable (part) in the constraint to convert,
		// this needs to be skipped as a negation of one variable is not allowed
		if (part > 1) {
			constraints.add(constraint);
			amount_constraints++;
		}

		// search for all "-" in constraint and remove it separately
		if (part != 2) {
			for (int i = -1; (i = constraint.indexOf("-", i + 1)) != -1;) {
				line = constraint.substring(0, i) + constraint.substring(i + 1, constraint.length());
				constraints.add(line);
				amount_constraints++;
			}
		}
	}
	
	
	/**
	 * This function sets a minus before each variable in a constraint.<br>
	 * This is necessary for SoD constraints.<br>
	 * E.g:<br>
	 * SoD: (t1 Bob WSPNumber 1 <--> t3 Bob WSPNumber 5)<br>
	 * Constraint: -1 -5<br>
	 * Meaning: Bob in t1 and Bob in t3 are not allowed to be both active<br>
	 * <br>
	 * @param constraint Stores a constraint e.g: 1 5
	 * @return Returns a new constraint e.g: -1 -5
	 */
	static String convertNAND(String constraint) {
		constraint = constraint.replace(" ", " -");
		constraint = "-" + constraint;
		return constraint;
	}
	
	
	/**
	 * Creates a file in DIMACS format with the constraints that were formed in convertXOR and convertNAND. 
	 * @param amount_variables Stores the amount of variables, which is needed for the first Line
	 * @throws IOException
	 */
	static void createSAT4JFile(int amount_variables) throws IOException {
		FileWriter fw = new FileWriter("src/de/uni/freiburg/iig/telematik/swatiiplugin/patrick/ressources/DimacsFile.cnf");
		BufferedWriter bw = new BufferedWriter(fw);

		// first line in CNF file
		bw.write("p cnf " + amount_variables + " " + amount_constraints);

		// add every constraint
		for (int i = 0; i < constraints.size(); i++) {
			bw.newLine();
			bw.write(constraints.get(i));
		}
		bw.close();
	}
	
	/**
	 *  Runs SAT4J-Solver to check a DIMACS file for a WSP.<br>
	 *  The DIMACS file contains one sequence.<br>
	 * @return {@code true} if the sequence is satisfiable, {@code false} otherwise 
	 */
	static boolean runSolver() {
		boolean retval = false;
		ISolver solver = SolverFactory.newDefault();
		solver.setTimeout(3600); // 1 hour timeout
		Reader reader = new DimacsReader(solver);
		try {
			IProblem problem = reader.parseInstance("src/de/uni/freiburg/iig/telematik/swatiiplugin/patrick/ressources/DimacsFile.cnf");
			if (problem.isSatisfiable()) {
				retval = true;
			}
		} catch (FileNotFoundException e) {
			result.exception.add("CNF file not found! \n");
		} catch (ParseFormatException e) {
			result.exception.add("CNF parsing exception! \n");
		} catch (IOException e) {
			result.exception.add("CNF IO Fehler! \n");
		} catch (ContradictionException e) {
			// WSP because of simple error in RBAC Model (SoD t1 Alice <--> t3 Alice)
			retval = false;
		} catch (TimeoutException e) {
			result.exception.add("Timeout, sorry! \n");
		}
		return retval;
	}
}
