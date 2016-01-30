/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package com.drs.beam.core.modules.innerio;

import com.drs.beam.shared.modules.ConfigModule;
import com.drs.beam.core.modules.IoInnerModule;
import com.drs.beam.core.modules.IoModule;
import com.drs.beam.core.modules.innerio.javafxgui.JavaFXGuiLauncher;
import com.drs.beam.shared.modules.config.Config;
import com.drs.gem.injector.module.GemModuleBuilder;

/**
 *
 * @author Diarsid
 */
class IoInnerModuleWorkerBuilder implements GemModuleBuilder<IoInnerModule>{
    
    private final IoModule ioOuterModule;
    private final ConfigModule configModule;
    
    IoInnerModuleWorkerBuilder(IoModule ioModule, ConfigModule configModule) {
        this.ioOuterModule = ioModule;
        this.configModule = configModule;
    }
    
    @Override
    public IoInnerModule buildModule(){
        //Gui gui = GuiJavaFX.buildAndLaunchGui(
        //        configModule.get(Config.IMAGES_LOCATION));
        JavaFXGuiLauncher launcher = new JavaFXGuiLauncher();
        Gui gui = launcher.buildGui(configModule.get(Config.IMAGES_LOCATION));
        return new IoInnerModuleWorker(ioOuterModule, gui);
    }
}
