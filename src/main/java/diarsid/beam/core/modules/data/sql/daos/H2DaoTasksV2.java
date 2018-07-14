/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package diarsid.beam.core.modules.data.sql.daos;

import java.util.List;

import diarsid.beam.core.base.control.io.base.actors.Initiator;
import diarsid.beam.core.base.control.io.base.actors.InnerIoEngine;
import diarsid.beam.core.base.data.DataBase;
import diarsid.beam.core.base.data.util.SqlPatternSelect;
import diarsid.beam.core.domain.entities.Task;
import diarsid.jdbc.transactions.JdbcTransaction;
import diarsid.jdbc.transactions.exceptions.TransactionHandledException;
import diarsid.jdbc.transactions.exceptions.TransactionHandledSQLException;

import static java.util.Collections.emptyList;
import static java.util.stream.Collectors.toList;

import static diarsid.beam.core.base.objects.Pool.takeFromPool;
import static diarsid.beam.core.base.util.CollectionsUtils.nonEmpty;
import static diarsid.beam.core.base.util.Logs.logError;
import static diarsid.beam.core.base.util.SqlUtil.lowerWildcard;
import static diarsid.beam.core.modules.data.sql.daos.RowToEntityConversions.ROW_TO_TASK;

/**
 *
 * @author Diarsid
 */
class H2DaoTasksV2 extends H2DaoTasksV0 {
    
    H2DaoTasksV2(DataBase dataBase, InnerIoEngine ioEngine) {
        super(dataBase, ioEngine);    
    }    

    @Override
    public List<Task> findTasksByTextPattern(
            Initiator initiator, String pattern) {
        try (
                JdbcTransaction transact = super.openTransaction();
                SqlPatternSelect patternSelect = takeFromPool(SqlPatternSelect.class)) 
        {
            
            List<Task> tasks = transact
                    .doQueryAndStreamVarargParams(
                            Task.class, 
                            "SELECT * " +
                            "FROM tasks " +
                            "WHERE ( LOWER(text) LIKE ? )", 
                            ROW_TO_TASK, 
                            lowerWildcard(pattern))
                    .collect(toList());
            
            if ( nonEmpty(tasks) ) {
                return tasks;
            }
                        
            tasks = transact
                    .doQueryAndStream(
                            Task.class, 
                            patternSelect
                                    .select("*")
                                    .from("tasks")
                                    .patternForWhereCondition(pattern)
                                    .patternColumnForWhereCondition("text")
                                    .compose(), 
                            ROW_TO_TASK)
                    .collect(toList());
            
            if ( nonEmpty(tasks) ) {
                return tasks;
            }
            
            tasks = transact
                    .doQueryAndStream(
                            Task.class, 
                            patternSelect
                                    .decreaseRequiredLikeness()
                                    .compose(), 
                            ROW_TO_TASK)
                    .collect(toList());
            
            return tasks;
            
        } catch (TransactionHandledSQLException|TransactionHandledException ex) {
            logError(this.getClass(), ex);
            
            return emptyList();
        }        
    }
}
