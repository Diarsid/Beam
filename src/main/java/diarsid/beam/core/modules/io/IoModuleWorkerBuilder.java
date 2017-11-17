/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package diarsid.beam.core.modules.io;


import diarsid.beam.core.application.gui.Gui;
import diarsid.beam.core.base.control.io.base.interaction.HelpContext;
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
        LimitedOuterIoEnginesManager limitedEnginesManager = 
                new LimitedOuterIoEnginesManager();
        UnlimitedOuterIoEnginesManager unlimitedEnginesManager = 
                new UnlimitedOuterIoEnginesManager();
        OuterIoEnginesHolder ioEnginesHolder = 
                new OuterIoEnginesHolder(limitedEnginesManager, unlimitedEnginesManager);
        HelpContext helpContext = new HelpContext();
        MainInnerIoEngine mainIo = new MainInnerIoEngine(ioEnginesHolder, gui, helpContext);
        return new IoModuleWorker(ioEnginesHolder, mainIo);
    }
}
