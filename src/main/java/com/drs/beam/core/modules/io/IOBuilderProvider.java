/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.drs.beam.core.modules.io;

import com.drs.beam.core.modules.ConfigModule;
import com.drs.beam.core.modules.IOBuilder;

/**
 *
 * @author Diarsid
 */
public interface IOBuilderProvider {
    
    static IOBuilder createBuilder(ConfigModule configModule){
        BeamIO io = new BeamIO(configModule);
        IOBuilder ioBuilder = new IOModulesBuilder(io);
        return ioBuilder;
    }
}
