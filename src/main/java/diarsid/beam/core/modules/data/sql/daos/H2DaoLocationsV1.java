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
import diarsid.beam.core.domain.entities.Location;
import diarsid.jdbc.transactions.JdbcTransaction;
import diarsid.jdbc.transactions.exceptions.TransactionHandledException;
import diarsid.jdbc.transactions.exceptions.TransactionHandledSQLException;

import static java.lang.String.format;
import static java.lang.String.join;
import static java.util.Collections.emptyList;
import static java.util.stream.Collectors.toList;

import static diarsid.beam.core.base.util.CollectionsUtils.nonEmpty;
import static diarsid.beam.core.base.util.Logs.debug;
import static diarsid.beam.core.base.util.Logs.logError;
import static diarsid.beam.core.base.util.SqlUtil.lowerWildcard;
import static diarsid.beam.core.base.util.SqlUtil.multipleLowerGroupedLikesAndOr;
import static diarsid.beam.core.base.util.SqlUtil.multipleLowerLikeAnd;
import static diarsid.beam.core.base.util.SqlUtil.patternToCharCriterias;
import static diarsid.beam.core.base.util.SqlUtil.shift;
import static diarsid.beam.core.modules.data.sql.daos.RowToEntityConversions.ROW_TO_LOCATION;

/**
 *
 * @author Diarsid
 */
class H2DaoLocationsV1 extends H2DaoLocationsV0 {
    
    H2DaoLocationsV1(DataBase dataBase, InnerIoEngine ioEngine) {
        super(dataBase, ioEngine);
    }

    @Override
    public List<Location> getLocationsByNamePattern(
            Initiator initiator, String pattern) {        
        try (JdbcTransaction transact = super.openTransaction()) {
            
            List<Location> found;
            
            found = transact
                    .doQueryAndStreamVarargParams(
                            ROW_TO_LOCATION,
                            "SELECT loc_name, loc_path " +
                            "FROM locations " +
                            "WHERE LOWER(loc_name) LIKE ?  ", 
                            lowerWildcard(pattern))
                    .collect(toList());
            
            if ( nonEmpty(found) ) {
                return found;
            } else {
                debug("[PATTERN FULL] not found : " + pattern);
            }
            
            List<String> criterias = patternToCharCriterias(pattern);
            debug("[PATTERN] criterias: " + join(" ", criterias));
            
            found = transact
                    .doQueryAndStreamVarargParams(
                            ROW_TO_LOCATION,
                            "SELECT loc_name, loc_path " +
                            "FROM locations " +
                            "WHERE " + multipleLowerLikeAnd("loc_name", criterias.size()), 
                            criterias)
                    .collect(toList());
            
            if ( nonEmpty(found) ) {
                return found;
            } else {
                debug("[PATTERN CRITERIAS AND] not found : " + pattern);
            }
            
            String andOrCondition = multipleLowerGroupedLikesAndOr("loc_name", criterias.size());
            List<Location> shiftedFound;
            
            found = transact
                    .doQueryAndStreamVarargParams(
                            ROW_TO_LOCATION,
                            "SELECT loc_name, loc_path " +
                            "FROM locations " +
                            "WHERE " + andOrCondition, 
                            criterias)
                    .collect(toList());
            
            shift(criterias);
            debug("[PATTERN] shuffled criterias: " + join(" ", criterias));
            shiftedFound = transact
                    .doQueryAndStreamVarargParams(
                            ROW_TO_LOCATION,
                            "SELECT loc_name, loc_path " +
                            "FROM locations " +
                            "WHERE " + andOrCondition, 
                            criterias)
                    .collect(toList());
            
            debug("[PATTERN] found by criterias : " + found.size());
            debug("[PATTERN] found by shuffled criterias : " + shiftedFound.size());
            shiftedFound.retainAll(found);
            found.retainAll(shiftedFound);
            
            return found;
            
        } catch (TransactionHandledSQLException|TransactionHandledException ex) {
            logError(this.getClass(), ex);
            super.ioEngine().report(
                    initiator, format("location search by name '%s' failed.", pattern));
            return emptyList();
        }
    }
}
