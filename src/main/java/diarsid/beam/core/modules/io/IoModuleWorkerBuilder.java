/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package diarsid.beam.core.modules.io;


import diarsid.beam.core.modules.ApplicationComponentsHolderModule;
import diarsid.beam.core.modules.IoModule;
import diarsid.beam.core.modules.io.javafxgui.GuiJavaFX;

import com.drs.gem.injector.module.GemModuleBuilder;

import static diarsid.beam.core.config.Config.IMAGES_LOCATION;

/**
 *
 * @author Diarsid
 */
class IoModuleWorkerBuilder implements GemModuleBuilder<IoModule> {
    
    private final ApplicationComponentsHolderModule appComponentsHolderModule;
    
    IoModuleWorkerBuilder(ApplicationComponentsHolderModule configModule) {
        this.appComponentsHolderModule = configModule;
    }
    
    @Override
    public IoModule buildModule() {
        Gui gui = new GuiJavaFX(
                this.appComponentsHolderModule.getConfiguration().get(IMAGES_LOCATION));
        OuterIoEnginesHolder ioEnginesHolder = new OuterIoEnginesHolder();
        MainInnerIoEngine mainIo = new MainInnerIoEngine(ioEnginesHolder, gui);
        return new IoModuleWorker(ioEnginesHolder, mainIo);
    }
}
