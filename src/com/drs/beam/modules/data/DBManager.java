/*
 * project: Beam
 * author: Diarsid
 */
package com.drs.beam.modules.data;

import com.drs.beam.modules.data.base.DataBaseH2Pooled;
import com.drs.beam.modules.data.base.DataBase;
import com.drs.beam.modules.data.dao.executor.ExecutorDao;
import com.drs.beam.modules.data.dao.executor.ExecutorDaoH2;
import com.drs.beam.util.config.ConfigContainer;
import com.drs.beam.modules.io.BeamIO;
import com.drs.beam.modules.io.InnerIOIF;
import com.drs.beam.modules.data.dao.tasks.TasksDaoH2;
import com.drs.beam.modules.data.dao.tasks.TasksDao;
import com.drs.beam.util.config.ConfigParams;

/*
 * 
 */
public class DBManager {
    // Fields =============================================================================
    private static final InnerIOIF ioEngine = BeamIO.getInnerIO();
    private static DataBase db;
    private static TasksDao tasksDao;
    private static ExecutorDao executorDao;       
        
    // Methods ============================================================================
        
    public static TasksDao getTasksDAO(){
        return tasksDao;
    }    
    
    public static ExecutorDao getExecutorDao(){        
        return executorDao;
    }
    
    public static void init(){
        DBInitializer init = new DBInitializer();
        try {
            Class.forName(ConfigContainer.getParam(ConfigParams.CORE_DB_DRIVER));
        } catch (Exception e) {
            // If there is any problem during database driver loading program can not 
            // work further and must be finished.
            ioEngine.informAboutException(e, true);
        }
        choosing: switch(ConfigContainer.getParam(ConfigParams.CORE_DB_NAME).toLowerCase()){
            case ("h2pooled") : {
                // user login : sa
                // user pass  : 
                // max connections in pool : 3
                db = new DataBaseH2Pooled(
                        ConfigContainer.getParam(ConfigParams.CORE_DB_URL), "sa", "", 3);                
                init.initDataBase(db);
                tasksDao = new TasksDaoH2(db);
                executorDao = new ExecutorDaoH2(db);
                break choosing;
            }
            case ("h2") : {
                //
                break choosing;
            }
            case ("sqlite") : {
                //
                break choosing;
            }
            default : {
                ioEngine.informAboutError("DBManager init error: unrecognized data base.",
                        true);
            }
        }
        if (tasksDao == null || executorDao == null){
            ioEngine.informAboutError(
                    "DBManager init error: DAO objects init error", true);
        }        
    }  
}
