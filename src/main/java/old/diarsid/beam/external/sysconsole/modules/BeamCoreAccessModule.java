/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package old.diarsid.beam.external.sysconsole.modules;

import old.diarsid.beam.core.rmi.interfaces.RmiExecutorInterface;
import old.diarsid.beam.core.rmi.interfaces.RmiLocationsHandlerInterface;
import old.diarsid.beam.core.rmi.interfaces.RmiRemoteControlInterface;
import old.diarsid.beam.core.rmi.interfaces.RmiTaskManagerInterface;
import old.diarsid.beam.core.rmi.interfaces.RmiWebPagesHandlerInterface;

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
