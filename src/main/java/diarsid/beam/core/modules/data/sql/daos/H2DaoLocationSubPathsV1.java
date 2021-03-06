/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package diarsid.beam.core.modules.data.sql.daos;

import java.util.List;

import diarsid.beam.core.base.data.DataBase;
import diarsid.beam.core.base.data.DataExtractionException;
import diarsid.beam.core.domain.entities.Location;
import diarsid.beam.core.domain.entities.LocationSubPath;
import diarsid.beam.core.modules.data.DaoLocationSubPaths;
import diarsid.jdbc.transactions.JdbcTransaction;
import diarsid.jdbc.transactions.exceptions.TransactionHandledException;
import diarsid.jdbc.transactions.exceptions.TransactionHandledSQLException;

import static java.util.stream.Collectors.toList;

import static diarsid.beam.core.base.control.io.commands.CommandType.OPEN_LOCATION_TARGET;
import static diarsid.beam.core.base.util.CollectionsUtils.nonEmpty;
import static diarsid.beam.core.base.util.SqlUtil.lowerWildcard;
import static diarsid.beam.core.base.util.SqlUtil.multipleLowerGroupedLikesAndOr;
import static diarsid.beam.core.base.util.SqlUtil.patternToCharCriterias;
import static diarsid.beam.core.base.util.SqlUtil.shift;
import static diarsid.support.strings.StringUtils.lower;

/**
 *
 * @author Diarsid
 */
class H2DaoLocationSubPathsV1 
        extends BeamCommonDao 
        implements DaoLocationSubPaths {
    
    H2DaoLocationSubPathsV1(DataBase dataBase) {
        super(dataBase);
    }

    @Override
    public List<LocationSubPath> getSubPathesByPattern(
            Location location, String pattern) throws DataExtractionException {
        throw new UnsupportedOperationException("Not supported yet."); 
    }
    

    @Override
    public List<LocationSubPath> getSubPathesByPattern(String pattern) 
            throws DataExtractionException {
        try (JdbcTransaction transact = super.openTransaction()) {
            
            List<LocationSubPath> foundSubPaths;
            
//            foundSubPaths = transact
//                    .doQueryAndStreamVarargParams(
//                            LocationSubPath.class, 
//                            "SELECT loc.loc_path, loc.loc_name, sub.subpath " +
//                            "FROM " +
//                            "   locations AS loc " +
//                            "   JOIN " +
//                            "   subpath_choices sub " +
//                            "   ON sub.loc_name = loc.loc_name " +
//                            "WHERE ( LOWER(sub.pattern) IS ? )", 
//                            (row) -> {
//                                return new LocationSubPath(
//                                        pattern, 
//                                        row.get("loc_name", String.class),
//                                        row.get("loc_path", String.class),
//                                        row.get("com_extended", String.class));
//                            }, 
//                            lower(pattern))
//                    .collect(toList());
//            
//            if ( nonEmpty(foundSubPaths) ) {
//                return foundSubPaths;
//            }
            
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
                            "   ( ( LOWER(com_original) IS ? ) OR ( LOWER(com_original) LIKE ? ) )",
                            OPEN_LOCATION_TARGET, lower(pattern), lowerWildcard(pattern))
                    .collect(toList());
            
            if ( nonEmpty(foundSubPaths) ) {
                return foundSubPaths;
            }    
            
            List<String> criterias = patternToCharCriterias(pattern);
            
//            foundSubPaths = transact
//                    .doQueryAndStreamVarargParams(
//                            LocationSubPath.class, 
//                            "SELECT loc.loc_path, loc.loc_name, sub.subpath " +
//                            "FROM " +
//                            "   locations AS loc " +
//                            "   JOIN " +
//                            "   subpath_choices sub " +
//                            "   ON sub.loc_name = loc.loc_name " +
//                            "WHERE " +
//                            "   ( " + multipleLowerLikeAnd("sub.pattern", criterias.size()) + " )", 
//                            (row) -> {
//                                return new LocationSubPath(
//                                        pattern, 
//                                        row.get("loc_name", String.class),
//                                        row.get("loc_path", String.class),
//                                        row.get("subpath", String.class));
//                            }, 
//                            criterias)
//                    .collect(toList());
//            
//            if ( nonEmpty(foundSubPaths) ) {
//                return foundSubPaths;
//            }
            
//            foundSubPaths = transact
//                    .doQueryAndStreamVarargParams(
//                            LocationSubPath.class, 
//                            "SELECT DISTINCT loc_name, loc_path, com_extended AS subpath " +
//                            "FROM " +
//                            "   commands AS com " +
//                            "   JOIN " +
//                            "   locations AS loc " +
//                            "   ON LOWER(com.com_extended) LIKE CONCAT(LOWER(loc.loc_name) , '%') " +
//                            "WHERE " +
//                            "   ( com_type IS ? ) AND " +
//                            "   ( " + multipleLowerLikeAnd("com_extended", criterias.size()) + " )", 
//                            (row) -> {
//                                return new LocationSubPath(
//                                        pattern, 
//                                        row.get("loc_name", String.class),
//                                        row.get("loc_path", String.class),
//                                        row.get("subpath", String.class));
//                            }, 
//                            OPEN_LOCATION_TARGET, criterias)
//                    .collect(toList());
//            
//            if ( nonEmpty(foundSubPaths) ) {
//                return foundSubPaths;
//            }
            
            String andOrCondition = multipleLowerGroupedLikesAndOr("com_extended", criterias.size());
            
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
                            "   ( " + andOrCondition + " )", 
                            OPEN_LOCATION_TARGET, criterias)
                    .collect(toList());            
            
            List<LocationSubPath> shiftedFoundSubPaths;
            shift(criterias);
            
            shiftedFoundSubPaths = transact
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
                            "   ( " + andOrCondition + " )", 
                            OPEN_LOCATION_TARGET, criterias)
                    .collect(toList());
            
            shiftedFoundSubPaths.retainAll(foundSubPaths);
            foundSubPaths.retainAll(shiftedFoundSubPaths);
            
            return foundSubPaths;
            
        } catch (TransactionHandledSQLException|TransactionHandledException e) {
            throw super.logAndWrap(e);
        }    
    }
}
