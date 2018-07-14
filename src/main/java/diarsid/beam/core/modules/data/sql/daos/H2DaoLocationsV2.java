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
import diarsid.beam.core.domain.entities.Location;
import diarsid.jdbc.transactions.JdbcTransaction;
import diarsid.jdbc.transactions.exceptions.TransactionHandledException;
import diarsid.jdbc.transactions.exceptions.TransactionHandledSQLException;

import static java.util.Collections.emptyList;
import static java.util.stream.Collectors.toList;

import static diarsid.beam.core.base.objects.Pool.takeFromPool;
import static diarsid.beam.core.base.util.CollectionsUtils.nonEmpty;
import static diarsid.beam.core.base.util.Logging.logFor;
import static diarsid.beam.core.base.util.SqlUtil.lowerWildcard;
import static diarsid.beam.core.modules.data.sql.daos.RowToEntityConversions.ROW_TO_LOCATION;

/**
 *
 * @author Diarsid
 */
class H2DaoLocationsV2 extends H2DaoLocationsV0 {
    
    H2DaoLocationsV2(DataBase dataBase, InnerIoEngine ioEngine) {
        super(dataBase, ioEngine);
    }

    @Override
    public List<Location> getLocationsByNamePattern(Initiator initiator, String pattern) {
        try (
                JdbcTransaction transact = super.openTransaction();
                SqlPatternSelect patternSelect = takeFromPool(SqlPatternSelect.class)) 
        {
            List<Location> found;
            
            found = transact
                    .doQueryAndStreamVarargParams(
                            Location.class, 
                            "SELECT loc_name, loc_path " +
                            "FROM locations " +
                            "WHERE LOWER(loc_name) LIKE ?  ", 
                            ROW_TO_LOCATION,
                            lowerWildcard(pattern))
                    .collect(toList());
            
            if ( nonEmpty(found) ) {
                return found;
            } 
            
            found = transact
                    .doQueryAndStream(
                            Location.class,
                            patternSelect
                                    .select("loc_name, loc_path")
                                    .from("locations")
                                    .patternColumnForWhereCondition("loc_name")
                                    .patternForWhereCondition(pattern)
                                    .compose(),
                            ROW_TO_LOCATION)
                    .collect(toList());
            
            if ( nonEmpty(found) ) {
                return found;
            }
            
            found = transact
                    .doQueryAndStream(
                            Location.class,
                            patternSelect
                                    .decreaseRequiredLikeness()
                                    .compose(),
                            ROW_TO_LOCATION)
                    .collect(toList());
            
            if ( nonEmpty(found) ) {
                return found;
            }
            
            found = transact
                    .doQueryAndStream(
                            Location.class,
                            patternSelect
                                    .decreaseRequiredLikeness()
                                    .compose(),
                            ROW_TO_LOCATION)
                    .collect(toList());
            
            return found;
            
        } catch (TransactionHandledException | TransactionHandledSQLException ex) {
            logFor(this).error("error on location search " + pattern, ex);
            super.ioEngine().report(
                    initiator, "locations search '" + pattern + "' failed.");
            return emptyList();
        }
    }
}
