package de.uni.freiburg.iig.telematik.swatiiplugin.main;

/*
 * Imports
 */
import alice.tuprolog.*;
import de.invation.code.toval.misc.soabase.SOABase;
import de.uni.freiburg.iig.telematik.sewol.accesscontrol.rbac.RBACModel;
import de.uni.freiburg.iig.telematik.sewol.accesscontrol.rbac.lattice.RoleLattice;
import de.uni.freiburg.iig.telematik.swatiiplugin.logic.Violation;
import de.uni.freiburg.iig.telematik.swatiiplugin.relations.EgoRelationException;
import de.uni.freiburg.iig.telematik.swatiiplugin.relations.Relation;
import de.uni.freiburg.iig.telematik.swatiiplugin.relations.Relations;
import java.util.Arrays;
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
        // Create rule       
        String rf = "rule_false :- (\n"
                + "    countall(hap(activity(AInstance,complete,AType,'S1',ARole),ATime),N),\n"
                + "    N>10,\n"
                + "    print_violation2e('too_much_activity','user','S1','count',N)\n"
                + ").\n";

        // Create Relations
        Relations rels = new Relations();
        try {
            rels.addRelated(new Relation("S1", "S2"));
        } catch (EgoRelationException ex) {
            System.out.println("Some strange Error");
        }

        // Create RBAC
        SOABase base = new SOABase("base");
        base.setActivities("A", "B");
        base.setSubjects("S1", "S2");
        RoleLattice lattice = new RoleLattice(Arrays.asList("R1", "R2"));
        lattice.addRelation("R1", "R2");
        RBACModel rbac = new RBACModel("RBAC", base, lattice);
        rbac.setRightsPropagation(true);
        rbac.setRoleMembership("R1", Arrays.asList("S1"));
        rbac.setRoleMembership("R2", Arrays.asList("S2"));
        rbac.setActivityPermission("R1", "A");
        //rbac.setActivityPermission("R2", "B");

        Solver s = new Solver();
        String path = "logs/4_eyes_principle_correct_BABA.mxml";
        String[] input = {path, rels.toString(), rf.toString(), "rule_false"};
        SolveInfo info = s.solve(input, rbac);
        System.out.println("\n\n\nOUTPUT:\n=======");
        if (info != null && info.isSuccess()) {
            String[] output = s.getOutput();
            List<Violation> vList = s.parseViolations(output);
            for (Violation v : vList) {
                System.out.println(v.toString());
            }
            System.out.println("Problem(s) found");
        } else {
            System.out.println("No problem found");
        }
    }
}
