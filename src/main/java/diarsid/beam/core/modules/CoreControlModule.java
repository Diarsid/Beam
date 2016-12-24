/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package diarsid.beam.core.modules;

import diarsid.beam.core.BeamModule;
import diarsid.beam.core.control.io.base.Initiator;

/**
 *
 * @author Diarsid
 */
public interface CoreControlModule extends BeamModule {
    
    void exitBeam();    
    
    void executeCommand(Initiator initiator, String commandLine);
}
