/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package diarsid.beam.core.modules.data;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import diarsid.beam.core.base.data.DataExtractionException;
import diarsid.beam.core.domain.entities.Task;
import diarsid.beam.core.domain.entities.TaskRepeat;
import diarsid.beam.core.domain.inputparsing.time.AllowedTimePeriod;

/**
 *
 * @author Diarsid
 */
public interface DaoTasks extends Dao {
    
    Optional<LocalDateTime> getTimeOfFirstActiveTask() 
            throws DataExtractionException;
    
    List<Task> getActiveTasksOfTypeBetweenDates(
            LocalDateTime from, LocalDateTime to, TaskRepeat... type) 
            throws DataExtractionException;
    
    List<Task> getActiveTasksBeforeTime(
            LocalDateTime fromNow) 
            throws DataExtractionException;
    
    List<Task> findTasksByTextPattern(
            String textPattern) 
            throws DataExtractionException;
    
    boolean updateTasks(
            List<Task> tasks) 
            throws DataExtractionException;
    
    boolean saveTask(
            Task task) 
            throws DataExtractionException;
    
    boolean deleteTaskById(
            int id) 
            throws DataExtractionException;
    
    boolean editTaskText(
            int taskId, List<String> newText) 
            throws DataExtractionException;
    
    boolean editTaskTime(
            int taskId, LocalDateTime newTime) 
            throws DataExtractionException;
    
    boolean editTaskTime(
            int taskId, LocalDateTime newTime, AllowedTimePeriod timePeriod) 
            throws DataExtractionException;
}
