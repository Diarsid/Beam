/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package diarsid.beam.core.modules;

import diarsid.beam.core.StoppableBeamModule;
import diarsid.beam.core.modules.io.gui.Gui;
import diarsid.beam.core.base.control.io.base.actors.Initiator;
import diarsid.beam.core.base.control.io.base.actors.InnerIoEngine;
import diarsid.beam.core.base.control.io.base.actors.OuterIoEngine;

/**
 *
 * @author Diarsid
 */
public interface IoModule extends StoppableBeamModule {
    
    InnerIoEngine getInnerIoEngine();
    
    Gui gui();
    
    boolean registerOuterIoEngine(OuterIoEngine ioEngine);
    
    boolean onIoEngineClosingRequest(Initiator initiator);
    
    boolean isInitiatorLegal(Initiator initiator);
    
}
