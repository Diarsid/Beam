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

import old.diarsid.beam.external.ExternalIOInterface;

import com.drs.gem.injector.module.GemModule;

/**
 *
 * @author Diarsid
 */
public interface RmiConsoleManagerModule extends GemModule {
    
    void exportAndConnectToCore(ExternalIOInterface external);    
    void loadBeamCoreInterfaces();    
    
    RmiExecutorInterface getExecutor();
    RmiLocationsHandlerInterface getLocationsHandler();
    RmiRemoteControlInterface getRemoteControl();
    RmiTaskManagerInterface getTaskManager();
    RmiWebPagesHandlerInterface getWebPageHandler();
}
