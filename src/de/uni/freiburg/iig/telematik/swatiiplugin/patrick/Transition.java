package de.uni.freiburg.iig.telematik.swatiiplugin.patrick;

import java.util.ArrayList;
import java.util.List;

import de.uni.freiburg.iig.telematik.sepia.petrinet.pt.PTTransition;

public class Transition implements Cloneable{
	private PTTransition transition = null;
	private List<String> authorizedUsers = new ArrayList<String>();
	private String WSPuser = "";
	private Integer WSPnumber = 0;

	public Transition(String transition, ArrayList<String> authorizedUsers) {
		setTransition(new PTTransition(transition));
		if (!(authorizedUsers == null)) {
			setAuthorizedUsers(authorizedUsers);
		}	
	}

	public PTTransition getTransition() {
		return transition;
	}

	public void setTransition(PTTransition transition) {
		this.transition = transition;
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
	
	@Override
	protected Object clone() throws CloneNotSupportedException {
	    return super.clone();
	}
}
