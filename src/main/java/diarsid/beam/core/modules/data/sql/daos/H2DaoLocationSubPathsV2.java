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
import diarsid.beam.core.domain.entities.LocationSubPath;
import diarsid.beam.core.modules.data.BeamCommonDao;
import diarsid.beam.core.modules.data.DaoLocationSubPaths;
import diarsid.jdbc.transactions.JdbcTransaction;
import diarsid.jdbc.transactions.exceptions.TransactionHandledException;
import diarsid.jdbc.transactions.exceptions.TransactionHandledSQLException;

import static java.util.Collections.emptyList;
import static java.util.stream.Collectors.toList;

import static diarsid.beam.core.base.control.io.commands.CommandType.OPEN_LOCATION_TARGET;
import static diarsid.beam.core.base.objects.Pool.takeFromPool;
import static diarsid.beam.core.base.util.CollectionsUtils.nonEmpty;
import static diarsid.beam.core.base.util.SqlUtil.lowerWildcard;
import static diarsid.beam.core.base.util.StringUtils.lower;

/**
 *
 * @author Diarsid
 */
class H2DaoLocationSubPathsV2 
        extends BeamCommonDao 
        implements DaoLocationSubPaths {
    
    
    H2DaoLocationSubPathsV2(DataBase dataBase, InnerIoEngine ioEngine) {
        super(dataBase, ioEngine);
    }

    @Override
    public List<LocationSubPath> getSubPathesByPattern(Initiator initiator, String pattern) {
        try (
                JdbcTransaction transact = super.openTransaction();
                SqlPatternSelect patternSelect = takeFromPool(SqlPatternSelect.class)) 
        {
            
            List<LocationSubPath> foundSubPaths;
            
            foundSubPaths = transact
                    .doQueryAndStreamVarargParams(
                            LocationSubPath.class, 
                            "SELECT DISTINCT loc_name, loc_path, com_extended AS subpath " +
                            "FROM " +
                            "   commands AS com " +
                            "   JOIN " +
                            "   locations AS loc " +
                            "   ON LOWER(com.com_extended) LIKE CONCAT(LOWER(loc.loc_name) , '%') " +
                            "WHERE " +
                            "   ( com_type IS ? ) AND " +
                            "   ( ( LOWER(com_original) IS ? ) OR ( LOWER(com_original) LIKE ? ) )", 
                            (row) -> {
                                return new LocationSubPath(
                                        pattern, 
                                        row.get("loc_name", String.class),
                                        row.get("loc_path", String.class),
                                        row.get("subpath", String.class));
                            }, 
                            OPEN_LOCATION_TARGET, lower(pattern), lowerWildcard(pattern))
                    .collect(toList());
            
            if ( nonEmpty(foundSubPaths) ) {
                return foundSubPaths;
            }    
            
            patternSelect
                    .selectDistinct("loc_name, loc_path, com_extended")
                    .from(
                            "commands AS com " +
                            "JOIN " +
                            "locations AS loc " +
                            "ON LOWER(com.com_extended) LIKE CONCAT(LOWER(loc.loc_name) , '%') ")
                    .patternForWhereCondition(pattern)
                    .patternColumnForWhereCondition("com_extended")
                    .anotherWhereClauses(" AND ( com_type IS ? )");
            
            foundSubPaths = transact
                    .doQueryAndStreamVarargParams(
                            LocationSubPath.class, 
                            patternSelect.compose(), 
                            (row) -> {
                                return new LocationSubPath(
                                        pattern, 
                                        row.get("loc_name", String.class),
                                        row.get("loc_path", String.class),
                                        row.get("com_extended", String.class));
                            }, 
                            OPEN_LOCATION_TARGET)
                    .collect(toList());
            
            if ( nonEmpty(foundSubPaths) ) {
                return foundSubPaths;
            }    
            
            foundSubPaths = transact
                    .doQueryAndStreamVarargParams(
                            LocationSubPath.class, 
                            patternSelect
                                    .decreaseRequiredLikeness()
                                    .compose(), 
                            (row) -> {
                                return new LocationSubPath(
                                        pattern, 
                                        row.get("loc_name", String.class),
                                        row.get("loc_path", String.class),
                                        row.get("subpath", String.class));
                            }, 
                            OPEN_LOCATION_TARGET)
                    .collect(toList());
            
            if ( nonEmpty(foundSubPaths) ) {
                return foundSubPaths;
            }
            
            foundSubPaths = transact
                    .doQueryAndStreamVarargParams(
                            LocationSubPath.class, 
                            patternSelect
                                    .decreaseRequiredLikeness()
                                    .compose(), 
                            (row) -> {
                                return new LocationSubPath(
                                        pattern, 
                                        row.get("loc_name", String.class),
                                        row.get("loc_path", String.class),
                                        row.get("subpath", String.class));
                            }, 
                            OPEN_LOCATION_TARGET)
                    .collect(toList());
            
            if ( nonEmpty(foundSubPaths) ) {
                return foundSubPaths;
            }
            
            return foundSubPaths;
            
        } catch (TransactionHandledSQLException|TransactionHandledException ex) {
            // TODO LOW
            return emptyList();
        }
    }
}
