/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package diarsid.beam.core.modules.taskswatcher;

import java.util.concurrent.ScheduledThreadPoolExecutor;

import diarsid.beam.core.modules.io.gui.TasksGui;
import diarsid.beam.core.base.control.io.base.actors.Initiator;
import diarsid.beam.core.modules.ApplicationComponentsHolderModule;
import diarsid.beam.core.modules.DomainKeeperModule;
import diarsid.beam.core.modules.IoModule;
import diarsid.beam.core.modules.TasksWatcherModule;
import diarsid.beam.core.modules.domainkeeper.TasksKeeper;

import com.drs.gem.injector.module.GemModuleBuilder;

import static diarsid.beam.core.Beam.systemInitiator;

/**
 *
 * @author Diarsid
 */
class TasksWatcherModuleWorkerBuilder implements GemModuleBuilder<TasksWatcherModule>{
    
    private final IoModule ioModule;
    private final ApplicationComponentsHolderModule applicationComponentsHolderModule;
    private final DomainKeeperModule domainKeeperModule;

    TasksWatcherModuleWorkerBuilder(
            ApplicationComponentsHolderModule applicationComponentsHolderModule, 
            IoModule ioModule,
            DomainKeeperModule domainKeeperModule) {
        this.applicationComponentsHolderModule = applicationComponentsHolderModule;
        this.ioModule = ioModule;
        this.domainKeeperModule = domainKeeperModule;
    }
    
    @Override
    public TasksWatcherModule buildModule() {
        TasksExecutionScheduler tasksExecutionScheduler;
        TasksNotificationScheduler tasksNotificationScheduler;
        ScheduledThreadPoolExecutor scheduler;        
        TasksGui tasksGui;
        TasksKeeper tasksKeeper;
        Initiator watcherPrivateInitiator = systemInitiator();
        
        tasksGui = this.ioModule.gui().tasksGui();
        tasksKeeper = this.domainKeeperModule.tasks();
        scheduler = new ScheduledThreadPoolExecutor(2);
        scheduler.setMaximumPoolSize(2);        
        tasksExecutionScheduler = new TasksExecutionScheduler(
                tasksGui, tasksKeeper, scheduler, watcherPrivateInitiator);
        tasksNotificationScheduler = new TasksNotificationScheduler(
                tasksGui, tasksKeeper, scheduler, watcherPrivateInitiator);
        
        TasksWatcherModuleWorker taskWatcher = new TasksWatcherModuleWorker(
                tasksNotificationScheduler, tasksExecutionScheduler, tasksKeeper, scheduler);        
        taskWatcher.beginWork();
        return taskWatcher;
    }
}
