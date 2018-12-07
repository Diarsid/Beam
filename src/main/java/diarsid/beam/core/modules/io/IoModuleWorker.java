/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package diarsid.beam.core.modules.io;

import java.io.IOException;

import diarsid.beam.core.modules.io.gui.Gui;
import diarsid.beam.core.base.control.io.base.actors.Initiator;
import diarsid.beam.core.base.control.io.base.actors.InnerIoEngine;
import diarsid.beam.core.base.control.io.base.actors.OuterIoEngine;
import diarsid.beam.core.modules.IoModule;

import static java.lang.String.format;

import static diarsid.support.log.Logging.logFor;



public class IoModuleWorker implements IoModule {
    
    private final Gui gui;
    private final OuterIoEnginesHolder ioEnginesHolder;
    private final MainInnerIoEngine mainIo;
    
    public IoModuleWorker(
            Gui gui, OuterIoEnginesHolder ioEnginesHolder, MainInnerIoEngine defaultIoEngine) {
        this.gui = gui;
        this.ioEnginesHolder = ioEnginesHolder;
        this.mainIo = defaultIoEngine;
    }

    @Override
    public InnerIoEngine getInnerIoEngine() {
        return this.mainIo;
    }

    @Override
    public boolean registerOuterIoEngine(OuterIoEngine ioEngine) {
        try {
            logFor(this).info(format("register ioEngine name:%s, type:%s", ioEngine.name(), ioEngine.type()));
            return this.ioEnginesHolder.acceptNewIoEngine(ioEngine);
        } catch (IOException ex) {
            logFor(this).error(ex.getMessage(), ex);
            return false;
        }        
    }

    @Override
    public boolean onIoEngineClosingRequest(Initiator initiator) {        
        return this.ioEnginesHolder.processCloseRequestBy(initiator);
    }

    @Override
    public boolean isInitiatorLegal(Initiator initiator) {
        return this.ioEnginesHolder.hasEngineBy(initiator);
    }

    @Override
    public void stopModule() {
        logFor(this).info("close all engines...");
        this.ioEnginesHolder.closeAllEngines();
    }

    @Override
    public Gui gui() {
        return this.gui;
    }
}
