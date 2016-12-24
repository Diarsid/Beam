/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package old.diarsid.beam.external.sysconsole.modules.workers;

import old.diarsid.beam.core.rmi.interfaces.RmiExecutorInterface;
import old.diarsid.beam.core.rmi.interfaces.RmiLocationsHandlerInterface;
import old.diarsid.beam.core.rmi.interfaces.RmiRemoteControlInterface;
import old.diarsid.beam.core.rmi.interfaces.RmiTaskManagerInterface;
import old.diarsid.beam.core.rmi.interfaces.RmiWebPagesHandlerInterface;

import old.diarsid.beam.external.sysconsole.modules.BeamCoreAccessModule;

/**
 *
 * @author Diarsid
 */
class BeamCoreAccess implements BeamCoreAccessModule {
    
    private final RmiTaskManagerInterface taskManager;
    private final RmiExecutorInterface executor;
    private final RmiRemoteControlInterface remoteControl;
    private final RmiLocationsHandlerInterface locations;
    private final RmiWebPagesHandlerInterface webPages;
    
    BeamCoreAccess(
            RmiTaskManagerInterface taskManager,
            RmiExecutorInterface executor,
            RmiRemoteControlInterface remoteControl,
            RmiLocationsHandlerInterface locations,
            RmiWebPagesHandlerInterface webPages) {
        
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
    public RmiWebPagesHandlerInterface webPages() { 
        return this.webPages;
    }    
}
