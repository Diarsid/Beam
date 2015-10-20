/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.drs.beam.core.modules.tasks;

import com.drs.beam.core.modules.DataManagerModule;
import com.drs.beam.core.modules.TaskManagerModule;
import com.drs.beam.core.modules.InnerIOModule;

/**
 *
 * @author Diarsid
 */
public interface TaskManagerModuleBuilder {
    
    static TaskManagerModule buildModule(InnerIOModule ioModule, DataManagerModule dataModule){
        return new TaskManager(ioModule, dataModule);
    }
}
