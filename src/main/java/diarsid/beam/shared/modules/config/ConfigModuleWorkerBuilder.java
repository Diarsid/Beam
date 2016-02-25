/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package diarsid.beam.shared.modules.config;

import java.io.File;

import diarsid.beam.shared.modules.ConfigModule;

import com.drs.gem.injector.module.GemModuleBuilder;

/**
 * ConfigModule builder.
 * Initiates xml config file reading, obtains raw configs data, 
 * and builds ConfigModule.
 * 
 * @author Diarsid
 */
public class ConfigModuleWorkerBuilder implements GemModuleBuilder<ConfigModule> {
    
    public ConfigModuleWorkerBuilder() {
    }
    
    @Override
    public ConfigModule buildModule() {
        ConfigModuleWorker configModule = new ConfigModuleWorker();
        File configXML = new File(ConfigModule.CONFIG_FILE);
        ConfigFileReader configReader = new ConfigFileReader();
        configModule.acceptConfigs(configReader.readConfigs(configXML));
        return configModule;
    }
}
