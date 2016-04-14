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
import java.util.List;
import java.util.Set;

public class Solver {

    /**
     * Solves rules for given log file and rules
     *
     * @param input
     * @param rbac
     * @return Solving information
     */
    public SolveInfo solve(String[] input, RBACModel rbac) {
        try {
            String prolog = "%\n% Logs\n%\n";
            prolog += logToString(LogParser.parse(new java.io.File(input[0])).get(0));
            prolog += "\n\n%\n% Functions\n%\n\n"
                    + "% Sums up all elements of a given grouping fact.\n"
                    + "% Example: sum(T, e('P1',_,_,_,_,_,_,T), Sum).\n"
                    + "% Returns: Sum / 7199820286610\n"
                    + "sum(X,P,S) :- (\n"
                    + "    findall(X,P,L),\n"
                    + "	   sumlist(L,S)\n"
                    + ").\n"
                    + "\n"
                    + "% Sums up all elements in the given list.\n"
                    + "% Example: sumlist([1,2,3,4], S).\n"
                    + "% Returns: S / 10\n"
                    + "sumlist([],0).\n"
                    + "sumlist([H|T],R) :- (\n"
                    + "	   sumlist(T,S),\n"
                    + "	   R is S+H\n"
                    + ").\n"
                    + "% Counts occurrences of a fact with the given pattern. Can contain duplicates.\n"
                    + "% Example: countall(e(P,TRACE,TYPE,A,sub_2,R,D,T), N).\n"
                    + "% Returns: N / 2\n"
                    + "countall(P,N) :- (\n"
                    + "    findall(_,P,L),\n"
                    + "    length(L,N)\n"
                    + ").\n";
            prolog += "\n\n%\n% RBAC\n%\n\n"
                    + "% RBAC-assignment\n"
                    + RBACToString(rbac)
                    + "% Basic RBAC-rules\n"
                    + "user(U,T):-belong(U,R),role(R,T).\n"
                    + "no_permissions:-(\n"
                    + "    hap(activity(AInstance,complete,AType,AOriginator,ARole),ATime),\n"
                    + "    not(user(AOriginator,AType)),\n"
                    + "    print('no_permissions')\n"
                    + ").\n";
            prolog += "\n\n%\n% Relations\n%\n\n"
                    + "% Relation assignments\n"
                    + input[1]
                    + "\n% Basic relation-rules\n"
                    + "related(X,Y):-related(Y,X).\n"
                    + "related(X,Z):-related(X,Y),related(Y,Z).\n"
                    + "partner_of(X,Y):-partner_of(Y,X).\n"
                    + "same_group(X,Y):-same_group(Y,X).\n";

            prolog += "\n\n%\n% Other rules\n%\n\n"
                    + "% Rule enforcement\n"
                    + "enforcement_breached:-(\n"
                    + "    no_permissions;\n"
                    + "    hap(activity(AInstance,complete,AType,AOriginator,ARole),ATime),(\n"
                    + "        (cannot_do_u(AOriginator,AType),\n"
                    + "        print('cannot_do_u'),print(AOriginator),print(AType));\n"
                    + "        (cannot_do_R(ARole,AType),\n"
                    + "        print('cannot_do_R'),print(ARole),print(AType));\n"
                    + "        ((must_execute_u(BOriginator),not(AOriginator = BOriginator)),\n"
                    + "        print('must_execute_u'),print(AOriginator));\n"
                    + "        ((must_execute_R(BRole),not(ARole = BRole)),\n"
                    + "        print('must_execute_R'),print(ARole))\n"
                    + "    )\n"                    
                    + ").\n\n"
                    + "% User-defined rules\n"
                    + input[2]
                    + "\n% Target rule\n"
                    + "go:-(\n"
                    + "    enforcement_breached;\n"
                    + "    " + input[3] + "\n"
                    + ").";

            System.out.println(prolog);

            // Prolog starten
            Prolog engine = new Prolog();
            Theory theory = new Theory(prolog);
            engine.setTheory(theory);
            return engine.solve("go.");  // Target
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
     *
     * @param rbac
     * @return
     */
    public String RBACToString(RBACModel rbac) {
        String out = "";
        Set<String> roles = rbac.getRoles();
        for (String role : roles) {
            Set<String> rtransactions = rbac.getAuthorizedTransactionsForRole(role);
            for (String rtransaction : rtransactions) {
                out += "role('" + role + "','" + rtransaction + "').\n";
            }
        }
        Set<String> subjects = rbac.getContext().getSubjects();
        for (String subject : subjects) {
            Set<String> sroles = rbac.getRolesFor(subject, true);
            for (String srole : sroles) {
                out += "belong('" + subject + "','" + srole + "').\n";
            }
            List<String> stransactions = rbac.getAuthorizedTransactionsForSubject(subject);
            for (String stransaction : stransactions) {
                out += "user('" + subject + "','" + stransaction + "').\n";
            }
        }
        return out + "\n";
    }

    /**
     * Returns a log as a String formatted for prolog
     *
     * @param log
     * @return
     */
    public String logToString(java.util.List<LogTrace<LogEntry>> log) {
        String out = "";
        for (LogTrace<LogEntry> trace : log) {
            for (LogEntry entry : trace.getEntries()) {
                String actString = "hap(activity(0, " + entry.getEventType().name() + ",'";
                actString += entry.getActivity() + "','" + entry.getOriginator() + "','";
                actString += entry.getRole() + "')," + entry.getTimestamp().getTime() + ").\n";
                out += actString;
            }
        }
        return out;
    }
}
