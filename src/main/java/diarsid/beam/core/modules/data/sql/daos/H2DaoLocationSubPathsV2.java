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
import diarsid.beam.core.domain.entities.LocationSubPath;
import diarsid.beam.core.modules.data.DaoLocationSubPaths;
import diarsid.jdbc.transactions.JdbcTransaction;
import diarsid.jdbc.transactions.exceptions.TransactionHandledException;
import diarsid.jdbc.transactions.exceptions.TransactionHandledSQLException;
import diarsid.support.objects.Pool;

import static java.util.stream.Collectors.toList;

import static diarsid.beam.core.base.control.io.commands.CommandType.OPEN_LOCATION_TARGET;
import static diarsid.beam.core.base.util.CollectionsUtils.nonEmpty;
import static diarsid.beam.core.base.util.SqlUtil.lowerWildcard;
import static diarsid.beam.core.base.util.SqlUtil.wildcardAfter;
import static diarsid.support.strings.StringUtils.lower;

/**
 *
 * @author Diarsid
 */
class H2DaoLocationSubPathsV2 
        extends BeamCommonDao 
        implements DaoLocationSubPaths {
        
    private final Pool<SqlPatternSelect> sqlPatternSelectPool;
    
    H2DaoLocationSubPathsV2(DataBase dataBase, Pool<SqlPatternSelect> sqlPatternSelectPool) {
        super(dataBase);
        this.sqlPatternSelectPool = sqlPatternSelectPool;
    }

    @Override
    public List<LocationSubPath> getSubPathesByPattern(Location location, String pattern) 
            throws DataExtractionException {
        try (
                JdbcTransaction transact = super.openTransaction();
                SqlPatternSelect patternSelect = this.sqlPatternSelectPool.give();) 
        {
            
            if ( location.hasSubPath() ) {
                return this.find(transact, patternSelect, (LocationSubPath) location, pattern);
            } else {
                return this.find(transact, patternSelect, location, pattern);
            }            
            
        } catch (TransactionHandledSQLException | TransactionHandledException e) {
            throw super.logAndWrap(e);
        }
    }   
    
    private List<LocationSubPath> find(
            final JdbcTransaction transact, 
            final SqlPatternSelect patternSelect, 
            final LocationSubPath locationSubPath, 
            final String pattern) 
            throws TransactionHandledSQLException, TransactionHandledException {
        List<LocationSubPath> foundSubPaths;
        
        foundSubPaths = transact 
                .doQueryAndStreamVarargParams(
                        (row) -> {
                            return new LocationSubPath(
                                    pattern,
                                    row.get("loc_name", String.class),
                                    row.get("loc_path", String.class),
                                    row.get("subpath", String.class));
                        },
                        "SELECT DISTINCT loc_name, loc_path, com_extended AS subpath " +
                        "FROM " +
                        "   commands AS com " +
                        "   JOIN " +
                        "   locations AS loc " +
                        "   ON LOWER(com.com_extended) LIKE CONCAT(LOWER(loc.loc_name) , '%') " +
                        "WHERE " +
                        "   ( com_type IS ? ) AND " +
                        "   ( loc_name IS ? ) AND " +
                        "   ( com.com_extended LIKE ? ) AND " +
                        "   ( ( LOWER(com_original) IS ? ) OR ( LOWER(com_original) LIKE ? ) )",
                        OPEN_LOCATION_TARGET,
                        locationSubPath.locationName(),
                        wildcardAfter(locationSubPath.name()),
                        lower(pattern), 
                        lowerWildcard(pattern))
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
                .anotherWhereClauses(
                        " AND ( com_type IS ? )" +
                        " AND ( loc_name IS ? )" +
                        " AND ( com.com_extended LIKE ? ) ");
        
        foundSubPaths = transact
                .doQueryAndStreamVarargParams(
                        (row) -> {
                            return new LocationSubPath(
                                    pattern,
                                    row.get("loc_name", String.class),
                                    row.get("loc_path", String.class),
                                    row.get("com_extended", String.class));
                        },
                        patternSelect.composeSql(), 
                        OPEN_LOCATION_TARGET,
                        locationSubPath.locationName(),
                        wildcardAfter(locationSubPath.name()))
                .collect(toList());
        
        if ( nonEmpty(foundSubPaths) ) {
            return foundSubPaths;
        }
        
        foundSubPaths = transact
                .doQueryAndStreamVarargParams(
                        (row) -> {
                            return new LocationSubPath(
                                    pattern,
                                    row.get("loc_name", String.class),
                                    row.get("loc_path", String.class),
                                    row.get("com_extended", String.class));
                        }, 
                        patternSelect
                                .decreaseRequiredLikeness()
                                .composeSql(), 
                        OPEN_LOCATION_TARGET,
                        locationSubPath.locationName(),
                        wildcardAfter(locationSubPath.name()))
                .collect(toList());
        
        if ( nonEmpty(foundSubPaths) ) {
            return foundSubPaths;
        }
        
        foundSubPaths = transact
                .doQueryAndStreamVarargParams(
                        (row) -> {
                            return new LocationSubPath(
                                    pattern,
                                    row.get("loc_name", String.class),
                                    row.get("loc_path", String.class),
                                    row.get("com_extended", String.class));
                        }, 
                        patternSelect
                                .decreaseRequiredLikeness()
                                .composeSql(), 
                        OPEN_LOCATION_TARGET,
                        locationSubPath.locationName(),
                        wildcardAfter(locationSubPath.name()))
                .collect(toList());
        
        if ( nonEmpty(foundSubPaths) ) {
            return foundSubPaths;
        }
   
        return foundSubPaths;
    }

    private List<LocationSubPath> find(
            final JdbcTransaction transact, 
            final SqlPatternSelect patternSelect, 
            final Location location, 
            final String pattern) 
            throws TransactionHandledSQLException, TransactionHandledException {
        List<LocationSubPath> foundSubPaths;
        
        foundSubPaths = transact 
                .doQueryAndStreamVarargParams(
                        (row) -> {
                            return new LocationSubPath(
                                    pattern,
                                    row.get("loc_name", String.class),
                                    row.get("loc_path", String.class),
                                    row.get("subpath", String.class));
                        },
                            "SELECT DISTINCT loc_name, loc_path, com_extended AS subpath " +
                            "FROM " +
                            "   commands AS com " +
                            "   JOIN " +
                            "   locations AS loc " +
                            "   ON LOWER(com.com_extended) LIKE CONCAT(LOWER(loc.loc_name) , '%') " +
                            "WHERE " +
                            "   ( com_type IS ? ) AND " +
                            "   ( loc_name IS ? ) AND " +
                            "   ( ( LOWER(com_original) IS ? ) OR ( LOWER(com_original) LIKE ? ) )",
                            OPEN_LOCATION_TARGET,
                            location.name(),
                            lower(pattern), 
                            lowerWildcard(pattern))
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
                .anotherWhereClauses(
                        " AND ( com_type IS ? )" +
                        " AND ( loc_name IS ? )");
        
        foundSubPaths = transact
                .doQueryAndStreamVarargParams(
                        (row) -> {
                            return new LocationSubPath(
                                    pattern,
                                    row.get("loc_name", String.class),
                                    row.get("loc_path", String.class),
                                    row.get("com_extended", String.class));
                        },
                        patternSelect.composeSql(), 
                        OPEN_LOCATION_TARGET,
                        location.name())
                .collect(toList());
        
        if ( nonEmpty(foundSubPaths) ) {
            return foundSubPaths;
        }
        
        foundSubPaths = transact
                .doQueryAndStreamVarargParams(
                        (row) -> {
                            return new LocationSubPath(
                                    pattern,
                                    row.get("loc_name", String.class),
                                    row.get("loc_path", String.class),
                                    row.get("com_extended", String.class));
                        }, 
                        patternSelect
                                .decreaseRequiredLikeness()
                                .composeSql(), 
                        OPEN_LOCATION_TARGET,
                        location.name())
                .collect(toList());
        
        if ( nonEmpty(foundSubPaths) ) {
            return foundSubPaths;
        }
        
        foundSubPaths = transact
                .doQueryAndStreamVarargParams(
                        (row) -> {
                            return new LocationSubPath(
                                    pattern,
                                    row.get("loc_name", String.class),
                                    row.get("loc_path", String.class),
                                    row.get("com_extended", String.class));
                        }, 
                        patternSelect
                                .decreaseRequiredLikeness()
                                .composeSql(), 
                        OPEN_LOCATION_TARGET,
                        location.name())
                .collect(toList());
        
        if ( nonEmpty(foundSubPaths) ) {
            return foundSubPaths;
        }
   
        return foundSubPaths;
    }

    @Override
    public List<LocationSubPath> getSubPathesByPattern(String pattern) 
            throws DataExtractionException {
        try (
                JdbcTransaction transact = super.openTransaction();
                SqlPatternSelect patternSelect = this.sqlPatternSelectPool.give();) 
        {
            
            List<LocationSubPath> foundSubPaths;
            
//            foundSubPaths = transact
//                    .doQueryAndStreamVarargParams(
//                            (row) -> {
//                                return new LocationSubPath(
//                                        pattern, 
//                                        row.get("loc_name", String.class),
//                                        row.get("loc_path", String.class),
//                                        row.get("subpath", String.class));
//                            }, 
//                            "SELECT DISTINCT loc_name, loc_path, com_extended AS subpath " +
//                            "FROM " +
//                            "   commands AS com " +
//                            "   JOIN " +
//                            "   locations AS loc " +
//                            "   ON LOWER(com.com_extended) LIKE CONCAT(LOWER(loc.loc_name) , '%') " +
//                            "WHERE " +
//                            "   ( com_type IS ? ) AND " +
//                            "   ( ( LOWER(com_original) IS ? ) OR ( LOWER(com_original) LIKE ? ) )", 
//                            OPEN_LOCATION_TARGET, lower(pattern), lowerWildcard(pattern))
//                    .collect(toList());
//            
//            if ( nonEmpty(foundSubPaths) ) {
//                return foundSubPaths;
//            }    
            
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
                            (row) -> {
                                return new LocationSubPath(
                                        pattern, 
                                        row.get("loc_name", String.class),
                                        row.get("loc_path", String.class),
                                        row.get("com_extended", String.class));
                            }, 
                            patternSelect.composeSql(), 
                            OPEN_LOCATION_TARGET)
                    .collect(toList());
            
            if ( nonEmpty(foundSubPaths) ) {
                return foundSubPaths;
            }    
            
            foundSubPaths = transact
                    .doQueryAndStreamVarargParams(
                            (row) -> {
                                return new LocationSubPath(
                                        pattern, 
                                        row.get("loc_name", String.class),
                                        row.get("loc_path", String.class),
                                        row.get("com_extended", String.class));
                            }, 
                            patternSelect
                                    .decreaseRequiredLikeness()
                                    .composeSql(), 
                            OPEN_LOCATION_TARGET)
                    .collect(toList());
            
            if ( nonEmpty(foundSubPaths) ) {
                return foundSubPaths;
            }
            
            foundSubPaths = transact
                    .doQueryAndStreamVarargParams(
                            (row) -> {
                                return new LocationSubPath(
                                        pattern, 
                                        row.get("loc_name", String.class),
                                        row.get("loc_path", String.class),
                                        row.get("com_extended", String.class));
                            }, 
                            patternSelect
                                    .decreaseRequiredLikeness()
                                    .composeSql(), 
                            OPEN_LOCATION_TARGET)
                    .collect(toList());
            
            if ( nonEmpty(foundSubPaths) ) {
                return foundSubPaths;
            }
            
            return foundSubPaths;
            
        } catch (TransactionHandledSQLException | TransactionHandledException e) {
            throw super.logAndWrap(e);
        }
    }
}
