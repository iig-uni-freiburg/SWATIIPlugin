package de.uni.freiburg.iig.telematik.swatiiplugin.main;

/*
 * Imports
 */
import alice.tuprolog.*;
import de.invation.code.toval.parser.ParserException;
import de.uni.freiburg.iig.telematik.swatiiplugin.logic.*;
import de.uni.freiburg.iig.telematik.sewol.parser.LogParser;
import de.uni.freiburg.iig.telematik.sewol.log.LogTrace;
import de.uni.freiburg.iig.telematik.sewol.log.LogEntry;
import java.io.File;
import java.io.IOException;
import java.util.List;


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
            
            // Trace parsen und in einen String packen
            String traceOut = "";
            List<LogTrace<LogEntry>> log = LogParser.parse(new File("../Logs/4_eyes_principle_correct_BABA.mxml")).get(0);
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
        } catch (IOException ex) {
            System.out.println("Datei nicht gefunden");
        } catch (ParserException ex) {
            System.out.println("Parser putt");
        }
    }
}
