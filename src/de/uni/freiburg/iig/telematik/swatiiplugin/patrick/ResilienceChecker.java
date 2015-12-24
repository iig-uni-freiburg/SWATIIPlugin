package de.uni.freiburg.iig.telematik.swatiiplugin.patrick;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Iterator;
import java.util.List;
import java.util.Vector;

import org.sat4j.minisat.SolverFactory;
import de.uni.freiburg.iig.telematik.sepia.graphic.AbstractGraphicalPN;
import de.uni.freiburg.iig.telematik.sepia.petrinet.properties.PropertyCheckingResult;
import de.uni.freiburg.iig.telematik.sewol.accesscontrol.rbac.RBACModel;
import de.uni.freiburg.iig.telematik.sepia.petrinet.properties.sequences.MGTraversalResult;
import de.uni.freiburg.iig.telematik.sepia.petrinet.properties.sequences.SequenceGeneration;
import de.uni.freiburg.iig.telematik.sepia.petrinet.properties.sequences.SequenceGenerationCallableGenerator;
import de.uni.freiburg.iig.telematik.sepia.petrinet.properties.sequences.SequenceGenerationException;

import org.sat4j.reader.DimacsReader;
import org.sat4j.reader.ParseFormatException;
import org.sat4j.reader.Reader;
import org.sat4j.specs.ContradictionException;
import org.sat4j.specs.IProblem;
import org.sat4j.specs.ISolver;
import org.sat4j.specs.TimeoutException;

/**
 * Class to check the resilience of a workflow in form of a Petri Net with absent users.<br>
 * The Petri Net is connected with an authorization model and BoD as well as SoD constraints.<br>
 * Transitions are tested, if they can be fired nevertheless authorized user are not available.<br>
 * The user which initiates this class can as a result lookup a report for the Petri Net to assess its resilience.<br>
 * 
 * @author Patrick Notz
 *
 */
public class ResilienceChecker {
	
	private static int amount_constraints = 0;
	private static Vector<Vector<ArrayList<String>>> summed_up_BoDs = new Vector<Vector<ArrayList<String>>>();
	private static Vector<Vector<ArrayList<String>>> summed_up_SoDs = new Vector<Vector<ArrayList<String>>>();
	private static List<String> constraints = new ArrayList<String>();
	private static ResilienceProperties result = new ResilienceProperties();
	public static Vector<Vector<BoDSoD>> BoDConstraints = new Vector<Vector<BoDSoD>>();
	public static Vector<Vector<BoDSoD>> SoDConstraints = new Vector<Vector<BoDSoD>>();
	public static Vector<WSPTransition> DimacsTransitions = new Vector<WSPTransition>();
	public static String BoDInfo = "";
	public static String SoDInfo = "";
	
	/**
	 * checkForResilience is the main function of this class.
	 * It can be called from an extern class to get information about the resilience of a workflow.
	 * Therefore this function calls multiple sub-functions.
	 * @param net Petri net which is used to extract sequences.
	 * @param acmodel Access control Model to know authorization allocation between user and transition.
	 * @param BoDs Binding of Duty constraints.
	 * @param SoDs Separation of Duty constraints.
	 * @param userDeletionCombi User which should tested when they are not available for the workflow.
	 * @return Returns a variable called result of the type {@link ResilienceProperties}.<br>
	 * It stores plenty of information about the resilience of the workflow.
	 * @throws Exception
	 */
	public static ResilienceProperties checkForResilience(AbstractGraphicalPN net, RBACModel acmodel, Vector<BoDSoD> BoDs, Vector<BoDSoD> SoDs, Vector<String> userDeletionCombi) throws Exception {
		boolean WSP = false;
		int WSPCounter = 0;
		PetriNetProperties validate = new PetriNetProperties(net, acmodel,BoDs, SoDs);
		
		// Check if the Petri net and the RBAC model is correct. Otherwise an error is thrown
		if (validate.net_status == PropertyCheckingResult.TRUE && validate.rbac_status == PropertyCheckingResult.TRUE) {
			Vector<Vector<WSPTransition>> sequences = new Vector<Vector<WSPTransition>>();
			sequences = getSequences(net);
			if (validate.sod_status == PropertyCheckingResult.TRUE) {
				checkSoDRelevance(sequences, SoDs);
				sumUpSoDs(sequences);
			}
			if (validate.bod_status == PropertyCheckingResult.TRUE) {
				checkBoDRelevance(sequences, BoDs);
				sumUpBoDs(sequences, acmodel);
			}
			// An exception could be thrown, if there is no common set of user for a BoD constraint
			if (result.exception.isEmpty()) {
				sequences = addUserToSequence(sequences, acmodel, validate);
				for (int i = 0; i < sequences.size(); i++) {
					String constraintError = "";
					if (validate.bod_status == PropertyCheckingResult.TRUE && validate.sod_status == PropertyCheckingResult.TRUE)
						constraintError = checkConstraints(SoDConstraints.get(i), summed_up_BoDs.get(i));
					if (constraintError.isEmpty()) {
						initializeGraph();
						if (validate.sod_status == PropertyCheckingResult.TRUE && !SoDConstraints.get(i).isEmpty()) 
							convertToDimacs(sequences.get(i), SoDConstraints.get(i));
						else {
							Vector<BoDSoD> emptyVector = new Vector<BoDSoD>();
							convertToDimacs(sequences.get(i), emptyVector);
						}
							
						createSAT4JFile(DimacsTransitions.size());
						if (runSolver()) {
							WSP = false;
							result.isSatifiable = PropertyCheckingResult.TRUE;
						}
						else {
							WSP = true;
							WSPCounter++;
							Report WSPReport = new Report();
							WSPReport.setWSPInfo("WSP found for sequence " + (i+1) + ": " + WSPReport.sequenceToString(sequences.get(i)));
							result.report.addElement(WSPReport);
						}
						if (!WSP) {
							for (int j = 0; j < sequences.get(i).size(); j++) {
								BoDInfo = "";
								SoDInfo = "";
								WSPTransition currentTransition = sequences.get(i).get(j);
								if (sequences.get(i).contains(currentTransition)) {
									if (validate.bod_status == PropertyCheckingResult.TRUE) 
										checkBoDs(sequences.get(i), currentTransition, BoDs);
									if (validate.sod_status == PropertyCheckingResult.TRUE && !summed_up_SoDs.get(i).isEmpty()) 
										checkSoDs(sequences.get(i), currentTransition, summed_up_SoDs.get(i));
									for (int z = 0; z < userDeletionCombi.size(); z++) {
										if (!compareDeletionWithAuthorization(currentTransition, userDeletionCombi.get(z)))
											createReport(sequences.get(i), currentTransition, userDeletionCombi);
									}
								}
							}
						}
					}
					else {
						System.out.println(constraintError);
					}
					WSP = false;
				}
				if (WSPCounter == sequences.size()) {
					result.isSatifiable = PropertyCheckingResult.FALSE;
				}
			}
		}
		return (result);
	}	
	
	
	/**
	 * This function checks every SoD constraint for the given sequence, if it is also a BoD constraint.<br>
	 * Another check verifies that the first transition in a SoD constraint is not also the second.<br>
	 * @param SoDs_for_sequence Separation of Duty constraints for one sequence.
	 * @param BoDs_for_sequence Binding of Duty constraints for one sequence.
	 * @return Exception-message if an error was found .
	 */
	private static String checkConstraints(Vector<BoDSoD> SoDs_for_sequence, Vector<ArrayList<String>> BoDs_for_sequence) {
		String retval = "";
		for (int i = 0; i < SoDs_for_sequence.size(); i++) {
			if (retval.isEmpty()) {
				for (int j = 0; j < BoDs_for_sequence.size(); j++) {
					String SoD1 = SoDs_for_sequence.get(i).getT1().getName();
					String SoD2 = SoDs_for_sequence.get(i).getT2().getName();	
					if (SoD1.equals(SoD2))
						retval = "First SoD constraint is also second SoD constraint(" + SoD1 + " <--> " + SoD2 + ")";
					else {
						if (BoDs_for_sequence.get(j).contains(SoD1) && BoDs_for_sequence.get(j).contains(SoD2) ) {
							retval = "SoD constraint (" + SoD1 + " <--> " + SoD2 + ") is also a BoD constraint";
							break;
						}
					}
				}
			}
		}
		return retval;
	}

	
	/**
	 * Checks, which SoD constraint is relevant for every particular sequence.<br>
	 * E.g. Before function:<br>
	 *  	  Sequence[1] = t1, t2, t3<br>
     *        Sequence[2] = t2, t3<br>
	 *        SoD[1] = t1, t2<br>
	 *        SoD[2] = t2, t3<br>
	 *        SoD[3] = t4, t5<br>
	 * <br>
	 * After function<br>
	 * 		  SoDConstraints[1] (for sequence 1) = [t1, t2], [t2, t3]<br>
	 *        SoDConstraints[2] (for sequence 2) = [t2, t3]<br>
	 * @param sequences Stores all sequences of a workflow.
	 * @param SoDs Stores all SoD constraints between transtitions.
	 */
	private static void checkSoDRelevance(Vector<Vector<WSPTransition>> sequences, Vector<BoDSoD> SoDs) {
		boolean condition1 = false;
		boolean condition2 = false;
		for (int i = 0; i < sequences.size(); i++) {
			Vector<BoDSoD> SoDs2 = new Vector<BoDSoD>();
			SoDs2 = (Vector<BoDSoD>) SoDs.clone();
			SoDConstraints.add(SoDs2);
			for (int j = 0; j < SoDs2.size(); j++) {
				for (int z = 0; z < sequences.get(i).size(); z++) {
					if (sequences.get(i).get(z).getName().equals(SoDs2.get(j).getT1().getName())) {
						condition1 = true;
					}
					if (sequences.get(i).get(z).getName().equals(SoDs2.get(j).getT2().getName())) {
						condition2 = true;
					}				
				}
				if (!condition1 || !condition2) {
					SoDConstraints.get(SoDConstraints.size()-1).remove(j);
					j--;
				}	
				condition1 = false;
				condition2 = false;
			}
		}		
	}

	
	/**
	 * Adds users to the transitions in the different sequences.<br>
	 * It is necessary to distinguish between single transitions and transitions which are connected to other transitions over a BoD constraint.<br>
	 * If an transition belongs to an BoD constraint, the authorized users for that BoD constraint needs to be assigned.<br>
	 * E.g.<br>
	 * Sequence[1] = t1, t2, t3<br>
	 * summed_up_BoDs[1] = t1, t2 [Bob]<br>
	 * <br>
	 * Before function:<br>
	 * Sequence[1].getAuthorizedUsers:<br>
	 * t1 = Alice, Bob<br>
	 * t2 = Bob, Claire<br>
	 * t3 = Alice, Bob, Claire<br> 
	 * <br>
	 * After function:<br>
	 * Sequence[1].getAuthorizedUsers:<br>
	 * t1 = Bob<br>
	 * t2 = Bob<br>
	 * t3 = Alice, Bob, Claire<br>
	 * @param sequences Stores all sequences of a workflow.
	 * @param acmodel Access control Model to know authorization allocation between user and transition.
	 * @param validate Stores information, if there are BoD constraints for the workflow.
	 * @return Returns the sequences with information about authorized user.
	 */
	private static Vector<Vector<WSPTransition>> addUserToSequence(Vector<Vector<WSPTransition>> sequences, RBACModel acmodel, PetriNetProperties validate) {
		for (int i = 0; i < sequences.size(); i++) {
			for (int j = 0; j < sequences.get(i).size(); j++) {
				if (validate.bod_status == PropertyCheckingResult.TRUE && !summed_up_BoDs.get(i).isEmpty()) {
					for (int z = 0; z < summed_up_BoDs.get(i).size(); z++) {
						if (summed_up_BoDs.get(i).get(z).toString().contains(sequences.get(i).get(j).getName())) {
							// setAuthorizedUsers according to BoD 
							sequences.get(i).get(j).setAuthorizedUsers(new ArrayList<String>(Arrays.asList(summed_up_BoDs.get(i).get(z).get(summed_up_BoDs.get(i).get(z).size()-1).toString().split(", "))));
							break;
						}
						else {
							// setAuthorizedUsers according to RBAC 
							sequences.get(i).get(j).setAuthorizedUsers(acmodel.getAuthorizedSubjectsForTransaction(sequences.get(i).get(j).getName()));
						}
					}
				}
				else
					// setAuthorizedUsers according to RBAC 
					sequences.get(i).get(j).setAuthorizedUsers(acmodel.getAuthorizedSubjectsForTransaction(sequences.get(i).get(j).getName()));
			}
		}
		return sequences;
	}

	/**
	 * Sorts and links SoD constraints to sequences. Each SoD constraint is connected to a trace, only when the trace contains the whole SoD constraint.<br>
	 * <br>
	 * E.g.: Before function:<br>
	 * Sequence[0] = t0, t1, t2, t5<br> 
	 * Sequence[1] = t0, t1, t3, t4<br>
	 * Sequence[2] = t1, t3, t4, t5<br>
	 * <br>
	 * SoD constraints: t0, t5<br>
	 * 					t0, t1<br>
	 * 					t1, t5<br>
	 * 					t3, t4<br>
	 * <br>
	 * AuthorizedUsers: t0 = Alice, Claire<br>
	 *                  t1 = Alice, Bob<br>
	 *                  t3 = Claire, Bob<br>
	 *                  t4 = Claire<br>
	 *                  t5 = Alice, Bob, Claire<br>
	 * <br>
	 * After function:<br>
	 * summed_up_SoDs[0] = t0, t1, t5<br>
	 * summed_up_SoDs[1] = []<br>
	 * summed_up_SoDs[2] = [t1, t5], [t3, t4]<br>
	 * @param sequences Stores all sequences of a workflow.
	 */
	private static void sumUpSoDs(Vector<Vector<WSPTransition>> sequences) {
		Vector<Vector<BoDSoD>> sorted_SoDs = new Vector<Vector<BoDSoD>>();
		int first = -1;
		int second = -1;
		Vector<BoDSoD> new_SoDConstraints = new Vector<BoDSoD>();
		// Sort SoD constraints by the transition which appears first in a sequence
		for (int i = 0; i < sequences.size(); i++) {
			new_SoDConstraints.clear();
			for (int z = 0; z < SoDConstraints.get(i).size(); z++) {
				new_SoDConstraints.add(new BoDSoD(SoDConstraints.get(i).get(z).getT1(), SoDConstraints.get(i).get(z).getT2()));
			}
			for (int j = 0; j < new_SoDConstraints.size(); j++) {
				first = -1;
				second = -1;
				for (int x = 0; x < sequences.get(i).size(); x++) {
					if (sequences.get(i).get(x).getName().equals(new_SoDConstraints.get(j).getT1().getName())) {
						first = x;
					}
					if (sequences.get(i).get(x).getName().equals(new_SoDConstraints.get(j).getT2().getName())) {
						second = x;
					}
					if (first != -1 && second != -1) {
						if (first > second) {
							WSPTransition temp = new_SoDConstraints.get(j).getT2();
							new_SoDConstraints.get(j).setT2(new_SoDConstraints.get(j).getT1());
							new_SoDConstraints.get(j).setT1(temp);
							break;
						}
						break;
					}
					if (x == sequences.get(i).size() - 1) {
						new_SoDConstraints.get(j).setT1(null);
						new_SoDConstraints.get(j).setT2(null);
					}
				}
			}
			sorted_SoDs.add((Vector<BoDSoD>) new_SoDConstraints.clone());
		}
		
		Vector<ArrayList<String>> SoDList = new Vector<ArrayList<String>>();
		Vector<Vector<String>> summed_up_SoDConstraints = new Vector<Vector<String>>();
		Vector<String> one_summed_up_SoDConstraints = new Vector<String>();
		boolean bool = false;
		int count = -1;
		
		// Sum up SoD constraints to create a concatenation
		for (int j = 0; j < sorted_SoDs.size(); j++) {
			for (int i = 0; i < sorted_SoDs.get(j).size(); i++) {
				bool = false;
				for (int x = 0; x < one_summed_up_SoDConstraints.size(); x++) {
					if (one_summed_up_SoDConstraints.get(x).contains(sorted_SoDs.get(j).get(i).getT1().getName())) {
						bool = true;
					}
				}
				if (!bool) {
					count++;
					one_summed_up_SoDConstraints.add(count,sorted_SoDs.get(j).get(i).getT1().getName());
					for (int z = sorted_SoDs.get(j).indexOf(sorted_SoDs.get(j).get(i)); z < sorted_SoDs.get(j).size(); z++) {
						for (int y = 0; y < one_summed_up_SoDConstraints.size(); y++) {
							if (one_summed_up_SoDConstraints.get(y).contains(sorted_SoDs.get(j).get(z).getT1().getName())&& !one_summed_up_SoDConstraints.get(y).contains(sorted_SoDs.get(j).get(z).getT2().getName())) {
								one_summed_up_SoDConstraints.set(y, one_summed_up_SoDConstraints.get(y)+ ", "+ sorted_SoDs.get(j).get(z).getT2().getName());
							}
							if (one_summed_up_SoDConstraints.get(y).contains(sorted_SoDs.get(j).get(z).getT2().getName())&& !one_summed_up_SoDConstraints.get(y).contains(sorted_SoDs.get(j).get(z).getT1().getName())) {
								one_summed_up_SoDConstraints.set(y,one_summed_up_SoDConstraints.get(y)+ ", "+ sorted_SoDs.get(j).get(z).getT1().getName());
							}
						}
					}
				}
			}				
			summed_up_SoDConstraints.add((Vector<String>) one_summed_up_SoDConstraints.clone());
			one_summed_up_SoDConstraints.clear();
			count = -1;
		}
		
		// Format and add summed up SoDs to a global variable
		for (int i = 0; i < summed_up_SoDConstraints.size(); i++) {
			for (int j = 0; j < summed_up_SoDConstraints.get(i).size(); j++) {	
				SoDList.add(new ArrayList<String>(Arrays.asList(summed_up_SoDConstraints.get(i).get(j).toString().split(", "))));
			}	
			summed_up_SoDs.add((Vector<ArrayList<String>>) SoDList.clone());
			SoDList.clear();
		}		
	}
	
	/**
	 * Sorts and links BoD constraints to sequences. Each BoD constraint is connected to a trace, only when the trace contains the whole BoD constraint.<br>
	 * Afterwards users are added to the variable BoDConstraints which are allowed to fire all transitions in a trace.<br>
	 * An exception is created, if no common set of users can be found.<br>
	 * <br>
	 * E.g.: Before function:<br>
	 * Sequence[0] = t0, t1, t2, t5<br> 
	 * Sequence[1] = t0, t1, t3, t4<br>
	 * Sequence[2] = t1, t3, t4, t5<br>
	 * <br>
	 * BoD constraints: t0, t5<br>
	 * 					t0, t1<br>
	 * 					t3, t4<br>
	 * <br>
	 * AuthorizedUsers: t0 = Alice, Claire<br>
	 *                  t1 = Alice, Bob<br>
	 *                  t3 = Claire, Bob<br>
	 *                  t4 = Claire<br>
	 *                  t5 = Alice, Bob, Claire<br>
	 * <br>
	 * After function:<br>
	 * summed_up_BoDs[0] = t0, t1, t5, [Alice]<br>
	 * summed_up_BoDs[1] = []<br>
	 * summed_up_BoDs[2] = [t1, t5, [Alice, Bob]], [t3, t4, [Claire]]<br>
	 * @param sequences Stores all sequences of a workflow.
	 * @param acmodel Stores information of users which are allowed to fire transitions.
	 */
	private static void sumUpBoDs(Vector<Vector<WSPTransition>> sequences, RBACModel acmodel) {
		Vector<Vector<BoDSoD>> sorted_BoDs = new Vector<Vector<BoDSoD>>();
		int first = -1;
		int second = -1;
		Vector<BoDSoD> new_BoDConstraints = new Vector<BoDSoD>();
		// Sort BoD constraints by the transition which appears first in a sequence
		for (int i = 0; i < sequences.size(); i++) {
			new_BoDConstraints.clear();
			for (int z = 0; z < BoDConstraints.get(i).size(); z++) {
				new_BoDConstraints.add(new BoDSoD(BoDConstraints.get(i).get(z).getT1(), BoDConstraints.get(i).get(z).getT2()));
			}
			for (int j = 0; j < new_BoDConstraints.size(); j++) {
				first = -1;
				second = -1;
				for (int x = 0; x < sequences.get(i).size(); x++) {
					if (sequences.get(i).get(x).getName().equals(new_BoDConstraints.get(j).getT1().getName())) {
						first = x;
					}
					if (sequences.get(i).get(x).getName().equals(new_BoDConstraints.get(j).getT2().getName())) {
						second = x;
					}
					if (first != -1 && second != -1) {
						if (first > second) {
							WSPTransition temp = new_BoDConstraints.get(j).getT2();
							new_BoDConstraints.get(j).setT2(new_BoDConstraints.get(j).getT1());
							new_BoDConstraints.get(j).setT1(temp);
							break;
						}
						break;
					}
					if (x == sequences.get(i).size() - 1) {
						new_BoDConstraints.get(j).setT1(null);
						new_BoDConstraints.get(j).setT2(null);
					}
				}
			}
			sorted_BoDs.add((Vector<BoDSoD>) new_BoDConstraints.clone());
		}
		
		
		Vector<ArrayList<String>> BoDList = new Vector<ArrayList<String>>();
		Vector<Vector<String>> summed_up_BoDConstraints = new Vector<Vector<String>>();
		Vector<String> one_summed_up_BoDConstraints = new Vector<String>();
		boolean bool = false;
		int count = -1;
		
		// Sum up BoD constraints to create a concatenation
		for (int j = 0; j < sorted_BoDs.size(); j++) {
			for (int i = 0; i < sorted_BoDs.get(j).size(); i++) {
				bool = false;
				for (int x = 0; x < one_summed_up_BoDConstraints.size(); x++) {
					if (one_summed_up_BoDConstraints.get(x).contains(sorted_BoDs.get(j).get(i).getT1().getName())) {
						bool = true;
					}
				}
				if (!bool) {
					count++;
					one_summed_up_BoDConstraints.add(count,sorted_BoDs.get(j).get(i).getT1().getName());
					for (int z = sorted_BoDs.get(j).indexOf(sorted_BoDs.get(j).get(i)); z < sorted_BoDs.get(j).size(); z++) {
						for (int y = 0; y < one_summed_up_BoDConstraints.size(); y++) {
							if (one_summed_up_BoDConstraints.get(y).contains(sorted_BoDs.get(j).get(z).getT1().getName())&& !one_summed_up_BoDConstraints.get(y).contains(sorted_BoDs.get(j).get(z).getT2().getName())) {
								one_summed_up_BoDConstraints.set(y, one_summed_up_BoDConstraints.get(y)+ ", "+ sorted_BoDs.get(j).get(z).getT2().getName());
							}
							if (one_summed_up_BoDConstraints.get(y).contains(sorted_BoDs.get(j).get(z).getT2().getName())&& !one_summed_up_BoDConstraints.get(y).contains(sorted_BoDs.get(j).get(z).getT1().getName())) {
								one_summed_up_BoDConstraints.set(y,one_summed_up_BoDConstraints.get(y)+ ", "+ sorted_BoDs.get(j).get(z).getT1().getName());
							}
						}
					}
				}
			}				
			summed_up_BoDConstraints.add((Vector<String>) one_summed_up_BoDConstraints.clone());
			one_summed_up_BoDConstraints.clear();
			count = -1;
		}
		
		// Format and add summed up BoDs to a global variable
		for (int i = 0; i < summed_up_BoDConstraints.size(); i++) {
			for (int j = 0; j < summed_up_BoDConstraints.get(i).size(); j++) {	
				BoDList.add(new ArrayList<String>(Arrays.asList(summed_up_BoDConstraints.get(i).get(j).toString().split(", "))));
			}	
			summed_up_BoDs.add((Vector<ArrayList<String>>) BoDList.clone());
			BoDList.clear();
		}	

		
		// Adding users to summed_up_BoDs and check if there is a common set of users.
		List<String> authorizedUsers = new ArrayList<String>();
		for (int i = 0; i < summed_up_BoDs.size(); i++) {
			for (int j = 0; j < summed_up_BoDs.get(i).size(); j++) {
				for (int z = 0; z < summed_up_BoDs.get(i).get(j).size(); z++) {
					if (authorizedUsers.isEmpty())
						authorizedUsers = acmodel.getAuthorizedSubjectsForTransaction(summed_up_BoDs.get(i).get(j).get(z));
					else {
						authorizedUsers.retainAll(acmodel.getAuthorizedSubjectsForTransaction(summed_up_BoDs.get(i).get(j).get(z)));
						if (authorizedUsers.isEmpty()) {
							result.exception.add("No common set of users for BoD Transitions" + summed_up_BoDs.get(i).get(j).toString() + "in Sequence " + (i+1) + ": " + sequences.get(i));
						}
					}
				}
				summed_up_BoDs.get(i).get(j).addAll(Arrays.asList(authorizedUsers.toString().substring(1, authorizedUsers.toString().length()-1)));
				authorizedUsers.clear();
			}	
		}	
	}

	
	/**
	 * Checks, which BoD constraint is relevant for every particular sequence.<br>
	 * E.g. Before function:<br>
	 *  	  Sequence[1] = t1, t2, t3<br>
     *        Sequence[2] = t2, t3<br>
	 *        BoD[1] = t1, t2<br>
	 *        BoD[2] = t2, t3<br>
	 *        BoD[3] = t4, t5<br>
	 * <br>
	 * After function<br>
	 * 		  BoDConstraints[1] (for sequence 1) = [t1, t2], [t2, t3]<br>
	 *        BoDConstraints[2] (for sequence 2) = [t2, t3]<br>
	 * @param sequences Stores all sequences of a workflow.
	 * @param BoDs Stores all BoD constraints between transtitions.
	 */
	private static void checkBoDRelevance(Vector<Vector<WSPTransition>> sequences, Vector<BoDSoD> BoDs) {
		boolean condition1 = false;
		boolean condition2 = false;
		for (int i = 0; i < sequences.size(); i++) {
			Vector<BoDSoD> BoDs2 = new Vector<BoDSoD>();
			BoDs2 = (Vector<BoDSoD>) BoDs.clone();
			BoDConstraints.add(BoDs2);
			for (int j = 0; j < BoDs2.size(); j++) {
				for (int z = 0; z < sequences.get(i).size(); z++) {
					if (sequences.get(i).get(z).getName().equals(BoDs2.get(j).getT1().getName())) {
						condition1 = true;
					}
					if (sequences.get(i).get(z).getName().equals(BoDs2.get(j).getT2().getName())) {
						condition2 = true;
					}
					
				}
				if (!condition1 || !condition2) {
					BoDConstraints.get(BoDConstraints.size()-1).remove(j);
					j--;
				}	
				condition1 = false;
				condition2 = false;
			}
		}		
	}

	/*
	 * Deletes CNF file if it exists already and initializes all global variables
	 */
	private static void initializeGraph() {
		// 
		File CNFfile = new File("src/de/uni/freiburg/iig/telematik/swatiiplugin/patrick/ressources/DimacsFile.cnf");
		if (CNFfile.exists()) {
			CNFfile.delete();
		}

		DimacsTransitions.clear();
		amount_constraints = 0;
		constraints.clear();
		
	}

	
	/**
	 * The user of {@link ResilienceChecker} needs to be informed, if a transition is connected to other transitions over a BoD constraint.<br>
	 * The authorized user, which need to be available and the BoD constraint which contains currentTransition is saved in BoDInfo.<br>
	 * BoDInfo is later added to a report which can be printed for examination.
	 * @param currentSequence Stores the sequence which needs to be checked.
	 * @param currentTransition Stores the transition which needs to be checked.
	 * @param BoDs Stores the BoD constraints which need to be checked.
	 */
	private static void checkBoDs(Vector<WSPTransition> currentSequence, WSPTransition currentTransition, Vector<BoDSoD> BoDs) {
		int index = 0;
		Report report = new Report();
		List<String> userlistT1 = new ArrayList<String>();
		List<String> userlistT2 = new ArrayList<String>();
		for (int i = 0; i < BoDs.size(); i++) {
			if (currentTransition.getName().equals(BoDs.get(i).getT2().getName())) {
				if (report.sequenceToString(currentSequence).contains(BoDs.get(i).getT1().getName())) {
					for (int j = 0; j < currentSequence.size(); j++) {
						if (currentSequence.get(j).getName().equals(BoDs.get(i).getT1().getName()))
							index = j;
					}
					userlistT2 = currentTransition.getAuthorizedUsers();
					userlistT1 = currentSequence.get(index).getAuthorizedUsers();
					userlistT2.retainAll(userlistT1);
					if (userlistT2.size() == 1) {
						BoDInfo = userlistT2 + " needs to be available because of the BoD-Constraint to Transition " + currentSequence.get(index).getName() + " " + currentSequence.get(index).getAuthorizedUsers();
					}
						
					else {
						BoDInfo = "One of the following subjects need to be available: " + userlistT2 + " depending on who performed " + currentSequence.get(index).getName() + " " + currentSequence.get(index).getAuthorizedUsers();
					}
				}
			}
		}
	}
	

	/**
	 * Extracts authorized users from the given input.
	 * @param currentSequence Stores the sequence which needs to be checked for the transition
	 * @param Transition Stores the transition, which contains authorized users
	 * @return List of authorized users for currentSequence and Transition
	 */
	private static List<String> getUser(Vector<WSPTransition> currentSequence, String Transition) {
		int index = 0;
		for (int j = 0; j < currentSequence.size(); j++) {
			if (currentSequence.get(j).getName().equals(Transition))
				index = j;
		}
		return currentSequence.get(index).getAuthorizedUsers();
	}
	
	
	/**
	 * The user of {@link ResilienceChecker} needs to be informed, if a transition is connected to other transitions over a SoD constraint.<br>
	 * The authorized user, which need to be available and the SoD constraint which contains currentTransition is saved in SoDInfo.<br>
	 * SoDInfo is later added to a report which can be printed for examination.
	 * @param currentSequence Stores the sequence which needs to be checked.
	 * @param currentTransition Stores the transition which needs to be checked.
	 * @param summedUpSoDsForSequence Stores the SoD constraints for currentSequence.
	 */
	private static void checkSoDs(Vector<WSPTransition> currentSequence, WSPTransition currentTransition, Vector<ArrayList<String>> summedUpSoDsForSequence) {
		Vector<List<String>> userlists = new Vector<List<String>>();
		int number_of_SoD = 0;
		for (int i = 0; i < summedUpSoDsForSequence.size(); i++) {
			for (int j = 0; j < summedUpSoDsForSequence.get(i).size(); j++) {
				// if currentStransition is in SoD constraint
				if (summedUpSoDsForSequence.get(i).get(j).equals(currentTransition.getName())) {
					// It is the first SoD if j < 1 --> not relevant as no authorized user are restricted
					if (j >= 1) {
						number_of_SoD = i;
						for (int z = 0; z <= j; z++) {
							// Adds relevant user of the SoD constraints to userlists
							userlists.add(getUser(currentSequence, summedUpSoDsForSequence.get(i).get(z)));
						}
					}
					
					Vector<String> single_user = new Vector<String>();
					Vector<String> delete_user = new Vector<String>();
					if (!userlists.isEmpty()) {
						// Adds user from userlists to single_user and ignores duplicates
						// e.G: (t1 A,C; t2 A,B; t3 A,B,C <--- single_user = A,B,C
						for (int z = 0; z < userlists.size()-1; z++) {
							for (int x = 0; x < userlists.get(z).size(); x++)
								if (!single_user.contains(userlists.get(z).get(x))) {
									single_user.add(userlists.get(z).get(x));
								}
							
							// Deletes user from single_user if they are not important
							// e.G: t1,t2,t3 = 3 Transitions
							//      single_user.size = 3 = A,B,C
							//      It's OK, if these user are not available for the next SoD transition (t4), as none of them can fire the transition
							if (single_user.size()-1 == z) {
								for (int y = 0; y < single_user.size(); y++) {
									if (!delete_user.contains(single_user.get(y))) {
										delete_user.add(single_user.get(y));
									}
								}
							}
						}		
						// Delete A,B,C from t4 A,B,C,D
						// --> D is extraordinary relevant for t4
						for (int y = 0; y < delete_user.size(); y++) {
							if (userlists.get(userlists.size()-1).contains(delete_user.get(y))) {
								userlists.get(userlists.size()-1).remove(userlists.get(userlists.size()-1).indexOf(delete_user.get(y)));
							}
						}
						if (userlists.get(userlists.size()-1).size() == 1)
							SoDInfo = userlists.get(userlists.size()-1) + " needs to be available because of the SoD-Constraint:" + summedUpSoDsForSequence.get(number_of_SoD);
						else
							SoDInfo = "One of the follwing user: " + userlists.get(userlists.size()-1) + " need to be available because of the SoD-Constraint:" + summedUpSoDsForSequence.get(number_of_SoD);
					}		
				}
			}	
		}
	}
	
	
	/**
	 * This function merges all information from the resilience checks of a transition into a report.<br>
	 * It's important to exclude duplicates from the report, as this routine is called for every single userDeletionCombi.<br>
	 * Therefore the hashcodes of all reports are compared to the report which should be added.
	 * @param currentSequence Stores the sequence which needs to be checked.
	 * @param currentTransition Stores the transition which needs to be checked.
	 * @param userDeletionCombi Stores all combinations of user, which are not available for the workflow.
	 */
	private static void createReport(Vector<WSPTransition> currentSequence, WSPTransition currentTransition, Vector<String> userDeletionCombi) {
		Report report = new Report();
		boolean duplicate = false;
		if (BoDInfo.isEmpty() && SoDInfo.isEmpty())
			report.setRelevantUsers(currentTransition.getAuthorizedUsers());
		else {
			if (!BoDInfo.isEmpty())
				report.setBoDInfo(BoDInfo);
			if (!SoDInfo.isEmpty())
				report.setSoDInfo(SoDInfo);
		}		
		report.setTestedUsers(userDeletionCombi);
		report.setSequence(currentSequence);
		report.setTransition(currentTransition);
		if (result.report.isEmpty()) 
			result.report.add(report);
		else {
			for (Iterator<Report> it = result.report.iterator(); it.hasNext();) { 
				if (report.hashCode() == it.next().hashCode())
					duplicate = true;
			}
			
			if (!duplicate)
				result.report.add(report);
		}			
	}

	/**
	 * This function checks, if all user in oneUserDeletionCombi hinder currentTransition to get fired.<br>
	 * E.g: t1 = A,B,C<br>
	 *      oneUserDeletionCombi = A,B<br>
	 *      returns false (A and B are not relevant for firing t1, as there is still C available)<br>
	 *      <br>
	 *      t2 = B,C<br>
	 *      oneUserDeletionCombi = B,C<br>
	 *      returns true<br>  
	 * @param currentTransition Stores the transition which needs to be checked.
	 * @param oneUserDeletionCombi Stores one combination of user, which are not available for the workflow.
	 * @return Return {@code true}, if all authorized user of currentTransition are also in oneUserDeletionCombi and if there are no extra user in currentTransition.<br>  
	 */
	private static boolean compareDeletionWithAuthorization(WSPTransition currentTransition, String oneUserDeletionCombi) {
		boolean retval = false;
		List<String> userDeletionList = new ArrayList<>();
		String temp = oneUserDeletionCombi.replace("[", "");
		temp = temp.replace("]", "");
		userDeletionList = Arrays.asList(temp.split(", "));
		if (currentTransition.getAuthorizedUsers().size() <= userDeletionList.size()) {
			if (userDeletionList.containsAll(currentTransition.getAuthorizedUsers()))
				retval = true;
		}	
		return retval;
	}

	
	/**
	 * Splits all sequences which are stored in the variable "sequences" into single transitions and pack them into newSequences.<br>
	 * @param net Stores the sequences. Transitions are not directly accessable.
	 * @return Returns a Vector, which stores a Vector for {@link WSPTransition}.<br>
	 * The advantage is, that every single transition of every sequence is accessable.<br>
	 * E.g.: newSequences.get[2].get[3] = t1
	 * @throws SequenceGenerationException
	 */
	static Vector<Vector<WSPTransition>> getSequences(AbstractGraphicalPN net) throws SequenceGenerationException {
		SequenceGenerationCallableGenerator generator = new SequenceGenerationCallableGenerator(net.getPetriNet());
		MGTraversalResult sequnences = SequenceGeneration.getFiringSequences(generator);		
		Vector<Vector<WSPTransition>> newSequences = new Vector<Vector<WSPTransition>>();
		Vector<WSPTransition> transitions = new Vector<WSPTransition>();
		
		// Iterate over sequences and extract the transitions
		for (Iterator<List<String>> it = sequnences.getCompleteSequences().iterator(); it.hasNext();) {
			String currentSequence = it.next().toString();		
			int beginn = 1;
			int end = 0;
			while (end != -1) {
				end = currentSequence.indexOf(", ", beginn);
				if (end != -1) {
					transitions.add(new WSPTransition (currentSequence.substring(beginn, end), null));
					beginn = end+2;
				}
			}
			end = currentSequence.indexOf("]", beginn);
			transitions.add(new WSPTransition (currentSequence.substring(beginn, end), null));
			newSequences.add((Vector<WSPTransition>)transitions.clone());
			transitions.clear();	
		}	
		return newSequences;
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
	 * This function sets a minus before each variable in a constraint.<br>
	 * This is necessary for SoD constraints.<br>
	 * E.g:<br>
	 * SoD: (t1 Bob WSPNumber 1 <--> t3 Bob WSPNumber 5)<br>
	 * Constraint: -1 -5<br>
	 * Meaning: Bob in t1 and Bob in t3 are not allowed to be both active<br>
	 * <br>
	 * @param constraint: Stores a constraint e.g: 1 5
	 * @return Returns a new constraint e.g: -1 -5
	 */
	static String convertNAND(String constraint) {
		constraint = constraint.replace(" ", " -");
		constraint = "-" + constraint;
		return constraint;
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