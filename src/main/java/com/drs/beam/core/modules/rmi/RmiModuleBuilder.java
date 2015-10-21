/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.drs.beam.core.modules.rmi;

import com.drs.beam.core.modules.ConfigModule;
import com.drs.beam.core.modules.ExecutorModule;
import com.drs.beam.core.modules.InnerIOModule;
import com.drs.beam.core.modules.RemoteControlModule;
import com.drs.beam.core.modules.RmiModule;
import com.drs.beam.core.modules.TaskManagerModule;

/**
 *
 * @author Diarsid
 */
public interface RmiModuleBuilder {
    
    static RmiModule buildModule(
            InnerIOModule ioModule, 
            ConfigModule configModule,
            ExecutorModule executorModule,
            TaskManagerModule taskManagerModule,
            RemoteControlModule remoteControlModule){
        return new RmiManager(ioModule, configModule, executorModule, taskManagerModule, remoteControlModule);
    }
}
