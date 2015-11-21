/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.drs.beam.core.modules.rmi;

import com.drs.beam.core.modules.ConfigModule;
import com.drs.beam.core.modules.DataManagerModule;
import com.drs.beam.core.modules.ExecutorModule;
import com.drs.beam.core.modules.InnerIOModule;
import com.drs.beam.core.modules.IoModule;
import com.drs.beam.core.modules.RmiModule;
import com.drs.beam.core.modules.TaskManagerModule;

/**
 *
 * @author Diarsid
 */
public interface RmiModuleBuilder {
    
    static RmiModule buildModule(
            IoModule ioModule,
            InnerIOModule innerIoModule, 
            ConfigModule configModule,
            DataManagerModule dataModule,
            ExecutorModule executorModule,
            TaskManagerModule taskManagerModule){
        
        return new RmiManager(ioModule, innerIoModule, configModule, 
                dataModule, executorModule, taskManagerModule);
    }
}
