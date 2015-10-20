/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package com.drs.beam.core.modules.executor.handlers;

import com.drs.beam.core.modules.DataManagerModule;
import com.drs.beam.core.modules.executor.CommandsHandler;
import com.drs.beam.core.modules.executor.LocationsHandler;
import com.drs.beam.core.modules.InnerIOModule;

/**
 *
 * @author Diarsid
 */
public interface HandlersBuilder {
                
    public static CommandsHandler buildCommandsHandler(InnerIOModule io, DataManagerModule data){
        return new CommandsHandlerWorker(data.getCommandsDao(), io);
    }
    
    public static LocationsHandler buildLocationsHandler(InnerIOModule io, DataManagerModule data){
        return new LocationsHandlerWorker(data.getLocationsDao(), io);
    }
}
