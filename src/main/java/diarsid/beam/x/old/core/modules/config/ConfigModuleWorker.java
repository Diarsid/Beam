/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package diarsid.beam.x.old.core.modules.config;

import java.util.HashMap;
import java.util.Map;

import diarsid.beam.shared.modules.ConfigModule;
import diarsid.beam.core.exceptions.ModuleInitializationException;
import diarsid.beam.core.exceptions.ModuleInitializationOrderException;

import diarsid.beam.shared.modules.config.Config;

/**
 *
 * @author Diarsid
 */
class ConfigModuleWorker implements ConfigModule {
    
    private final Map<Config, String> configurations;    
    
    ConfigModuleWorker(){    
        this.configurations = new HashMap<>();
    }

    /**
    @Override
    public void parseStartArgumentsIntoConfiguration(String[] startArgs){
        if (this.configurations.isEmpty()){
            ConfigParam configParam;
            String configParamName;
            String configParamValue;
            int indexOfEqual;
            for(String configurationPair : startArgs){
                indexOfEqual = configurationPair.indexOf("=");
                configParamName = configurationPair.substring(0, indexOfEqual);
                configParam = ConfigParam.valueOf(configParamName);
                configParamValue = configurationPair.substring(indexOfEqual+1, configurationPair.length()); 
                this.configurations.put(configParam, configParamValue);
            }
        } else {
            throw new ModuleInitializationException();
        }        
    }
    */
    
    @Override
    public String get(Config param) {
        if (this.configurations.isEmpty()){
            throw new ModuleInitializationOrderException();
        } else {
            return this.configurations.get(param);
        }        
    }
}
