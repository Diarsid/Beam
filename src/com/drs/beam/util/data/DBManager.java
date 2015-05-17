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
    
    static {
        ConfigReader config = ConfigReader.getReader();
        try {
            Class.forName(config.getCoreDBDriver());
        } catch (Exception e) {
            // if there is any problem during database driver loading program can not 
            // work further and must be finished.
            ioEngine.informAboutException(e, true);
        }
    }
    
    // Methods ============================================================================
        
    public static TasksDao getTasksDAO(){
        ConfigReader config = ConfigReader.getReader();
        TasksDao dao = null;
        choosing: switch(config.getCoreDBName()){
            case ("H2pooled") : {
                DataBase db = new DataBaseH2Pooled(config.getCoreDBURL(), "sa", "", 3);
                dao = new TasksDaoH2(db);
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
        }
        if (dao == null){
            ioEngine.informAboutError(
                    "Database connection failure: TasksDao == null.", true);
        }
        return dao;
    }    
    
    public static ExecutorDao getExecutorDao(){
        ConfigReader config = ConfigReader.getReader();
        ExecutorDao dao = null;
        choosing: switch(config.getCoreDBName()){
            case ("H2pooled") : {
                DataBase db = new DataBaseH2Pooled(config.getCoreDBURL(), "sa", "", 3);
                dao = new ExecutorDaoH2(db);
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
        }
        if (dao == null){
            ioEngine.informAboutError(
                    "Database connection failure: ExecutorDao == null.", true);
        }
        return dao;
    }
}
