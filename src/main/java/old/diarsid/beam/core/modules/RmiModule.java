/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package old.diarsid.beam.core.modules;

import old.diarsid.beam.core.rmi.interfaces.RmiExecutorInterface;
import old.diarsid.beam.core.rmi.interfaces.RmiLocationsHandlerInterface;
import old.diarsid.beam.core.rmi.interfaces.RmiRemoteControlInterface;
import old.diarsid.beam.core.rmi.interfaces.RmiTaskManagerInterface;
import old.diarsid.beam.core.rmi.interfaces.RmiWebPagesHandlerInterface;

import diarsid.beam.core.StoppableBeamModule;

/**
 * Is responsible for creating the RMI registry, binding remote objects, 
 * exporting and storing them.
 * 
 * @author Diarsid
 */
public interface RmiModule extends StoppableBeamModule {
    
    RmiExecutorInterface getRmiExecutorInterface();
    
    RmiTaskManagerInterface getRmiTaskManagerInterface();
    
    RmiRemoteControlInterface getRmiRemoteControlInterface();
    
    RmiWebPagesHandlerInterface getRmiWebPageHandlerInterface();
    
    RmiLocationsHandlerInterface getRmiLocationsHandlerInterface();
    
    void exportInterfaces();
}
