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
import diarsid.beam.core.domain.entities.Task;
import diarsid.jdbc.transactions.JdbcTransaction;
import diarsid.jdbc.transactions.exceptions.TransactionHandledException;
import diarsid.jdbc.transactions.exceptions.TransactionHandledSQLException;

import static java.util.Collections.emptyList;
import static java.util.stream.Collectors.toList;

import static diarsid.beam.core.base.util.CollectionsUtils.nonEmpty;
import static diarsid.support.log.Logging.logFor;
import static diarsid.beam.core.base.util.SqlUtil.lowerWildcard;
import static diarsid.beam.core.base.util.SqlUtil.multipleLowerLikeAnd;
import static diarsid.beam.core.base.util.SqlUtil.patternToCharCriterias;
import static diarsid.beam.core.modules.data.sql.daos.RowToEntityConversions.ROW_TO_TASK;

/**
 *
 * @author Diarsid
 */
class H2DaoTasksV1 extends H2DaoTasksV0 {
    
    H2DaoTasksV1(DataBase dataBase, InnerIoEngine ioEngine) {
        super(dataBase, ioEngine);    
    }    

    @Override
    public List<Task> findTasksByTextPattern(
            Initiator initiator, String textPattern) {
        try (JdbcTransaction transact = super.openTransaction()) {
            
            List<Task> tasks = transact
                    .doQueryAndStreamVarargParams(
                            ROW_TO_TASK, 
                            "SELECT * " +
                            "FROM tasks " +
                            "WHERE ( LOWER(text) LIKE ? )", 
                            lowerWildcard(textPattern))
                        .collect(toList());
            
            if ( nonEmpty(tasks) ) {
                return tasks;
            }
            
            List<String> criterias = patternToCharCriterias(textPattern);
            tasks = transact
                    .doQueryAndStreamVarargParams( 
                            ROW_TO_TASK, 
                            "SELECT * " +
                            "FROM tasks " +
                            "WHERE " + multipleLowerLikeAnd("text", criterias.size()),
                            criterias)
                        .collect(toList());
            
            return tasks;
            
        } catch (TransactionHandledSQLException|TransactionHandledException ex) {
            logFor(this).error(ex.getMessage(), ex);
            
            return emptyList();
        }        
    }
}
