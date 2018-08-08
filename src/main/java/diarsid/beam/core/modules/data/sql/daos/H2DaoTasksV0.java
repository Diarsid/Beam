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

import diarsid.beam.core.base.control.io.base.actors.Initiator;
import diarsid.beam.core.base.control.io.base.actors.InnerIoEngine;
import diarsid.beam.core.base.data.DataBase;
import diarsid.beam.core.domain.entities.Task;
import diarsid.beam.core.domain.entities.TaskRepeat;
import diarsid.beam.core.domain.inputparsing.time.AllowedTimePeriod;
import diarsid.beam.core.modules.data.BeamCommonDao;
import diarsid.beam.core.modules.data.DaoTasks;
import diarsid.jdbc.transactions.JdbcTransaction;
import diarsid.jdbc.transactions.RowConversion;
import diarsid.jdbc.transactions.core.Params;
import diarsid.jdbc.transactions.exceptions.TransactionHandledException;
import diarsid.jdbc.transactions.exceptions.TransactionHandledSQLException;

import static java.util.Arrays.stream;
import static java.util.Collections.emptyList;
import static java.util.Objects.nonNull;
import static java.util.stream.Collectors.toList;
import static java.util.stream.Collectors.toSet;

import static diarsid.beam.core.base.util.Logging.logFor;
import static diarsid.beam.core.base.util.SqlUtil.multipleValues;
import static diarsid.beam.core.domain.entities.Tasks.stringifyTaskText;
import static diarsid.beam.core.modules.data.sql.daos.RowToEntityConversions.ROW_TO_TASK;
import static diarsid.jdbc.transactions.core.Params.params;



abstract class H2DaoTasksV0 
        extends BeamCommonDao 
        implements DaoTasks {
    
    private final Function<Task, Params> taskToParamsConversion;
    private final RowConversion<LocalDateTime> rowToTimeConversion;
    
    H2DaoTasksV0(DataBase dataBase, InnerIoEngine ioEngine) {
        super(dataBase, ioEngine);        
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
    public Optional<LocalDateTime> getTimeOfFirstActiveTask(
            Initiator initiator) {
        try {
            return super.openDisposableTransaction()
                    .doQueryAndConvertFirstRow(
                            this.rowToTimeConversion,
                            "SELECT MIN(time) AS time " +
                            "FROM tasks " +
                            "WHERE status IS TRUE");
        } catch (TransactionHandledSQLException|TransactionHandledException e) {
            logFor(this).error(e.getMessage(), e);
            
            return Optional.empty();
        }
    }

    @Override
    public List<Task> getActiveTasksOfTypeBetweenDates(
            Initiator initiator, LocalDateTime from, LocalDateTime to, TaskRepeat... types) {
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
            logFor(this).error(e.getMessage(), e);
            
            return emptyList();
        }
    }

    @Override
    public List<Task> getActiveTasksBeforeTime(
            Initiator initiator, LocalDateTime tillNow) {
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
            logFor(this).error(e.getMessage(), e);
            
            return emptyList();
        }
    }

    @Override
    public boolean updateTasks(
            Initiator initiator, List<Task> tasks) {
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
            logFor(this).error(e.getMessage(), e);
            
            return false;
        }
    }
    
    private Set<Params> tasksToParams(List<Task> tasks) {
        return tasks
                .stream()
                .map(this.taskToParamsConversion)
                .collect(toSet());
    }

    @Override
    public boolean saveTask(
            Initiator initiator, Task task) {
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
            logFor(this).error(e.getMessage(), e);
            
            return false;
        }
    }

    @Override
    public boolean deleteTaskById(
            Initiator initiator, int id) {
        try (JdbcTransaction transact = super.openTransaction()) {
            
            int removed = transact
                    .doUpdateVarargParams(
                            "DELETE FROM tasks " +
                            "WHERE ( id IS ? ) ", 
                            id);
            
            transact.ifTrue( removed != 1 ).rollbackAndProceed();
            
            return ( removed == 1 );
        } catch (TransactionHandledSQLException|TransactionHandledException e) {
            logFor(this).error(e.getMessage(), e);
            
            return false;
        }
    }

    @Override
    public boolean editTaskText(
            Initiator initiator, int taskId, List<String> newText) {
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
            logFor(this).error(e.getMessage(), e);
            
            return false;
        }        
    }

    @Override
    public boolean editTaskTime(
            Initiator initiator, int taskId, LocalDateTime newTime) {
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
            logFor(this).error(e.getMessage(), e);
            
            return false;
        }  
    }

    @Override
    public boolean editTaskTime(
            Initiator initiator, int taskId, LocalDateTime newTime, AllowedTimePeriod timePeriod) {
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
            logFor(this).error(e.getMessage(), e);
            
            return false;
        }  
    }
}
