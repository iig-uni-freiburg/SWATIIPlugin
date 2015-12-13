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
import de.uni.freiburg.iig.telematik.sepia.util.PNUtils;
import de.uni.freiburg.iig.telematik.sewol.accesscontrol.rbac.RBACModel;
import de.uni.freiburg.iig.telematik.sewol.accesscontrol.rbac.lattice.RoleLattice;

public class TestResilience {
	
	private static Set transitions = null;
	
	public static void main(String[] args) throws Exception {

		Vector<BoDSoD> BoDs = new Vector<BoDSoD>();
		Vector<BoDSoD> SoDs = new Vector<BoDSoD>();

		
		// Create SoDs
		SoDs.add(new BoDSoD(new Transition("Develop_interview_questions", null), new Transition("Arrange_meeting_with_new_candidate1", null)));
		
		// Create BoDs
		BoDs.add(new BoDSoD(new Transition("Develop_interview_questions", null), new Transition("Conduct_interview", null)));
		BoDs.add(new BoDSoD(new Transition("Develop_interview_questions", null), new Transition("Select_candidate", null)));
	 	BoDs.add(new BoDSoD(new Transition("Determine_pay_range", null), new Transition("Make_employment_offer", null)));	
		
		// Parse Petri Net in order to access transitions and paths
		String net_path = "src/de/uni/freiburg/iig/telematik/swatiiplugin/patrick/ressources/Einstellungsprozess1.pnml";
		AbstractGraphicalPN net = parse_petri_net(net_path);
		
		// Create RBAC Model
		RBACModel acmodel = createRbacModel(net);
		
		// Define User which should not available
		HashSet<String> userset = new HashSet<String>();
		userset.add("Alice");
		userset.add("Bob");
		userset.add("Claire");
		Vector<String> userDeletionCombi = createDeletionCombi(userset);
		
		// Check for resilience
		ResilienceChecker.checkForResilience(net, acmodel, BoDs, SoDs, userDeletionCombi);	
	}
	
	
	
	/*
	 * Creates, sorts and returns the cartesian product of a set of users.
	 * E.g.: Input = Claire, Bob, Alice
	 *       Output = [[[Claire], [Bob], [Bob, Claire], [Alice], [Alice, Claire], [Alice, Bob], [Alice, Bob, Claire]]
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



	static AbstractGraphicalPN parse_petri_net(String path) throws IOException, ParserException, PNValidationException {
		// Parsing Petri Net
		try {
			AbstractGraphicalPN net = PNParsing.parse(new File(path));
			transitions = PNUtils.getNameSetFromTransitions(net.getPetriNet().getTransitions(), false);
			return net;
		} catch (Exception e) {
			return null;
		}
	}
		
	static RBACModel createRbacModel(AbstractGraphicalPN net) {
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
			roleLattice.addRole("bc");
			roleLattice.addRole("de");

			// Allocate User to roles and transitions
			HashSet<String> transitions_for_a = new HashSet<String>();
			HashSet<String> transitions_for_ab = new HashSet<String>();
			HashSet<String> transitions_for_abc = new HashSet<String>();
			HashSet<String> transitions_for_bc = new HashSet<String>();
			HashSet<String> transitions_for_de = new HashSet<String>();			
			
			acmodel.addRoleMembership("Alice", "abc");
			acmodel.addRoleMembership("Bob", "abc");
			acmodel.addRoleMembership("Claire", "abc");
			transitions_for_abc.add("Hire_candidate");
			acmodel.setActivityPermission("abc", transitions_for_abc);
			
			acmodel.addRoleMembership("Alice", "a");
			transitions_for_a.add("Determine_pay_range");
			transitions_for_a.add("Make_employment_offer");		
			acmodel.setActivityPermission("a", transitions_for_a);

			acmodel.addRoleMembership("Alice", "ab");
			acmodel.addRoleMembership("Bob", "ab");
			transitions_for_ab.add("Arrange_meeting_with_new_candidate1");
			acmodel.setActivityPermission("ab", transitions_for_ab);
			
			acmodel.addRoleMembership("Bob", "bc");
			acmodel.addRoleMembership("Claire", "bc");
			transitions_for_bc.add("Develop_interview_questions");
			transitions_for_bc.add("Develop_recruitment_strategy");
			transitions_for_bc.add("Arrange_meeting_with_new_candidate0");
			transitions_for_bc.add("Arrange_meeting_with_new_candidate2");		
			transitions_for_bc.add("Conduct_interview");
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
