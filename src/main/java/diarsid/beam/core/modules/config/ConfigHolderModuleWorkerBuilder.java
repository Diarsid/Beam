/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package diarsid.beam.core.modules.config;

import java.io.File;

import diarsid.beam.core.config.ConfigFileReader;
import diarsid.beam.core.config.Configuration;
import diarsid.beam.core.modules.ConfigHolderModule;

import com.drs.gem.injector.module.GemModuleBuilder;

import static diarsid.beam.core.Beam.CONFIG_FILE;

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
        File configXML = new File(CONFIG_FILE);
        ConfigFileReader configReader = new ConfigFileReader();
        Configuration configuration = configReader.readConfigurationFile(configXML);
        ConfigHolderModule configModule = new ConfigHolderModuleWorker(configuration);
        return configModule;
    }
}
