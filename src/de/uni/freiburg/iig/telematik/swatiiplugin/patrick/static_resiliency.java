package de.uni.freiburg.iig.telematik.swatiiplugin.patrick;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;
import java.util.TreeSet;
import java.util.Vector;

import de.invation.code.toval.types.HashList;
import de.uni.freiburg.iig.telematik.sepia.graphic.AbstractGraphicalPN;
import de.uni.freiburg.iig.telematik.sepia.petrinet.properties.PropertyCheckingResult;
import de.uni.freiburg.iig.telematik.sewol.accesscontrol.rbac.RBACModel;
import de.uni.freiburg.iig.telematik.sepia.petrinet.properties.sequences.MGTraversalResult;
import de.uni.freiburg.iig.telematik.swat.patterns.logic.patterns.BoundedWith;
import de.uni.freiburg.iig.telematik.swat.patterns.logic.patterns.USegregatedFrom;
import de.uni.freiburg.iig.telematik.sepia.petrinet.properties.sequences.SequenceGeneration;
import de.uni.freiburg.iig.telematik.sepia.petrinet.properties.sequences.SequenceGenerationCallableGenerator;
import de.uni.freiburg.iig.telematik.sepia.petrinet.properties.sequences.SequenceGenerationException;
import de.uni.freiburg.iig.telematik.sepia.petrinet.pt.PTTransition;

public class static_resiliency {
	
	
	private static SatisfiabilityProperties result = new SatisfiabilityProperties();
	
	static void checkForResilience(AbstractGraphicalPN net, RBACModel acmodel, Vector<bodSod> BoDs, Vector<bodSod> SoDs, Vector<String> userDeletionCombi) throws Exception {

		PetriNetProperties validate = new PetriNetProperties(net, acmodel,BoDs, SoDs);
		
		if (validate.net_status == PropertyCheckingResult.TRUE && validate.rbac_status == PropertyCheckingResult.TRUE) {
			Vector<Vector<String>> sequences = new Vector<Vector<String>>();
			sequences = getSequences(net);
			for (int i = 0; i < sequences.size(); i++) {
				for (int j = 0; j < sequences.get(i).size(); j++) {
					String currentTransition = sequences.get(i).get(j);
					if (sequences.get(i).contains(currentTransition)) {
						List<String> authorizedUsers = new ArrayList<String>();
						authorizedUsers = acmodel.getAuthorizedSubjectsForTransaction(currentTransition);					
						for (int z = 0; z < userDeletionCombi.size(); z++) {
							authorizedUsers = checkBoD(currentTransition, BoDs, authorizedUsers);
								
							if (compareDeletionWithAuthorization(authorizedUsers, currentTransition, userDeletionCombi.get(z), BoDs) == true) {
								createReport(sequences.get(i), currentTransition, authorizedUsers, userDeletionCombi);
							}
						}
					}
				}
			}
		}
		result.printReport();	
	}	

	private static List<String> checkBoD(String currentTransition, Vector<bodSod> BoDs, List<String> authorizedUsers) {
		for (int i = 0; i < BoDs.size(); i++) {
			if (currentTransition.equals(BoDs.get(i).getT2())) {
				
				
				// TODO
				
				
				
			}
		}
		return authorizedUsers;
	}

	private static void createReport(Vector<String> sequences, String currentTransition, List<String> authorizedUsers, Vector<String> userDeletionCombi) {
		Report report = new Report();
		boolean duplicate = false;
		report.setTestedUsers(userDeletionCombi);
		report.setRelevantUsers(authorizedUsers);
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

	private static boolean compareDeletionWithAuthorization(List<String> authorizedUsers, String currentTransition, String userDeletionCombi, Vector<bodSod> BoDs) {
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

	static Vector<Vector<String>> getSequences(AbstractGraphicalPN net) throws SequenceGenerationException {
		// Getting all sequences and save them in vector_sequences
		Vector<String> sequences = new Vector<String>();
		SequenceGenerationCallableGenerator generator = new SequenceGenerationCallableGenerator(net.getPetriNet());
		MGTraversalResult sequneces = SequenceGeneration.getFiringSequences(generator);		
		Vector<Vector<String>> newSequences = new Vector<Vector<String>>();
		Vector<String> transitions = new Vector<String>();
		
		for (Iterator<List<String>> it = sequneces.getCompleteSequences().iterator(); it.hasNext();) {
			String currentSequence = it.next().toString();
			
			int beginn = 1;
			int ende = 0;
			while (ende != -1) {
				ende = currentSequence.indexOf(", ", beginn);
				if (ende != -1) {
					transitions.add(currentSequence.substring(beginn, ende));
					beginn = ende+2;
				}
			}
			ende = currentSequence.indexOf("]", beginn);
			transitions.add(currentSequence.substring(beginn, ende));
			newSequences.add((Vector<String>)transitions.clone());
			transitions.clear();
			
		}	
		return newSequences;
	}

}
