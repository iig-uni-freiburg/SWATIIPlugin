/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package de.uni.freiburg.iig.telematik.swatiiplugin.main;

/*
 * Imports
 */
import alice.tuprolog.*;
import de.uni.freiburg.iig.telematik.swatiiplugin.data.*;
import de.uni.freiburg.iig.telematik.swatiiplugin.logic.*;

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
            Rule rl = new Rule(RuleType.FOUR_EYES);
            rl.setaType("A");
            rl.setbType("B");
            rl.setcType("C");

            // Trace erstellen
            LogTrace trace = new LogTrace();         
            LogEntry log0 = new LogEntry(ActivityStatus.COMPLETE, "B", "s1", null, 1440140438317L); 
            trace.add(log0);
            LogEntry log1 = new LogEntry(ActivityStatus.COMPLETE, "C", null, null, 1440140438322L);
            trace.add(log1);
            LogEntry log2 = new LogEntry(ActivityStatus.COMPLETE, "D", null, null, 1440140438327L);
            trace.add(log2);
            LogEntry log3 = new LogEntry(ActivityStatus.COMPLETE, "A", "s1", null, 1440140438332L);
            trace.add(log3);
            
            // Traces und Regel zusammensetzen            
            String query = new String();
            query += trace.toPrologTrace();
            query += "\n";
            query += "\n";
            query += rl.asString();

            // Prolog starten
            Prolog engine = new Prolog();
            Theory theory = new Theory(query);
            engine.setTheory(theory);
            SolveInfo info = engine.solve("rule_false.");

            // Ergebnis            
            System.out.print(rl.getRuleType());
            System.out.println(" gets Result: " + !info.isSuccess());

        } catch (MalformedGoalException ex) {
            System.out.println("Ziel falsch gesetzt");
        } catch (InvalidTheoryException ex) {
            System.out.println("Theorie nicht gültig");
        } catch (PrerequisitesException ex) {
            System.out.println("Nicht alle Variablen gesetzt!");
            System.out.println("Nötig ist " + ex.getPrerequisites());
        }
    }
}
