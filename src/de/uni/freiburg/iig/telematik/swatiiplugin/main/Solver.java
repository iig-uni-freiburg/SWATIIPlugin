package de.uni.freiburg.iig.telematik.swatiiplugin.main;

import alice.tuprolog.*;
import de.invation.code.toval.parser.ParserException;

/**
 *
 * @author mosers
 */
import de.uni.freiburg.iig.telematik.sewol.log.LogEntry;
import de.uni.freiburg.iig.telematik.sewol.log.LogTrace;
import de.uni.freiburg.iig.telematik.sewol.parser.LogParser;
import java.io.IOException;

public class Solver {
    /**
     * Solves rules for given log file and rules
     * @param logPath
     * @param rules
     * @return Solving information
     */
    public SolveInfo solve(String logPath, String rules) {
        try {
            String traceOut = "";
            java.util.List<LogTrace<LogEntry>> log = LogParser.parse(new java.io.File(logPath)).get(0);
            for(LogTrace<LogEntry> trace : log) {
                for(LogEntry entry : trace.getEntries()) {
                    String actString = "hap(activity(0, " + entry.getEventType().name() + ",'";
                    actString += entry.getActivity() + "','" + entry.getOriginator() + "','";
                    actString += entry.getRole() + "')," + entry.getTimestamp().getTime() + ").\n";
                    traceOut +=  actString;
                    System.out.print(actString);
                }
            }
            
            //Adding Transitivity and commutativity to the rules
            String trans = "related(X,Y):-\nrelated(Y,X).\n"
                    + "related(X,Z):-\nrelated(X,Y),\nrelated(Y,Z).\n"
                    + "partner_of(X,Y):-\npartner_of(Y,X)"
                    + "same_group(X,Y):-\nsame_group(Y,X)";
            rules += trans;
            
            // Prolog starten
            Prolog engine = new Prolog();
            Theory theory = new Theory(traceOut + "\n\n" + rules);
            engine.setTheory(theory);
            return engine.solve("rule_false.");
        } catch (InvalidTheoryException ex) {
            System.out.println("Invalid Theory");
        } catch (MalformedGoalException ex) {
            System.out.println("Malformed Goal");
        } catch (IOException ex) {
            System.out.println("File Input Error");
        } catch (ParserException ex) {
            System.out.println("Parser putt");
        }
        return null;
    }
}
