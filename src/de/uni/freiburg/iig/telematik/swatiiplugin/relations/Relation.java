package de.uni.freiburg.iig.telematik.swatiiplugin.relations;

/**
 *
 * @author mosers
 */
public class Relation {
    private String user1;
    private String user2;
    
    public Relation(String user1, String user2) throws EgoRelationException {
        if(!(user1.equals(user2))) {
            this.user1 = user1;
            this.user2 = user2;
        } else {
            throw new EgoRelationException();
        }
        
    }

    /**
     * @return the user1
     */
    public String getUser1() {
        return user1;
    }

    /**
     * @return the user2
     */
    public String getUser2() {
        return user2;
    }
    
    public String toString() {
        return "(" + getUser1() + "," + getUser2() + ")";
    }
}
