/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package diarsid.beam.core.modules.control.cli;

import java.util.function.Function;

import diarsid.beam.core.base.control.flow.ValueFlow;
import diarsid.beam.core.base.control.flow.ValueFlowCompleted;
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
        ValueFlow<Message> messageFlow = this.allKeeper.findAll(initiator, command);
        Function<ValueFlowCompleted<Message>, Message> ifSuccess = (success) -> {
            return success.orThrow();
        };
        super.reportValueFlow(initiator, messageFlow, ifSuccess, "anything found.");
    }
}
