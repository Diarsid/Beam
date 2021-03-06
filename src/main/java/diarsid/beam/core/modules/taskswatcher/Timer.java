/*
 * project: Beam
 * author: Diarsid
 */
package diarsid.beam.core.modules.taskswatcher;

import java.time.LocalDateTime;

    /*
    Runnable class to watch current time, check alarm task's time
    and perform tasks when their time comes
    */

class Timer implements Runnable {
    
    // Fields =============================================================================
    private final TasksWatcherModuleWorker taskManager;

    // Constructor ========================================================================
    
    Timer(TasksWatcherModuleWorker tm){
        this.taskManager = tm;
    }

    // Methods ============================================================================
    @Override
    public void run(){
        /*
        try{
            
            // endless loop for work
            while (true){
                // check if there are any tasks to watch their alarm time
                while (taskManager.isAnyTasks()){
                    // compare current time with the earliest task's time.
                    if  (LocalDateTime.now().isAfter(taskManager.getFirstTaskTime())){
                        // If first task`s time comes perform it's task
                        taskManager.performFirstTasks();
                    }
                    // pause
                    Thread.sleep(300);
                }
                // pause
                Thread.sleep(300);
            }
        } catch (InterruptedException e){}
        */
    }
}