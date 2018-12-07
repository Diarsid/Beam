/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package diarsid.beam.core.modules.responsivedata;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import diarsid.beam.core.base.control.io.base.actors.Initiator;
import diarsid.beam.core.base.control.io.base.actors.InnerIoEngine;
import diarsid.beam.core.base.data.DataExtractionException;
import diarsid.beam.core.domain.entities.Task;
import diarsid.beam.core.domain.entities.TaskRepeat;
import diarsid.beam.core.domain.inputparsing.time.AllowedTimePeriod;
import diarsid.beam.core.modules.data.DaoTasks;

import static java.util.Collections.emptyList;

/**
 *
 * @author Diarsid
 */
public class ResponsiveDaoTasks extends BeamCommonResponsiveDao<DaoTasks> {

    ResponsiveDaoTasks(DaoTasks dao, InnerIoEngine ioEngine) {
        super(dao, ioEngine);
    }
    
    public Optional<LocalDateTime> getTimeOfFirstActiveTask(
            Initiator initiator) {
        try {
            return super.dao().getTimeOfFirstActiveTask();
        } catch (DataExtractionException e) {
            super.responseOn(initiator, e);
            return Optional.empty();
        }
    } 
    
    public List<Task> getActiveTasksOfTypeBetweenDates(
            Initiator initiator, LocalDateTime from, LocalDateTime to, TaskRepeat... type) { 
        try {
            return super.dao().getActiveTasksOfTypeBetweenDates(from, to, type);
        } catch (DataExtractionException e) {
            super.responseOn(initiator, e);
            return emptyList();
        }
    } 
    
    public List<Task> getActiveTasksBeforeTime(
            Initiator initiator, LocalDateTime fromNow) { 
        try {
            return super.dao().getActiveTasksBeforeTime(fromNow);
        } catch (DataExtractionException e) {
            super.responseOn(initiator, e);
            return emptyList();
        }
    } 
    
    public List<Task> findTasksByTextPattern(
            Initiator initiator, String textPattern) {
        try {
            return super.dao().findTasksByTextPattern(textPattern);
        } catch (DataExtractionException e) {
            super.responseOn(initiator, e);
            return emptyList();
        }
    } 
    
    public boolean updateTasks(
            Initiator initiator, List<Task> tasks) {
        try {
            return super.dao().updateTasks(tasks);
        } catch (DataExtractionException e) {
            super.responseOn(initiator, e);
            return false;
        }
    } 
    
    public boolean saveTask(
            Initiator initiator, Task task) {
        try {
            return super.dao().saveTask(task);
        } catch (DataExtractionException e) {
            super.responseOn(initiator, e);
            return false;
        }
    } 
    
    public boolean deleteTaskById(
            Initiator initiator, int id) {
        try {
            return super.dao().deleteTaskById(id);
        } catch (DataExtractionException e) {
            super.responseOn(initiator, e);
            return false;
        }
    } 
    
    public boolean editTaskText(
            Initiator initiator, int taskId, List<String> newText) {
        try {
            return super.dao().editTaskText(taskId, newText);
        } catch (DataExtractionException e) {
            super.responseOn(initiator, e);
            return false;
        }
    } 
    
    public boolean editTaskTime(
            Initiator initiator, int taskId, LocalDateTime newTime) {
        try {
            return super.dao().editTaskTime(taskId, newTime);
        } catch (DataExtractionException e) {
            super.responseOn(initiator, e);
            return false;
        }
    } 
    
    public boolean editTaskTime(
            Initiator initiator, int taskId, LocalDateTime newTime, AllowedTimePeriod timePeriod) { 
        try {
            return super.dao().editTaskTime(taskId, newTime, timePeriod);
        } catch (DataExtractionException e) {
            super.responseOn(initiator, e);
            return false;
        }
    } 
}
