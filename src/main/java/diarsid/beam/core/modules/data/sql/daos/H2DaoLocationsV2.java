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
import diarsid.beam.core.domain.entities.Location;
import diarsid.jdbc.transactions.JdbcTransaction;
import diarsid.jdbc.transactions.exceptions.TransactionHandledException;
import diarsid.jdbc.transactions.exceptions.TransactionHandledSQLException;

import static java.util.stream.Collectors.toList;

import static diarsid.beam.core.base.util.CollectionsUtils.nonEmpty;
import static diarsid.beam.core.base.util.SqlUtil.lowerWildcard;
import static diarsid.beam.core.modules.data.sql.daos.RowToEntityConversions.ROW_TO_LOCATION;
import static diarsid.support.objects.Pools.takeFromPool;

/**
 *
 * @author Diarsid
 */
class H2DaoLocationsV2 extends H2DaoLocationsV0 {
    
    H2DaoLocationsV2(DataBase dataBase) {
        super(dataBase);
    }

    @Override
    public List<Location> getLocationsByNamePattern(String pattern) throws DataExtractionException {
        try (
                JdbcTransaction transact = super.openTransaction();
                SqlPatternSelect patternSelect = takeFromPool(SqlPatternSelect.class)) 
        {
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
            } 
            
            found = transact
                    .doQueryAndStream(
                            ROW_TO_LOCATION,
                            patternSelect
                                    .select("loc_name, loc_path")
                                    .from("locations")
                                    .patternColumnForWhereCondition("loc_name")
                                    .patternForWhereCondition(pattern)
                                    .composeSql())
                    .collect(toList());
            
            if ( nonEmpty(found) ) {
                return found;
            }
            
            found = transact
                    .doQueryAndStream(
                            ROW_TO_LOCATION,
                            patternSelect
                                    .decreaseRequiredLikeness()
                                    .composeSql())
                    .collect(toList());
            
            if ( nonEmpty(found) ) {
                return found;
            }
            
            found = transact
                    .doQueryAndStream(
                            ROW_TO_LOCATION,
                            patternSelect
                                    .decreaseRequiredLikeness()
                                    .composeSql())
                    .collect(toList());
            
            return found;
            
        } catch (TransactionHandledException | TransactionHandledSQLException e) {
            throw super.logAndWrap(e);
        }
    }
}
