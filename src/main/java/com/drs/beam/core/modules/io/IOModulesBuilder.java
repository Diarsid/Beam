/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package com.drs.beam.core.modules.io;

import com.drs.beam.core.modules.IOBuilder;
import com.drs.beam.core.modules.InnerIOModule;
import com.drs.beam.core.modules.RemoteControlModule;

/**
 *
 * @author Diarsid
 */
class IOModulesBuilder implements IOBuilder{
    // Fields =============================================================================
    
    private final BeamIO io;

    // Constructors =======================================================================
    IOModulesBuilder(BeamIO io){    
        this.io = io;
    }

    // Methods ============================================================================
    @Override
    public InnerIOModule buildInnerIOModule() {
        return (InnerIOModule) io;
    }
    
    @Override
    public RemoteControlModule buildRemoteControlModule(){
        return (RemoteControlModule) io;
    }
}
