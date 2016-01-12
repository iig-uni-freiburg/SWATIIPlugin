package de.uni.freiburg.iig.telematik.swatiiplugin.rbac;

/**
 *
 * @author mosers
 */
public class RelationRT {
    private String task;
    private String role;
    
    public RelationRT(String role, String task) {
        this.task = task;
        this.role = role;
    }

    /**
     * @return the task
     */
    public String getTask() {
        return task;
    }

    /**
     * @return the role
     */
    public String getRole() {
        return role;
    }
}
