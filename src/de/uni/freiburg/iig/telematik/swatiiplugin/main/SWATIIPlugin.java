package de.uni.freiburg.iig.telematik.swatiiplugin.main;

/*
 * Imports
 */
import de.uni.freiburg.iig.telematik.swatiiplugin.logic.RuleContainer.*;
import de.uni.freiburg.iig.telematik.swatiiplugin.logic.RuleObjects.*;
import alice.tuprolog.*;
import de.uni.freiburg.iig.telematik.sewol.log.EventType;

/**
 *
 * @author mosers
 */
public class SWATIIPlugin {

    /**
     * @param args the command line arguments
     */
    public static void main(String[] args) {
        // Create rule
        And a = new And();
        Hap h = new Hap(Hap.Letter.A, EventType.complete);
        a.add(h);
        Originator o = new Originator(Originator.Letter.A, Originator.Comparator.EQUAL, "S1");
        a.add(o);
        Rule rf = new Rule("rule_false");
        rf.add(a);
        Solver s = new Solver();
        String path = "logs/4_eyes_principle_correct_BABA.mxml";
        SolveInfo info = s.solve(path, rf.toString());
        if(info != null && info.isSuccess()) {
            System.out.println("Match for the Rule found");
        } else {
            System.out.println("No Match for the Rule found");
        }
    }
}
