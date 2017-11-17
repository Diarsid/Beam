/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package diarsid.beam.core.modules.io;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

import diarsid.beam.core.base.control.io.base.actors.Initiator;
import diarsid.beam.core.base.control.io.base.actors.OuterIoEngine;

/**
 *
 * @author Diarsid
 */
class UnlimitedOuterIoEnginesManager implements OuterIoEnginesManager {
    
    private final Map<Initiator, OuterIoEngine> unlimitedEngines;
    
    public UnlimitedOuterIoEnginesManager() {
        this.unlimitedEngines = new HashMap<>();
    }

    @Override
    public OuterIoEngine getEngineBy(Initiator initiator) {
        return this.unlimitedEngines.get(initiator);
    }

    @Override
    public boolean hasEngineBy(Initiator initiator) {
        return this.unlimitedEngines.containsKey(initiator);
    }

    @Override
    public boolean closeAndRemoveEngineBy(Initiator initiator) throws IOException {
        this.unlimitedEngines.get(initiator).close();
        this.unlimitedEngines.remove(initiator);
        return true;
    }
    
}
