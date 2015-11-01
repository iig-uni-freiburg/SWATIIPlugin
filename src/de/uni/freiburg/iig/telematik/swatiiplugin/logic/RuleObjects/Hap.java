/*
 * 2015 mosers
 *
 */
package de.uni.freiburg.iig.telematik.swatiiplugin.logic.RuleObjects;

import de.uni.freiburg.iig.telematik.sewol.log.EventType;

/**
 *
 * @author mosers
 */
public class Hap extends AbstractRuleObject{
    private EventType evt;

    public Hap(Letter typeCase, EventType evt) {
        super(typeCase);
        this.evt = evt;
    }

    @Override
    public String toString() {
        return "hap(activity(" + typeCase + "Instance," + evt.toString() + "," + typeCase + "Type," + typeCase + "Originator," + typeCase + "Role)," + typeCase + "Time)";
    }
    
}
