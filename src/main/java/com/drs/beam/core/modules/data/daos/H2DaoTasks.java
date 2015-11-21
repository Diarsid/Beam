/*
 * project: Beam
 * author: Diarsid
 */
package com.drs.beam.core.modules.data.daos;

import com.drs.beam.core.modules.data.DaoTasks;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import com.drs.beam.core.exceptions.NullDependencyInjectionException;
import com.drs.beam.core.modules.IoInnerModule;
import com.drs.beam.core.modules.tasks.Task;
import com.drs.beam.core.modules.data.DataBase;
import com.drs.beam.core.exceptions.ModuleInitializationException;

/**
 *
 * @author Diarsid
 */
public class H2DaoTasks implements DaoTasks{
    // Fields =============================================================================
    private final DataBase data;
    private final IoInnerModule ioEngine;
    
    private final String INSERT_NEW_TASK = 
            "INSERT INTO tasks (t_time, t_content, t_type, t_status) " +
            "VALUES (?, ?, ?, ?)";  
    private final String GET_FIRST_TIME = 
            "SELECT MIN(t_time) " +
            "FROM tasks " +
            "WHERE t_status = TRUE";
    private final String EXTRACT_FIRST_TASKS = 
            "SELECT t_id, t_time, t_content, t_type, t_status " +
            "FROM tasks " + 
            "WHERE t_time IN " +
            "       (SELECT MIN(t_time) " + 
            "       FROM tasks " +
            "       WHERE t_status = TRUE)";
    private final String EXTRACT_EXPIRED_TASKS = 
            "SELECT t_id, t_time, t_content, t_type, t_status " +
            "FROM tasks " + 
            "WHERE (t_time < ?) AND (t_status IS TRUE)";
    private final String SELECT_ALL_TASKS = 
            "SELECT t_time, t_content, t_type " +
            "FROM tasks";
    private final String SELECT_ACTUAL_TASKS = 
            "SELECT t_time, t_content, t_type " +
            "FROM tasks " + 
            "WHERE t_status IS TRUE";
    private final String SELECT_NON_ACTUAL_TASKS = 
            "SELECT t_time, t_content, t_type " +
            "FROM tasks " + 
            "WHERE t_status IS FALSE";
    private final String SELECT_TASKS_WHERE_TIME = 
            "SELECT t_time, t_content, t_type " +
            "FROM tasks " +
            "WHERE t_time = ?";
    private final String DELETE_ALL_TASKS = 
            "DELETE " + 
            "FROM tasks ";
    private final String DELETE_ACTUAL_TASKS = 
            "DELETE " +
            "FROM tasks " +
            "WHERE t_status IS TRUE";
    private final String DELETE_NON_ACTUAL_TASKS = 
            "DELETE " +
            "FROM tasks " +
            "WHERE t_status IS FALSE";
    private final String DELETE_TASKS_WHERE_TEXT = 
            "DELETE " +
            "FROM tasks " +
            "WHERE t_content LIKE ?";
    
    private final String ANY_SYMBOLS = "%";
    
    // Constructor ========================================================================
    public H2DaoTasks(IoInnerModule io, DataBase dataBase){
        if (io == null){
            throw new NullDependencyInjectionException(
                    H2DaoTasks.class.getSimpleName(), IoInnerModule.class.getSimpleName());
        }
        if (dataBase == null){
            throw new NullDependencyInjectionException(
                    H2DaoTasks.class.getSimpleName(), DataBase.class.getSimpleName());
        }
        this.data = dataBase;
        this.ioEngine = io;
    }

    // Methods ============================================================================
          
    private Task getTaskFromResultSet(ResultSet rs) throws SQLException{
        return new Task(
                    rs.getString("t_type"),
                    LocalDateTime.parse(
                            rs.getString("t_time"), 
                            DateTimeFormatter.ofPattern(Task.DB_TIME_PATTERN)), 
                    rs.getString("t_content"));        
    }
    
    private void update(ResultSet rs, Task taskToUpdate) throws SQLException{
        updateSQL:
                switch(taskToUpdate.getType()) {
                    case("event") : {
                        // update: increase year by 1, status remains TRUE
                        String newCalendarEventTime = 
                            taskToUpdate.getTime().plusYears(1)
                            .format(DateTimeFormatter.ofPattern(Task.DB_TIME_PATTERN));
                        rs.updateString("t_time", newCalendarEventTime);
                        break updateSQL;
                    }
                    case("task") : {
                        // update: change status to FALSE
                        rs.updateBoolean("t_status", false);
                        break updateSQL;
                    }
                    default : {
                        throw new SQLException();
                    }
                }
        rs.updateRow();   
    }
    
    // TasksDAO interface methods to perform SQL
    
    /*
    * Method for saving tasks into H2 database.
    */
    @Override
    public void saveTask(Task task){
        try (Connection con = data.connect();
            PreparedStatement st = con.prepareStatement(INSERT_NEW_TASK);) {
            
            st.setString    (1, task.getTimeDBString());
            st.setString    (2, task.getContentForStoring());
            st.setString    (3, task.getType());
            st.setBoolean   (4, true);

            st.executeUpdate();
        } catch (SQLException e) {
            this.ioEngine.reportException(e, "SQLException: task saving.");
        }
    }       
    
    @Override
    public LocalDateTime getFirstTaskTime(){        
        try (Connection con = data.connect();
            Statement st = con.createStatement();
            ResultSet rs = st.executeQuery(GET_FIRST_TIME);) {
            
            rs.first();
            if (rs.getString(1) != null){
                return LocalDateTime.parse(
                        rs.getString(1), 
                        DateTimeFormatter.ofPattern(Task.DB_TIME_PATTERN));
            } else {
                return null;
            }
        } catch (SQLException e){
            this.ioEngine.reportExceptionAndExitLater(e, 
                    "SQLException: get first task time.", 
                    "Program will be closed.");
            throw new ModuleInitializationException();
        }
    }  
    
    @Override
    public List<Task> extractFirstTasks(){
        try (Connection con = data.connect();
            Statement st = con.createStatement(
                    ResultSet.TYPE_SCROLL_INSENSITIVE, 
                    ResultSet.CONCUR_UPDATABLE);
            ResultSet rs = st.executeQuery(EXTRACT_FIRST_TASKS);) {
            
            List<Task> retrievedTasks = new ArrayList<>();
                
            while (rs.next()){
                retrievedTasks.add(new Task(
                    rs.getString("t_type"),
                    LocalDateTime.parse(
                            rs.getString("t_time"), 
                            DateTimeFormatter.ofPattern(Task.DB_TIME_PATTERN)), 
                    rs.getString("t_content")));
                
                rs.updateBoolean("t_status", false);
                rs.updateRow();
            }

            Collections.sort(retrievedTasks);
            return retrievedTasks;
        } catch (SQLException e){
            this.ioEngine.reportExceptionAndExitLater(e, 
                    "SQLException: extract firs tasks.", 
                    "Program will be closed.");
            throw new ModuleInitializationException();
        }        
    }
    
    @Override
    public List<Task> extractExpiredTasks(LocalDateTime fromNow){
        ResultSet rs = null;
        try (Connection con = data.connect();
            PreparedStatement st = con.prepareStatement(
                EXTRACT_EXPIRED_TASKS,
                ResultSet.TYPE_SCROLL_INSENSITIVE, 
                ResultSet.CONCUR_UPDATABLE);) {
            
            List<Task> expiredTasks = new ArrayList<>();            
            st.setString(1, fromNow.format(DateTimeFormatter.ofPattern(Task.DB_TIME_PATTERN)));
            rs = st.executeQuery();
            
            while (rs.next()){
                expiredTasks.add(new Task(
                    rs.getString("t_type"),
                    LocalDateTime.parse(
                            rs.getString("t_time"), 
                            DateTimeFormatter.ofPattern(Task.DB_TIME_PATTERN)), 
                    rs.getString("t_content")));
                
                rs.updateBoolean("t_status", false);
                rs.updateRow();
            }
            
            Collections.sort(expiredTasks);
            return expiredTasks;
        } catch (SQLException e){
            this.ioEngine.reportExceptionAndExitLater(e, 
                    "SQLException: extract expired tasks.", 
                    "Program will be closed.");
            throw new ModuleInitializationException();
        } finally {
            if (rs != null) {
                try {
                    rs.close();
                } catch (SQLException se){
                    this.ioEngine.reportExceptionAndExitLater(se, 
                        "Unknown problem in TasksDao.extractExpiredTasks:",
                        "ResultSet close exception.",
                        " ",
                        "Program will be closed.");
                    throw new ModuleInitializationException();
                }
            }
        }
    }
    
   
    @Override
    public List<Task> getAllTasks(){
        try(Connection con = data.connect();
            Statement st = con.createStatement();
            ResultSet rs = st.executeQuery(SELECT_ALL_TASKS)) {
            
            List<Task> tasks = new ArrayList<>();
            while (rs.next()){
                tasks.add(new Task(
                    rs.getString("t_type"),
                    LocalDateTime.parse(
                            rs.getString("t_time"), 
                            DateTimeFormatter.ofPattern(Task.DB_TIME_PATTERN)), 
                    rs.getString("t_content")));
            }
            Collections.sort(tasks);
            return tasks;
        } catch (SQLException e){
            this.ioEngine.reportExceptionAndExitLater(e, 
                    "SQLException: get all tasks.", 
                    "Program will be closed.");
            return Collections.emptyList();
        }
    }
    
    @Override
    public List<Task> getActualTasks(){
        try(Connection con = data.connect();
            Statement st = con.createStatement();
            ResultSet rs = st.executeQuery(SELECT_ACTUAL_TASKS)) {
            
            List<Task> tasks = new ArrayList<>();
            while (rs.next()){
                tasks.add(new Task(
                    rs.getString("t_type"),
                    LocalDateTime.parse(
                            rs.getString("t_time"), 
                            DateTimeFormatter.ofPattern(Task.DB_TIME_PATTERN)), 
                    rs.getString("t_content")));
            }
            Collections.sort(tasks);
            return tasks;
        } catch (SQLException e){
            this.ioEngine.reportExceptionAndExitLater(e, 
                    "SQLException: get actual tasks.", 
                    "Program will be closed.");
            return Collections.emptyList();
        }
    }
    
    @Override
    public List<Task> getNonActualTasks(){
        try(Connection con = data.connect();
            Statement st = con.createStatement();
            ResultSet rs = st.executeQuery(SELECT_NON_ACTUAL_TASKS)) {
            
            List<Task> tasks = new ArrayList<>();
            while (rs.next()){
                tasks.add(new Task(
                    rs.getString("t_type"),
                    LocalDateTime.parse(
                            rs.getString("t_time"), 
                            DateTimeFormatter.ofPattern(Task.DB_TIME_PATTERN)), 
                    rs.getString("t_content")));
            }
            Collections.reverse(tasks);
            return tasks;
        } catch (SQLException e){
            this.ioEngine.reportExceptionAndExitLater(e, 
                    "SQLException: get non-actual tasks.", 
                    "Program will be closed.");
            return Collections.emptyList();
        }
    }
    
    @Override
    public List<Task> getTasksByTime(LocalDateTime time){
        ResultSet rs = null;
        try(Connection con = data.connect();
            PreparedStatement st = con.prepareStatement(SELECT_TASKS_WHERE_TIME);) {
            
            ArrayList<Task> tasks = new ArrayList<>();
            
            st.setString(1, time.format(DateTimeFormatter.ofPattern(Task.DB_TIME_PATTERN)));
            rs = st.executeQuery();
            while (rs.next()){
                tasks.add(new Task(
                    rs.getString("t_type"),
                    LocalDateTime.parse(
                            rs.getString("t_time"), 
                            DateTimeFormatter.ofPattern(Task.DB_TIME_PATTERN)), 
                    rs.getString("t_content")));
            }
            Collections.sort(tasks);
            return tasks;
        } catch (SQLException e){
            this.ioEngine.reportException(e, "SQLException: get tasks by time.");
            return Collections.emptyList();
        } finally {
            if (rs != null) {
                try{
                    rs.close();
                } catch (SQLException se){
                    this.ioEngine.reportExceptionAndExitLater(se, 
                        "Unknown problem in TasksDao.getTasksByTime:",
                        "ResultSet close exception.",
                        " ",
                        "Program will be closed.");
                }
            }
        }
    }
    
    @Override
    public boolean deleteTaskByText(String text){
        try(Connection con = data.connect();
            PreparedStatement st = con.prepareStatement(DELETE_TASKS_WHERE_TEXT);) {
            
            st.setString(1, ANY_SYMBOLS+text+ANY_SYMBOLS);
            int qty = st.executeUpdate();
            
            return (qty > 0); 
        } catch (SQLException e){
            this.ioEngine.reportException(e, "SQLException: delete tasks by text.");
            return false;
        }
    }
        
    @Override
    public boolean deleteAllTasks(){
        try(Connection con = data.connect();
            Statement st = con.createStatement();) {
            
            int qty = st.executeUpdate(DELETE_ALL_TASKS);
            return (qty > 0);
        } catch (SQLException e) {
            this.ioEngine.reportException(e, "SQLException: delete alll tasks.");
            return false;
        }
    }
    
    @Override
    public boolean deleteActualTasks(){
        try(Connection con = data.connect();
            Statement st = con.createStatement();) {
            
            int qty = st.executeUpdate(DELETE_ACTUAL_TASKS);
            return (qty > 0);
        } catch (SQLException e) {
            this.ioEngine.reportException(e, "SQLException: delete actual tasks.");
            return false;
        }
    }
    
    @Override
    public boolean deleteNonActualTasks(){
        try(Connection con = data.connect();
            Statement st = con.createStatement();) {
            
            int qty = st.executeUpdate(DELETE_NON_ACTUAL_TASKS);
            return (qty > 0);
        } catch (SQLException e) {
            this.ioEngine.reportException(e, "SQLException: delete non-actual tasks.");
            return false;
        }
    }
}
