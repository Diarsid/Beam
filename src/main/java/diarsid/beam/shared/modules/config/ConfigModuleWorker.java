/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package diarsid.beam.shared.modules.config;

import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

import diarsid.beam.core.exceptions.ModuleInitializationOrderException;

import diarsid.beam.shared.modules.ConfigModule;

/**
 * Module intended to convey configuration parameters 
 * across all program where they are required.
 * 
 * It is devised to avoid static classes and methods usage and 
 * clarify real dependencies of modules.
 * 
 * @author Diarsid
 */
class ConfigModuleWorker implements ConfigModule {
    
    private final Map<Config, String> configurations;    
    
    ConfigModuleWorker() {    
        this.configurations = new HashMap<>();
    }
    
    void acceptConfigs(Map<Config, String> conf) {
        this.configurations.putAll(conf);
    }
    
    @Override
    public String get(Config param) {
        if (this.configurations.isEmpty()){
            throw new ModuleInitializationOrderException();
        } else {
            return this.configurations.get(param);
        } 
    }

    @Override
    public int hashCode() {
        int hash = 7;
        hash = 41 * hash + Objects.hashCode(this.configurations);
        return hash;
    }

    @Override
    public boolean equals(Object obj) {
        if (obj == null) {
            return false;
        }
        if (getClass() != obj.getClass()) {
            return false;
        }
        final ConfigModuleWorker other = (ConfigModuleWorker) obj;
        if (!Objects.equals(this.configurations, other.configurations)) {
            return false;
        }
        return true;
    }
}
