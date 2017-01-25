/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package diarsid.beam.core.modules.config;

import diarsid.beam.core.config.Configuration;
import diarsid.beam.core.modules.ConfigHolderModule;

import com.drs.gem.injector.module.GemModuleBuilder;

import static diarsid.beam.core.config.Configuration.readConfiguration;

/**
 * ConfigHolderModule builder.
 * Initiates xml config file reading, obtains raw configs data, 
 and builds ConfigHolderModule.
 * 
 * @author Diarsid
 */
public class ConfigHolderModuleWorkerBuilder implements GemModuleBuilder<ConfigHolderModule> {
    
    public ConfigHolderModuleWorkerBuilder() {
    }
    
    @Override
    public ConfigHolderModule buildModule() {        
        Configuration configuration = readConfiguration();
        ConfigHolderModule configModule = new ConfigHolderModuleWorker(configuration);
        return configModule;
    }
}
