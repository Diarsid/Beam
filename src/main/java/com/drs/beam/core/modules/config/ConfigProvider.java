/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package com.drs.beam.core.modules.config;

import java.util.HashMap;
import java.util.Map;

import com.drs.beam.core.modules.ConfigModule;
import com.drs.beam.core.exceptions.ModuleInitializationException;
import com.drs.beam.core.exceptions.ModuleInitializationOrderException;
import com.drs.beam.util.config.ConfigParam;

/**
 *
 * @author Diarsid
 */
public class ConfigProvider implements ConfigModule{
    // Fields =============================================================================
    
    private final Map<ConfigParam, String> configurations;
    
    // Constructors =======================================================================
    ConfigProvider(){    
        this.configurations = new HashMap<>();
    }

    // Methods ============================================================================
    
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
    
    @Override
    public String getParameter(ConfigParam param) {
        if (this.configurations.isEmpty()){
            throw new ModuleInitializationOrderException();
        } else {
            return this.configurations.get(param);
        }        
    }
}
