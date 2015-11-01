/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package de.uni.freiburg.iig.telematik.swatiiplugin.logicold;

/**
 *
 * @author mosers
 */
public class PrerequisitesException extends Exception{
    private String prerequisites;
    
    /**
     * 
     * @return Die Vorraussetzungen
     */
    public String getPrerequisites(){
        return prerequisites;
    }
    
    /**
     * 
     * @param pr alle Vorssaussetzungen
     */
    public PrerequisitesException(String pr){
        prerequisites = pr;
    }
    
}
