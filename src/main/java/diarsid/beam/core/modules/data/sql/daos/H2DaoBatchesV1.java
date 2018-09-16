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
import diarsid.jdbc.transactions.JdbcTransaction;
import diarsid.jdbc.transactions.exceptions.TransactionHandledException;
import diarsid.jdbc.transactions.exceptions.TransactionHandledSQLException;

import static java.util.Collections.emptyList;
import static java.util.stream.Collectors.toList;

import static diarsid.beam.core.base.util.CollectionsUtils.nonEmpty;
import static diarsid.support.log.Logging.logFor;
import static diarsid.beam.core.base.util.SqlUtil.lowerWildcard;
import static diarsid.beam.core.base.util.SqlUtil.multipleLowerGroupedLikesAndOr;
import static diarsid.beam.core.base.util.SqlUtil.multipleLowerLikeAnd;
import static diarsid.beam.core.base.util.SqlUtil.patternToCharCriterias;
import static diarsid.beam.core.base.util.SqlUtil.shift;

/**
 *
 * @author Diarsid
 */
class H2DaoBatchesV1 extends H2DaoBatchesV0 {
    
    H2DaoBatchesV1(DataBase dataBase, InnerIoEngine ioEngine) {
        super(dataBase, ioEngine);
    }    

    @Override
    public List<String> getBatchNamesByNamePattern(
            Initiator initiator, String pattern) {
        try (JdbcTransaction transact = super.openTransaction()) {
            
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
            
            List<String> criterias = patternToCharCriterias(pattern);
            found = transact
                    .doQueryAndStreamVarargParams(
                            super.rowToBatchNameConversion(),
                            "SELECT bat_name " +
                            "FROM batches " +
                            "WHERE " + multipleLowerLikeAnd("bat_name", criterias.size()),
                            criterias)
                    .collect(toList());
            
            if ( nonEmpty(found) ) {
                return found;
            }
            
            String andOrCondition = multipleLowerGroupedLikesAndOr("bat_name", criterias.size());
            List<String> shuffleFound;
            
            found = transact
                    .doQueryAndStreamVarargParams(
                            super.rowToBatchNameConversion(),
                            "SELECT bat_name " +
                            "FROM batches " +
                            "WHERE " + andOrCondition,
                            criterias)
                    .collect(toList());
            
            shift(criterias);
            shuffleFound = transact
                    .doQueryAndStreamVarargParams(
                            super.rowToBatchNameConversion(),
                            "SELECT bat_name " +
                            "FROM batches " +
                            "WHERE " + andOrCondition,
                            criterias)
                    .collect(toList());
            
            shuffleFound.retainAll(found);
            found.retainAll(shuffleFound);
            
            return found;
            
        } catch (TransactionHandledSQLException|TransactionHandledException ex) {
            logFor(this).error("error on batch search " + pattern, ex);
            super.ioEngine().report(
                    initiator, "batches search '" + pattern + "' failed.");
            return emptyList();
        }
    }
}
