/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package diarsid.beam.core.modules.control.cli;

import java.util.List;
import java.util.function.Function;

import diarsid.beam.core.base.control.flow.ValueOperation;
import diarsid.beam.core.base.control.flow.ValueOperationComplete;
import diarsid.beam.core.base.control.flow.VoidOperation;
import diarsid.beam.core.base.control.io.base.actors.Initiator;
import diarsid.beam.core.base.control.io.base.actors.InnerIoEngine;
import diarsid.beam.core.base.control.io.base.interaction.Message;
import diarsid.beam.core.base.control.io.commands.ArgumentsCommand;
import diarsid.beam.core.base.control.io.commands.executor.InvocationCommand;
import diarsid.beam.core.modules.domainkeeper.CommandsMemoryKeeper;

import static java.util.stream.Collectors.toList;

import static diarsid.beam.core.base.control.io.base.interaction.Messages.linesToMessage;

/**
 *
 * @author Diarsid
 */
class CliAdapterForCommandsMemoryKeeper extends AbstractCliAdapter {
    
    private final CommandsMemoryKeeper commandsMemoryKeeper;
    
    CliAdapterForCommandsMemoryKeeper(
            CommandsMemoryKeeper commandsMemoryKeeper, InnerIoEngine ioEngine) {
        super(ioEngine);
        this.commandsMemoryKeeper = commandsMemoryKeeper;
    }
    
    void findCommandAndReport(Initiator initiator, ArgumentsCommand command) {
        ValueOperation<List<InvocationCommand>> commandFlow = 
                this.commandsMemoryKeeper.findMems(initiator, command);
        Function<ValueOperationComplete, Message> ifSuccess = (success) -> {
            List<InvocationCommand> foundCommands = (List<InvocationCommand>) success.getOrThrow();
            return linesToMessage(foundCommands
                    .stream()
                    .map(foundCommand -> foundCommand.toMessageString())
                    .collect(toList()));
        };
        super.reportValueOperationFlow(initiator, commandFlow, ifSuccess, "not found.");
    }
    
    void deleteMemAndReport(Initiator initiator, ArgumentsCommand command) {
        VoidOperation removeFlow = this.commandsMemoryKeeper.remove(initiator, command);
        super.reportVoidOperationFlow(initiator, removeFlow);
    }
}
