/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package de.uni.freiburg.iig.telematik.swatiiplugin.logicold;

import de.uni.freiburg.iig.telematik.swatiiplugin.logicold.RuleType;

/**
 *
 * @author mosers
 */
public class Rule {
    /*
     * Constants for rule selection
     */
    /*public static final int B_AFTER_A = 0;
     public static final int FOUR_EYES = 1;
     public static final int B_OR_C_AFTER_A = 2;
     public static final int ABSENT_A = 3;
     public static final int EXISTS_A = 4;*/

    /*
     * Variables for rules
     */
    private String aType;
    private String bType;
    private String cType;

    /*
     * The chosen Rule
     */
    public RuleType ruleType;

    // The getters and setters
    /**
     * @return the aType
     */
    public String getaType() {
        return aType;
    }

    /**
     * @return the bType
     */
    public String getbType() {
        return bType;
    }

    /**
     * @return the cType
     */
    public String getcType() {
        return cType;
    }

    /**
     * @param aType the aType to set
     */
    public void setaType(String aType) {
        this.aType = aType;
    }

    /**
     * @param bType the bType to set
     */
    public void setbType(String bType) {
        this.bType = bType;
    }

    /**
     * @param cType the cType to set
     */
    public void setcType(String cType) {
        this.cType = cType;
    }

    /**
     * @return the ruleType
     */
    public RuleType getRuleType() {
        return ruleType;
    }

    /**
     * Constructor for Role
     *
     * @param type
     */
    public Rule(RuleType type) {
        this.ruleType = type;
    }

    /**
     *
     * @return rule as string
     * @throws
     * de.uni.freiburg.iig.telematik.swatiiplugin.logic.PrerequisitesException
     */
    public String asString() throws PrerequisitesException {
        String out = "rule_false:-(\n";
        switch (getRuleType()) {
            case B_AFTER_A:
                if (getaType() == null || getbType() == null) {
                    throw new PrerequisitesException("AType und BType");
                }
                out += "  AType='" + getaType() + "',\n";
                out += "  hap(activity(AInstance, complete,AType,AOriginator,ARole),ATime),\n";
                out += "  not((\n";
                out += "      BType='" + getbType() + "',\n";
                out += "      hap(activity(BInstance, complete,BType,BOriginator,BRole),BTime),\n";
                out += "      BTime>ATime\n";
                out += "  ))\n";
                break;

            case FOUR_EYES:
                if (getaType() == null || getbType() == null) {
                    throw new PrerequisitesException("AType und BType");
                }
                out += "  AType='" + getaType() + "',\n";
                out += "  hap(activity(AInstance, complete,AType,AOriginator,ARole),ATime),\n";
                out += "  not((\n";
                out += "      BType='" + getbType() + "',\n";
                out += "      BOriginator=AOriginator,\n";
                out += "      not((\n";
                out += "        hap(activity(BInstance, complete,BType,BOriginator,BRole),BTime)\n";
                out += "      ))\n";
                out += "  ))\n";
                break;

            case B_OR_C_AFTER_A:
                if (getaType() == null || getbType() == null || getcType() == null) {
                    throw new PrerequisitesException("AType, BType und CType");
                }
                out += "  AType='" + getaType() + "',\n";
                out += "  hap(activity(AInstance, complete,AType,AOriginator,ARole),ATime),\n";
                out += "  not((\n";
                out += "      BType='" + getbType() + "',\n";
                out += "      hap(activity(BInstance, complete,BType,BOriginator,BRole),BTime),\n";
                out += "      BTime>ATime\n";
                out += "  )),\n";
                out += "  not((\n";
                out += "      CType='" + getcType() + "',\n";
                out += "      hap(activity(CInstance, complete,CType,COriginator,CRole),CTime),\n";
                out += "      CTime>ATime\n";
                out += "  ))\n";
                break;

            case ABSENT_A:
                if (getaType() == null) {
                    throw new PrerequisitesException("AType");
                }
                out += "  AType='" + getaType() + "',\n";
                out += "  hap(activity(AInstance, complete,AType,AOriginator,ARole),ATime)\n";
                break;

            case EXISTS_A:
                if (getaType() == null) {
                    throw new PrerequisitesException("AType");
                }
                out += " not((\n";
                out += "    AType='" + getaType() + "',\n";
                out += "    hap(activity(AInstance, complete,AType,AOriginator,ARole),ATime)\n";
                out += " ))\n";
        }
        out += ").";
        return out;
    }

}
