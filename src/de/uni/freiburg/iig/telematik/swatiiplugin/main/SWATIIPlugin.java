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
            Rule rl = new Rule(RuleType.EXISTS_A);
            rl.setaType("D");
            rl.setbType("B");
            rl.setcType("C");

            // Traces und Regel zusammensetzen
            String query = new String();
            query += "hap(activity(complete,'B','s1','null'),1440140438317).\n";
            query += "hap(activity(complete,'C','null','null'),1440140438322).\n";
            query += "hap(activity(complete,'D','null','null'),1440140438327).\n";
            query += "hap(activity(complete,'A','s1','null'),1440140438332).\n";
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
