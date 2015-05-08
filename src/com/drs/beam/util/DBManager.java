package com.drs.beam.util;

import com.drs.beam.io.BeamIO;
import com.drs.beam.io.InnerIOIF;
import com.drs.beam.tasks.dao.H2PooledTasksDao;
import com.drs.beam.tasks.dao.TasksDao;
import org.h2.jdbcx.JdbcConnectionPool;

/**
 * Created with IntelliJ IDEA.
 * User: Diarsid
 * Date: 02.09.14
 * Time: 0:56
 * To change this template use File | Settings | File Templates.
 */
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
                dao = getPooledH2(config);
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
    
    /*
    * Private method provides H2PooledTasksDao implementation of TasksDao interface for
    * H2 database with built in H2 connection pool.
    */
    private static H2PooledTasksDao getPooledH2(ConfigReader config){
        JdbcConnectionPool cp = JdbcConnectionPool.create(
            config.getCoreDBURL(), "sa", "");
        return new H2PooledTasksDao(cp);
    }
    
}
