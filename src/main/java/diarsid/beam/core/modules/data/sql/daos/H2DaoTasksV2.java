/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package diarsid.beam.core.modules.data.sql.daos;

import java.util.List;

import diarsid.beam.core.base.data.DataBase;
import diarsid.beam.core.base.data.DataExtractionException;
import diarsid.beam.core.base.data.util.SqlPatternSelect;
import diarsid.beam.core.domain.entities.Task;
import diarsid.jdbc.transactions.JdbcTransaction;
import diarsid.jdbc.transactions.exceptions.TransactionHandledException;
import diarsid.jdbc.transactions.exceptions.TransactionHandledSQLException;
import diarsid.support.objects.Pool;

import static java.util.stream.Collectors.toList;

import static diarsid.beam.core.base.util.CollectionsUtils.nonEmpty;
import static diarsid.beam.core.base.util.SqlUtil.lowerWildcard;
import static diarsid.beam.core.modules.data.sql.daos.RowToEntityConversions.ROW_TO_TASK;

/**
 *
 * @author Diarsid
 */
class H2DaoTasksV2 extends H2DaoTasksV0 {
        
    private final Pool<SqlPatternSelect> sqlPatternSelectPool;
    
    H2DaoTasksV2(DataBase dataBase, Pool<SqlPatternSelect> sqlPatternSelectPool) {
        super(dataBase);    
        this.sqlPatternSelectPool = sqlPatternSelectPool;
    }    

    @Override
    public List<Task> findTasksByTextPattern(String pattern) throws DataExtractionException {
        try (
                JdbcTransaction transact = super.openTransaction();
                SqlPatternSelect patternSelect = this.sqlPatternSelectPool.give();) 
        {
            
            List<Task> tasks = transact
                    .doQueryAndStreamVarargParams(
                            ROW_TO_TASK, 
                            "SELECT * " +
                            "FROM tasks " +
                            "WHERE ( LOWER(text) LIKE ? )", 
                            lowerWildcard(pattern))
                    .collect(toList());
            
            if ( nonEmpty(tasks) ) {
                return tasks;
            }
                        
            tasks = transact
                    .doQueryAndStream(
                            ROW_TO_TASK,
                            patternSelect
                                    .select("*")
                                    .from("tasks")
                                    .patternForWhereCondition(pattern)
                                    .patternColumnForWhereCondition("text")
                                    .composeSql())
                    .collect(toList());
            
            if ( nonEmpty(tasks) ) {
                return tasks;
            }
            
            tasks = transact
                    .doQueryAndStream(
                            ROW_TO_TASK,
                            patternSelect
                                    .decreaseRequiredLikeness()
                                    .composeSql())
                    .collect(toList());
            
            return tasks;
            
        } catch (TransactionHandledSQLException|TransactionHandledException e) {
            throw super.logAndWrap(e);
        }        
    }
}
