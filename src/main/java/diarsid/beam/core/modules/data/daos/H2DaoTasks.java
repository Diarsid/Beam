/*
 * project: Beam
 * author: Diarsid
 */
package diarsid.beam.core.modules.data.daos;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

import diarsid.beam.core.exceptions.NullDependencyInjectionException;

import old.diarsid.beam.core.modules.IoInnerModule;

import diarsid.beam.core.modules.tasks.Task;
import diarsid.beam.core.modules.data.DataBase;
import diarsid.beam.core.exceptions.ModuleInitializationException;
import diarsid.beam.core.modules.data.DaoTasks;
import diarsid.beam.core.modules.data.HandledTransactSQLException;
import diarsid.beam.core.modules.data.JdbcTransaction;
import diarsid.beam.core.modules.tasks.TimeMessage;
import diarsid.beam.core.modules.tasks.TaskType;

/**
 *
 * @author Diarsid
 */
class H2DaoTasks implements DaoTasks {
    
    private final DataBase data;
    private final IoInnerModule ioEngine;
    
    private final String INSERT_NEW_TASK = 
            "INSERT INTO tasks (t_time, t_content, t_type, t_status, t_days, t_hours) " +
            "VALUES (?, ?, ?, ?, ?, ?)";  
    private final String UPDATE_TASK_TIME_AND_STATUS_BY_TASK_ID = 
            "UPDATE tasks " +
            "SET t_time = ?, t_status = ? " +
            "WHERE t_id IS ? ";
    private final String GET_FIRST_TIME = 
            "SELECT MIN(t_time) " +
            "FROM tasks " +
            "WHERE t_status = TRUE";    
    private final String GET_FIRST_TASKS = 
            "SELECT t_id, t_time, t_content, t_type, t_status, t_days, t_hours " +
            "FROM tasks " + 
            "WHERE t_time IS " +
            "       (SELECT MIN(t_time) " + 
            "       FROM tasks " +
            "       WHERE t_status = TRUE)";
    private final String GET_EXPIRED_TASKS = 
            "SELECT t_id, t_time, t_content, t_type, t_status, t_days, t_hours " +
            "FROM tasks " + 
            "WHERE (t_time <= ?) AND (t_status IS TRUE)";
    private final String SELECT_NON_NOURLY_AND_NON_DAILY_TASKS_BETWEEN_DATES = 
            "SELECT t_time, t_content " +
            "FROM tasks " +
            "WHERE (t_time >= ?) AND " + 
            "       (t_time < ?) AND " + 
            "       (t_status IS TRUE) AND " + 
            "       (t_type IS NOT 'HOURLY') AND " + 
            "       (t_type IS NOT 'DAILY')";
    private final String SELECT_ALL_TASKS = 
            "SELECT t_id, t_time, t_content, t_type, t_status, t_days, t_hours " +
            "FROM tasks";
    private final String SELECT_ACTUAL_TASKS = 
            "SELECT t_id, t_time, t_content, t_type, t_status, t_days, t_hours " +
            "FROM tasks " + 
            "WHERE (t_status IS TRUE) AND (t_type IS 'USUAL')";
    private final String SELECT_ACTUAL_REMINDERS = 
            "SELECT t_id, t_time, t_content, t_type, t_status, t_days, t_hours " +
            "FROM tasks " + 
            "WHERE (t_status IS TRUE) AND ( (t_type IS 'HOURLY') OR (t_type IS 'DAILY') )";
    private final String SELECT_ACTUAL_EVENTS = 
            "SELECT t_id, t_time, t_content, t_type, t_status, t_days, t_hours " +
            "FROM tasks " + 
            "WHERE (t_status IS TRUE) AND ( (t_type IS 'MONTHLY') OR (t_type IS 'YEARLY') )";
    private final String SELECT_NON_ACTUAL_TASKS = 
            "SELECT t_id, t_time, t_content, t_type, t_status, t_days, t_hours " +
            "FROM tasks " + 
            "WHERE t_status IS FALSE";
    private final String SELECT_TASKS_WHERE_TIME = 
            "SELECT t_time, t_content " +
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
    private final String DB_STRING_ARRAY_DILIMITER = "::::";
    private final String DB_INT_ARRAY_DILIMITER = "-";
    
    H2DaoTasks(IoInnerModule io, DataBase data) {
        if (io == null){
            throw new NullDependencyInjectionException(
                    H2DaoTasks.class.getSimpleName(), IoInnerModule.class.getSimpleName());
        }
        if (data == null){
            throw new NullDependencyInjectionException(
                    H2DaoTasks.class.getSimpleName(), DataBase.class.getSimpleName());
        }
        this.data = data;
        this.ioEngine = io;
    }
    
    private String timeToString(LocalDateTime time) {
        return time.format(DateTimeFormatter.ofPattern(DB_TIME_PATTERN));
    }
    
    private LocalDateTime parseTime(String sqlTimeString) {
        return LocalDateTime.parse(sqlTimeString,
                        DateTimeFormatter.ofPattern(DaoTasks.DB_TIME_PATTERN));
    }
    
    private String contentToString(String[] arr) {
        return String.join(DB_STRING_ARRAY_DILIMITER, arr);
    }
    
    private String[] contentToArray(String s) {
        return s.split(DB_STRING_ARRAY_DILIMITER);
    }
    
    private String integersToString(Integer[] ints) {
        Set<String> strings = Arrays.asList(ints)
                .stream()
                .map(Object::toString)
                .collect(Collectors.toSet());
        return String.join(DB_INT_ARRAY_DILIMITER, strings);
    }
    
    private Integer[] integersToArray(String ints) {
        if (ints.isEmpty()) {
            return new Integer[0];
        }
        String[] stringInts = ints.split(DB_INT_ARRAY_DILIMITER);
        Integer[] integers = new Integer[stringInts.length];
        for (int i = 0; i < stringInts.length; i++) {
            integers[i] = Integer.parseInt(stringInts[i]);
        }
        return integers;
    }
    
    @Override
    public LocalDateTime addTask(Task task) {        
        JdbcTransaction transact = this.data.beginTransaction();
        try {  
            //System.out.println("[DAO new task] ");
            PreparedStatement insertStatement = 
                    transact.getPreparedStatement(INSERT_NEW_TASK);
                        
            insertStatement.setString(1, this.timeToString(task.getTime()));
            insertStatement.setString(2, this.contentToString(task.getContent()));
            insertStatement.setString(3, task.getType().name());
            insertStatement.setBoolean(4, task.getStatus());
            insertStatement.setString(5, this.integersToString(task.getActiveDays()));
            insertStatement.setString(6, this.integersToString(task.getActiveHours()));
            
            boolean updated = 
                    transact.executePreparedUpdate(insertStatement) > 0 ;
            
            LocalDateTime time = null;
            if ( updated ) {
                ResultSet rs = transact.executeQuery(GET_FIRST_TIME);
                rs.first();
                time = this.parseTime(rs.getString(1));
                transact.commitThemAll();
                return time;
            } else {
                transact.rollbackAllAndReleaseResources();
                return time;
            }
        } catch (HandledTransactSQLException e) { 
            this.ioEngine.reportException(e, "SQLException: task saving.");
            return null;
        } catch (SQLException e) {
            transact.rollbackAllAndReleaseResources();
            this.ioEngine.reportException(e, "SQLException: task saving.");
            return null;
        } catch (NullPointerException e) {
            transact.rollbackAllAndReleaseResources();
            this.ioEngine.reportError(
                    "Incorrect usage of JdbcTransaction: " +
                    "database connection is NULL.");
            return null;
        } 
    }       
    
    @Override
    public LocalDateTime updateTasksAndGetNextFirstTime(List<Task> tasksToUpdate) {
        JdbcTransaction transact = this.data.beginTransaction();
        try {
            PreparedStatement ps;
            
            for (Task task : tasksToUpdate) {
                ps = transact.getPreparedStatement(
                        UPDATE_TASK_TIME_AND_STATUS_BY_TASK_ID);

                ps.setString(1, this.timeToString(task.getTime()));
                ps.setBoolean(2, task.getStatus());
                ps.setInt(3, task.getId());
                transact.executePreparedUpdate(ps);
            }
            
            ResultSet rs = transact.executeQuery(GET_FIRST_TIME);
            LocalDateTime time = LocalDateTime.MIN;
            rs.first();
            String timeString = rs.getString(1);
            if ( timeString != null ) {
                time = parseTime(timeString);                
            } else {
                // there are no rows. It means
                // there are no active tasks in the table.
                // return MIN time to indicate that operation has been
                // performed properly but there is no time to return.
            }
            transact.commitThemAll();
            
            return time;
        } catch (HandledTransactSQLException e) { 
            this.ioEngine.reportException(e, "SQLException: task list upating.");
            return null;
        } catch (SQLException e) {
            transact.rollbackAllAndReleaseResources();
            this.ioEngine.reportException(e, "SQLException: task list upating.");
            return null;
        } catch (NullPointerException e) {
            transact.rollbackAllAndReleaseResources();
            this.ioEngine.reportError(
                    "Incorrect usage of JdbcTransaction: " +
                    "database connection is NULL.");
            return null;
        }
    }
    
    @Override
    public LocalDateTime getFirstTaskTime() {        
        try (Connection con = data.connect();
            Statement st = con.createStatement();
            ResultSet rs = st.executeQuery(GET_FIRST_TIME);) {
            
            rs.first();
            String timeString = rs.getString(1);
            if ( timeString != null ) {
                return this.parseTime(timeString);                
            } else {
                // there are no rows. It means
                // there are no active tasks in the table.
                // return MIN time to indicate that operation has been
                // performed properly but there is no time to return.
                return LocalDateTime.MIN;
            }
        } catch (SQLException e) {
            this.ioEngine.reportExceptionAndExitLater(e, 
                    "SQLException: get first task time.", 
                    "Program will be closed.");
            throw new ModuleInitializationException();
        }
    }  
    
    @Override
    public List<Task> getFirstTasks() {
        try (Connection con = data.connect();
            Statement st = con.createStatement();
            ResultSet rs = st.executeQuery(GET_FIRST_TASKS);) {
            
            List<Task> retrievedTasks = new ArrayList<>();
            //System.out.println("[DAO get first tasks] ");    
            while ( rs.next() ) {
                retrievedTasks.add(Task.restoreTask(               
                        rs.getInt("t_id"), 
                        TaskType.valueOf(rs.getString("t_type")),
                        this.parseTime(rs.getString("t_time")),
                        this.contentToArray(rs.getString("t_content")),
                        rs.getBoolean("t_status"), 
                        this.integersToArray(rs.getString("t_days")),
                        this.integersToArray(rs.getString("t_hours"))
                ));
                //System.out.println("[DAO parsing first tasks] time : " + rs.getString("t_time"));
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
    public List<Task> getExpiredTasks(LocalDateTime fromNow) {
        ResultSet rs = null;
        try (Connection con = data.connect();
            PreparedStatement st = con.prepareStatement(GET_EXPIRED_TASKS);) {
            
            List<Task> expiredTasks = new ArrayList<>();            
            st.setString(1, fromNow.format(
                    DateTimeFormatter.ofPattern(DB_TIME_PATTERN)));
            rs = st.executeQuery();
            
            while ( rs.next() ) {
                expiredTasks.add(Task.restoreTask(               
                        rs.getInt("t_id"), 
                        TaskType.valueOf(rs.getString("t_type")),
                        this.parseTime(rs.getString("t_time")),
                        this.contentToArray(rs.getString("t_content")),
                        rs.getBoolean("t_status"), 
                        this.integersToArray(rs.getString("t_days")),
                        this.integersToArray(rs.getString("t_hours"))
                ));
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
    public List<TimeMessage> getAllTasks() {
        try(Connection con = data.connect();
            Statement st = con.createStatement();
            ResultSet rs = st.executeQuery(SELECT_ALL_TASKS)) {
            
            List<TimeMessage> tasks = new ArrayList<>();
            while ( rs.next() ) {
                tasks.add(new TimeMessage(
                        this.parseTime(rs.getString("t_time")),
                        this.contentToArray(rs.getString("t_content")))
                );
            }
            Collections.sort(tasks);
            return tasks;
        } catch (SQLException e) {
            this.ioEngine.reportExceptionAndExitLater(e, 
                    "SQLException: get all tasks.", 
                    "Program will be closed.");
            return Collections.emptyList();
        }
    }
    
    @Override
    public List<TimeMessage> getActualTasks() {
        try(Connection con = data.connect();
            Statement st = con.createStatement();
            ResultSet rs = st.executeQuery(SELECT_ACTUAL_TASKS)) {
            
            List<TimeMessage> tasks = new ArrayList<>();
            while ( rs.next() ) {
                tasks.add(new TimeMessage(
                        this.parseTime(rs.getString("t_time")),
                        this.contentToArray(rs.getString("t_content")))
                );
            }
            Collections.sort(tasks);
            return tasks;
        } catch (SQLException e) {
            this.ioEngine.reportExceptionAndExitLater(e, 
                    "SQLException: get actual tasks.", 
                    "Program will be closed.");
            return Collections.emptyList();
        }
    }
    
    @Override
    public List<TimeMessage> getNonActualTasks() {
        try(Connection con = data.connect();
            Statement st = con.createStatement();
            ResultSet rs = st.executeQuery(SELECT_NON_ACTUAL_TASKS)) {
            
            List<TimeMessage> tasks = new ArrayList<>();
            while ( rs.next() ) {
                tasks.add(new TimeMessage(
                        this.parseTime(rs.getString("t_time")),
                        this.contentToArray(rs.getString("t_content")))
                );
            }
            Collections.reverse(tasks);
            return tasks;
        } catch (SQLException e) {
            this.ioEngine.reportExceptionAndExitLater(e, 
                    "SQLException: get non-actual tasks.", 
                    "Program will be closed.");
            return Collections.emptyList();
        }
    }
    
    @Override
    public List<TimeMessage> getActualReminders() {
        try (Connection con = this.data.connect();
             Statement st = con.createStatement();
             ResultSet rs = st.executeQuery(SELECT_ACTUAL_REMINDERS)) {
            
            List<TimeMessage> reminders = new ArrayList<>();
            while ( rs.next() ) {
                reminders.add(new TimeMessage(
                        this.parseTime(rs.getString("t_time")),
                        this.contentToArray(rs.getString("t_content")))
                );
            }
            Collections.sort(reminders);
            return reminders;
            
        } catch (SQLException e) {
            this.ioEngine.reportExceptionAndExitLater(e, 
                    "SQLException: get actual reminders.", 
                    "Program will be closed.");
            return Collections.emptyList();
        }
    }
    
    @Override
    public List<TimeMessage> getActualEvents() {
        try (Connection con = this.data.connect();
             Statement st = con.createStatement();
             ResultSet rs = st.executeQuery(SELECT_ACTUAL_EVENTS)) {
            
            List<TimeMessage> events = new ArrayList<>();
            while ( rs.next() ) {
                events.add(new TimeMessage(
                        this.parseTime(rs.getString("t_time")),
                        this.contentToArray(rs.getString("t_content")))
                );
            }
            Collections.sort(events);
            return events;
            
        } catch (SQLException e) {
            this.ioEngine.reportExceptionAndExitLater(e, 
                    "SQLException: get actual reminders.", 
                    "Program will be closed.");
            return Collections.emptyList();
        }
    }
    
    @Override
    public List<TimeMessage> getTasksByTime(LocalDateTime time){
        ResultSet rs = null;
        try(Connection con = data.connect();
            PreparedStatement st = con.prepareStatement(SELECT_TASKS_WHERE_TIME);) {
            
            List<TimeMessage> tasks = new ArrayList<>();
            
            st.setString(1, time.format(DateTimeFormatter.ofPattern(DB_TIME_PATTERN)));
            rs = st.executeQuery();
            while ( rs.next() ) {
                tasks.add(new TimeMessage(
                        this.parseTime(rs.getString("t_time")),
                        this.contentToArray(rs.getString("t_content")))
                );
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
    public LocalDateTime deleteTaskByText(String text) {
        JdbcTransaction transact = this.data.beginTransaction();
        try {            
            PreparedStatement ps = transact.getPreparedStatement(DELETE_TASKS_WHERE_TEXT);
            ps.setString(1, ANY_SYMBOLS+text+ANY_SYMBOLS);
            
            transact.executePreparedUpdate(ps);
            
            ResultSet rs = transact.executeQuery(GET_FIRST_TIME);
            LocalDateTime time = LocalDateTime.MIN;
            rs.first();
            String timeString = rs.getString(1);
            if ( timeString != null ) {
                time = this.parseTime(timeString);                
            } else {
                // there are no rows. It means
                // there are no active tasks in the table.
                // return MIN time to indicate that operation has been
                // performed properly but there is no time to return.
            }
            transact.commitThemAll();
            
            return time;
            
        } catch (HandledTransactSQLException e) { 
            this.ioEngine.reportException(e, "SQLException: delete task by text.");
            return null;
        } catch (SQLException e) {
            transact.rollbackAllAndReleaseResources();
            this.ioEngine.reportException(e, "SQLException: delete task by text.");
            return null;
        } catch (NullPointerException e) {
            transact.rollbackAllAndReleaseResources();
            this.ioEngine.reportError(
                    "Incorrect usage of JdbcTransaction: " +
                    "database connection is NULL.");
            return null;
        }
    }
        
    @Override
    public LocalDateTime deleteAllTasks() {
        JdbcTransaction transact = this.data.beginTransaction();
        try {
            transact.executeUpdate(DELETE_ALL_TASKS);
            
            ResultSet rs = transact.executeQuery(GET_FIRST_TIME);
            LocalDateTime time = LocalDateTime.MIN;
            rs.first();
            String timeString = rs.getString(1);
            if ( timeString != null ) {
                time = this.parseTime(timeString);                
            } else {
                // there are no rows. It means
                // there are no active tasks in the table.
                // return MIN time to indicate that operation has been
                // performed properly but there is no time to return.
            }
            transact.commitThemAll();
            
            return time;
        } catch (HandledTransactSQLException e) { 
            this.ioEngine.reportException(e, "SQLException: delete all tasks.");
            return null;
        } catch (SQLException e) {
            transact.rollbackAllAndReleaseResources();
            this.ioEngine.reportException(e, "SQLException: delete all tasks.");
            return null;
        } catch (NullPointerException e) {
            transact.rollbackAllAndReleaseResources();
            this.ioEngine.reportError(
                    "Incorrect usage of JdbcTransaction: " +
                    "database connection is NULL.");
            return null;
        }
    }
    
    @Override
    public LocalDateTime deleteActualTasks() {
        JdbcTransaction transact = this.data.beginTransaction();
        try {            
            transact.executeUpdate(DELETE_ACTUAL_TASKS);
            
            ResultSet rs = transact.executeQuery(GET_FIRST_TIME);
            LocalDateTime time = LocalDateTime.MIN;
            rs.first();
            String timeString = rs.getString(1);
            if ( timeString != null ) {
                time = this.parseTime(timeString);                
            } else {
                // there are no rows. It means
                // there are no active tasks in the table.
                // return MIN time to indicate that operation has been
                // performed properly but there is no time to return.
            }
            transact.commitThemAll();
            
            return time;
        } catch (HandledTransactSQLException e) { 
            this.ioEngine.reportException(e, "SQLException: delete actual tasks.");
            return null;
        } catch (SQLException e) {
            transact.rollbackAllAndReleaseResources();
            this.ioEngine.reportException(e, "SQLException: delete actual tasks.");
            return null;
        } catch (NullPointerException e) {
            transact.rollbackAllAndReleaseResources();
            this.ioEngine.reportError(
                    "Incorrect usage of JdbcTransaction: " +
                    "database connection is NULL.");
            return null;
        }
    }
    
    @Override
    public LocalDateTime deleteNonActualTasks(){
        JdbcTransaction transact = this.data.beginTransaction();
        try {            
            transact.executeUpdate(DELETE_NON_ACTUAL_TASKS);
            
            ResultSet rs = transact.executeQuery(GET_FIRST_TIME);
            LocalDateTime time = LocalDateTime.MIN;
            rs.first();
            String timeString = rs.getString(1);
            if ( timeString != null ) {
                time = this.parseTime(timeString);                
            } else {
                // there are no rows. It means
                // there are no active tasks in the table.
                // return MIN time to indicate that operation has been
                // performed properly but there is no time to return.
            }
            transact.commitThemAll();
            
            return time;
        } catch (HandledTransactSQLException e) { 
            this.ioEngine.reportException(e, "SQLException: delete non actual tasks.");
            return null;
        } catch (SQLException e) {
            transact.rollbackAllAndReleaseResources();
            this.ioEngine.reportException(e, "SQLException: delete non actual tasks.");
            return null;
        } catch (NullPointerException e) {
            transact.rollbackAllAndReleaseResources();
            this.ioEngine.reportError(
                    "Incorrect usage of JdbcTransaction: " +
                    "database connection is NULL.");
            return null;
        }
    }
    
    @Override
    public List<TimeMessage> getCalendarTasksBetweenDates(
            LocalDateTime from, LocalDateTime to) {
        
        JdbcTransaction transact = this.data.beginTransaction();
        try {
            PreparedStatement ps = transact.getPreparedStatement(
                    SELECT_NON_NOURLY_AND_NON_DAILY_TASKS_BETWEEN_DATES);
            ps.setString(1, this.timeToString(from));
            ps.setString(2, this.timeToString(to));
            
            ResultSet rs = transact.executePreparedQuery(ps);
            
            List<TimeMessage> tasks = new ArrayList<>();
            while ( rs.next() ) {
                tasks.add(new TimeMessage(
                        this.parseTime(rs.getString("t_time")),
                        this.contentToArray(rs.getString("t_content")))
                );
            }
            
            transact.commitThemAll();
            Collections.sort(tasks);
            return tasks;
        } catch (HandledTransactSQLException e) {
            transact.rollbackAllAndReleaseResources();
            this.ioEngine.reportException(e, "SQLException: get all calendar tasks between dates.");
            return Collections.emptyList();
        } catch (SQLException e) {
            transact.rollbackAllAndReleaseResources();
            this.ioEngine.reportException(e, "SQLException: get all calendar tasks between dates.");
            return Collections.emptyList();
        }
    }
}
