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
import diarsid.jdbc.transactions.PerRowConversion;
import diarsid.jdbc.transactions.exceptions.TransactionHandledSQLException;

import static java.lang.String.format;
import static java.lang.String.join;
import static java.util.Arrays.stream;
import static java.util.Collections.emptyList;
import static java.util.stream.Collectors.toList;
import static java.util.stream.Collectors.toSet;

import static diarsid.beam.core.base.control.io.base.interaction.Messages.error;
import static diarsid.beam.core.base.util.CollectionsUtils.nonEmpty;
import static diarsid.beam.core.base.util.Logs.logError;
import static diarsid.beam.core.base.util.SqlUtil.SqlOperator.AND;
import static diarsid.beam.core.base.util.SqlUtil.lowerWildcard;
import static diarsid.beam.core.base.util.SqlUtil.lowerWildcardList;
import static diarsid.beam.core.base.util.StringIgnoreCaseUtil.replaceIgnoreCase;
import static diarsid.beam.core.base.util.StringUtils.lower;
import static diarsid.jdbc.transactions.core.Params.params;
import static diarsid.beam.core.base.util.SqlUtil.multipleLowerLIKE;


class H2DaoLocations 
        extends BeamCommonDao 
        implements DaoLocations {
    
    private final PerRowConversion<Location> rowToLocationConversion;
    
    H2DaoLocations(DataBase dataBase, InnerIoEngine ioEngine) {
        super(dataBase, ioEngine);
        this.rowToLocationConversion = (row) -> {
            return new Location(
                    (String) row.get("loc_name"),
                    (String) row.get("loc_path"));
        };
    }

    @Override
    public boolean isNameFree(Initiator initiator, String exactName) {
        try {
            return ! super.getDisposableTransaction()
                    .doesQueryHaveResultsVarargParams(
                            "SELECT loc_name " +
                            "FROM locations " +
                            "WHERE LOWER(loc_name) IS ? ",
                            lower(exactName));
        } catch (TransactionHandledSQLException ex) {
            logError(H2DaoLocations.class, ex);
            super.ioEngine().report(initiator, "is name free request failed.");
            return false;
        }
    }

    @Override
    public Optional<Location> getLocationByExactName(Initiator initiator, String exactName) {
        try {
            return super.getDisposableTransaction()
                    .doQueryAndStreamVarargParams(
                            Location.class,
                            "SELECT loc_name, loc_path " +
                            "FROM locations " +
                            "WHERE ( LOWER(loc_name) IS ? ) ",
                            this.rowToLocationConversion,
                            lower(exactName))
                    .findFirst();
        } catch (TransactionHandledSQLException ex) {
            logError(this.getClass(), ex);
            super.ioEngine().report(
                    initiator, format("location search by exact name '%s' failed.", exactName));
            return Optional.empty();
        }
    }

    @Override
    public List<Location> getLocationsByNamePattern(
            Initiator initiator, String locationName) {
        try {
            return super.getDisposableTransaction()
                    .doQueryAndStreamVarargParams(
                            Location.class, 
                            "SELECT loc_name, loc_path " +
                            "FROM locations " +
                            "WHERE LOWER(loc_name) LIKE ?  ", 
                            this.rowToLocationConversion,
                            lowerWildcard(locationName))
                    .collect(toList());
        } catch (TransactionHandledSQLException ex) {
            logError(this.getClass(), ex);
            super.ioEngine().report(
                    initiator, format("location search by name '%s' failed.", locationName));
            return emptyList();
        }
    }

    @Override
    public List<Location> getLocationsByNamePatternParts(
            Initiator initiator, List<String> nameParts) {
        
        try {
            return super.getDisposableTransaction()
                    .ifTrue( nonEmpty(nameParts) )
                    .doQueryAndStream(Location.class,                            
                            "SELECT loc_name, loc_path " +
                            "FROM locations " +
                            "WHERE " + multipleLowerLIKE("loc_name", nameParts.size(), AND), 
                            this.rowToLocationConversion,
                            lowerWildcardList(nameParts))                    
                    .collect(toList());
        } catch (TransactionHandledSQLException ex) {
            logError(this.getClass(), ex);
            super.ioEngine().reportMessage(initiator, error(
                    "location search by name parts: ", 
                    "   " + join(" + ", nameParts),
                    "failed."));
            return emptyList();
        }
    }

    @Override
    public boolean saveNewLocation(
            Initiator initiator, Location location) {
        try (JdbcTransaction transact = super.getTransaction()) {
            
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
                            location.name(), location.getPath());
            
            return ( updated == 1 && nameIsFree );
        } catch (TransactionHandledSQLException ex) {
            logError(this.getClass(), ex);
            super.ioEngine().reportMessage(initiator, error(
                    "Location saving failed:",
                    "   name: " + location.name(),
                    "   path: " + location.getPath()));
            return false;
        }
    }

    @Override
    public boolean removeLocation(
            Initiator initiator, String locationName) {
        try {
            int removed = super.getDisposableTransaction()
                    .doUpdateVarargParams(
                            "DELETE FROM locations " +
                            "WHERE LOWER(loc_name) IS ? ", 
                            lower(locationName));
            return ( removed > 0 );
        } catch (TransactionHandledSQLException ex) {
            logError(this.getClass(), ex);
            super.ioEngine().report(
                    initiator, format("Location removing by '%s' failed.", locationName));
            return false;
        }
    }

    @Override
    public boolean editLocationPath(
            Initiator initiator, String locationName, String newPath) {
        try (JdbcTransaction transact = super.getTransaction()) {
            
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
        } catch (TransactionHandledSQLException ex) {
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
        try (JdbcTransaction transact = super.getTransaction()) {
            
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
        } catch (TransactionHandledSQLException ex) {
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
        try (JdbcTransaction transact = super.getTransaction()) {
            
            List<Location> locationsToModify = transact
                    .doQueryAndStreamVarargParams(
                            Location.class,                            
                            "SELECT loc_name, loc_path " +
                            "FROM locations " +
                            "WHERE LOWER(loc_path) LIKE ? ", 
                            this.rowToLocationConversion, 
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
                                                    location.getPath(),
                                                    replaceable, 
                                                    replacement), 
                                            location.name()))
                                    .collect(toSet()))
            ).sum();
                
            return ( modified > 0 );            
        } catch (TransactionHandledSQLException ex) {
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
            return super.getDisposableTransaction()
                    .doQueryAndStream(
                            Location.class,
                            "SELECT loc_name, loc_path " +
                            "FROM locations", 
                            this.rowToLocationConversion)
                    .collect(toList());
        } catch (TransactionHandledSQLException ex) {
            logError(this.getClass(), ex);
            super.ioEngine().report(initiator, "All locations obtaining failed.");
            return emptyList();
        }
    }
}
