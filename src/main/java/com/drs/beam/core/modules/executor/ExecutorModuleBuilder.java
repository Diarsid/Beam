/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.drs.beam.core.modules.executor;

import com.drs.beam.core.modules.ConfigModule;
import com.drs.beam.core.modules.DataManagerModule;
import com.drs.beam.core.modules.ExecutorModule;
import com.drs.beam.core.modules.executor.os.OSProvider;
import com.drs.beam.core.modules.InnerIOModule;

/**
 *
 * @author Diarsid
 */
public interface ExecutorModuleBuilder {
    
    static ExecutorModule buildModule(InnerIOModule ioModule, 
            DataManagerModule dataModule, ConfigModule configModule){        
        
        OS os = OSProvider.getOS(ioModule, configModule);
        
        return new Executor(ioModule, dataModule, os);
    }
}
