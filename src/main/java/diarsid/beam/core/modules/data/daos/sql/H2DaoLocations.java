/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package diarsid.beam.core.modules.data.daos.sql;

import java.util.List;
import java.util.Optional;

import diarsid.beam.core.base.control.io.base.actors.Initiator;
import diarsid.beam.core.base.control.io.base.actors.InnerIoEngine; 
import diarsid.beam.core.domain.entities.Location;
import diarsid.beam.core.modules.data.DaoLocations;
import diarsid.beam.core.modules.data.DataBase;
import diarsid.beam.core.modules.data.daos.BeamCommonDao;
import diarsid.jdbc.transactions.JdbcTransaction;
import diarsid.jdbc.transactions.exceptions.TransactionHandledException;
import diarsid.jdbc.transactions.exceptions.TransactionHandledSQLException;

import static java.lang.String.format;
import static java.lang.String.join;
import static java.util.Arrays.stream;
import static java.util.Collections.emptyList;
import static java.util.stream.Collectors.toList;
import static java.util.stream.Collectors.toSet;

import static diarsid.beam.core.base.control.io.base.interaction.Messages.error;
import static diarsid.beam.core.base.util.CollectionsUtils.nonEmpty;
import static diarsid.beam.core.base.util.Logs.debug;
import static diarsid.beam.core.base.util.Logs.logError;
import static diarsid.beam.core.base.util.SqlUtil.lowerWildcard;
import static diarsid.beam.core.base.util.SqlUtil.multipleLowerGroupedLikesOr;
import static diarsid.beam.core.base.util.SqlUtil.multipleLowerLikeAnd;
import static diarsid.beam.core.base.util.SqlUtil.patternToCharCriterias;
import static diarsid.beam.core.base.util.SqlUtil.shift;
import static diarsid.beam.core.base.util.StringIgnoreCaseUtil.replaceIgnoreCase;
import static diarsid.beam.core.base.util.StringUtils.lower;
import static diarsid.beam.core.modules.data.daos.sql.RowToEntityConversions.ROW_TO_LOCATION;
import static diarsid.jdbc.transactions.core.Params.params;


class H2DaoLocations 
        extends BeamCommonDao 
        implements DaoLocations {
    
    H2DaoLocations(DataBase dataBase, InnerIoEngine ioEngine) {
        super(dataBase, ioEngine);
    }

    @Override
    public boolean isNameFree(Initiator initiator, String exactName) {
        try {
            return ! super.openDisposableTransaction()
                    .doesQueryHaveResultsVarargParams(
                            "SELECT loc_name " +
                            "FROM locations " +
                            "WHERE LOWER(loc_name) IS ? ",
                            lower(exactName));
        } catch (TransactionHandledSQLException|TransactionHandledException ex) {
            logError(H2DaoLocations.class, ex);
            super.ioEngine().report(initiator, "is name free request failed.");
            return false;
        }
    }

    @Override
    public Optional<Location> getLocationByExactName(Initiator initiator, String exactName) {
        try {
            return super.openDisposableTransaction()
                    .doQueryAndStreamVarargParams(
                            Location.class,
                            "SELECT loc_name, loc_path " +
                            "FROM locations " +
                            "WHERE ( LOWER(loc_name) IS ? ) ",
                            ROW_TO_LOCATION,
                            lower(exactName))
                    .findFirst();
        } catch (TransactionHandledSQLException|TransactionHandledException ex) {
            logError(this.getClass(), ex);
            super.ioEngine().report(
                    initiator, format("location search by exact name '%s' failed.", exactName));
            return Optional.empty();
        }
    }

    @Override
    public List<Location> getLocationsByNamePattern(
            Initiator initiator, String pattern) {        
        try (JdbcTransaction transact = super.openTransaction()) {
            
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
            } else {
                debug("[PATTERN FULL] not found : " + pattern);
            }
            
            List<String> criterias = patternToCharCriterias(pattern);
            debug("[PATTERN] criterias: " + join(" ", criterias));
            
            found = transact
                    .doQueryAndStreamVarargParams(
                            Location.class, 
                            "SELECT loc_name, loc_path " +
                            "FROM locations " +
                            "WHERE " + multipleLowerLikeAnd("loc_name", criterias.size()), 
                            ROW_TO_LOCATION,
                            criterias)
                    .collect(toList());
            
            if ( nonEmpty(found) ) {
                return found;
            } else {
                debug("[PATTERN CRITERIAS AND] not found : " + pattern);
            }
            
            String andOrCondition = multipleLowerGroupedLikesOr("loc_name", criterias.size());
            List<Location> shiftedFound;
            
            found = transact
                    .doQueryAndStreamVarargParams(
                            Location.class, 
                            "SELECT loc_name, loc_path " +
                            "FROM locations " +
                            "WHERE " + andOrCondition, 
                            ROW_TO_LOCATION,
                            criterias)
                    .collect(toList());
            
            shift(criterias);
            debug("[PATTERN] shuffled criterias: " + join(" ", criterias));
            shiftedFound = transact
                    .doQueryAndStreamVarargParams(
                            Location.class, 
                            "SELECT loc_name, loc_path " +
                            "FROM locations " +
                            "WHERE " + andOrCondition, 
                            ROW_TO_LOCATION,
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

    @Override
    public boolean saveNewLocation(
            Initiator initiator, Location location) {
        try (JdbcTransaction transact = super.openTransaction()) {
            
            boolean nameIsFree = ! transact
                    .doesQueryHaveResultsVarargParams(
                            "SELECT loc_name " +
                            "FROM locations " +
                            "WHERE LOWER(loc_name) IS ? ", 
                            lower(location.name()));
            
            int updated = transact
                    .ifTrue( nameIsFree )
                    .doUpdateVarargParams(
                            "INSERT INTO locations (loc_name, loc_path) " +
                            "VALUES ( ?, ? ) ", 
                            location.name(), location.path());
            
            return ( updated == 1 && nameIsFree );
        } catch (TransactionHandledSQLException|TransactionHandledException ex) {
            logError(this.getClass(), ex);
            super.ioEngine().reportMessage(initiator, error(
                    "Location saving failed:",
                    "   name: " + location.name(),
                    "   path: " + location.path()));
            return false;
        }
    }

    @Override
    public boolean removeLocation(
            Initiator initiator, String locationName) {
        try {
            int removed = super.openDisposableTransaction()
                    .doUpdateVarargParams(
                            "DELETE FROM locations " +
                            "WHERE LOWER(loc_name) IS ? ", 
                            lower(locationName));
            return ( removed > 0 );
        } catch (TransactionHandledSQLException|TransactionHandledException ex) {
            logError(this.getClass(), ex);
            super.ioEngine().report(
                    initiator, format("Location removing by '%s' failed.", locationName));
            return false;
        }
    }

    @Override
    public boolean editLocationPath(
            Initiator initiator, String locationName, String newPath) {
        try (JdbcTransaction transact = super.openTransaction()) {
            
            int modified = transact
                    .doUpdateVarargParams(
                            "UPDATE locations " +
                            "SET loc_path = ? " +
                            "WHERE LOWER(loc_name) IS ? ", 
                            newPath, lower(locationName));
            
            transact
                    .ifTrue( modified != 1 )
                    .rollbackAndProceed();
            
            return ( modified == 1 );
        } catch (TransactionHandledSQLException|TransactionHandledException ex) {
            logError(this.getClass(), ex);
            super.ioEngine().reportMessage(
                    initiator, 
                    error(
                            "Location path changing:", 
                            "   by name: " + locationName,
                            "   new path: " + newPath, 
                            "failed."));
            return false;
        }
    }

    @Override
    public boolean editLocationName(
            Initiator initiator, String locationName, String newName) {
        try (JdbcTransaction transact = super.openTransaction()) {
            
            boolean nameIsFree = ! transact
                    .doesQueryHaveResultsVarargParams(
                            "SELECT loc_name " +
                            "FROM locations " +
                            "WHERE LOWER(loc_name) IS ? ", 
                            lower(newName));
            
            int modified = transact
                    .ifTrue( nameIsFree )
                    .doUpdateVarargParams(
                            "UPDATE locations " +
                            "SET loc_name = ? " +
                            "WHERE LOWER(loc_name) IS ? ", 
                            newName, lower(locationName));
            
            transact
                    .ifTrue( modified != 1 )
                    .rollbackAndProceed();
            
            return ( modified == 1 );
        } catch (TransactionHandledSQLException|TransactionHandledException ex) {
            logError(this.getClass(), ex);
            super.ioEngine().reportMessage(
                    initiator, 
                    error(
                            "Location name changing:", 
                            "   by name: " + locationName,
                            "   new name: " + newName, 
                            "failed."));
            return false;
        }
    }

    @Override
    public boolean replaceInPaths(
            Initiator initiator, String replaceable, String replacement) {        
        try (JdbcTransaction transact = super.openTransaction()) {
            
            List<Location> locationsToModify = transact
                    .doQueryAndStreamVarargParams(
                            Location.class,                            
                            "SELECT loc_name, loc_path " +
                            "FROM locations " +
                            "WHERE LOWER(loc_path) LIKE ? ", 
                            ROW_TO_LOCATION, 
                            lowerWildcard(replaceable))
                    .collect(toList());            

            int modified = stream(transact
                    .ifTrue( nonEmpty(locationsToModify) )
                    .doBatchUpdate(
                            "UPDATE locations " +
                            "SET loc_path = ? " +
                            "WHERE loc_name IS ? ", 
                            locationsToModify
                                    .stream()
                                    .map(location -> params(
                                            replaceIgnoreCase(
                                                    location.path(),
                                                    replaceable, 
                                                    replacement), 
                                            location.name()))
                                    .collect(toSet()))
            ).sum();
                
            return ( modified > 0 );            
        } catch (TransactionHandledSQLException|TransactionHandledException ex) {
            logError(this.getClass(), ex);
            super.ioEngine().reportMessage(
                    initiator, 
                    error(
                            "Locations path replacing:", 
                            "   of: " + replaceable,
                            "   with: " + replacement, 
                            "failed."));
            return false;
        }
    }

    @Override
    public List<Location> getAllLocations(
            Initiator initiator) {
        try {
            return super.openDisposableTransaction()
                    .doQueryAndStream(
                            Location.class,
                            "SELECT loc_name, loc_path " +
                            "FROM locations", 
                            ROW_TO_LOCATION)
                    .collect(toList());
        } catch (TransactionHandledSQLException|TransactionHandledException ex) {
            logError(this.getClass(), ex);
            super.ioEngine().report(initiator, "All locations obtaining failed.");
            return emptyList();
        }
    }
}
