/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package diarsid.beam.core.modules.io;


import diarsid.beam.core.base.control.io.base.interaction.ApplicationHelpContext;
import diarsid.beam.core.base.data.DataExtractionException;
import diarsid.beam.core.base.exceptions.ModuleInitializationException;
import diarsid.beam.core.modules.ApplicationComponentsHolderModule;
import diarsid.beam.core.modules.DataModule;
import diarsid.beam.core.modules.IoModule;
import diarsid.beam.core.modules.io.gui.Gui;
import diarsid.beam.core.modules.io.gui.javafx.GuiJavaFX;
import diarsid.support.configuration.Configuration;

import com.drs.gem.injector.module.GemModuleBuilder;

import static java.lang.String.format;

import static diarsid.beam.core.application.starter.Launcher.getLauncher;
import static diarsid.support.log.Logging.logFor;

/**
 *
 * @author Diarsid
 */
class IoModuleWorkerBuilder implements GemModuleBuilder<IoModule> {
    
    private final ApplicationComponentsHolderModule applicationComponentsHolderModule;
    private final DataModule dataModule;
    
    IoModuleWorkerBuilder(
            ApplicationComponentsHolderModule applicationComponentsHolderModule,
            DataModule dataModule) {
        this.applicationComponentsHolderModule = applicationComponentsHolderModule;
        this.dataModule = dataModule;
    }
    
    @Override
    public IoModule buildModule() {
        Configuration configuration = this.applicationComponentsHolderModule.configuration();
        
        LimitedOuterIoEnginesManager limitedEnginesManager = 
                new LimitedOuterIoEnginesManager();
        UnlimitedOuterIoEnginesManager unlimitedEnginesManager = 
                new UnlimitedOuterIoEnginesManager();
        OuterIoEnginesHolder ioEnginesHolder = 
                new OuterIoEnginesHolder(limitedEnginesManager, unlimitedEnginesManager);
        
        ApplicationHelpContext helpContext = new ApplicationHelpContext();
        
        Gui gui;
        try {
            gui = new GuiJavaFX(configuration, getLauncher(), this.dataModule);
        } catch (DataExtractionException e) {
            logFor(this).error(e.getMessage(), e);
            throw new ModuleInitializationException(format(
                    "Cannot initialize %s: %s", 
                    IoModule.class.getSimpleName(), e.getMessage()));
        }
        
        MainInnerIoEngine mainIo = new MainInnerIoEngine(ioEnginesHolder, gui, helpContext);
        
        return new IoModuleWorker(gui, ioEnginesHolder, mainIo);
    }
}
