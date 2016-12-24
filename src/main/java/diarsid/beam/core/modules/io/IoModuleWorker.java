/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package diarsid.beam.core.modules.io;

import diarsid.beam.core.control.io.base.Initiator;
import diarsid.beam.core.control.io.base.InnerIoEngine;
import diarsid.beam.core.control.io.base.OuterIoEngine;
import diarsid.beam.core.control.io.base.TimeScheduledIo;
import diarsid.beam.core.modules.IoModule;


public class IoModuleWorker implements IoModule {
    
    private final OuterIoEnginesHolder ioEnginesHolder;
    private final MainInnerIoEngine mainIo;
    
    public IoModuleWorker(
            OuterIoEnginesHolder ioEnginesHolder, MainInnerIoEngine defaultIoEngine) {
        this.ioEnginesHolder = ioEnginesHolder;
        this.mainIo = defaultIoEngine;
    }
    
    @Override
    public Initiator getSystemInitiator() {
        return this.mainIo.getSystemInitiator();
    }

    @Override
    public InnerIoEngine getInnerIoEngine() {
        return this.mainIo;
    }
    
    @Override
    public TimeScheduledIo getTimeScheduledIo() {
        return this.mainIo;
    }

    @Override
    public void registerOuterIoEngine(OuterIoEngine ioEngine) {
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
        this.ioEnginesHolder.closeAllEngines();
    }
}
