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
import diarsid.beam.core.modules.domainkeeper.AllKeeper;

/**
 *
 * @author Diarsid
 */
class CliAdapterForAllKeeper extends AbstractCliAdapter {
    
    private final AllKeeper allKeeper;

    public CliAdapterForAllKeeper(AllKeeper allKeeper, InnerIoEngine ioEngine) {
        super(ioEngine);
        this.allKeeper = allKeeper;
    }
    
    void findAll(Initiator initiator, ArgumentsCommand command) {
        ValueOperation<Message> messageFlow = this.allKeeper.findAll(initiator, command);
        Function<ValueOperationComplete, Message> ifSuccess = (success) -> {
            return (Message) success.getOrThrow();
        };
        super.reportValueOperationFlow(initiator, messageFlow, ifSuccess, "anything found.");
    }
}
