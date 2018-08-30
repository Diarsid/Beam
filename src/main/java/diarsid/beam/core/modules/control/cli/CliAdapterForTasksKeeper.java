/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package diarsid.beam.core.modules.control.cli;

import java.util.List;
import java.util.function.Function;

import diarsid.beam.core.base.control.flow.ValueFlow;
import diarsid.beam.core.base.control.flow.ValueFlowCompleted;
import diarsid.beam.core.base.control.flow.VoidFlow;
import diarsid.beam.core.base.control.io.base.actors.Initiator;
import diarsid.beam.core.base.control.io.base.actors.InnerIoEngine;
import diarsid.beam.core.base.control.io.base.interaction.Message;
import diarsid.beam.core.base.control.io.commands.ArgumentsCommand;
import diarsid.beam.core.domain.entities.Task;
import diarsid.beam.core.modules.domainkeeper.TasksKeeper;

import static diarsid.beam.core.base.control.io.base.interaction.Messages.tasksToMessage;

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
        VoidFlow flow = this.tasksKeeper.deleteTask(initiator, command);
        super.reportVoidFlow(initiator, flow, "removed.");
    }
    
    void createTaskAndReport(Initiator initiator, ArgumentsCommand command) {
        VoidFlow flow = this.tasksKeeper.createTask(initiator, command);
        super.reportVoidFlow(initiator, flow, "created!");
    }
    
    void editTaskAndReport(Initiator initiator, ArgumentsCommand command) {
        VoidFlow flow = this.tasksKeeper.editTask(initiator, command);
        super.reportVoidFlow(initiator, flow, "done!");
    }
    
    void findTasksAndReport(Initiator initiator, ArgumentsCommand command) {
        ValueFlow<List<Task>> flow = this.tasksKeeper.findTasks(initiator, command);
        Function<ValueFlowCompleted<List<Task>>, Message> ifSuccess = (success) -> {
            return tasksToMessage(success.orThrow());
        };
        super.reportValueFlow(initiator, flow, ifSuccess, "tasks not found.");
    }
}
