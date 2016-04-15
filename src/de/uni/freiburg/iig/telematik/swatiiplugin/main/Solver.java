package de.uni.freiburg.iig.telematik.swatiiplugin.main;



/**
 *
 * @author mosers
 */
import alice.tuprolog.*;
import alice.tuprolog.event.OutputEvent;
import alice.tuprolog.event.OutputListener;
import de.invation.code.toval.parser.ParserException;
import de.uni.freiburg.iig.telematik.sewol.accesscontrol.rbac.RBACModel;
import de.uni.freiburg.iig.telematik.sewol.log.LogEntry;
import de.uni.freiburg.iig.telematik.sewol.log.LogTrace;
import de.uni.freiburg.iig.telematik.sewol.parser.LogParser;
import de.uni.freiburg.iig.telematik.swatiiplugin.logic.Violation;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;


public class Solver implements OutputListener {

    private String output;

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
            prolog += logToPlString(LogParser.parse(new java.io.File(input[0])).get(0));
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
                    + ").\n"
                    + "%\n% OUTPUT\n%\n"
                    + "output_start :- print(\"<violation>\").\n"
                    + "values_start :- print(\"<values>\").\n"
                    + "entries_start :- print(\"<entries>\").\n"
                    + "entry_output(P,A,S,R,T) :- (\n"
                    + "    print(\"<entry>\"),\n"
                    + "    print(\"<process>\"),print(P),print(\"</process>\"),\n"
                    + "%   print(\"<type>\"),print(TYPE),print(\"</type>\"),\n" //,AInstance,AType,AOriginator,ARole,ATime
                    + "	   print(\"<activity>\"),print(A),print(\"</activity>\"),\n"
                    + "	   print(\"<subject>\"),print(S),print(\"</subject>\"),\n"
                    + "	   print(\"<role>\"),print(R),print(\"</role>\"),\n"
                    + "	   print(\"<time>\"),print(T),print(\"</time>\"),\n"
                    + "	   print(\"</entry>\")\n"
                    + ").\n"
                    + "message_output(M) :- (\n"
                    + "	   print(\"<message>\"),\n"
                    + "	   print(M),\n"
                    + "	   print(\"</message>\")\n"
                    + ").\n"
                    + "value_output(K,V) :- (\n"
                    + "	   print(\"<value key=\"\"\"),\n"
                    + "	   print(K),\n"
                    + "	   print(\"\"\">\"),\n"
                    + "	   print(V),\n"
                    + "	   print(\"</value>\")\n"
                    + ").\n"
                    + "name_output(M) :- (\n"
                    + "	   print(\"<name>\"),\n"
                    + "	   print(M),\n"
                    + "	   print(\"</name>\")\n"
                    + ").\n"
                    + "entries_end :- print(\"</entries>\").\n"
                    + "values_end :- print(\"</values>\").\n"
                    + "output_end :- print(\"</violation>\"),nl.\n"
                    + "print_violation2(N,A,B,C,D,AInstance,AType,AOriginator,ARole,ATime) :- (\n"
                    + "    output_start,name_output(N),value_output(A,B),\n"
                    + "    value_output(C,D),entry_output(AInstance,AType,AOriginator,ARole,ATime),output_end\n"
                    + ").\n"
                    + "print_violation2e(N,A,B,C,D) :- (\n"
                    + "    output_start,name_output(N),value_output(A,B),\n"
                    + "    value_output(C,D),output_end\n"
                    + ").\n"
                    + "print_violation1(N,A,B,AInstance,AType,AOriginator,ARole,ATime) :- (\n"
                    + "    output_start,name_output(N),value_output(A,B),\n"
                    + "    entry_output(AInstance,AType,AOriginator,ARole,ATime),output_end\n"
                    + ").\n";
            prolog += "\n\n%\n% RBAC\n%\n\n"
                    + "% RBAC-assignment\n"
                    + RBACToPlString(rbac)
                    + "% Basic RBAC-rules\n"
                    + "user(U,T):-belong(U,R),role(R,T).\n"
                    + "no_permissions:-(\n"
                    + "    hap(activity(AInstance,complete,AType,AOriginator,ARole),ATime),\n"
                    + "    not(user(AOriginator,AType)),\n"
                    + "    print_violation2('no_permission','user',AOriginator,'action',AType,AInstance,AType,AOriginator,ARole,ATime)\n"
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
                    + "    (hap(activity(AInstance,complete,AType,AOriginator,ARole),ATime),(\n"
                    + "        (cannot_do_u(AOriginator,AType),\n"
                    + "        print_violation2('cannot_do_u','user',AOriginator,'action',AType,AInstance,AType,AOriginator,ARole,ATime));\n"
                    + "        (cannot_do_R(ARole,AType),\n"
                    + "        print_violation2('cannot_do_R','role',ARole,'action',AType,AInstance,AType,AOriginator,ARole,ATime));\n"
                    + "        ((must_execute_u(BOriginator),not(AOriginator = BOriginator)),\n"
                    + "        print_violation1('must_execute_u','user',AOriginator,AInstance,AType,AOriginator,ARole,ATime));\n"
                    + "        ((must_execute_R(BRole),not(ARole = BRole)),\n"
                    + "        print_violation1('must_execute_R','role',ARole,AInstance,AType,AOriginator,ARole,ATime))\n"
                    + "    ))\n"
                    + ").\n"
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
            engine.addOutputListener(this);
            this.resetOutput();
            SolveInfo info = engine.solve("go.");
            while (engine.hasOpenAlternatives()) {
                engine.solveNext();
            }
            return info;
        } catch (InvalidTheoryException ex) {
            System.out.println("Invalid Theory");
        } catch (MalformedGoalException ex) {
            System.out.println("Malformed Goal");
        } catch (IOException ex) {
            System.out.println("File Input Error");
        } catch (ParserException ex) {
            System.out.println("Parser putt");
        } catch (NoMoreSolutionException ex) {
            System.out.println("Keine weiteren Lösungen");
        }
        return null;
    }

    /**
     * Returns a RBAC as a String formatted for prolog
     *
     * @param rbac
     * @return the output string
     */
    public String RBACToPlString(RBACModel rbac) {
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
     * @return the output string
     */
    public String logToPlString(java.util.List<LogTrace<LogEntry>> log) {
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

    /**
     * @param e the outputevent
     */
    @Override
    public void onOutput(OutputEvent e) {
        this.output += e.getMsg();
    }

    /**
     * @return the output
     */
    public String[] getOutput() {
        return output.split("\\n");
    }

    /**
     * @param output the output to set
     */
    private void resetOutput() {
        this.output = "";
    }

    /**
     *
     * @param violationStrings
     * @return the list of violations
     */
    public List parseViolations(String[] violationStrings) {
        List<Violation> violationList = new ArrayList<>();
        for (String violationString : violationStrings) {
            //System.out.println(violationString);            // DEBUG
            try {
                Violation newV = new Violation(violationString);
                boolean joined = false;
                for (Violation v : violationList) {
                    if (v.equals(newV)) {
                        v.append(newV);
                        joined = true;
                        break;
                    }
                }
                if (!joined) {
                    violationList.add(newV);
                }
            } catch (Exception e) {

            }
        }
        return violationList;
    }
}
