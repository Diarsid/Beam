/*
 * project: Beam
 * author: Diarsid
 */

package diarsid.beam.x.old.util.config;

import diarsid.beam.shared.modules.config.Config;

import java.util.Map;
import java.util.HashMap;

/**
 *
 * @author Diarsid
 */
public class ConfigContainer {
    // Fields ==================================================================
    private static ConfigContainer container = new ConfigContainer();
    
    private Map<Config, String> configurations;
    
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
        Config configParam;
        String configParamName;
        String configParamValue;
        int indexOfEqual;
        for(String configurationPair : startArgs){
            indexOfEqual = configurationPair.indexOf("=");
            configParamName = configurationPair.substring(0, indexOfEqual);
            configParam = Config.valueOf(configParamName);
            configParamValue = configurationPair.substring(indexOfEqual+1, configurationPair.length()); 
            container.configurations.put(configParam, configParamValue);
        }
    }    
    
    public static String getParam(Config name){
        try{
            return container.configurations.get(name);
        } catch(NullPointerException npe){
            return "";
        }        
    }
}
