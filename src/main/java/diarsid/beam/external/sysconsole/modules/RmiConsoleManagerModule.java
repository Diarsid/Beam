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
import diarsid.beam.core.rmi.interfaces.RmiWebPageHandlerInterface;

import diarsid.beam.external.ExternalIOInterface;

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
    RmiWebPageHandlerInterface getWebPageHandler();
}
