package de.uni.freiburg.iig.telematik.swatiiplugin.patrick;

import de.uni.freiburg.iig.telematik.sepia.petrinet.pt.PTTransition;

public class bodSod {
	private PTTransition t1 = null;
	private PTTransition t2 = null;
	public bodSod(PTTransition t1, PTTransition t2) {
		setT1(t1);
		setT1(t2);
	}
	public PTTransition getT1() {
		return t1;
	}
	public void setT1(PTTransition t1) {
		this.t1 = t1;
	}
	public PTTransition getT2() {
		return t2;
	}
	public void setT2(PTTransition t2) {
		this.t2 = t2;
	}	
}
