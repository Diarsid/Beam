/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package diarsid.beam.core.modules.tasks;

import java.util.concurrent.ScheduledThreadPoolExecutor;

import diarsid.beam.core.modules.DataModule;
import diarsid.beam.core.modules.IoInnerModule;
import diarsid.beam.core.modules.TaskManagerModule;

import diarsid.beam.core.modules.data.DaoTasks;

import com.drs.gem.injector.module.GemModuleBuilder;

/**
 *
 * @author Diarsid
 */
class TaskManagerModuleWorkerBuilder implements GemModuleBuilder<TaskManagerModule>{
    
    private final IoInnerModule ioEngine;
    private final DaoTasks tasksDao;
    
    TaskManagerModuleWorkerBuilder(IoInnerModule io, DataModule data) {
        this.ioEngine = io;
        this.tasksDao = data.getTasksDao();
    }
    
    @Override
    public TaskManagerModule buildModule() {
        TaskTimeFormatter formatter = new TaskTimeFormatter();
        Object execLock = new Object();
        Object notifyLock = new Object();
        ScheduledThreadPoolExecutor sheduler = new ScheduledThreadPoolExecutor(2);
        sheduler.setMaximumPoolSize(2);
        
        TaskManagerModuleWorker taskManager = new TaskManagerModuleWorker(
                this.ioEngine, this.tasksDao, formatter, execLock, notifyLock, sheduler);
        
        taskManager.beginWork();
        return taskManager;
    }
}
