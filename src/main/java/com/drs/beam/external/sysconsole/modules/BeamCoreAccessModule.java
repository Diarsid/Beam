/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.drs.beam.external.sysconsole.modules;

import com.drs.beam.core.rmi.interfaces.RmiExecutorInterface;
import com.drs.beam.core.rmi.interfaces.RmiLocationsHandlerInterface;
import com.drs.beam.core.rmi.interfaces.RmiRemoteControlInterface;
import com.drs.beam.core.rmi.interfaces.RmiTaskManagerInterface;
import com.drs.beam.core.rmi.interfaces.RmiWebPageHandlerInterface;
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
    RmiWebPageHandlerInterface webPages();
}
