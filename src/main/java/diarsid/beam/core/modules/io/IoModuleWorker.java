/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package diarsid.beam.core.modules.io;

import java.io.IOException;

import diarsid.beam.core.base.control.io.base.actors.Initiator;
import diarsid.beam.core.base.control.io.base.actors.InnerIoEngine;
import diarsid.beam.core.base.control.io.base.actors.OuterIoEngine;
import diarsid.beam.core.base.control.io.base.actors.TimeMessagesIo;
import diarsid.beam.core.modules.IoModule;

import static diarsid.beam.core.base.util.Logs.debug;
import static diarsid.beam.core.base.util.Logs.logError; 


public class IoModuleWorker implements IoModule {
    
    private final OuterIoEnginesHolder ioEnginesHolder;
    private final MainInnerIoEngine mainIo;
    
    public IoModuleWorker(
            OuterIoEnginesHolder ioEnginesHolder, MainInnerIoEngine defaultIoEngine) {
        this.ioEnginesHolder = ioEnginesHolder;
        this.mainIo = defaultIoEngine;
    }

    @Override
    public InnerIoEngine getInnerIoEngine() {
        return this.mainIo;
    }
    
    @Override
    public TimeMessagesIo getTimeScheduledIo() {
        return this.mainIo;
    }

    @Override
    public void registerOuterIoEngine(OuterIoEngine ioEngine) {
        try {
            debug("register ioEngine: " + ioEngine.getName());
        } catch (IOException ex) {
            logError(this.getClass(), ex);
        }
        this.ioEnginesHolder.acceptNewIoEngine(ioEngine);
    }

    @Override
    public boolean unregisterIoEngine(Initiator initiator) {
        return this.ioEnginesHolder.deleteEngine(initiator);
    }

    @Override
    public boolean isInitiatorLegal(Initiator initiator) {
        return this.ioEnginesHolder.hasEngine(initiator);
    }

    @Override
    public void stopModule() {
        debug("close all engines...");
        this.ioEnginesHolder.closeAllEngines();
    }
}
