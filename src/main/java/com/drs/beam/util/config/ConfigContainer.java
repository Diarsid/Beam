/*
 * project: Beam
 * author: Diarsid
 */

package com.drs.beam.util.config;

import java.util.Map;
import java.util.HashMap;

/**
 *
 * @author Diarsid
 */
public class ConfigContainer {
    // Fields ==================================================================
    private static ConfigContainer container = new ConfigContainer();
    
    private Map<ConfigParam, String> configurations;
    
    // Constructors ============================================================
    private ConfigContainer() {
        this.configurations = new HashMap<>();
    }
    
    // Methods =================================================================
    
    public static void cancel(){
        container.configurations = null;
        container = null;
    }
    
    public static void parseStartArgumentsIntoConfiguration(String[] startArgs){
        ConfigParam configParam;
        String configParamName;
        String configParamValue;
        int indexOfEqual;
        for(String configurationPair : startArgs){
            indexOfEqual = configurationPair.indexOf("=");
            configParamName = configurationPair.substring(0, indexOfEqual);
            configParam = ConfigParam.valueOf(configParamName);
            configParamValue = configurationPair.substring(indexOfEqual+1, configurationPair.length()); 
            container.configurations.put(configParam, configParamValue);
        }
    }    
    
    public static String getParam(ConfigParam name){
        try{
            return container.configurations.get(name);
        } catch(NullPointerException npe){
            return "";
        }        
    }
}
