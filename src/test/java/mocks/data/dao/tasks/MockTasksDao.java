/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package mocks.data.dao.tasks;

import java.sql.SQLException;
import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.drs.beam.server.entities.task.Task;
import com.drs.beam.server.modules.data.dao.tasks.TasksDao;

/**
 *
 * @author Diarsid
 */
public class MockTasksDao implements TasksDao{
    // Fields =============================================================================
    private int id;
    private Map<Integer, Task> fakeData;

    // Constructors =======================================================================

    public MockTasksDao() {
        id = 0;
        fakeData = new HashMap<>();
    }    
    
    // Methods ============================================================================
    
    @Override
    public void saveTask(Task task) throws SQLException{
        fakeData.put(id, task);
        id++;
    } 
    
    @Override
    public boolean deleteTaskByText(String text) throws SQLException{
        
    }  
    
    @Override
    public boolean deleteTasks(int tasksSort) throws SQLException{
        
    }  
        
    @Override
    public List<Task> extractFirstTasks () throws SQLException{
        
    }  
    
    @Override
    public List<Task> extractExpiredTasks (LocalDateTime fromNow) throws SQLException{
        
    }  
   
    @Override
    public LocalDateTime getFirstTaskTime () throws SQLException{
        
    } 
    
    @Override
    public List<Task> getTasks (int isActive) throws SQLException{
        
    } 
    
    @Override
    public List<Task> getTasksByTime (LocalDateTime firstTaskTime) throws SQLException{
        
    } 
}
