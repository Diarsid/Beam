/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package com.drs.beam.server.modules.executor.handlers;

import com.drs.beam.server.modules.data.DataManagerModule;
import com.drs.beam.server.modules.io.InnerIOModule;

/**
 *
 * @author Diarsid
 */
public class HandlersBuilder {
    
    public HandlersBuilder(){        
    }
            
    public CommandsHandler buildCommandsHandler(InnerIOModule io, DataManagerModule data){
        return new CommandsHandlerWorker(data.getCommandsDao(), io);
    }
    
    public LocationsHandler buildLocationsHandler(InnerIOModule io, DataManagerModule data){
        return new LocationsHandlerWorker(data.getLocationsDao(), io);
    }
}
