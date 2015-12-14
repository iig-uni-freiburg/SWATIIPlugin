package de.uni.freiburg.iig.telematik.swatiiplugin.patrick;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
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

public class ResilienceChecker {
	
	private static int amount_constraints = 0;
	private static Vector<Vector<ArrayList<String>>> summed_up_BoDs = new Vector<Vector<ArrayList<String>>>();
	private static List<String> constraints = new ArrayList<String>();
	private static SatisfiabilityProperties result = new SatisfiabilityProperties();
	public static Vector<Vector<BoDSoD>> BoDConstraints = new Vector<Vector<BoDSoD>>();
	public static Vector<Vector<BoDSoD>> SoDConstraints = new Vector<Vector<BoDSoD>>();
	public static Vector<Transition> DimacsTransitions = new Vector<Transition>();
	
	static void checkForResilience(AbstractGraphicalPN net, RBACModel acmodel, Vector<BoDSoD> BoDs, Vector<BoDSoD> SoDs, Vector<String> userDeletionCombi) throws Exception {

		boolean WSP = false;
		PetriNetProperties validate = new PetriNetProperties(net, acmodel,BoDs, SoDs);
		
		// Check if the petri net and the RBAC model is correct. Otherwise an error is thrown
		if (validate.net_status == PropertyCheckingResult.TRUE && validate.rbac_status == PropertyCheckingResult.TRUE) {
			Vector<Vector<Transition>> sequences = new Vector<Vector<Transition>>();
			sequences = getSequences(net);
			// Sorts all BoDs by T1
			Collections.sort(BoDs);
			checkSoDRelevance(sequences, SoDs);
			checkBoDRelevance(sequences, BoDs);
			sumUpBoDs(acmodel);
			sequences = addUserToSequence(sequences, acmodel);
			for (int i = 0; i < sequences.size(); i++) {
				String SoDIsAlsoBoD = checkSoDIsAlsoBoD(SoDConstraints.get(i), summed_up_BoDs.get(i));
				if (SoDIsAlsoBoD.isEmpty()) {
					initializeGraph();
					convertToDimacs(sequences.get(i), SoDConstraints.get(i));
					createSAT4JFile(DimacsTransitions.size(), i);
					if (runSolver() == true)
						System.out.println("No WSP");
					else {
						System.out.println("WSP");
						WSP = true;
					}
					/*
					if (WSP == false) {
						for (int j = 0; j < sequences.get(i).size(); j++) {
							Transition currentTransition = sequences.get(i).get(j);
							if (sequences.get(i).contains(currentTransition)) {
								List<String> authorizedUsers = new ArrayList<String>();
								authorizedUsers = acmodel.getAuthorizedSubjectsForTransaction(currentTransition.getTransition().getName());
								for (int z = 0; z < userDeletionCombi.size(); z++) {
									String BoDSoDInfo = checkBoDs(sequences.get(i), currentTransition, BoDs, acmodel);
									if (BoDSoDInfo.equals("LEERE MENGE"))
										result.isSatifiable = PropertyCheckingResult.FALSE;
									else
										if (compareDeletionWithAuthorization(authorizedUsers, currentTransition, userDeletionCombi.get(z), BoDs) == true)
											createReport(sequences.get(i), currentTransition, authorizedUsers, userDeletionCombi, BoDSoDInfo);
								}
							}
						}
					}
					*/
				}
				else {
					System.out.println(SoDIsAlsoBoD);
				}
				WSP = false;
			}
		}
		result.printReport();	
		/*
		for (int i = 0; i < result.exception.size(); i++)
			System.out.println(result.exception.get(i) + "\n");
		*/
	}	

	
	/*
	 * This function checks every SoD Constraint, if it is also a BoD Constraint
	 */
	private static String checkSoDIsAlsoBoD(Vector<BoDSoD> SoDs_for_sequence, Vector<ArrayList<String>> BoDs_for_sequence) {
		String retval = "";
		for (int i = 0; i < SoDs_for_sequence.size(); i++) {
			if (retval.isEmpty()) {
				for (int j = 0; j < BoDs_for_sequence.size(); j++) {
					String SoD1 = SoDs_for_sequence.get(i).getT1().getTransition().getName();
					String SoD2 = SoDs_for_sequence.get(i).getT2().getTransition().getName();					
					if (BoDs_for_sequence.get(j).contains(SoD1) && BoDs_for_sequence.get(j).contains(SoD2) ) {
						retval = "SoD Constraint (" + SoD1 + " <--> " + SoD2 + ") is also a BoD Constraint";
						break;
					}
				}
			}
		}
		return retval;
	}



	/*
	 *  Checks every SoD constraint, if it is needed for the sequences
	 *  E.g. Input:
	 *        Sequence[1] = t1, t2, t3
     *        Sequence[2] = t2, t3
	 *        SoD[1] = t1, t2
	 *        SoD[2] = t2, t3
	 *        SoD[3] = t4, t5
	 *       
	 *        Output:
	 *        SoDConstraints[1] (for sequence 1) = [t1, t2], [t2, t3]
	 *        SoDConstraints[2] (for sequence 2) = [t2, t3]
	 */	
	private static void checkSoDRelevance(Vector<Vector<Transition>> sequences, Vector<BoDSoD> SoDs) {
		boolean condition1 = false;
		boolean condition2 = false;
		for (int i = 0; i < sequences.size(); i++) {
			Vector<BoDSoD> SoDs2 = new Vector<BoDSoD>();
			SoDs2 = (Vector<BoDSoD>) SoDs.clone();
			SoDConstraints.add(SoDs2);
			for (int j = 0; j < SoDs2.size(); j++) {
				for (int z = 0; z < sequences.get(i).size(); z++) {
					if (sequences.get(i).get(z).getTransition().getName().equals(SoDs2.get(j).getT1().getTransition().getName())) {
						condition1 = true;
					}
					if (sequences.get(i).get(z).getTransition().getName().equals(SoDs2.get(j).getT2().getTransition().getName())) {
						condition2 = true;
					}
					
				}
				if (condition1 == false || condition2 == false) {
					SoDConstraints.get(SoDConstraints.size()-1).remove(j);
					j--;
				}	
				condition1 = false;
				condition2 = false;
			}
		}		
	}


	/*
	 * Adds users to the transitions in the different sequences.
	 * It is necessary to distinguish between single transitions and transitions which are connected with a BoD constraint to other transitions.
	 * If an transition belongs to an BoD constraint, the authorized users for that BoD constraint needs to be assigned.
	 * 
	 * E.g. Input:
	 * Sequence[1] = t1, t2, t3
	 * summed_up_BoDs[1] = t1, t2 [Bob]
	 * 
	 * Authorized Users according to RBAC Model:
	 * t1 = Alice, Bob
	 * t2 = Bob, Claire
	 * t3 = Alice, Bob, Claire 
	 * 
	 * Authorized Users when considering BoD constraints for Sequence[1]:
	 * t1 = Bob
	 * t2 = Bob
	 * t3 = Alice, Bob, Claire
	 * 
	 * Goal of the function: Transitions with authorized Users are accessable through the variable sequences
	 */
	private static Vector<Vector<Transition>> addUserToSequence(Vector<Vector<Transition>> sequences, RBACModel acmodel) {
		for (int i = 0; i < sequences.size(); i++) {
			for (int j = 0; j < sequences.get(i).size(); j++) {
				for (int z = 0; z < summed_up_BoDs.get(i).size(); z++) {
					if (summed_up_BoDs.get(i).get(z).toString().contains(sequences.get(i).get(j).getTransition().getName())) {						
						sequences.get(i).get(j).setAuthorizedUsers(Arrays.asList(summed_up_BoDs.get(i).get(z).get(summed_up_BoDs.get(i).get(z).size()-1).toString().split(", ")));
						break;
					}
					else {
						sequences.get(i).get(j).setAuthorizedUsers(acmodel.getAuthorizedSubjectsForTransaction(sequences.get(i).get(j).getTransition().getName()));
					}
				}
			}
		}
		return sequences;
	}



	/*
	 * Links BoD-Constraints to sequences. Each BoD-Constraint is connected to a trace, only when the trace contains the whole BoDConstraint.
	 * Afterwards Users are added to the variable BoDConstraints which are allowed to fire all transitions in a constraint 
	 * 
	 * E.g.: Input: 
	 * Sequence[0] = t0, t1, t2, t5 
	 * Sequence[1] = t0, t1, t3, t4
	 * Sequence[2] = t1, t3, t4, t5
	 * 
	 * BoD-Constraints: t0, t5
	 * 					t1, t5
	 * 					t3, t4
	 * 
	 * AuthorizedUsers: t0 = Alice, Claire
	 *                  t1 = Alice, Bob
	 *                  t3 = Claire, Bob
	 *                  t4 = Claire
	 *                  t5 = Alice, Bob, Claire
	 * 
	 * Output:
	 * summed_up_BoDs[0] = t0, t1, t5, [Alice]
	 * summed_up_BoDs[1] = []
	 * summed_up_BoDs[2] = [t1, t5 [Alice, Bob]], [t3, t4 [Claire]
	 */
	private static void sumUpBoDs(RBACModel acmodel) {
		Vector<ArrayList<String>> BoDList = new Vector<ArrayList<String>>();
		Vector<Vector<String>> summed_up_BoDConstraints = new Vector<Vector<String>>();
		Vector<String> one_summed_up_BoDConstraints = new Vector<String>();
		boolean bool = false;
		int count = -1;
		
		// Sum up BoD Constraints
		for (int j = 0; j < BoDConstraints.size(); j++) {
			for (int i = 0; i < BoDConstraints.get(j).size(); i++) {
				bool = false;
				for (int x = 0; x < one_summed_up_BoDConstraints.size(); x++) {
					if (one_summed_up_BoDConstraints.get(x).contains(BoDConstraints.get(j).get(i).getT1().getTransition().getName())) {
						bool = true;
					}
				}
				if (bool == false) {
					count++;
					one_summed_up_BoDConstraints.add(count,BoDConstraints.get(j).get(i).getT1().getTransition().getName());
					for (int z = BoDConstraints.get(j).indexOf(BoDConstraints.get(j).get(i)); z < BoDConstraints.get(j).size(); z++) {
						for (int y = 0; y < one_summed_up_BoDConstraints.size(); y++) {
							if (one_summed_up_BoDConstraints.get(y).contains(BoDConstraints.get(j).get(z).getT1().getTransition().getName())&& one_summed_up_BoDConstraints.get(y).contains(BoDConstraints.get(j).get(z).getT2().getTransition().getName()) == false) {
								one_summed_up_BoDConstraints.set(y, one_summed_up_BoDConstraints.get(y)+ ", "+ BoDConstraints.get(j).get(z).getT2().getTransition().getName());
							}
							if (one_summed_up_BoDConstraints.get(y).contains(BoDConstraints.get(j).get(z).getT2().getTransition().getName())&& one_summed_up_BoDConstraints.get(y).contains(BoDConstraints.get(j).get(z).getT1().getTransition().getName()) == false) {
								one_summed_up_BoDConstraints.set(y,one_summed_up_BoDConstraints.get(y)+ ", "+ BoDConstraints.get(j).get(z).getT1().getTransition().getName());
							}
						}
					}
				}
			}				
			summed_up_BoDConstraints.add((Vector<String>) one_summed_up_BoDConstraints.clone());
			one_summed_up_BoDConstraints.clear();
			count = -1;
		}
		
		// Adding summed up BoDs to summed_up_BoDs
		for (int i = 0; i < summed_up_BoDConstraints.size(); i++) {
			for (int j = 0; j < summed_up_BoDConstraints.get(i).size(); j++) {	
				BoDList.add(new ArrayList<String>(Arrays.asList(summed_up_BoDConstraints.get(i).get(j).toString().split(", "))));
			}	
			summed_up_BoDs.add((Vector<ArrayList<String>>) BoDList.clone());
			BoDList.clear();
		}
			
		
		// Adding users to summed_up_BoDs
		List<String> authorizedUsers = new ArrayList<String>();
		for (int i = 0; i < summed_up_BoDs.size(); i++) {
			for (int j = 0; j < summed_up_BoDs.get(i).size(); j++) {
				for (int z = 0; z < summed_up_BoDs.get(i).get(j).size(); z++) {
					if (authorizedUsers.isEmpty())
						authorizedUsers = acmodel.getAuthorizedSubjectsForTransaction(summed_up_BoDs.get(i).get(j).get(z));
					else {
						authorizedUsers.retainAll(acmodel.getAuthorizedSubjectsForTransaction(summed_up_BoDs.get(i).get(j).get(z)));
						if (authorizedUsers.isEmpty()) {
							result.exception.add("No common set of users for BoD Transitions" + summed_up_BoDs.get(i).get(j).toString());
						}
					}
				}
				summed_up_BoDs.get(i).get(j).addAll(Arrays.asList(authorizedUsers.toString().substring(1, authorizedUsers.toString().length()-1)));
				authorizedUsers.clear();
			}	
		}
	}

	/*
	 * Similar to function checkSoDRelevance()
	 */
	private static void checkBoDRelevance(Vector<Vector<Transition>> sequences, Vector<BoDSoD> BoDs) {
		boolean condition1 = false;
		boolean condition2 = false;
		for (int i = 0; i < sequences.size(); i++) {
			Vector<BoDSoD> BoDs2 = new Vector<BoDSoD>();
			BoDs2 = (Vector<BoDSoD>) BoDs.clone();
			BoDConstraints.add(BoDs2);
			for (int j = 0; j < BoDs2.size(); j++) {
				for (int z = 0; z < sequences.get(i).size(); z++) {
					if (sequences.get(i).get(z).getTransition().getName().equals(BoDs2.get(j).getT1().getTransition().getName())) {
						condition1 = true;
					}
					if (sequences.get(i).get(z).getTransition().getName().equals(BoDs2.get(j).getT2().getTransition().getName())) {
						condition2 = true;
					}
					
				}
				if (condition1 == false || condition2 == false) {
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

	private static String checkBoDs(Vector<Transition> sequences, Transition currentTransition, Vector<BoDSoD> BoDs, RBACModel acmodel) {
		String BoDSoDInfo = "";
		List<String> userlistT1 = new ArrayList<String>();
		List<String> userlistT2 = new ArrayList<String>();
		for (int i = 0; i < BoDs.size(); i++) {
			if (currentTransition.equals(BoDs.get(i).getT2().getTransition().getName())) {
				if (sequences.contains(BoDs.get(i).getT1().getTransition().getName())) {
					userlistT2 = acmodel.getAuthorizedSubjectsForTransaction(currentTransition.getTransition().getName());
					userlistT1 = acmodel.getAuthorizedSubjectsForTransaction(BoDs.get(i).getT1().getTransition().getName());
					userlistT2.retainAll(userlistT1);
					if (userlistT2.isEmpty())
						BoDSoDInfo = "LEERE MENGE";
					else
						BoDSoDInfo = "One of the following subjects needs to be available: " + userlistT2;
				}
			}
		}
		return BoDSoDInfo;
	}

	private static void createReport(Vector<Transition> sequences, Transition currentTransition, List<String> authorizedUsers, Vector<String> userDeletionCombi, String BoDSoDInfo) {
		Report report = new Report();
		boolean duplicate = false;
		if (!BoDSoDInfo.isEmpty())
			report.setBoDSoDInfo(BoDSoDInfo);
		else
			report.setRelevantUsers(authorizedUsers);
		report.setTestedUsers(userDeletionCombi);
		report.setSequence(sequences);
		report.setTransition(currentTransition);
		if (result.report.isEmpty()) 
			result.report.add(report);
		else {
			for (Iterator<Report> it = result.report.iterator(); it.hasNext();) { 
				if (report.hashCode() == it.next().hashCode())
					duplicate = true;
			}
			if (duplicate == false)
				result.report.add(report);
		}			
	}

	private static boolean compareDeletionWithAuthorization(List<String> authorizedUsers, Transition currentTransition, String userDeletionCombi, Vector<BoDSoD> BoDs) {
		boolean retval = false;
		List<String> userDeletionList = new ArrayList<>();
		String temp = userDeletionCombi.replace("[", "");
		temp = temp.replace("]", "");
		userDeletionList = Arrays.asList(temp.split(", "));
		if (authorizedUsers.size() <= userDeletionList.size()) {
			if (userDeletionList.containsAll(authorizedUsers))
				retval = true;
		}	
		return retval;
	}

	
	/*
	 * Splits all sequences which are stored in the variable "sequences" into single transitions and pack them into newSequences
	 * Input: [[Post_job_internally, Examine_existing_resumes_on_file, Arrange_meeting_with_new_candidate0, Conduct_preliminary_interview, Develop_interview_questions, Conduct_interview, Select_candidate, Determine_pay_range, Make_employment_offer, Hire_candidate], [Post_job_internally, Arrange_meeting_with_new_candidate2, Conduct_preliminary_interview, Develop_interview_questions, Conduct_interview, Select_candidate, Determine_pay_range, Make_employment_offer, Hire_candidate], [Post_job_internally, Examine_existing_resumes_on_file, Develop_recruitment_strategy, Place_ads, Arrange_meeting_with_new_candidate1, Conduct_preliminary_interview, Develop_interview_questions, Conduct_interview, Select_candidate, Determine_pay_range, Make_employment_offer, Hire_candidate]]
	 * Output: newSequences.get[1] = [Post_job_internally, Examine_existing_resumes_on_file, Arrange_meeting_with_new_candidate0, Conduct_preliminary_interview, Develop_interview_questions, Conduct_interview, Select_candidate, Determine_pay_range, Make_employment_offer, Hire_candidate]
	 *         newSequences.get[2] = [Post_job_internally, Arrange_meeting_with_new_candidate2, Conduct_preliminary_interview, Develop_interview_questions, Conduct_interview, Select_candidate, Determine_pay_range, Make_employment_offer, Hire_candidate]
	 *         newSequences.get[3] = [Post_job_internally, Examine_existing_resumes_on_file, Develop_recruitment_strategy, Place_ads, Arrange_meeting_with_new_candidate1, Conduct_preliminary_interview, Develop_interview_questions, Conduct_interview, Select_candidate, Determine_pay_range, Make_employment_offer, Hire_candidate]]
	 *         
	 * Advantage: Every single transition is accessable: newSequences.get[2].get[3] = Conduct_preliminary_interview
	 *            A transition is connected to for example authorized Users: newSequences.get[2].get[3].getAuthorizedUsers() = Dave, Emil
	 */
	static Vector<Vector<Transition>> getSequences(AbstractGraphicalPN net) throws SequenceGenerationException {
		SequenceGenerationCallableGenerator generator = new SequenceGenerationCallableGenerator(net.getPetriNet());
		MGTraversalResult sequnences = SequenceGeneration.getFiringSequences(generator);		
		Vector<Vector<Transition>> newSequences = new Vector<Vector<Transition>>();
		Vector<Transition> transitions = new Vector<Transition>();
		
		for (Iterator<List<String>> it = sequnences.getCompleteSequences().iterator(); it.hasNext();) {
			String currentSequence = it.next().toString();
			
			int beginn = 1;
			int end = 0;
			while (end != -1) {
				end = currentSequence.indexOf(", ", beginn);
				if (end != -1) {
					transitions.add(new Transition (currentSequence.substring(beginn, end), null));
					beginn = end+2;
				}
			}
			end = currentSequence.indexOf("]", beginn);
			transitions.add(new Transition (currentSequence.substring(beginn, end), null));
			newSequences.add((Vector<Transition>)transitions.clone());
			transitions.clear();
			
		}	
		return newSequences;
	}
	
	
	/*
	 * Creates the Dimacs File for the SAT4J solver
	 */
	static void createSAT4JFile(int amount_variables, int j) throws IOException {
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
	
	/*
	 * Sets a Minus before each variable in a constraint
	 * E.g: constraint = 2 5
	 *      new_constraint = -2 -5
	 */
	static String convertNAND(String constraint) throws IOException {
		constraint = constraint.replace(" ", " -");
		constraint = "-" + constraint;
		return constraint;
	}
	
	
	/*
	 * The variable DimacsTransitions contains a Number for each user of a transition and the user isself
	 * E.g.: currentTransition = t1 [Alice, Bob, Claire]
	 * 		 DimacsTransitions[0] = 1 Alice
	 * 		 DimacsTransitions[1] = 2 Bob
	 * 		 DimacsTransitions[2] = 3 Claire
	 * 
	 * 		 currentTransition = t2 [Claire]
	 *       DimacsTransitions[3] = 4 Claire
	 *       
	 * This is passed to convertXOR()
	 * Subsequently the transitions are checked, if they are in a SoD constraint.
	 * The number of the users which belong to both SoD transitions are passed to convertNAND()
	 * E.g.: SoD t1 [Alice(1), Bob(2)] <--> t2 [Alice(3), Bob(4), Claire(5)]
	 * convertNAND(1, 3)
	 * convertNAND(2, 4) 
	 */
	static void convertToDimacs(Vector<Transition> onesequence, Vector<BoDSoD> SoDConstraints_for_sequence) throws IOException, CloneNotSupportedException {
		Integer count = 0;
		for (int i = 0; i < onesequence.size(); i++) {
			Transition currentTransition = onesequence.get(i);
			for (int j = 0; j < currentTransition.getAuthorizedUsers().size(); j++) {
				count++;
				currentTransition.setWSPuser(currentTransition.getAuthorizedUsers().get(j));
				currentTransition.setWSPNumber(count);
				DimacsTransitions.addElement((Transition) currentTransition.clone());
			}
			convertXOR(count, currentTransition.getAuthorizedUsers().size());
		}
			
		for (int i = 0; i < DimacsTransitions.size(); i++) {
			for (int z = 0; z < DimacsTransitions.size(); z++) {
				if (DimacsTransitions.elementAt(i).getWSPuser().contentEquals(DimacsTransitions.elementAt(z).getWSPuser()) && DimacsTransitions.elementAt(i).getTransition().getName().compareTo(DimacsTransitions.elementAt(z).getTransition().getName()) < 0) {
					for (int j = 0; j < SoDConstraints_for_sequence.size(); j++) {
						if (SoDConstraints_for_sequence.get(j).getT1().getTransition().getName().toString().contentEquals(DimacsTransitions.elementAt(i).getTransition().getName())&& SoDConstraints_for_sequence.get(j).getT2().getTransition().getName().contentEquals(DimacsTransitions.elementAt(z).getTransition().getName())) {
							constraints.add(convertNAND((i + 1) + " " + (z + 1)) + " 0");
							amount_constraints++;
						}
						if (SoDConstraints_for_sequence.get(j).getT2().getTransition().getName().contentEquals(DimacsTransitions.elementAt(i).getTransition().getName())&& SoDConstraints_for_sequence.get(j).getT1().getTransition().getName().contentEquals(DimacsTransitions.elementAt(z).getTransition().getName())) {
							constraints.add(convertNAND((i + 1) + " " + (z + 1)) + " 0");
							amount_constraints++;
						}
					}
				}
			}
		}
	}
	
	static void convertXOR(int overall, int part)throws IOException {
		/*
		 * It's important to distinguish between the number of variables (saved in variable part) when
		 * they are converted to constraints in the CNF Dimacs File. The line has to be converted different
		 * 
		 * Line in File for part = 1: 3 0
		 * Meaning: 3 has to be true
		 * 
		 * Line in File for part = 2: 8 9 0 
		 *                           -8 -9 0
		 * Meaning: 8 or 9 has to be true and 8 or 9 has to be false
		 * 
		 * Line in File for part > 2: -18 -19 -20 -21 0
		 *                             18 -19 -20 -21 0
		 *                            -18 19 -20 -21 0
		 *                            -18 -19 20 -21 0
		 *                            -18 -19 -20 21 0
		 * Meaning: One of the variables 18, 19, 20 or 21 has to be true and the rest has to be false
		 */

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
	
	// Runs SAT4J Solver to check the sequence for a WSP
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
			result.exception.add("Unsatisfiable (trivial)! \n");
		} catch (TimeoutException e) {
			result.exception.add("Timeout, sorry! \n");
		}
		return retval;
	}
}
