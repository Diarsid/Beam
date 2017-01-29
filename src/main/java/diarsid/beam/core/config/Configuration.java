/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package diarsid.beam.core.config;

import java.io.File;
import java.util.Map;

import static diarsid.beam.core.Beam.CONFIG_FILE;

/**
 *
 * @author Diarsid
 */
public class Configuration {
    
    private static final Configuration CONFIGURATION;
    
    static {
        File configXML = new File(CONFIG_FILE);
        ConfigFileReader configReader = new ConfigFileReader();
        CONFIGURATION = configReader.readConfigurationFile(configXML);
    }
    
    private final Map<Config, String> configs;
    
    public Configuration(Map<Config, String> configs) {
        this.configs = configs;
    }
    
    public static Configuration getConfiguration() {
        return CONFIGURATION;
    }
    
    public String get(Config config) {
        return this.configs.getOrDefault(config, "");
    }
    
    public boolean isSet() {
        return ! this.configs.isEmpty();
    }
    
    public boolean isNotSet() {
        return this.configs.isEmpty();
    }
}
