/*
 * project: Beam
 * author: Diarsid
 */
package diarsid.beam.core.modules.taskswatcher;

import java.util.concurrent.ScheduledThreadPoolExecutor;

import diarsid.beam.core.modules.TasksWatcherModule;
import diarsid.beam.core.modules.domainkeeper.TasksKeeper;

import static diarsid.beam.core.base.events.BeamEventRuntime.onEvent;
import static diarsid.beam.core.base.events.BeamEventRuntime.subscribe;
import static diarsid.support.log.Logging.logFor;


/**
 * Pivotal program's class intended to operate with tasks.
 * 
 * Interacts with and logically connects program's database, the way 
 * tasks are executed, scheduling of every next time of tasks execution.
 * 
 * Defines the logical sequence of actions that should be 
 * performed while tasks are executed, updated and other types of 
 * operation with tasks are performed.
 * 
 * Initially reads tasks from database when program starts it's work.
 * 
 * @author Diarsid
 */
class TasksWatcherModuleWorker implements TasksWatcherModule {
    
    private final TasksNotificationScheduler notificationScheduler;
    private final TasksExecutionScheduler executionScheduler;
    private final TasksKeeper tasksKeeper;  
    private final ScheduledThreadPoolExecutor scheduler; 

    public TasksWatcherModuleWorker(
            TasksNotificationScheduler notificationScheduler, 
            TasksExecutionScheduler executionScheduler, 
            TasksKeeper tasksKeeper, 
            ScheduledThreadPoolExecutor scheduler) {
        this.notificationScheduler = notificationScheduler;
        this.executionScheduler = executionScheduler;
        this.tasksKeeper = tasksKeeper;
        this.scheduler = scheduler;
    }
    
    @Override
    public void stopModule() {        
        this.scheduler.shutdown();
    }
    
    void beginWork() {
        this.executionScheduler.beginTasksProcessing();
        this.notificationScheduler.beginNotificationsProcessing();
        subscribe(onEvent("tasks_updated")
                .withCallback(event -> {
                    logFor(this).info("async tasks refreshing...");
                    this.executionScheduler.refresh();
                }));
    }
    
}
