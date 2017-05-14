/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package diarsid.beam.core.modules.control.cli;

import java.util.function.Function;

import diarsid.beam.core.base.control.flow.ValueOperation;
import diarsid.beam.core.base.control.flow.VoidOperation;
import diarsid.beam.core.base.control.io.base.actors.Initiator;
import diarsid.beam.core.base.control.io.base.actors.InnerIoEngine;
import diarsid.beam.core.base.control.io.base.interaction.Message;
import diarsid.beam.core.base.control.io.commands.ArgumentsCommand;
import diarsid.beam.core.domain.entities.WebDirectory;
import diarsid.beam.core.modules.domainkeeper.WebDirectoriesKeeper;
import diarsid.beam.core.base.control.flow.ValueOperationComplete;

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
        ValueOperation<? extends WebDirectory> flow = 
                this.directoriesKeeper.findWebDirectory(initiator, command);
        Function<ValueOperationComplete, Message> onSuccess = (success) -> {
            return ((WebDirectory) success.getOrThrow()).toMessage();
        };
        super.reportValueOperationFlow(initiator, flow, onSuccess, "web directory not found.");
    }
    
    void createWebDirectoryAndReport(Initiator initiator, ArgumentsCommand command) {
        VoidOperation flow = this.directoriesKeeper.createWebDirectory(initiator, command);
        super.reportVoidOperationFlow(initiator, flow, "created!");
    }
    
    void editWebDirectoryAndReport(Initiator initiator, ArgumentsCommand command) {
        VoidOperation flow = this.directoriesKeeper.editWebDirectory(initiator, command);
        super.reportVoidOperationFlow(initiator, flow, "done!");
    }
    
    void removeWebDirectoryAndReport(Initiator initiator, ArgumentsCommand command) {
        VoidOperation flow = this.directoriesKeeper.deleteWebDirectory(initiator, command);
        super.reportVoidOperationFlow(initiator, flow, "removed.");
    }
    
}
