/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package diarsid.beam.core.modules.data.daos.sql;

import java.sql.Timestamp;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import diarsid.beam.core.base.control.io.base.actors.Initiator;
import diarsid.beam.core.base.control.io.base.actors.InnerIoEngine;
import diarsid.beam.core.domain.entities.Task;
import diarsid.beam.core.domain.entities.TaskRepeat;
import diarsid.beam.core.domain.inputparsing.time.AllowedTimePeriod;
import diarsid.beam.core.modules.data.DaoTasks;
import diarsid.beam.core.modules.data.DataBase;
import diarsid.beam.core.modules.data.daos.BeamCommonDao;
import diarsid.jdbc.transactions.PerRowConversion;
import diarsid.jdbc.transactions.exceptions.TransactionHandledSQLException;

import static java.time.LocalDateTime.parse;
import static java.util.Collections.emptyList;
import static java.util.stream.Collectors.toList;

import static diarsid.beam.core.base.util.Logs.logError;
import static diarsid.beam.core.base.util.SqlUtil.multipleEnumValues;
import static diarsid.beam.core.domain.entities.TaskRepeat.valueOf;
import static diarsid.beam.core.domain.entities.Tasks.restoreTask;



class H2DaoTasks 
        extends BeamCommonDao 
        implements DaoTasks {
    
    private final PerRowConversion<Task> rowToTaskConversion;
    
    H2DaoTasks(DataBase dataBase, InnerIoEngine ioEngine) {
        super(dataBase, ioEngine);
        this.rowToTaskConversion = (row) -> {
            return restoreTask(
                    (int) row.get("id"), 
                    valueOf((String) row.get("type")), 
                    ((Timestamp) row.get("time")).toLocalDateTime(), 
                    (boolean) row.get("status"), 
                    (String) row.get("days"), 
                    (String) row.get("hours"), 
                    (String) row.get("text"));
        };
    }

    @Override
    public Optional<LocalDateTime> getTimeOfFirstActiveTask(
            Initiator initiator) {
        try {
            return super.getDisposableTransaction()
                    .doQueryAndConvertFirstRow(
                            LocalDateTime.class, 
                            "SELECT MIN(time) " +
                            "FROM tasks " +
                            "WHERE status IS TRUE", 
                            (row) -> {
                                return Optional.of(parse((String) row.get("time")));
                            });
        } catch (TransactionHandledSQLException e) {
            logError(this.getClass(), e);
            
            return Optional.empty();
        }
    }

    @Override
    public List<Task> getActiveTasksOfTypeBetweenDates(
            Initiator initiator, LocalDateTime from, LocalDateTime to, TaskRepeat... types) {
        try {
            return super.getDisposableTransaction()
                    .doQueryAndStreamVarargParams(
                            Task.class,
                            "SELECT * " +
                            "FROM tasks " +
                            "WHERE  type IN " + multipleEnumValues(types) + 
                            "       AND ( time >= ? ) AND ( time <= ? ) " +        
                            "       AND ( status IS TRUE ) " +
                            "ORDER BY time ",
                            this.rowToTaskConversion,
                            types, from, to)
                    .collect(toList());
        } catch (TransactionHandledSQLException ex) {
            logError(this.getClass(), ex);
            
            return emptyList();
        }
    }

    @Override
    public List<Task> getActiveTasksBeforeTime(
            Initiator initiator, LocalDateTime fromNow) {
        try {
            return super.getDisposableTransaction()
                    .doQueryAndStreamVarargParams(
                            Task.class,
                            "SELECT * " +
                            "FROM tasks " +
                            "WHERE ( status IS TRUE ) AND ( time >= ? ) ",
                            this.rowToTaskConversion,
                            fromNow)
                    .collect(toList());
        } catch (TransactionHandledSQLException ex) {
            logError(this.getClass(), ex);
            
            return emptyList();
        }
    }

    @Override
    public List<Task> getFirstActiveTasks(
            Initiator initiator) {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    @Override
    public boolean updateTasks(
            Initiator initiator, List<Task> tasks) {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    @Override
    public boolean saveTask(
            Initiator initiator, Task task) {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    @Override
    public boolean deleteTaskById(
            Initiator initiator, int id) {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    @Override
    public boolean editTaskText(
            Initiator initiator, int taskId, List<String> newText) {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    @Override
    public boolean editTaskTime(
            Initiator initiator, int taskId, LocalDateTime newTime) {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    @Override
    public List<Task> findTasksByTextPattern(Initiator initiator, String textPattern) {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    @Override
    public boolean editTaskTime(Initiator initiator, int taskId, LocalDateTime newTime, AllowedTimePeriod timePeriod) {
        throw new UnsupportedOperationException("Not supported yet.");
    }
}
