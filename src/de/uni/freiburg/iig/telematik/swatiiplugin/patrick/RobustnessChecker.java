package de.uni.freiburg.iig.telematik.swatiiplugin.patrick;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Set;
import java.util.Vector;

import com.google.common.collect.Sets;

import de.uni.freiburg.iig.telematik.sepia.graphic.AbstractGraphicalPN;
import de.uni.freiburg.iig.telematik.sepia.petrinet.properties.PropertyCheckingResult;
import de.uni.freiburg.iig.telematik.sewol.accesscontrol.rbac.RBACModel;
import de.uni.freiburg.iig.telematik.sepia.petrinet.properties.sequences.MGTraversalResult;
import de.uni.freiburg.iig.telematik.sepia.petrinet.properties.sequences.SequenceGeneration;
import de.uni.freiburg.iig.telematik.sepia.petrinet.properties.sequences.SequenceGenerationCallableGenerator;
import de.uni.freiburg.iig.telematik.sepia.petrinet.properties.sequences.SequenceGenerationException;

/**
 * Class to check the robustness of a workflow in form of a Petri Net with absent users.<br>
 * The Petri Net is connected with an authorization model and BoD as well as SoD constraints.<br>
 * Transitions are tested, if they can be fired nevertheless authorized user are not available.<br>
 * The user which initiates this class can as a result lookup a report for the Petri Net to assess its robustness.<br>
 * 
 * @author Patrick Notz
 *
 */
public class RobustnessChecker {
	
	private static Vector<Vector<ArrayList<String>>> summed_up_BoDs = new Vector<Vector<ArrayList<String>>>();
	private static Vector<Vector<ArrayList<String>>> summed_up_SoDs = new Vector<Vector<ArrayList<String>>>();
	private static RobustnessProperties result = new RobustnessProperties();
	public static Vector<Vector<BoDSoD>> BoDConstraints = new Vector<Vector<BoDSoD>>();
	public static Vector<Vector<BoDSoD>> SoDConstraints = new Vector<Vector<BoDSoD>>();
	public static String firstBoDInfo = "";
	public static String secondBoDInfo = "";
	public static String SoDInfo = "";
	
	/**
	 * checkForRobustness is the main function of this class.
	 * It can be called from an extern class to get information about the robustness of a workflow.
	 * Therefore this function calls multiple sub-functions.
	 * @param net Petri net which is used to extract sequences.
	 * @param acmodel Access control Model to know authorization allocation between user and transition.
	 * @param BoDs Binding of Duty constraints.
	 * @param SoDs Separation of Duty constraints.
	 * @param userDeletionCombi User which should tested when they are not available for the workflow.
	 * @return Returns a variable called result of the type {@link RobustnessProperties}.<br>
	 * It stores plenty of information about the robustness of the workflow.
	 * @throws Exception
	 */
	public static RobustnessProperties checkForRobustness(AbstractGraphicalPN net, RBACModel acmodel, Vector<BoDSoD> BoDs, Vector<BoDSoD> SoDs, HashSet<String> userset) throws Exception {
		int WSPCounter = 0;
		PetriNetProperties validate = new PetriNetProperties(net, acmodel,BoDs, SoDs);
		
		// Sorts all BoDs by first transition
		Collections.sort(BoDs);
	
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
				Vector<String> userDeletionCombi = createDeletionCombi(userset);
				sequences = addUserToSequence(sequences, acmodel, validate);
				for (int i = 0; i < sequences.size(); i++) {
					String constraintError = "";
					if (validate.bod_status == PropertyCheckingResult.TRUE && validate.sod_status == PropertyCheckingResult.TRUE)
						constraintError = checkConstraints(SoDConstraints.get(i), summed_up_BoDs.get(i));
					if (constraintError.isEmpty()) {
						WSPCalculation wspcalc = new WSPCalculation();
						wspcalc.calculateWSP(validate.sod_status, SoDConstraints.get(i), summed_up_BoDs, sequences.get(i));
						if (!wspcalc.result.exception.isEmpty()) {
							for (int z = 0; z < wspcalc.result.exception.size(); z++)
								result.exception.add(wspcalc.result.exception.get(z));
							return result;
						}
						if (!wspcalc.WSP) {
							for (int j = 0; j < sequences.get(i).size(); j++) {
								firstBoDInfo = "";
								secondBoDInfo = "";
								SoDInfo = "";
								WSPTransition currentTransition = sequences.get(i).get(j);
								if (validate.bod_status == PropertyCheckingResult.TRUE) 
									checkBoDs(sequences.get(i), currentTransition, BoDs, acmodel);
								if (validate.sod_status == PropertyCheckingResult.TRUE && !summed_up_SoDs.get(i).isEmpty()) 
									checkSoDs(sequences.get(i), currentTransition, summed_up_SoDs.get(i));
								for (int z = 0; z < userDeletionCombi.size(); z++) {
									if (compareDeletionWithAuthorization(currentTransition, userDeletionCombi.get(z)))
										createReport(sequences.get(i), currentTransition, userDeletionCombi, acmodel);
								}
							}
						}
						else {
							Report WSPReport = new Report();
							WSPReport.setWSPInfo("Workflow is not satisfiable for sequence " + (i+1) + ": " + WSPReport.sequenceToString(sequences.get(i)));
							result.report.addElement(WSPReport);
							WSPCounter++;
						}
					}
					else {
						result.report.clear();
						System.out.println(constraintError);
						break;
					}
				}
				if (WSPCounter == sequences.size() && result.exception.isEmpty())
					result.isSatifiable = PropertyCheckingResult.FALSE;
				else
					result.isSatifiable = PropertyCheckingResult.TRUE;
			}
		}
		return (result);
	}	
	
	
	/**
	 * Creates, sorts and returns the cartesian product of a set of user.
	 *       
	 * @param userset Set of user which should be taken from the workflow, e.g. [Claire, Bob, Alice]
	 * @return Cartesian product of userset, e.g. [Claire], [Bob], [Bob, Claire], [Alice], [Alice, Claire], [Alice, Bob], [Alice, Bob, Claire]
	 */
	private static Vector<String> createDeletionCombi(HashSet<String> userset) {	
		Vector<String> userDeletionCombi = new Vector<String>();
		Set<Set<String>> set = Sets.powerSet(userset);
		for(Set<String> item : set){	
			List sortedList = new ArrayList(item);
			Collections.sort(sortedList);
			userDeletionCombi.add(String.valueOf(sortedList));
		}	
		// userDeletionCombi[0] = []
		userDeletionCombi.remove(0);
		return userDeletionCombi;
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
	 * summed_up_SoDs[0] = [t0, t1, t5]<br>
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
							Report report = new Report();
							result.exception.add("No common set of users for BoD Transitions" + summed_up_BoDs.get(i).get(j).toString() + "in Sequence " + (i+1) + ": " + report.sequenceToString(sequences.get(i)));
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


	/**
	 * The user of {@link RobustnessChecker} needs to be informed, if a transition is connected to other transitions over a BoD constraint.<br>
	 * The authorized user, which need to be available and the BoD constraint which contains currentTransition is saved in BoDInfo.<br>
	 * BoDInfo is later added to a report which can be printed for examination.
	 * @param currentSequence Stores the sequence which needs to be checked.
	 * @param currentTransition Stores the transition which needs to be checked.
	 * @param BoDs Stores the BoD constraints which need to be checked.
	 * @param acmodel 
	 */
	private static void checkBoDs(Vector<WSPTransition> currentSequence, WSPTransition currentTransition, Vector<BoDSoD> BoDs, RBACModel acmodel) {
		int index = 0;
		Report report = new Report();
		boolean BoD1 = false;
		boolean BoD2 = false;
		List<String> userlistT1 = new ArrayList<String>();
		List<String> userlistT2 = new ArrayList<String>();
		for (int i = 0; i < BoDs.size(); i++) {
			BoD1 = currentTransition.getName().equals(BoDs.get(i).getT2().getName());
			BoD2 = currentTransition.getName().equals(BoDs.get(i).getT1().getName());
			if (BoD1 || BoD2) {
				if (report.sequenceToString(currentSequence).contains(BoDs.get(i).getT1().getName()) && BoD1) {
					for (int j = 0; j < currentSequence.size(); j++) {
						if (currentSequence.get(j).getName().equals(BoDs.get(i).getT1().getName()))
							index = j;
					}
				}
				if (report.sequenceToString(currentSequence).contains(BoDs.get(i).getT2().getName()) && BoD2) {
					for (int j = 0; j < currentSequence.size(); j++) {
						if (currentSequence.get(j).getName().equals(BoDs.get(i).getT2().getName()))
							index = j;
					}
				}
					userlistT2 = currentTransition.getAuthorizedUsers();
					userlistT1 = currentSequence.get(index).getAuthorizedUsers();
					userlistT2.retainAll(userlistT1);
					if (currentTransition.getAuthorizedUsers() != acmodel.getAuthorizedSubjectsForTransaction(currentTransition.getName()))
						if (userlistT1.size() == 1) {
							firstBoDInfo = userlistT1 + " needs to be available because of the BoD-Constraint to transition " + currentTransition.getName();
						}
						else {
							firstBoDInfo = "One of the following subjects need to be available: " + userlistT1 + "because of the BoD-Constraint to transition " + currentTransition.getName();
						}
					if (userlistT2.size() == 1) {
						secondBoDInfo = userlistT2 + " needs to be available because of the BoD-Constraint to transition " + currentSequence.get(index).getName();
					}
						
					else {
						secondBoDInfo = "One of the following subjects need to be available: " + userlistT2 + " depending on who performed " + currentSequence.get(index).getName() + " " + currentSequence.get(index).getAuthorizedUsers();
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
	 * The user of {@link RobustnessChecker} needs to be informed, if a transition is connected to other transitions over a SoD constraint.<br>
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
							SoDInfo = "One of the follwing subjects: " + userlists.get(userlists.size()-1) + " need to be available because of the SoD-Constraint:" + summedUpSoDsForSequence.get(number_of_SoD);
					}		
				}
			}	
		}
	}
	
	
	/**
	 * This function merges all information from the robustness checks of a transition into a report.<br>
	 * It's important to exclude duplicates from the report, as this routine is called for every single userDeletionCombi.<br>
	 * Therefore the hashcodes of all reports are compared to the report which should be added.
	 * @param currentSequence Stores the sequence which needs to be checked.
	 * @param currentTransition Stores the transition which needs to be checked.
	 * @param userDeletionCombi Stores all combinations of user, which are not available for the workflow.
	 * @param originalTransition 
	 * @param acmodel 
	 */
	private static void createReport(Vector<WSPTransition> currentSequence, WSPTransition currentTransition, Vector<String> userDeletionCombi, RBACModel acmodel) {
		Report report = new Report();
		boolean duplicate = false;
		if (firstBoDInfo.isEmpty() && SoDInfo.isEmpty() && secondBoDInfo.isEmpty())
			report.setRelevantSubjects(currentTransition.getAuthorizedUsers());
		else {
			if (!firstBoDInfo.isEmpty())
				report.setBoDInfo(firstBoDInfo);
			if (!secondBoDInfo.isEmpty())
				report.setBoDInfo(secondBoDInfo);
			if (!SoDInfo.isEmpty())
				report.setSoDInfo(SoDInfo);
		}		
		report.setAllSubjects(acmodel.getAuthorizedSubjectsForTransaction(currentTransition.getName()));
		report.setTestedSubjects(userDeletionCombi);
		report.setSequence(currentSequence);
		report.setTransition(currentTransition);
		if (result.report.isEmpty()) 
			result.report.add(report);
		else {
			for (Iterator<Report> it = result.report.iterator(); it.hasNext();) {
				Report tempReport = it.next();
				if (tempReport.getWSPInfo().isEmpty())
					if (report.hashCode() == tempReport.hashCode())
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
}