/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package diarsid.beam.external.sysconsole.modules.workers;

import diarsid.beam.core.rmi.interfaces.RmiExecutorInterface;
import diarsid.beam.core.rmi.interfaces.RmiLocationsHandlerInterface;
import diarsid.beam.core.rmi.interfaces.RmiRemoteControlInterface;
import diarsid.beam.core.rmi.interfaces.RmiTaskManagerInterface;
import diarsid.beam.core.rmi.interfaces.RmiWebPageHandlerInterface;

import diarsid.beam.external.sysconsole.modules.BeamCoreAccessModule;

/**
 *
 * @author Diarsid
 */
class BeamCoreAccess implements BeamCoreAccessModule {
    
    private final RmiTaskManagerInterface taskManager;
    private final RmiExecutorInterface executor;
    private final RmiRemoteControlInterface remoteControl;
    private final RmiLocationsHandlerInterface locations;
    private final RmiWebPageHandlerInterface webPages;
    
    BeamCoreAccess(
            RmiTaskManagerInterface taskManager,
            RmiExecutorInterface executor,
            RmiRemoteControlInterface remoteControl,
            RmiLocationsHandlerInterface locations,
            RmiWebPageHandlerInterface webPages) {
        
        this.taskManager = taskManager;
        this.executor = executor;
        this.webPages = webPages;
        this.remoteControl = remoteControl;
        this.locations = locations;
    }
    
    @Override
    public RmiExecutorInterface executor() { 
        return this.executor;
    }
    
    @Override
    public RmiLocationsHandlerInterface locations() { 
        return this.locations;
    }
    
    @Override
    public RmiRemoteControlInterface remoteControl() { 
        return this.remoteControl;        
    }
    
    @Override
    public RmiTaskManagerInterface taskManager() { 
        return this.taskManager;
    }
    
    @Override
    public RmiWebPageHandlerInterface webPages() { 
        return this.webPages;
    }    
}
