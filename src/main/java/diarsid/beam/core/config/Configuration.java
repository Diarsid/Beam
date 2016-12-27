/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package diarsid.beam.core.config;

import java.util.Map;

/**
 *
 * @author Diarsid
 */
public class Configuration {
    
    private final Map<Config, String> configs;
    
    public Configuration(Map<Config, String> configs) {
        this.configs = configs;
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
