/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package diarsid.beam.core.modules.control.cli;

import java.util.function.Function;

import diarsid.beam.core.base.control.flow.ValueOperation;
import diarsid.beam.core.base.control.flow.ValueOperationComplete;
import diarsid.beam.core.base.control.io.base.actors.Initiator;
import diarsid.beam.core.base.control.io.base.actors.InnerIoEngine;
import diarsid.beam.core.base.control.io.base.interaction.Message;
import diarsid.beam.core.base.control.io.commands.ArgumentsCommand;
import diarsid.beam.core.domain.entities.Program;
import diarsid.beam.core.modules.domainkeeper.ProgramsKeeper;

import static diarsid.beam.core.base.control.io.base.interaction.Messages.toMessage;

/**
 *
 * @author Diarsid
 */
class CliAdapterForProgramsKeeper extends AbstractCliAdapter {
    
    private final ProgramsKeeper programsKeeper;

    CliAdapterForProgramsKeeper(ProgramsKeeper programsKeeper, InnerIoEngine ioEngine) {
        super(ioEngine);
        this.programsKeeper = programsKeeper;
    }
    
    void findProgramAndReport(Initiator initiator, ArgumentsCommand command) {
        ValueOperation<Program> flow = this.programsKeeper.findProgram(initiator, command);
        Function<ValueOperationComplete, Message> ifSuccess = (success) -> {
            return toMessage((Program) success.getOrThrow());
        };
        super.reportValueOperationFlow(initiator, flow, ifSuccess, "not found.");
    }
}
