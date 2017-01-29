/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package diarsid.beam.core.modules.corecontrol.cli;

import java.util.Optional;

import diarsid.beam.core.control.io.base.Initiator;
import diarsid.beam.core.control.io.base.InnerIoEngine;
import diarsid.beam.core.control.io.commands.FindEntityCommand;
import diarsid.beam.core.domain.entities.Program;
import diarsid.beam.core.modules.domainkeeper.ProgramsKeeper;

/**
 *
 * @author Diarsid
 */
class CliAdapterForProgramsKeeper {
    
    private final InnerIoEngine ioEngine;
    private final ProgramsKeeper programsKeeper;

    CliAdapterForProgramsKeeper(ProgramsKeeper programsKeeper, InnerIoEngine ioEngine) {
        this.ioEngine = ioEngine;
        this.programsKeeper = programsKeeper;
    }
    
    void findProgramAndReport(Initiator initiator, FindEntityCommand command) {
        Optional<Program> optionalProgram = this.programsKeeper.findProgram(initiator, command);
        if ( optionalProgram.isPresent() ) {
            this.ioEngine.report(initiator, optionalProgram.get().getFullName());
        } else {
            this.ioEngine.report(initiator, "not found.");
        }
    }
}