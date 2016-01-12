package de.uni.freiburg.iig.telematik.swatiiplugin.rbac;

import java.util.Collection;
import java.util.HashSet;

/**
 *
 * @author mosers
 */


public class RBAC {
    private Collection<RelationUR> relationsUR = new HashSet();
    private Collection<RelationRT> relationsRT = new HashSet();
    
    /**
     * 
     */
    public RBAC() {
        
    }
    
    /**
     * Adds a UR-pair to the set of relation
     * @param ur 
     */
    public void addUR(RelationUR ur) {
        getRelationsUR().add(ur);
    }
    
    /**
     * Adds a RT-pair to the set of relation
     * @param rt 
     */
    public void addRT(RelationRT rt) {
        getRelationsRT().add(rt);
    }

    /**
     * @return the relationsUR
     */
    public Collection<RelationUR> getRelationsUR() {
        return relationsUR;
    }

    /**
     * @param relationsUR the relationsUR to set
     */
    public void setRelationsUR(Collection<RelationUR> relationsUR) {
        this.relationsUR = relationsUR;
    }

    /**
     * @return the relationsRT
     */
    public Collection<RelationRT> getRelationsRT() {
        return relationsRT;
    }

    /**
     * @param relationsRT the relationsRT to set
     */
    public void setRelationsRT(Collection<RelationRT> relationsRT) {
        this.relationsRT = relationsRT;
    }
    
    /**
     * Returns the UR-relations with given user
     * @param user
     * @return Collection of relations
     */
    public Collection getURByUser(String user) {
        Collection<RelationUR> result = new HashSet();
        for(RelationUR rel : relationsUR) {
            if(rel.getUser().equals(user)) {
                result.add(rel);
            }
        }
        return result;
    }

    /**
     * Returns the UR-relations with given role
     * @param role
     * @return Collection of relations
     */    
    public Collection getURByRole(String role) {
        Collection<RelationUR> result = new HashSet();
        for(RelationUR rel : relationsUR) {
            if(rel.getRole().equals(role)) {
                result.add(rel);
            }
        }
        return result;
    }
    
    /**
     * Returns the RT-relations with given role
     * @param role
     * @return Collection of relations
     */    
    public Collection getRTByRole(String role) {
        Collection<RelationRT> result = new HashSet();
        for(RelationRT rel : relationsRT) {
            if(rel.getRole().equals(role)) {
                result.add(rel);
            }
        }
        return result;
    }
    
    /**
     * Returns the RT-relations with given task
     * @param task
     * @return Collection of relations
     */    
    public Collection getRTByTask(String task) {
        Collection<RelationRT> result = new HashSet();
        for(RelationRT rel : relationsRT) {
            if(rel.getTask().equals(task)) {
                result.add(rel);
            }
        }
        return result;
    }
}
