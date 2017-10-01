/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package diarsid.beam.core.modules.control.cli;

import java.util.function.Function;

import diarsid.beam.core.base.control.io.base.actors.Initiator;
import diarsid.beam.core.base.control.io.base.actors.InnerIoEngine;
import diarsid.beam.core.base.control.io.base.interaction.Message;
import diarsid.beam.core.base.control.io.commands.ArgumentsCommand;
import diarsid.beam.core.domain.entities.WebDirectory;
import diarsid.beam.core.modules.domainkeeper.WebDirectoriesKeeper;
import diarsid.beam.core.base.control.flow.VoidFlow;
import diarsid.beam.core.base.control.flow.ValueFlow;
import diarsid.beam.core.base.control.flow.ValueFlowCompleted;

/**
 *
 * @author Diarsid
 */
class CliAdapterForWebDirectoriesKeeper extends AbstractCliAdapter {
    
    private final WebDirectoriesKeeper directoriesKeeper;

    CliAdapterForWebDirectoriesKeeper(
            WebDirectoriesKeeper directoriesKeeper, InnerIoEngine ioEngine) {
        super(ioEngine);
        this.directoriesKeeper = directoriesKeeper;
    }
    
    void findWebDirectoryAndReport(Initiator initiator, ArgumentsCommand command) {
        ValueFlow<? extends WebDirectory> flow = 
                this.directoriesKeeper.findWebDirectory(initiator, command);
        Function<ValueFlowCompleted, Message> onSuccess = (success) -> {
            return ((WebDirectory) success.getOrThrow()).toMessage();
        };
        super.reportValueFlow(initiator, flow, onSuccess, "web directory not found.");
    }
    
    void createWebDirectoryAndReport(Initiator initiator, ArgumentsCommand command) {
        VoidFlow flow = this.directoriesKeeper.createWebDirectory(initiator, command);
        super.reportVoidFlow(initiator, flow, "created!");
    }
    
    void editWebDirectoryAndReport(Initiator initiator, ArgumentsCommand command) {
        VoidFlow flow = this.directoriesKeeper.editWebDirectory(initiator, command);
        super.reportVoidFlow(initiator, flow, "done!");
    }
    
    void removeWebDirectoryAndReport(Initiator initiator, ArgumentsCommand command) {
        VoidFlow flow = this.directoriesKeeper.deleteWebDirectory(initiator, command);
        super.reportVoidFlow(initiator, flow, "removed.");
    }
    
}
