/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package diarsid.beam.core.modules.config;

import java.util.Objects;

import diarsid.beam.core.config.Config;
import diarsid.beam.core.config.Configuration;
import diarsid.beam.core.modules.ConfigHolderModule;

/**
 * Module intended to convey configuration parameters 
 * across all program where they are required.
 * 
 * It is devised to avoid static classes and methods usage and 
 * clarify real dependencies of modules.
 * 
 * @author Diarsid
 */
class ConfigHolderModuleWorker implements ConfigHolderModule {
    
    private final Configuration configuration;    
    
    ConfigHolderModuleWorker(Configuration configuration) {    
        this.configuration = configuration;
    }
    
    @Override
    public String get(Config param) {
        return this.configuration.get(param);
    }

    @Override
    public int hashCode() {
        int hash = 7;
        hash = 41 * hash + Objects.hashCode(this.configuration);
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
        final ConfigHolderModuleWorker other = (ConfigHolderModuleWorker) obj;
        if (!Objects.equals(this.configuration, other.configuration)) {
            return false;
        }
        return true;
    }
}
