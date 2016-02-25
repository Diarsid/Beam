/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package diarsid.beam.shared.modules;

import diarsid.beam.shared.modules.config.Config;

import com.drs.gem.injector.module.GemModule;

/**
 *
 * @author Diarsid
 */
public interface ConfigModule extends GemModule {
    
    String CONFIG_FILE = "./../config/config.xml";
    
    String get(Config param);
}
