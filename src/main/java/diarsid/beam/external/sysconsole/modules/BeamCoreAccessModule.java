/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package diarsid.beam.external.sysconsole.modules;

import diarsid.beam.core.rmi.interfaces.RmiExecutorInterface;
import diarsid.beam.core.rmi.interfaces.RmiLocationsHandlerInterface;
import diarsid.beam.core.rmi.interfaces.RmiRemoteControlInterface;
import diarsid.beam.core.rmi.interfaces.RmiTaskManagerInterface;
import diarsid.beam.core.rmi.interfaces.RmiWebPagesHandlerInterface;

import com.drs.gem.injector.module.GemModule;

/**
 *
 * @author Diarsid
 */
public interface BeamCoreAccessModule extends GemModule {
    
    RmiExecutorInterface executor();
    RmiLocationsHandlerInterface locations();
    RmiRemoteControlInterface remoteControl();
    RmiTaskManagerInterface taskManager();
    RmiWebPagesHandlerInterface webPages();
}
