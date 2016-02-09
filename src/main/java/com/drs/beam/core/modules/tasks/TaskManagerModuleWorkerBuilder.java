/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package com.drs.beam.core.modules.tasks;

import java.util.concurrent.ScheduledThreadPoolExecutor;

import com.drs.beam.core.modules.DataModule;
import com.drs.beam.core.modules.IoInnerModule;
import com.drs.beam.core.modules.TaskManagerModule;
import com.drs.beam.core.modules.data.DaoTasks;
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
        Object lock = new Object();
        ScheduledThreadPoolExecutor sheduler = new ScheduledThreadPoolExecutor(1);
        sheduler.setMaximumPoolSize(1);
        
        TaskManagerModuleWorker taskManager = new TaskManagerModuleWorker(
                this.ioEngine, this.tasksDao, formatter, lock, sheduler);
        
        taskManager.beginWork();
        return taskManager;
    }
}
