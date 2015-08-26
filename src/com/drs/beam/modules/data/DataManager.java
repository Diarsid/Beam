/*
 * project: Beam
 * author: Diarsid
 */
package com.drs.beam.modules.data;

import com.drs.beam.modules.data.base.DataBaseH2Pooled;
import com.drs.beam.modules.data.base.DataBase;
import com.drs.beam.modules.data.dao.commands.CommandsDao;
import com.drs.beam.modules.data.dao.commands.CommandsDaoH2;
import com.drs.beam.modules.data.dao.locations.LocationsDao;
import com.drs.beam.modules.data.dao.locations.LocationsDaoH2;
import com.drs.beam.util.config.ConfigContainer;
import com.drs.beam.modules.io.InnerIOInterface;
import com.drs.beam.modules.data.dao.tasks.TasksDaoH2;
import com.drs.beam.modules.data.dao.tasks.TasksDao;
import com.drs.beam.util.config.ConfigParam;

/*
 * 
 */
public class DataManager{
    // Fields =============================================================================
    private TasksDao tasksDao;
    private LocationsDao locationsDao;
    private CommandsDao commandsDao;
    
    // Constructor ========================================================================
    public DataManager(InnerIOInterface io) {
        DBInitializer initializer = new DBInitializer(io);
        try {
            Class.forName(ConfigContainer.getParam(ConfigParam.CORE_DB_DRIVER));
        } catch (Exception e) {
            // If there is any problem during database driver loading program can not 
            // work further and must be finished.
            io.informAboutException(e, true);
        }
        choosing: switch(ConfigContainer.getParam(ConfigParam.CORE_DB_NAME).toLowerCase()){
            case ("h2pooled") : {
                // user login : sa
                // user pass  : 
                // max connections in pool : 3
                DataBase db = new DataBaseH2Pooled(
                        ConfigContainer.getParam(ConfigParam.CORE_DB_URL), 
                        "sa", 
                        "", 
                        3);                
                initializer.checkDataBase(db);
                this.tasksDao = new TasksDaoH2(db, io);
                this.locationsDao = new LocationsDaoH2(db, io);
                this.commandsDao = new CommandsDaoH2(db, io);
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
                io.informAboutError(
                        "DataManager initialization error: unrecognized data base.",
                        true);
            }
        }       
    }
        
    // Methods ============================================================================
        
    public TasksDao getTasksDAO(){
        return this.tasksDao;
    }    
    
    public LocationsDao getLocationsDao(){        
        return this.locationsDao;
    }
    
    public CommandsDao getCommandsDao(){
        return this.commandsDao;
    }
}
