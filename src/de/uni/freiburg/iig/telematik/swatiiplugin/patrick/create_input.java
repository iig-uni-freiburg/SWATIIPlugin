package de.uni.freiburg.iig.telematik.swatiiplugin.patrick;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;
import java.util.Vector;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableSet;
import com.google.common.collect.Sets;
import de.invation.code.toval.misc.soabase.SOABase;
import de.invation.code.toval.misc.soabase.SOABaseProperties;
import de.invation.code.toval.parser.ParserException;
import de.invation.code.toval.types.HashList;
import de.uni.freiburg.iig.telematik.sepia.exception.PNValidationException;
import de.uni.freiburg.iig.telematik.sepia.graphic.AbstractGraphicalPN;
import de.uni.freiburg.iig.telematik.sepia.parser.PNParsing;
import de.uni.freiburg.iig.telematik.sepia.petrinet.pt.PTTransition;
import de.uni.freiburg.iig.telematik.sepia.util.PNUtils;
import de.uni.freiburg.iig.telematik.sewol.accesscontrol.rbac.RBACModel;
import de.uni.freiburg.iig.telematik.sewol.accesscontrol.rbac.lattice.RoleLattice;
import de.uni.freiburg.iig.telematik.swat.patterns.logic.patterns.BoundedWith;
import de.uni.freiburg.iig.telematik.swat.patterns.logic.patterns.USegregatedFrom;

public class create_input {
	
	private static Set transitions = null;
	
	public static void main(String[] args) throws Exception {

		Vector<bodSod> BoDs = new Vector<bodSod>();
		Vector<bodSod> SoDs = new Vector<bodSod>();
		
		BoDs.add(new bodSod(new PTTransition("Develop_interview_questions"), new PTTransition("Conduct_interview")));

		
//		SoDs.add(new USegregatedFrom(new Transition("t1"), new Transition("t4")));
		
		
		// Parse Petri Net
		String net_path = "src/de/uni/freiburg/iig/telematik/swatiiplugin/patrick/net/Einstellungsprozess1.pnml";
		AbstractGraphicalPN net = parse_petri_net(net_path);
		
		// Create RBAC Model
		RBACModel acmodel = createRbacModel(net);
		
		
		HashSet<String> userset = new HashSet<String>();
		userset.add("Alice");
		userset.add("Bob");
		userset.add("Claire");
		Vector<String> userDeletionCombi = createDeletionCombi(userset);
		
		static_resiliency testStaticResilience = new static_resiliency();
		static_resiliency.checkForResilience(net, acmodel, BoDs, SoDs, userDeletionCombi);
		
	}
	
	
	
	private static Vector<String> createDeletionCombi(HashSet<String> userset) {
		
		Vector<String> userDeletionCombi = new Vector<String>();
		Set<Set<String>> set = Sets.powerSet(userset);
		for(Set<String> item : set){	
			// Entry in userDeletionCombi without sortedList: Claire, Bob, Alice
			// Entry in userDeletionCombi with sortedList: Alice, Bob, Claire
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
			// subjects which are needed for the transitions
			SOABaseProperties propertiesContext = new SOABaseProperties();

			propertiesContext.setSubjects(subjects);
			propertiesContext.setActivities(transitions);
			propertiesContext.setName("propertyname");
			SOABase context = new SOABase(propertiesContext);
			RoleLattice roleLattice = new RoleLattice();
			acmodel = new RBACModel("propertyname", context, roleLattice);

			// Creating Roles
			roleLattice.addRole("a");
			roleLattice.addRole("bc");
			roleLattice.addRole("de");

			// Allocate User to roles and to transitions
			HashSet<String> transitions_for_a = new HashSet<String>();
			HashSet<String> transitions_for_bc = new HashSet<String>();
			HashSet<String> transitions_for_de = new HashSet<String>();			
			
			acmodel.addRoleMembership("Alice", "a");
			transitions_for_a.add("Determine_pay_range");
			transitions_for_a.add("Hire_candidate");
			transitions_for_a.add("Make_employment_offer");		
			acmodel.setActivityPermission("a", transitions_for_a);

			acmodel.addRoleMembership("Bob", "bc");
			acmodel.addRoleMembership("Claire", "bc");
			transitions_for_bc.add("Develop_recruitment_strategy");
			transitions_for_bc.add("Arrange_meeting_with_new_candidate0");
			transitions_for_bc.add("Arrange_meeting_with_new_candidate1");
			transitions_for_bc.add("Arrange_meeting_with_new_candidate2");
			transitions_for_bc.add("Develop_interview_questions");
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
