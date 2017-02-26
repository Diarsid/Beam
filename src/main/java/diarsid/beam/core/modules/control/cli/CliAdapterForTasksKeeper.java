/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package diarsid.beam.core.modules.control.cli;

import java.util.List;
import java.util.function.Function;

import diarsid.beam.core.base.control.flow.VoidOperation;
import diarsid.beam.core.base.control.io.base.actors.Initiator;
import diarsid.beam.core.base.control.io.base.actors.InnerIoEngine;
import diarsid.beam.core.base.control.io.base.interaction.Message;
import diarsid.beam.core.base.control.io.commands.ArgumentsCommand;
import diarsid.beam.core.domain.entities.Task;
import diarsid.beam.core.modules.domainkeeper.TasksKeeper;

import static diarsid.beam.core.base.control.io.base.interaction.Messages.tasksToMessage;

import diarsid.beam.core.base.control.flow.ValueOperation;
import diarsid.beam.core.base.control.flow.OkValueOperation;

/**
 *
 * @author Diarsid
 */
public class CliAdapterForTasksKeeper extends AbstractCliAdapter {
    
    private final TasksKeeper tasksKeeper;
    
    public CliAdapterForTasksKeeper(TasksKeeper tasksKeeper, InnerIoEngine ioEngine) {
        super(ioEngine);
        this.tasksKeeper = tasksKeeper;
    }
    
    void removeTaskAndReport(Initiator initiator, ArgumentsCommand command) {
        VoidOperation flow = this.tasksKeeper.deleteTask(initiator, command);
        super.reportVoidOperationFlow(initiator, flow, "removed.");
    }
    
    void createTaskAndReport(Initiator initiator, ArgumentsCommand command) {
        VoidOperation flow = this.tasksKeeper.createTask(initiator, command);
        super.reportVoidOperationFlow(initiator, flow, "created!");
    }
    
    void editTaskAndReport(Initiator initiator, ArgumentsCommand command) {
        VoidOperation flow = this.tasksKeeper.editTask(initiator, command);
        super.reportVoidOperationFlow(initiator, flow, "done!");
    }
    
    void findTasksAndReport(Initiator initiator, ArgumentsCommand command) {
        ValueOperation<List<Task>> flow = this.tasksKeeper.findTasks(initiator, command);
        Function<OkValueOperation, Message> ifSuccess = (success) -> {
            return tasksToMessage((List<Task>) success.getOrThrow());
        };
        super.reportReturnOperationFlow(initiator, flow, ifSuccess, "tasks not found.");
    }
}
