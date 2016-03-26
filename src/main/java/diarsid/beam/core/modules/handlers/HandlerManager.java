/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package diarsid.beam.core.modules.handlers;

import diarsid.beam.core.modules.DataModule;
import diarsid.beam.core.modules.HandlerManagerModule;
import diarsid.beam.core.modules.IoInnerModule;

/**
 *
 * @author Diarsid
 */
class HandlerManager implements HandlerManagerModule {
    
    private final DataModule data;
    private final IoInnerModule ioEngine;
    
    HandlerManager(IoInnerModule io, DataModule data) {
        this.data = data;
        this.ioEngine = io;
    }
    
    @Override
    public LocationsHandler getLocationsHandler() {
        return new LocationsHandlerWorker(this.ioEngine, this.data.getLocationsDao());
    }
    
    @Override
    public WebPagesHandler getWebPagesHandler() {
        return new WebPagesHandlerWorker(this.ioEngine, this.data.getWebPagesDao());
    }
}
