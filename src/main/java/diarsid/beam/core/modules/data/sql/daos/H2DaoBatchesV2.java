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
import diarsid.jdbc.transactions.JdbcTransaction;
import diarsid.jdbc.transactions.exceptions.TransactionHandledException;
import diarsid.jdbc.transactions.exceptions.TransactionHandledSQLException;

import static java.util.Collections.emptyList;
import static java.util.stream.Collectors.toList;

import static diarsid.beam.core.base.objects.Pools.takeFromPool;
import static diarsid.beam.core.base.util.CollectionsUtils.nonEmpty;
import static diarsid.beam.core.base.util.Logging.logFor;
import static diarsid.beam.core.base.util.SqlUtil.lowerWildcard;

/**
 *
 * @author Diarsid
 */
class H2DaoBatchesV2 extends H2DaoBatchesV0 {
    
    H2DaoBatchesV2(DataBase dataBase, InnerIoEngine ioEngine) {
        super(dataBase, ioEngine);
    }

    @Override
    public List<String> getBatchNamesByNamePattern(Initiator initiator, String pattern) {
        try (
                JdbcTransaction transact = super.openTransaction();
                SqlPatternSelect patternSelect = takeFromPool(SqlPatternSelect.class)) 
        {
            List<String> found;
            
            found = transact
                    .doQueryAndStreamVarargParams(
                            super.rowToBatchNameConversion(),
                            "SELECT bat_name " +
                            "FROM batches " +
                            "WHERE LOWER(bat_name) LIKE ? ",
                            lowerWildcard(pattern))
                    .collect(toList());
            
            if ( nonEmpty(found) ) {
                return found;
            }
            
            found = transact
                    .doQueryAndStream(
                            super.rowToBatchNameConversion(),
                            patternSelect
                                    .select("bat_name")
                                    .from("batches")
                                    .patternColumnForWhereCondition("bat_name")
                                    .patternForWhereCondition(pattern)
                                    .composeSql())
                    .collect(toList());
            
            if ( nonEmpty(found) ) {
                return found;
            }
            
            found = transact
                    .doQueryAndStream(
                            super.rowToBatchNameConversion(),
                            patternSelect
                                    .decreaseRequiredLikeness()
                                    .composeSql())
                    .collect(toList());
            
            if ( nonEmpty(found) ) {
                return found;
            }
            
            found = transact
                    .doQueryAndStream(
                            super.rowToBatchNameConversion(),
                            patternSelect
                                    .decreaseRequiredLikeness()
                                    .composeSql())
                    .collect(toList());
            
            return found;
            
        } catch (TransactionHandledException | TransactionHandledSQLException ex) {
            logFor(this).error("error on batch search " + pattern, ex);
            super.ioEngine().report(
                    initiator, "batches search '" + pattern + "' failed.");
            return emptyList();
        }
    }
}
