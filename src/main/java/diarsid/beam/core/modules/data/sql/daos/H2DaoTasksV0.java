/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package diarsid.beam.core.modules.data.sql.daos;

import java.sql.Timestamp;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.function.Function;

import diarsid.beam.core.base.data.DataBase;
import diarsid.beam.core.base.data.DataExtractionException;
import diarsid.beam.core.domain.entities.Task;
import diarsid.beam.core.domain.entities.TaskRepeat;
import diarsid.beam.core.domain.inputparsing.time.AllowedTimePeriod;
import diarsid.beam.core.modules.data.DaoTasks;
import diarsid.jdbc.transactions.JdbcTransaction;
import diarsid.jdbc.transactions.RowConversion;
import diarsid.jdbc.transactions.core.Params;
import diarsid.jdbc.transactions.exceptions.TransactionHandledException;
import diarsid.jdbc.transactions.exceptions.TransactionHandledSQLException;

import static java.util.Arrays.stream;
import static java.util.Objects.nonNull;
import static java.util.stream.Collectors.toList;
import static java.util.stream.Collectors.toSet;

import static diarsid.beam.core.base.util.SqlUtil.multipleValues;
import static diarsid.beam.core.domain.entities.Tasks.stringifyTaskText;
import static diarsid.beam.core.modules.data.sql.daos.RowToEntityConversions.ROW_TO_TASK;
import static diarsid.jdbc.transactions.core.Params.params;



abstract class H2DaoTasksV0 
        extends BeamCommonDao 
        implements DaoTasks {
    
    private final Function<Task, Params> taskToParamsConversion;
    private final RowConversion<LocalDateTime> rowToTimeConversion;
    
    H2DaoTasksV0(DataBase dataBase) {
        super(dataBase);        
        this.taskToParamsConversion = (task) -> {            
            return params(
                    task.time(), 
                    task.stringifyText(), 
                    task.status(), 
                    task.days(), 
                    task.hours(), 
                    task.type(),
                    task.id());
        };
        this.rowToTimeConversion = (row) -> {
            Object time = row.get("time");
            if ( nonNull(time) ) {
                return ((Timestamp) time).toLocalDateTime();
            } else {
                return null;
            }                                
        };
    }

    @Override
    public Optional<LocalDateTime> getTimeOfFirstActiveTask() throws DataExtractionException {
        try {
            return super.openDisposableTransaction()
                    .doQueryAndConvertFirstRow(
                            this.rowToTimeConversion,
                            "SELECT MIN(time) AS time " +
                            "FROM tasks " +
                            "WHERE status IS TRUE");
        } catch (TransactionHandledSQLException|TransactionHandledException e) {
            throw super.logAndWrap(e);
        }
    }

    @Override
    public List<Task> getActiveTasksOfTypeBetweenDates(
            LocalDateTime from, LocalDateTime to, TaskRepeat... types) 
            throws DataExtractionException {
        try {
            return super.openDisposableTransaction()
                    .doQueryAndStreamVarargParams(
                            ROW_TO_TASK,
                            "SELECT * " +
                            "FROM tasks " +
                            "WHERE type IN " + multipleValues(types.length) + 
                            "       AND ( time >= ? ) AND ( time <= ? ) " +        
                            "       AND ( status IS TRUE ) " +
                            "ORDER BY time ",
                            types, from, to)
                    .collect(toList());
        } catch (TransactionHandledSQLException|TransactionHandledException e) {
            throw super.logAndWrap(e);
        }
    }

    @Override
    public List<Task> getActiveTasksBeforeTime(LocalDateTime tillNow) 
            throws DataExtractionException {
        try {
            return super.openDisposableTransaction()
                    .doQueryAndStreamVarargParams(
                            ROW_TO_TASK,
                            "SELECT * " +
                            "FROM tasks " +
                            "WHERE ( status IS TRUE ) AND ( time <= ? ) ",
                            tillNow)
                    .collect(toList());
        } catch (TransactionHandledSQLException|TransactionHandledException e) {
            throw super.logAndWrap(e);
        }
    }

    @Override
    public boolean updateTasks(List<Task> tasks) throws DataExtractionException {
        try (JdbcTransaction transact = super.openTransaction()) {
            
            int[] updated = transact
                    .doBatchUpdate(
                            "UPDATE tasks " +
                            "SET " +
                            "   time = ?, " +
                            "   text = ?, " +
                            "   status = ?, " +
                            "   days = ?, " +
                            "   hours = ?, " +
                            "   type = ? " +
                            "WHERE ( id IS ? )",
                            this.tasksToParams(tasks));
            
            boolean isOk = ( stream(updated).sum() == tasks.size() );
            
            transact.ifTrue( ! isOk ).rollbackAndProceed();
            
            return isOk;
        } catch (TransactionHandledSQLException|TransactionHandledException e) {
            throw super.logAndWrap(e);
        }
    }
    
    private Set<Params> tasksToParams(List<Task> tasks) {
        return tasks
                .stream()
                .map(this.taskToParamsConversion)
                .collect(toSet());
    }

    @Override
    public boolean saveTask(Task task) throws DataExtractionException {
        try (JdbcTransaction transact = super.openTransaction()) {
            
            int updated = transact
                    .doUpdateVarargParams(
                            "INSERT INTO tasks ( type, time, status, text, days, hours ) " +
                            "VALUES ( ?, ?, ?, ?, ?, ? ) ", 
                            task.type(), 
                            task.time(), 
                            task.status(), 
                            task.stringifyText(), 
                            task.days(), 
                            task.hours());
            
            transact.ifTrue( updated != 1 ).rollbackAndProceed();
            
            return ( updated == 1 );
        } catch (TransactionHandledSQLException|TransactionHandledException e) {
            throw super.logAndWrap(e);
        }
    }

    @Override
    public boolean deleteTaskById(int id) throws DataExtractionException {
        try (JdbcTransaction transact = super.openTransaction()) {
            
            int removed = transact
                    .doUpdateVarargParams(
                            "DELETE FROM tasks " +
                            "WHERE ( id IS ? ) ", 
                            id);
            
            transact.ifTrue( removed != 1 ).rollbackAndProceed();
            
            return ( removed == 1 );
        } catch (TransactionHandledSQLException|TransactionHandledException e) {
            throw super.logAndWrap(e);
        }
    }

    @Override
    public boolean editTaskText(int taskId, List<String> newText) throws DataExtractionException {
        try (JdbcTransaction transact = super.openTransaction()) {
            
            int updated = transact
                    .doUpdateVarargParams(
                            "UPDATE tasks " +
                            "SET text = ? " +
                            "WHERE ( id IS ? ) ", 
                            stringifyTaskText(newText), taskId);
            
            transact.ifTrue( updated != 1 ).rollbackAndProceed();
            
            return ( updated == 1 );
        } catch (TransactionHandledSQLException|TransactionHandledException e) {
            throw super.logAndWrap(e);
        }        
    }

    @Override
    public boolean editTaskTime(int taskId, LocalDateTime newTime) throws DataExtractionException {
        try (JdbcTransaction transact = super.openTransaction()) {
            
            int updated = transact
                    .doUpdateVarargParams(
                            "UPDATE tasks " +
                            "SET time = ? " +
                            "WHERE ( id IS ? ) ", 
                            newTime, taskId);
            
            transact.ifTrue( updated != 1 ).rollbackAndProceed();
            
            return ( updated == 1 );
        } catch (TransactionHandledSQLException|TransactionHandledException e) {
            throw super.logAndWrap(e);
        }  
    }

    @Override
    public boolean editTaskTime(
            int taskId, LocalDateTime newTime, AllowedTimePeriod timePeriod) 
            throws DataExtractionException {
        try (JdbcTransaction transact = super.openTransaction()) {
            String days = timePeriod.stringifyDays();
            String hours = timePeriod.stringifyHours();
            int updated = transact
                    .doUpdateVarargParams(
                            "UPDATE tasks " +
                            "SET time = ?, days = ?, hours = ? " +
                            "WHERE ( id IS ? ) ", 
                            newTime, 
                            days, 
                            hours, 
                            taskId);
            
            transact.ifTrue( updated != 1 ).rollbackAndProceed();
            
            return ( updated == 1 );
        } catch (TransactionHandledSQLException|TransactionHandledException e) {
            throw super.logAndWrap(e);
        }  
    }
}
