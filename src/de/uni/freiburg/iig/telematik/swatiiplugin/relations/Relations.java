package de.uni.freiburg.iig.telematik.swatiiplugin.relations;

import java.util.Collection;
import java.util.HashSet;

/**
 *
 * @author mosers
 */
public class Relations {
    private Collection<Relation> related = new HashSet();
    private Collection<Relation> partner_of = new HashSet();
    private Collection<Relation> same_group = new HashSet();

    /**
     * @return the related
     */
    public Collection<Relation> getRelated() {
        return related;
    }

    /**
     * @param related the related to set
     */
    public void setRelated(Collection<Relation> related) {
        this.related = related;
    }

    /**
     * @return the partner_of
     */
    public Collection<Relation> getPartner_of() {
        return partner_of;
    }

    /**
     * @param partner_of the partner_of to set
     */
    public void setPartner_of(Collection<Relation> partner_of) {
        this.partner_of = partner_of;
    }

    /**
     * @return the same_group
     */
    public Collection<Relation> getSame_group() {
        return same_group;
    }

    /**
     * @param same_group the same_group to set
     */
    public void setSame_group(Collection<Relation> same_group) {
        this.same_group = same_group;
    }
    
    /**
     * Adds a "related"-pair
     * @param rel 
     */
    public void addRelated(Relation rel) {
        related.add(rel);
    }
    
    /**
     * Adds a "partner_of"-pair
     * @param rel 
     */
    public void addPartner(Relation rel) {
        partner_of.add(rel);
    }
    
    /**
     * Adds a "same_group"-pair
     * @param rel 
     */
    public void addGroup(Relation rel) {
        same_group.add(rel);
    }
    
    /**
     * Adds the new relations in "related" given by transitivity
     */
    public void elaborateTransitivity(){
        for(Relation rel1 : related) {            
            for(Relation rel2 : related) {
                Relation relNew = transitive(rel1, rel2);
                if(null != relNew) {                    
                    related.add(relNew);
                }
            }         
        }
    }
    
    /**
     * Returns the relation created by transitivity, and null if there is none
     * Cannot handle two equal relations
     * @param rel1
     * @param rel2
     * @return Relation or null
     */
    public Relation transitive(Relation rel1, Relation rel2) throws ReflexivityException {
        if(rel1.getUser1().equals(rel2.getUser1())) {
            return new Relation(rel1.getUser2(), rel2.getUser2());
        }
        if(rel1.getUser1().equals(rel2.getUser2())) {
            return new Relation(rel1.getUser2(), rel2.getUser1());
        }        
        if(rel1.getUser2().equals(rel2.getUser1())) {
            return new Relation(rel1.getUser1(), rel2.getUser2());
        }
        if(rel1.getUser2().equals(rel2.getUser2())) {
            return new Relation(rel1.getUser1(), rel2.getUser1());
        }
        return null;
    }
    
    @Override
    public String toString() {
        String out = "";
        for(Relation rel : related) {
            out += "related" + rel.toString() + ".\n";
        }
        for(Relation rel : partner_of) {
            out += "partner_of" + rel.toString() + ".\n";
        }
        for(Relation rel : same_group) {
            out += "same_group" + rel.toString() + ".\n";
        }
        return out;
    }   
}
