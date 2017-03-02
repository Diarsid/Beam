/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package diarsid.beam.core.modules.control;

import diarsid.beam.core.base.control.io.base.actors.Initiator;
import diarsid.beam.core.base.control.io.base.interaction.Choice;
import diarsid.beam.core.base.control.io.interpreter.CommandLineProcessor;
import diarsid.beam.core.modules.ControlModule;
import diarsid.beam.core.modules.IoModule;

import static diarsid.beam.core.Beam.exitBeamCoreNow;
import static diarsid.beam.core.base.util.ConcurrencyUtil.asyncDoIndependently;
import static diarsid.beam.core.base.util.Logs.debug;

/**
 *
 * @author Diarsid
 */
public class ControlModuleWorker implements ControlModule {
    
    private final IoModule ioModule;
    private final CommandLineProcessor cliProcessor;
    
    public ControlModuleWorker(IoModule ioModule, CommandLineProcessor cliProcessor) {
        this.ioModule = ioModule;
        this.cliProcessor = cliProcessor;
    }

    @Override
    public void exitBeam(Initiator initiator) {
        if ( this.ioModule.isInitiatorLegal(initiator) ) {            
            Choice choice = this.ioModule.getInnerIoEngine().ask(initiator, "are you sure?");
            if ( choice.isPositive() ) {
                asyncDoIndependently(() -> exitBeamCoreNow());
            }
        }
    }

    @Override
    public void executeCommand(Initiator initiator, String commandLine) {
        debug("initiator:" + initiator.getId() + " command: " + commandLine );
        if ( this.ioModule.isInitiatorLegal(initiator) ) {
            this.cliProcessor.process(initiator, commandLine);
        }
        debug("executed...");
    }
}
