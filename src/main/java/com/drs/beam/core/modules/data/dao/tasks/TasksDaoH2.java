/*
 * project: Beam
 * author: Diarsid
 */
package com.drs.beam.core.modules.data.dao.tasks;

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

import com.drs.beam.core.entities.Task;
import com.drs.beam.core.modules.data.base.DataBase;

/**
 *
 * @author Diarsid
 */
public class TasksDaoH2 implements TasksDao{
    // Fields =============================================================================
    private final DataBase data;
    
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
    private final String SELECT_TASKS_WHERE_STATUS = 
            "SELECT t_time, t_content, t_type " +
            "FROM tasks " + 
            "WHERE t_status IS ?";
    private final String SELECT_TASKS_WHERE_TIME = 
            "SELECT t_time, t_content, t_type " +
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
    
    private final String ANY_SYMBOLS = "%";
    
    // Constructor ========================================================================
    public TasksDaoH2(DataBase dataBase){
        this.data = dataBase;
    }

    // Methods ============================================================================
          
    private Task getTaskFromResultSet(ResultSet rs) throws SQLException{
        Task task = new Task(
                    rs.getString("t_type"),
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
    public void saveTask(Task task) throws SQLException{
        Connection con = data.connect();
        PreparedStatement st = con.prepareStatement(INSERT_NEW_TASK);

        st.setString    (1, task.getTimeDBString());
        st.setString    (2, task.getContentForStoring());
        st.setString    (3, task.getType());
        st.setBoolean   (4, true);

        st.executeUpdate();
        
        st.close();
        con.close();
    }       
    
    @Override
    public LocalDateTime getFirstTaskTime() throws SQLException{        
        Connection con = data.connect();
        Statement st = con.createStatement();
        ResultSet rs = st.executeQuery(GET_FIRST_TIME);
        LocalDateTime time;
        rs.first();
        String timeString = rs.getString(1);
        if (timeString != null){
            time = LocalDateTime.parse(
                    timeString, 
                    DateTimeFormatter.ofPattern(Task.DB_TIME_PATTERN));
            this.closeAll(con, st, rs);
            return time;
        } else {
            this.closeAll(con, st, rs);
            return null;
        }        
    }  
    
    @Override
    public List<Task> extractFirstTasks() throws SQLException{
        List<Task> retrievedTasks = new ArrayList<>();
        Connection con = data.connect();
        Statement st = con.createStatement(
                ResultSet.TYPE_SCROLL_INSENSITIVE, 
                ResultSet.CONCUR_UPDATABLE);
        ResultSet rs = st.executeQuery(EXTRACT_FIRST_TASKS);        
        while (rs.next()){
            Task task = getTaskFromResultSet(rs);
            retrievedTasks.add(task);
            // updating DB table trough the updatable result set
            update(rs, task);
        }
        this.closeAll(con, st, rs);
        Collections.sort(retrievedTasks);
        return retrievedTasks;
    }
    
    @Override
    public List<Task> extractExpiredTasks(LocalDateTime fromNow) throws SQLException{
        List<Task> expiredTasks = new ArrayList<>();
        Connection con = data.connect();
        PreparedStatement st = con.prepareStatement(
                EXTRACT_EXPIRED_TASKS,
                ResultSet.TYPE_SCROLL_INSENSITIVE, 
                ResultSet.CONCUR_UPDATABLE);
        st.setString(1, fromNow.format(DateTimeFormatter.ofPattern(Task.DB_TIME_PATTERN)));
        ResultSet rs = st.executeQuery();
        while (rs.next()){
            Task task = this.getTaskFromResultSet(rs);
            expiredTasks.add(task);
            // updating DB table trough the updatable result set
            update(rs, task);
        }
        this.closeAll(con, st, rs);
        Collections.sort(expiredTasks);
        return expiredTasks;
    }
    
    /*
    * Gets tasks from DB according to passed int value representing required tasks status:
    * -1 : get all expired tasks (t_status = FALSE)
    * 1 : get all actual tasks (t_status = TRUE)
    * 0 : get all tasks regardless of its status
    */
    @Override
    public List<Task> getTasks(int isActive) throws SQLException{
        List<Task> tasks = new ArrayList<>();
        Connection con = data.connect();
        
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

        this.closeAll(con, st, rs);

        Collections.sort(tasks);
        if (isActive < 0){
            Collections.reverse(tasks);
        }
        return tasks;
    }
    
    @Override
    public ArrayList<Task> getTasksByTime(LocalDateTime time) throws SQLException{
        ArrayList<Task> tasks = new ArrayList<>();
        Connection con = data.connect();
        PreparedStatement st = con.prepareStatement(SELECT_TASKS_WHERE_TIME);

        st.setString(1, time.format(DateTimeFormatter.ofPattern(Task.DB_TIME_PATTERN)));
        ResultSet rs = st.executeQuery();
        while (rs.next()){
            Task task = getTaskFromResultSet(rs);
            tasks.add(task);
        }
        this.closeAll(con, st, rs);
        Collections.sort(tasks);
        return tasks;
    }
    
    @Override
    public boolean deleteTaskByText(String text) throws SQLException{
        Connection con = data.connect();
        PreparedStatement st = con.prepareStatement(DELETE_TASKS_WHERE_TEXT);

        st.setString(1, ANY_SYMBOLS+text+ANY_SYMBOLS);
        int qty = st.executeUpdate();
        st.close();
        con.close();
        return (qty > 0); 
    }
        
    @Override
    public boolean deleteTasks(int tasksSort) throws SQLException{
        Connection con = data.connect();   
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
        con.close();
        return (qty > 0);
    }
    
    private void closeAll(Connection con, Statement st, ResultSet rs) throws SQLException{
        rs.close();
        st.close();
        con.close();
    }
}
