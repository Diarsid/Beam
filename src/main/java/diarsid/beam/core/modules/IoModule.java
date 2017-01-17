/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package diarsid.beam.core.modules;

import diarsid.beam.core.StoppableBeamModule;
import diarsid.beam.core.control.io.base.Initiator;
import diarsid.beam.core.control.io.base.InnerIoEngine;
import diarsid.beam.core.control.io.base.OuterIoEngine;
import diarsid.beam.core.control.io.base.TimeScheduledIo;

/**
 *
 * @author Diarsid
 */
public interface IoModule extends StoppableBeamModule {
    
    InnerIoEngine getInnerIoEngine();
    
    TimeScheduledIo getTimeScheduledIo();
    
    void registerOuterIoEngine(OuterIoEngine ioEngine);
    
    boolean unregisterIoEngine(Initiator initiator);
    
    boolean isInitiatorLegal(Initiator initiator);
    
}
