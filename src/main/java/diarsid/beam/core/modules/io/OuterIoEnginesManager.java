/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package diarsid.beam.core.modules.io;

import java.io.IOException;
import java.util.Optional;

import diarsid.beam.core.base.control.io.base.actors.Initiator;
import diarsid.beam.core.base.control.io.base.actors.OuterIoEngine;

/**
 *
 * @author Diarsid
 */
interface OuterIoEnginesManager {
    
    Optional<Initiator> registerEngine(OuterIoEngine engine) throws IOException;

    OuterIoEngine getEngineBy(Initiator initiator);

    boolean hasEngineBy(Initiator initiator);

    boolean closeAndRemoveEngineBy(Initiator initiator) throws IOException;    
}
