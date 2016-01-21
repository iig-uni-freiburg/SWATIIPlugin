package de.uni.freiburg.iig.telematik.swatiiplugin.patrick;

import java.util.ArrayList;
import java.util.List;

import de.uni.freiburg.iig.telematik.sepia.petrinet.pt.PTTransition;

/**
 * This class extends {@link PTTransition} by adding several properties, which are necessary to determine the resilience of a Petri net.
 * 
 * @author Patrick Notz
 *
 */

public class WSPTransition extends PTTransition {
	private List<String> authorizedUsers = new ArrayList<String>();
	private String WSPuser = "";
	private Integer WSPnumber = 0;

	/**
	 * Generates a WSPTransition and add user which are allowed to fire it.
	 * @param transition Name of WSPTransition
	 * @param authorizedUsers User who are allowed to fire the WSPTransition
	 */
	public WSPTransition(String transition, List<String> authorizedUsers) {
		super(transition);
		if (!(authorizedUsers == null)) {
			setAuthorizedUsers(authorizedUsers);
		}	
	}

	public List<String> getAuthorizedUsers() {
		return authorizedUsers;
	}

	public void setAuthorizedUsers(List<String> authorizedUsers) {
		this.authorizedUsers = authorizedUsers;
	}

	public String getWSPuser() {
		return WSPuser;
	}

	public void setWSPuser(String wSPUser) {
		WSPuser = wSPUser;
	}

	public Integer getWSPnumber() {
		return WSPnumber;
	}

	public void setWSPNumber(Integer wSPNumber) {
		WSPnumber = wSPNumber;
	}
	
	/**
	 * 
	 * @param name Name of the WSPTransition
	 * @param user User who are allowed to fire the WSPTransition
	 * @param wspnumber Number that is associated to a WSPTransition.<br>
	 * It is needed to create a file in DIMACS format.
	 * @param wspuser Number that is associated to a user of a WSPTransition.<br>
	 * It is needed to create a file in DIMACS format.
	 * @return Returns a cloned WSPTransition
	 */
	protected WSPTransition newInstance(String name, List<String> user, Integer wspnumber, String wspuser) {
		WSPTransition result = new WSPTransition(name, user);
		result.setWSPNumber(wspnumber);
		result.setWSPuser(wspuser);
		return result;
	}
	
	/**
	 * Calls newInstance() to clone a {@link WSPTransition}
	 */
	public WSPTransition clone() {
	    return newInstance(getName(), getAuthorizedUsers(), getWSPnumber(), getWSPuser());
	}
}
