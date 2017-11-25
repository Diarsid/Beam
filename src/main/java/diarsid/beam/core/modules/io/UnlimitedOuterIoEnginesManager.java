/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package diarsid.beam.core.modules.io;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

import diarsid.beam.core.base.control.io.base.actors.Initiator;
import diarsid.beam.core.base.control.io.base.actors.OuterIoEngine;

/**
 *
 * @author Diarsid
 */
class UnlimitedOuterIoEnginesManager implements OuterIoEnginesManager {
        
    private final static int UNLIMITED_ENGINES_SLOT_NUMBER = 0;
    
    private final Map<Initiator, OuterIoEngine> unlimitedEngines;
    
    public UnlimitedOuterIoEnginesManager() {
        this.unlimitedEngines = new HashMap<>();
    }

    @Override
    public Optional<Initiator> registerEngine(OuterIoEngine engine) throws IOException {
        Initiator initiator = new Initiator(UNLIMITED_ENGINES_SLOT_NUMBER, engine.type());
        engine.accept(initiator);
        
        if ( this.unlimitedEngines.containsKey(initiator) ) {
            engine.report("engine with this Initiator is already registered");
            return Optional.empty();
        } else {
            this.unlimitedEngines.put(initiator, engine);
            return Optional.of(initiator);
        }
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
