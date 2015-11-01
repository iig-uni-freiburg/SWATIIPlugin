/*
 * 2015 mosers
 *
 */
package de.uni.freiburg.iig.telematik.swatiiplugin.main;

import de.uni.freiburg.iig.telematik.sewol.log.EventType;
import de.uni.freiburg.iig.telematik.swatiiplugin.logic.RuleContainer.RuleFalse;
import de.uni.freiburg.iig.telematik.swatiiplugin.logic.RuleObjects.*;

/**
 *
 * @author mosers
 */
public class TestMain {

    /**
     * @param args the command line arguments
     */
    public static void main(String[] args) {
        RuleFalse rf = new RuleFalse();
        Type t = new Type(Type.Letter.A, Type.Comparator.EQUAL, Type.Letter.C);
        rf.add(t);        
        Hap ha = new Hap(AbstractRuleObject.Letter.A, EventType.complete);
        Hap hc = new Hap(AbstractRuleObject.Letter.C, EventType.suspend);
        rf.add(ha);
        rf.add(hc);
        System.out.println(rf.toString());
    }
    
}
