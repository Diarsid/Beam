/*
 * project: Beam
 * author: Diarsid
 */
package com.drs.beam.util.data;

import com.drs.beam.executor.dao.ExecutorDao;
import com.drs.beam.executor.dao.ExecutorDaoH2;
import com.drs.beam.util.config.ConfigReader;
import com.drs.beam.io.BeamIO;
import com.drs.beam.io.InnerIOIF;
import com.drs.beam.tasks.dao.TasksDaoH2;
import com.drs.beam.tasks.dao.TasksDao;

/*
 * 
 */
public class DBManager {
    // Fields =============================================================================
    private static final InnerIOIF ioEngine = BeamIO.getInnerIO();
    private static DataBase db;
    private static TasksDao tasksDao;
    private static ExecutorDao executorDao;
    
    static {
        ConfigReader config = ConfigReader.getReader();
        try {
            Class.forName(config.getCoreDBDriver());
        } catch (Exception e) {
            // if there is any problem during database driver loading program can not 
            // work further and must be finished.
            ioEngine.informAboutException(e, true);
        }
        choosing: switch(config.getCoreDBName()){
            case ("H2pooled") : {
                db = new DataBaseH2Pooled(config.getCoreDBURL(), "sa", "", 3);
                tasksDao = new TasksDaoH2(db);
                executorDao = new ExecutorDaoH2(db);
                break choosing;
            }
            case ("H2") : {
                //
                break choosing;
            }
            case ("SQLite") : {
                //
                break choosing;
            }
            default : {
                ioEngine.informAboutError("DBManager init error: unrecognized data base.",
                        true);
            }
        }
        if (tasksDao == null){
            ioEngine.informAboutError(
                    "DBManager init error: tasks DAO == null.", true);
        }
        if (executorDao == null){
            ioEngine.informAboutError(
                    "DBManager init error: executor DAO == null.", true);
        }
        
    }
    
    // Methods ============================================================================
        
    public static TasksDao getTasksDAO(){
        return tasksDao;
    }    
    
    public static ExecutorDao getExecutorDao(){        
        return executorDao;
    }
}
