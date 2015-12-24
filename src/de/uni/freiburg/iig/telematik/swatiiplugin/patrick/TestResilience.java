package de.uni.freiburg.iig.telematik.swatiiplugin.patrick;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.Vector;

import com.google.common.collect.Sets;
import de.invation.code.toval.misc.soabase.SOABase;
import de.invation.code.toval.misc.soabase.SOABaseProperties;
import de.invation.code.toval.parser.ParserException;
import de.uni.freiburg.iig.telematik.sepia.exception.PNValidationException;
import de.uni.freiburg.iig.telematik.sepia.graphic.AbstractGraphicalPN;
import de.uni.freiburg.iig.telematik.sepia.parser.PNParsing;
import de.uni.freiburg.iig.telematik.sepia.petrinet.properties.PropertyCheckingResult;
import de.uni.freiburg.iig.telematik.sepia.util.PNUtils;
import de.uni.freiburg.iig.telematik.sewol.accesscontrol.rbac.RBACModel;
import de.uni.freiburg.iig.telematik.sewol.accesscontrol.rbac.lattice.RoleLattice;

/**
 * Test class for creating and parsing a Petri net with a RBAC Model with the goal to organize the input for the class {@link ResilienceChecker}.<br>
 * Optionally, Separation of Duty (SoD) and / or Binding of Duty (BoD) constraints can be defined.<br>
 * ResilienceChecker returns an instance of the class {@link ResilienceProperties}.<br>
 * The return value is going to be checked and printed in form of a report.
 * 
 * @author Patrick Notz
 *
 */

public class TestResilience {
	
	private static Set transitions = null;
	private static ResilienceProperties result = new ResilienceProperties();
	
	
	/**
	 * Calls up different functions to call {@link ResilienceChecker} and output the results.
	 * @param args
	 * @throws Exception
	 */
	public static void main(String[] args) throws Exception {
		Vector<BoDSoD> BoDs = new Vector<BoDSoD>();
		Vector<BoDSoD> SoDs = new Vector<BoDSoD>();
		
		/*
		 *  Create SoDs
 		 *  Activate fifth SoD constraint to test that first SoD constraint is also the second SoD constraint
 		 *  Activate sixth SoD constraint to test that the whole workflow is not satisfiable
 		 *  Activate seventh SoD constraint to test that the third sequence is not satisfiable
		 */
		SoDs.add(new BoDSoD(new WSPTransition("Develop_interview_questions", null), new WSPTransition("Arrange_meeting_with_new_candidate1", null)));
		SoDs.add(new BoDSoD(new WSPTransition("Develop_interview_questions", null), new WSPTransition("Hire_candidate", null)));
		SoDs.add(new BoDSoD(new WSPTransition("Arrange_meeting_with_new_candidate1", null), new WSPTransition("Hire_candidate", null)));
		SoDs.add(new BoDSoD(new WSPTransition("Post_job_internally", null), new WSPTransition("Examine_existing_resumes_on_file", null)));
		// SoDs.add(new BoDSoD(new WSPTransition("Develop_interview_questions", null), new WSPTransition("Develop_interview_questions", null)));
		// SoDs.add(new BoDSoD(new WSPTransition("Develop_interview_questions", null), new WSPTransition("Make_employment_offer", null)));
		// SoDs.add(new BoDSoD(new WSPTransition("Determine_pay_range", null), new WSPTransition("Arrange_meeting_with_new_candidate1", null)));

		/* 
		 * Create BoDs
		 * Activate third BoD constraint to test that there is no common set of users for the summed up BoD constraints
		 */
		BoDs.add(new BoDSoD(new WSPTransition("Develop_interview_questions", null), new WSPTransition("Conduct_interview", null)));
		BoDs.add(new BoDSoD(new WSPTransition("Develop_interview_questions", null), new WSPTransition("Select_candidate", null)));
		//BoDs.add(new BoDSoD(new WSPTransition("Develop_interview_questions", null), new WSPTransition("Determine_pay_range", null)));

		// Parse Petri Net in order to access transitions and paths
		String net_path = "src/de/uni/freiburg/iig/telematik/swatiiplugin/patrick/ressources/Einstellungsprozess1.pnml";
		AbstractGraphicalPN net = parse_petri_net(net_path);
		
		// Create RBAC Model
		RBACModel acmodel = createRbacModel();
		
		// Define set of users which should be removed from the workflow
		HashSet<String> userset = new HashSet<String>();
		userset.add("Alice");
		userset.add("Bob");
		userset.add("Claire");
		Vector<String> userDeletionCombi = createDeletionCombi(userset);
		
		// Check for resilience
		result = ResilienceChecker.checkForResilience(net, acmodel, BoDs, SoDs, userDeletionCombi);
		
		// Print results of ResilienceChecker
		outputReport();
	}
	
	
	/**
	 * Print results of ResilienceChecker stored in the variable "result"
	 */
	private static void outputReport() {
		if (result.exception.isEmpty()) {
			if (result.isSatifiable == PropertyCheckingResult.TRUE)
				// Output of resilience information
				result.printReport();
			else
				// WSP found for whole workflow (all sequences)
				System.out.println("The whole Workflow is not satisfiable");
		}
		else {
			// Output of all exceptions
			for (int i=0; i<result.exception.size(); i++) {
				System.out.println(result.exception.get(i));
			}
		}
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
	 * Function to parse a Petri Net.
	 * 
	 * @param path Path to Petri Net.
	 * @return Instance of {@link AbstractGraphicalPN} parsed from the path
	 * @throws IOException
	 * @throws ParserException
	 * @throws PNValidationException
	 */
	static AbstractGraphicalPN parse_petri_net(String path) throws IOException, ParserException, PNValidationException {
		// Parsing Petri Net
		try {
			AbstractGraphicalPN net = PNParsing.parse(new File(path));
			// Store transitions of the Petri Net
			transitions = PNUtils.getNameSetFromTransitions(net.getPetriNet().getTransitions(), false);
			return net;
		} catch (Exception e) {
			return null;
		}
	}
	
	
	/**
	 * Creates a RBAC model which needs to be compatible to the transitions of the Petri net which is parsed. 
	 * @return Instance of the class {@link RBACModel} which represents an access control model
	 */
	static RBACModel createRbacModel() {
		RBACModel acmodel = null;
		try {
			// Creating User
			Set<String> subjects = new HashSet<String>();
			subjects.add("Alice");
			subjects.add("Bob");
			subjects.add("Claire");
			subjects.add("Dave");
			subjects.add("Emil");

			// Creating RBAC-Model. Each role is a unique combination of
			// subjects which are allowed to execute transitions
			SOABaseProperties propertiesContext = new SOABaseProperties();
			propertiesContext.setSubjects(subjects);
			propertiesContext.setActivities(transitions);
			propertiesContext.setName("propertyname");
			SOABase context = new SOABase(propertiesContext);
			RoleLattice roleLattice = new RoleLattice();
			acmodel = new RBACModel("propertyname", context, roleLattice);

			// Creating Roles
			roleLattice.addRole("a");
			roleLattice.addRole("ab");
			roleLattice.addRole("abc");
			roleLattice.addRole("b");
			roleLattice.addRole("bc");
			roleLattice.addRole("de");

			// Allocate User to roles and transitions
			HashSet<String> transitions_for_a = new HashSet<String>();
			HashSet<String> transitions_for_ab = new HashSet<String>();
			HashSet<String> transitions_for_abc = new HashSet<String>();
			HashSet<String> transitions_for_b = new HashSet<String>();
			HashSet<String> transitions_for_bc = new HashSet<String>();
			HashSet<String> transitions_for_de = new HashSet<String>();			
			
			acmodel.addRoleMembership("Alice", "abc");
			acmodel.addRoleMembership("Bob", "abc");
			acmodel.addRoleMembership("Claire", "abc");
			transitions_for_abc.add("Hire_candidate");
			acmodel.setActivityPermission("abc", transitions_for_abc);
			
			acmodel.addRoleMembership("Alice", "a");
			transitions_for_a.add("Determine_pay_range");	
			acmodel.setActivityPermission("a", transitions_for_a);

			acmodel.addRoleMembership("Alice", "ab");
			acmodel.addRoleMembership("Bob", "ab");
			transitions_for_ab.add("Arrange_meeting_with_new_candidate1");
			transitions_for_ab.add("Conduct_interview");
			acmodel.setActivityPermission("ab", transitions_for_ab);
			
			acmodel.addRoleMembership("Bob", "b");
			transitions_for_b.add("Make_employment_offer");		
			acmodel.setActivityPermission("b", transitions_for_b);
			
			acmodel.addRoleMembership("Bob", "bc");
			acmodel.addRoleMembership("Claire", "bc");
			transitions_for_bc.add("Develop_interview_questions");
			transitions_for_bc.add("Develop_recruitment_strategy");
			transitions_for_bc.add("Arrange_meeting_with_new_candidate0");
			transitions_for_bc.add("Arrange_meeting_with_new_candidate2");		
			transitions_for_bc.add("Select_candidate");
			acmodel.setActivityPermission("bc", transitions_for_bc);
			
			acmodel.addRoleMembership("Dave", "de");
			acmodel.addRoleMembership("Emil", "de");
			transitions_for_de.add("Post_job_internally");
			transitions_for_de.add("Examine_existing_resumes_on_file");
			transitions_for_de.add("Place_ads");
			transitions_for_de.add("Conduct_preliminary_interview");
			acmodel.setActivityPermission("de", transitions_for_de);
	
			return acmodel;
		} catch (Exception e) {
			return null;
		}
	}
}
