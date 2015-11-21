/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.drs.beam.core.modules.innerio;

import com.drs.beam.core.modules.ConfigModule;
import com.drs.beam.core.modules.InnerIOModule;
import com.drs.beam.core.modules.IoModule;
import com.drs.beam.core.modules.io.Gui;
import com.drs.beam.core.modules.innerio.gui.GuiJavaFX;
import com.drs.beam.util.config.ConfigParam;

/**
 *
 * @author Diarsid
 */
public interface InnerIOModuleBuilder {
    
    static InnerIOModule buildModule(IoModule ioModule, ConfigModule configModule){
        Gui gui = GuiJavaFX.buildAndLaunchGui(configModule.getParameter(ConfigParam.IMAGES_LOCATION));
        return new InnerIOModuleWorker(ioModule, gui);
    }
}
