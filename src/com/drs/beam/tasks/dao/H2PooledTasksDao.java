/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.drs.beam.tasks.dao;

import com.drs.beam.io.BeamIO;
import com.drs.beam.io.InnerIOIF;
import com.drs.beam.tasks.Task;
import java.sql.*;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import org.h2.jdbcx.JdbcConnectionPool;

/**
 *
 * @author Diarsid
 */
public class H2PooledTasksDao implements TasksDao{
    // Fields =============================================================================
    private final JdbcConnectionPool conPool;
    private final InnerIOIF ioEngine = BeamIO.getInnerIO();
    
    // SQL statements
    private final String INSERT_NEW_TASK = 
            "INSERT INTO tasks VALUES (?, ?, ?, ?, ?)";
    private final String DETECT_TABLE = 
            "SELECT TOP 1 * FROM tasks";
    private final String CREATE_TABLE = 
            "CREATE TABLE tasks (" +
            "t_id       INTEGER         NOT NULL PRIMARY KEY, " +
            "t_time     CHARACTER(16)   NOT NULL, " +
            "t_content  VARCHAR(200)    NOT NULL, " +
            "t_type     VARCHAR(5)      NOT NULL," +
            "t_status   BOOLEAN         NOT NULL)";
    private final String GET_MAX_ID = 
            "SELECT MAX(t_id) " +
            "FROM tasks ";
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
            "SELECT t_id, t_time, t_content, t_type " +
            "FROM tasks";
    private final String SELECT_TASKS_WHERE_STATUS = 
            "SELECT t_id, t_time, t_content, t_type " +
            "FROM tasks " + 
            "WHERE t_status IS ?";
    private final String SELECT_TASKS_WHERE_TIME = 
            "SELECT t_id, t_time, t_content, t_type " +
            "FROM tasks " +
            "WHERE t_time = ?";
    private final String DELETE_ALL_TASKS = 
            "DELETE " + 
            "FROM tasks ";
    private final String DELETE_TASKS_WHERE_STATUS = 
            "DELETE " +
            "FROM tasks " +
            "WHERE t_status IS ?";
    private final String DELETE_TASKS_WHERE_TEXT = 
            "DELETE " +
            "FROM tasks " +
            "WHERE t_content LIKE ?";
    
    // Constructor ========================================================================
    public H2PooledTasksDao(JdbcConnectionPool cp){
        this.conPool = cp;
        cp.setMaxConnections(1);
    }

    // Methods ============================================================================
    
    /*
    * Method for saving tasks into H2 database.
    */
    @Override
    public void saveTask(Task task) {
        try(Connection con = conPool.getConnection();
            PreparedStatement st = con.prepareStatement(INSERT_NEW_TASK);)
        {   
            st.setInt       (1, task.getId());
            st.setString    (2, task.getTimeDBString());
            st.setString    (3, task.getContentForStoring());
            st.setString    (4, task.getType());
            st.setBoolean   (5, true);
            
            st.executeUpdate();
        } catch (SQLException e){
            processSQLException(e);
        }
    }
    
    /*
    * Method to check whether currently using database has required table for tasks storing.
    * Check if table "tasks" exist in the database and if it has 5 columns.
    */
    @Override
    public boolean isDBinitialized(){
        try(Connection con = conPool.getConnection();
            Statement st = con.createStatement();)
        {
            ResultSet rs = st.executeQuery(DETECT_TABLE);
            ResultSetMetaData tableData = rs.getMetaData();
            return (tableData.getColumnCount() == 5);            
        }catch (SQLException e){
            processSQLException(e);            
            return false;
        }
    }
    
    /*
    * Method to create required table for tasks storing if such one doesn`t exists yet.
    */
    @Override
    public void initTasksTable(){
        try(Connection con = conPool.getConnection();
            Statement st = con.createStatement();)
        {            
            st.executeUpdate(CREATE_TABLE);            
        } catch (SQLException e){
            processSQLException(e);
        }
    }
    
    @Override
    public int getLastId(){
        try(Connection con = conPool.getConnection();
            Statement st = con.createStatement();
            ResultSet rs = st.executeQuery(GET_MAX_ID);)
        {
            rs.next();
            return rs.getInt(1);            
        } catch (SQLException e){
            processSQLException(e); 
            return -1;
        }
    }
    
    @Override
    public LocalDateTime getFirstTaskTime(){        
        try(Connection con = conPool.getConnection();
            Statement st = con.createStatement();
            ResultSet rs = st.executeQuery(GET_FIRST_TIME);)
        {
            LocalDateTime time;
            String timeString = null;
            rs.first();
            timeString = rs.getString(1);
            if (timeString != null){
                time = LocalDateTime.parse(
                        timeString, 
                        DateTimeFormatter.ofPattern(Task.DB_TIME_PATTERN));
                return time;
            } else {
                return null;
            }                    
        }catch (SQLException e){
            processSQLException(e);
            return null;
        }
    }  
    
    @Override
    public ArrayList<Task> extractFirstTasks(){
        ArrayList<Task> retrievedTasks = new ArrayList<>();
        try (Connection con = conPool.getConnection();
            // create updatable ResultSet because we need update 't_status' field 
            // for usual task and increase 't_time' value of event for 1 year 
            Statement st = con.createStatement(
                    ResultSet.TYPE_SCROLL_INSENSITIVE, 
                    ResultSet.CONCUR_UPDATABLE);
            ResultSet rs = st.executeQuery(EXTRACT_FIRST_TASKS);)
        {            
            // processing each row of this result set
            while (rs.next()){
                // restoring tasks back from DB
                Task task = getTaskFromResultSet(rs);
                retrievedTasks.add(task);
                // updating DB table trough the updatable result set
                update(rs, task);
            }
        } catch (SQLException e) {
            processSQLException(e);
        }
        return retrievedTasks;
    }
    
    @Override
    public ArrayList<Task> extractExpiredTasks(LocalDateTime fromNow){
        ArrayList<Task> expiredTasks = new ArrayList<>();
        try(
            Connection con = conPool.getConnection();
            PreparedStatement st = con.prepareStatement(
                    EXTRACT_EXPIRED_TASKS,
                    ResultSet.TYPE_SCROLL_INSENSITIVE, 
                    ResultSet.CONCUR_UPDATABLE);
            )            
        {
            st.setString(1, 
                    fromNow.format(DateTimeFormatter.ofPattern(Task.DB_TIME_PATTERN)));
            ResultSet rs = st.executeQuery();
            // processing each row of this result set
            while (rs.next()){
                // restoring tasks back from DB
                Task task = getTaskFromResultSet(rs);
                expiredTasks.add(task);
                // updating DB table trough the updatable result set
                update(rs, task);
            }
            rs.close();
        }catch (SQLException e) {
            processSQLException(e);
        }
        return expiredTasks;
    }
    
    /*
    * Gets tasks from DB according to passed int value representing required tasks status:
    * -1 : get all expired tasks (t_status = FALSE)
    * 1 : get all actual tasks (t_status = TRUE)
    * 0 : get all tasks regardless of its status
    */
    @Override
    public ArrayList<Task> getTasks(int isActive){
        ArrayList<Task> tasks = new ArrayList<>();
        try(Connection con = conPool.getConnection();)
        {
            PreparedStatement st;
            if (isActive == 0){
                st = con.prepareStatement(SELECT_ALL_TASKS);
            } else {
                st = con.prepareStatement(SELECT_TASKS_WHERE_STATUS);
                st.setBoolean(1, isActive > 0);
            }
            
            ResultSet rs = st.executeQuery();
            while (rs.next()){
                Task task = getTaskFromResultSet(rs);
                tasks.add(task);
            } 
            
            rs.close();
            st.close();
        }catch (SQLException e) {
            processSQLException(e);
        }
        return tasks;
    }
    
    @Override
    public ArrayList<Task> getTasksByTime(LocalDateTime time){
        ArrayList<Task> tasks = new ArrayList<>();
        try(Connection con = conPool.getConnection();
            PreparedStatement st = con.prepareStatement(SELECT_TASKS_WHERE_TIME);)
        {
            st.setString(1,
                    time.format(DateTimeFormatter.ofPattern(Task.DB_TIME_PATTERN)));
            ResultSet rs = st.executeQuery();
            while (rs.next()){
                Task task = getTaskFromResultSet(rs);
                tasks.add(task);
            }
            rs.close();
        }catch (SQLException e) {
            processSQLException(e);
        }
        return tasks;
    }
    
    @Override
    public boolean deleteTaskByText(String text){
        try(Connection con = conPool.getConnection();
            PreparedStatement st = con.prepareStatement(DELETE_TASKS_WHERE_TEXT);)            
        {
            st.setString(1, "%"+text+"%");
            int qty = st.executeUpdate();
            return (qty > 0); 
        }catch (SQLException e) {
            processSQLException(e);
            return false;
        }
    }
        
    @Override
    public boolean deleteTasks(int tasksSort){
        try(Connection con = conPool.getConnection();)            
        {
            PreparedStatement st;
            if (tasksSort == 0){
                st = con.prepareStatement(DELETE_ALL_TASKS);
            } else {
                st = con.prepareStatement(DELETE_TASKS_WHERE_STATUS);
                // set BOOLEAN status value according to given int value
                st.setBoolean(1, tasksSort > 0);
            }
            int qty = st.executeUpdate();
            st.close();
            return (qty > 0);
        }catch (SQLException e) {
            processSQLException(e);            
            return false;
        }
    }
    
    // privat util methods
    
    private void processSQLException(SQLException e){
        ioEngine.informAboutError(e.getMessage());
        ioEngine.informAboutError("------> stack trace :");
        for (StackTraceElement element : e.getStackTrace()){
            ioEngine.informAboutError(element.toString());
        }        
    }
    
    private Task getTaskFromResultSet(ResultSet rs) throws SQLException{
        Task task = Task.restoreTask(
                    rs.getString("t_type"), 
                    rs.getInt("t_id"), 
                    LocalDateTime.parse(
                            rs.getString("t_time"), 
                            DateTimeFormatter.ofPattern(Task.DB_TIME_PATTERN)), 
                    rs.getString("t_content"));
        return task;
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
                        // update: cnage status to FALSE
                        rs.updateBoolean("t_status", false);
                        break updateSQL;
                    }
                }
        rs.updateRow();   
    }
}
