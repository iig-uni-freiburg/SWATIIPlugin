package de.uni.freiburg.iig.telematik.swatiiplugin.main;

import alice.tuprolog.*;
import de.invation.code.toval.parser.ParserException;
import de.uni.freiburg.iig.telematik.sewol.accesscontrol.rbac.RBACModel;

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
     * @param input
     * @param rbac
     * @return Solving information
     */
    public SolveInfo solve(String[] input, RBACModel rbac) {
        try {
            String out = "";
            out += logToString(LogParser.parse(new java.io.File(input[0])).get(0));
            out += "\n\n" + RBACToString(rbac);
            out += "permitted_to_s(S,A):-member_of(S,R),permitted_to_r(R,A).\n"
                    + "no_permissions:-(\n"    //Basic RBAC-rules
                    + "hap(activity(AInstance,complete,AType,AOriginator,ARole),ATime),"
                    + "not(permitted_to_s(AOriginator,AType),"
                    + "permitted_to_r(ARole,AType))"; 
            
            out += "\n\n" + input[1];       // Relations 
            out += "related(X,Y):-related(Y,X).\n" // Basic relation-rules
                + "related(X,Z):-related(X,Y),related(Y,Z).\n"
                + "partner_of(X,Y):-partner_of(Y,X).\n"
                + "same_group(X,Y):-same_group(Y,X).\n";
            out += "\n\n" + input[2];       // Custom Rules           
            
            System.out.println(out);
            
            // Prolog starten
            Prolog engine = new Prolog();
            Theory theory = new Theory(out);
            engine.setTheory(theory);
            return engine.solve(input[3]);  // Target
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
    
    /**
     * Returns a RBAC as a String formatted for prolog
     * @param rbac
     * @return 
     */
    public String RBACToString(RBACModel rbac) {
        String out = "";
        
        return out;
    }
    
    /**
     * Returns a log as a String formatted for prolog
     * @param log
     * @return 
     */
    public String logToString(java.util.List<LogTrace<LogEntry>> log) {
        String out = "";
        for(LogTrace<LogEntry> trace : log) {
                for(LogEntry entry : trace.getEntries()) {
                    String actString = "hap(activity(0, " + entry.getEventType().name() + ",'";
                    actString += entry.getActivity() + "','" + entry.getOriginator() + "','";
                    actString += entry.getRole() + "')," + entry.getTimestamp().getTime() + ").\n";
                    out +=  actString;
                }
            }
        return out;
    }
}
