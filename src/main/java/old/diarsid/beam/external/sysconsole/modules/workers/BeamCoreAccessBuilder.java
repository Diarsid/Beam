/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package old.diarsid.beam.external.sysconsole.modules.workers;

import old.diarsid.beam.external.sysconsole.modules.RmiConsoleManagerModule;

import com.drs.gem.injector.module.GemModuleBuilder;

/**
 *
 * @author Diarsid
 */
class BeamCoreAccessBuilder implements GemModuleBuilder<BeamCoreAccess> {
    
    private final RmiConsoleManagerModule rmi;
    
    BeamCoreAccessBuilder(RmiConsoleManagerModule rmi) {
        this.rmi = rmi;
    }
    
    @Override
    public BeamCoreAccess buildModule() {
        rmi.loadBeamCoreInterfaces();
        return new BeamCoreAccess(
                rmi.getTaskManager(), 
                rmi.getExecutor(), 
                rmi.getRemoteControl(), 
                rmi.getLocationsHandler(), 
                rmi.getWebPageHandler());
    }
}
