/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package diarsid.beam.core.modules.innerio;

import diarsid.beam.core.modules.IoInnerModule;
import diarsid.beam.core.modules.IoModule;
import diarsid.beam.core.modules.innerio.javafxgui.GuiJavaFX;
import diarsid.beam.shared.modules.ConfigModule;

import com.drs.gem.injector.module.GemModuleBuilder;

import static diarsid.beam.shared.modules.config.Config.IMAGES_LOCATION;

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
    public IoInnerModule buildModule() {
        Gui gui = new GuiJavaFX(this.configModule.get(IMAGES_LOCATION));
        return new IoInnerModuleWorker(ioOuterModule, gui);
    }
}
