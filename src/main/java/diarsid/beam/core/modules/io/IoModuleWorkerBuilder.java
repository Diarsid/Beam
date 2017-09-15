/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package diarsid.beam.core.modules.io;


import diarsid.beam.core.application.gui.Gui;
import diarsid.beam.core.modules.ApplicationComponentsHolderModule;
import diarsid.beam.core.modules.IoModule;

import com.drs.gem.injector.module.GemModuleBuilder;

/**
 *
 * @author Diarsid
 */
class IoModuleWorkerBuilder implements GemModuleBuilder<IoModule> {
    
    private final ApplicationComponentsHolderModule applicationComponentsHolderModule;
    
    IoModuleWorkerBuilder(
            ApplicationComponentsHolderModule applicationComponentsHolderModule) {
        this.applicationComponentsHolderModule = applicationComponentsHolderModule;
    }
    
    @Override
    public IoModule buildModule() {
        Gui gui = this.applicationComponentsHolderModule.gui();
        OuterIoEnginesManager enginesManager = new OuterIoEnginesManager();
        OuterIoEnginesHolder ioEnginesHolder = new OuterIoEnginesHolder(enginesManager);
        MainInnerIoEngine mainIo = new MainInnerIoEngine(ioEnginesHolder, gui);
        return new IoModuleWorker(ioEnginesHolder, mainIo);
    }
}
