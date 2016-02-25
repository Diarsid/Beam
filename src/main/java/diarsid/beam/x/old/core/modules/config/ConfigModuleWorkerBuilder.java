/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package diarsid.beam.x.old.core.modules.config;

import diarsid.beam.core.Beam;

import diarsid.beam.shared.modules.ConfigModule;

import com.drs.gem.injector.module.GemModuleBuilder;

/**
 *
 * @author Diarsid
 */
class ConfigModuleWorkerBuilder implements GemModuleBuilder<ConfigModule> {
    
    ConfigModuleWorkerBuilder() {
    }
    
    @Override
    public ConfigModule buildModule(){
        ConfigModule configModule = new ConfigModuleWorker();
        //configModule.parseStartArgumentsIntoConfiguration(Beam.getConfigArgs());
        return configModule;
    }
}
