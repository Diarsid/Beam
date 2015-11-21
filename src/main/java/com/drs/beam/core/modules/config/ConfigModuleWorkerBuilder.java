/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package com.drs.beam.core.modules.config;

import com.drs.beam.core.Beam;
import com.drs.beam.core.modules.ConfigModule;
import com.drs.gem.injector.module.ModuleBuilder;

/**
 *
 * @author Diarsid
 */
class ConfigModuleWorkerBuilder implements ModuleBuilder<ConfigModule> {
    
    ConfigModuleWorkerBuilder() {
    }
    
    @Override
    public ConfigModule buildModule(){
        ConfigModule configModule = new ConfigModuleWorker();
        configModule.parseStartArgumentsIntoConfiguration(Beam.getConfigArgs());
        return configModule;
    }
}
