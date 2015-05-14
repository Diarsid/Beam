/*
 * project: Beam
 * author: Diarsid
 */
package com.drs.beam.util;

import com.drs.beam.io.BeamIO;
import com.drs.beam.io.InnerIOIF;
import com.drs.beam.tasks.dao.H2PooledTasksDao;
import com.drs.beam.tasks.dao.TasksDao;

public class DBManager {
    // Fields =============================================================================
    private static InnerIOIF ioEngine = BeamIO.getInnerIO();
    
    // Methods ============================================================================
        
    /*
    * Method provides TasksDao interface implementation instance for further usage by
    * TaskManager instance
    */
    public static TasksDao getTasksDAO(){
        ConfigReader config = ConfigReader.getReader();
        try {
            Class.forName(config.getCoreDBDriver());
        } catch (Exception e) {
            // if there is any problem during database driver loading program can not 
            // work further and must be finished.
            e.printStackTrace();
            System.exit(1);
        }
        TasksDao dao = null;
        choosing: switch(config.getCoreDBName()){
            case ("H2pooled") : {
                dao = new H2PooledTasksDao(config.getCoreDBURL(), "sa", "");
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
            ioEngine.informAboutError("Database connection failure: dao == null.");
            System.exit(1);
        }
        return dao;
    }    
}
