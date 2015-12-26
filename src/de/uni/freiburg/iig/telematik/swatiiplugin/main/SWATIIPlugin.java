package de.uni.freiburg.iig.telematik.swatiiplugin.main;

/*
 * Imports
 */
import de.uni.freiburg.iig.telematik.swatiiplugin.logic.RuleContainer.*;
import de.uni.freiburg.iig.telematik.swatiiplugin.logic.RuleObjects.*;
import alice.tuprolog.*;

import de.invation.code.toval.parser.ParserException;
import de.uni.freiburg.iig.telematik.sewol.log.*;
import de.uni.freiburg.iig.telematik.sewol.parser.LogParser;

/**
 *
 * @author mosers
 */
public class SWATIIPlugin {

    /**
     * @param args the command line arguments
     */
    public static void main(String[] args) {
        try {
            // Regel erstellen
            /*Rule rl = new Rule(RuleType.FOUR_EYES);
            rl.setaType("A");
            rl.setbType("B");
            rl.setcType("C");*/
            // FOUR EYES
            RuleFalse rf = new RuleFalse();            
            rf.add(new Type(AbstractRuleObject.Letter.A, AbstractRuleObject.Comparator.EQUAL, "A"));
            rf.add(new Hap(AbstractRuleObject.Letter.A, EventType.complete));
            Not n = new Not();
            n.add(new Type(AbstractRuleObject.Letter.B, AbstractRuleObject.Comparator.EQUAL, "B"));
            n.add(new Originator(AbstractRuleObject.Letter.B, AbstractRuleObject.Comparator.EQUAL, AbstractRuleObject.Letter.A));            
            n.add(new Not().add(new Hap(AbstractRuleObject.Letter.B, EventType.complete)));
            rf.add(n);
            
            System.out.println(rf.toString());
            
            // Trace parsen und in einen String packen
            String traceOut = "";
            java.util.List<LogTrace<LogEntry>> log = LogParser.parse(new java.io.File("logs/4_eyes_principle_correct_BABA.mxml")).get(0);
            for(LogTrace<LogEntry> trace : log) {                
                for(LogEntry entry : trace.getEntries()) {
                    String actString = "hap(activity(0, " + entry.getEventType().name() + ",'";
                    actString += entry.getActivity() + "','" + entry.getOriginator() + "','";
                    actString += entry.getRole() + "')," + entry.getTimestamp().getTime() + ").\n";
                    traceOut +=  actString;
                    System.out.print(actString);
                }
            }
            
            // Traces und Regel zusammensetzen            
            String query = new String();
            query += traceOut;
            query += "\n";
            query += "\n";
            query += rf.toString();

            // Prolog starten
            Prolog engine = new Prolog();
            Theory theory = new Theory(query);
            engine.setTheory(theory);
            SolveInfo info = engine.solve("rule_false.");

            // Ergebnis           
            
            System.out.println("Result: " + !info.isSuccess());

        } catch (MalformedGoalException ex) {
            System.out.println("Ziel falsch gesetzt");
        } catch (InvalidTheoryException ex) {
            System.out.println("Theorie nicht gültig");
        } catch (java.io.IOException ex) {
            System.out.println("Datei nicht gefunden");
        } catch (ParserException ex) {
            System.out.println("Parser putt");
        }
    }
}
