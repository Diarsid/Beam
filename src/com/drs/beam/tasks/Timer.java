package com.drs.beam.tasks;

import java.time.LocalDateTime;

/**
 * Created with IntelliJ IDEA.
 * User: Diarsid
 * Date: 04.09.14
 * Time: 1:15
 * To change this template use File | Settings | File Templates.
 */

    /*
    Runnable class to watch current time, check alarm task's time
    and perform tasks when their time comes
    */

class Timer implements Runnable {
    // Fields declaration ----------------------------------------------------------------------------------------------
    private TaskManager taskManager;   // TaskManager to operate with tasks

    // Constructor accepts TaskManager to operate with tasks
    // Creates his own Thread, names and starts it
    Timer(TaskManager tm){
        this.taskManager = tm;
        (
                new Thread(this, this.getClass().getName())
        ).start();
    }

    // Methods ---------------------------------------------------------------------------------------------------------
    public void run(){
        try{
            // endless loop for work
            while (true){
                // check if there are any tasks to watch their alarm time
                while (taskManager.isAnyTasks()){
                    // compare current time with the earliest task's time.
                    if  (LocalDateTime.now().isAfter(taskManager.getFirstTaskTime())){
                        // If first task`s time comes perform it's task
                        taskManager.performFirstTask();
                    }
                    // pause
                    Thread.sleep(300);
                }
                // pause
                Thread.sleep(300);
            }
        } catch (InterruptedException e){}
    }
}