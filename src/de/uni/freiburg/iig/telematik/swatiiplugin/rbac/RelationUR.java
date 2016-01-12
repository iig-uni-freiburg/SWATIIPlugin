package de.uni.freiburg.iig.telematik.swatiiplugin.rbac;

/**
 *
 * @author mosers
 */
public class RelationUR {
    private String user;
    private String role;
    
    public RelationUR(String user, String role) {
        this.user = user;
        this.role = role;
    }

    /**
     * @return the user
     */
    public String getUser() {
        return user;
    }

    /**
     * @return the role
     */
    public String getRole() {
        return role;
    }
}
