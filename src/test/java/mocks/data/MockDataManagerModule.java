/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package mocks.data;

import com.drs.beam.server.modules.data.DataManagerModule;
import com.drs.beam.server.modules.data.dao.commands.CommandsDao;
import com.drs.beam.server.modules.data.dao.locations.LocationsDao;
import com.drs.beam.server.modules.data.dao.tasks.TasksDao;

/**
 *
 * @author Diarsid
 */
public class MockDataManagerModule implements DataManagerModule{
    // Fields =============================================================================

    // Constructors =======================================================================

    // Methods ============================================================================
    
    @Override
    public TasksDao getTasksDao(){
        
    }   
    
    @Override
    public LocationsDao getLocationsDao(){
        
    }   
    
    @Override
    public CommandsDao getCommandsDao(){
        
    }     
}
