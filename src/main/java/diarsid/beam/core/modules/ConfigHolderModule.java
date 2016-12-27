/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package diarsid.beam.core.modules;

import diarsid.beam.core.config.Config;

import com.drs.gem.injector.module.GemModule;

/**
 *
 * @author Diarsid
 */
public interface ConfigHolderModule extends GemModule {
    
    String get(Config param);
}
