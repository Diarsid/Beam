/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package diarsid.beam.core.modules.data.sql.daos;

import java.util.List;
import java.util.Optional;

import diarsid.beam.core.base.control.io.base.actors.Initiator;
import diarsid.beam.core.base.control.io.base.actors.InnerIoEngine;
import diarsid.beam.core.base.data.DataBase;
import diarsid.beam.core.domain.entities.Location;
import diarsid.beam.core.modules.data.BeamCommonDao;
import diarsid.beam.core.modules.data.DaoLocations;
import diarsid.jdbc.transactions.JdbcTransaction;
import diarsid.jdbc.transactions.exceptions.TransactionHandledException;
import diarsid.jdbc.transactions.exceptions.TransactionHandledSQLException;

import static java.lang.String.format;
import static java.util.Arrays.stream;
import static java.util.Collections.emptyList;
import static java.util.stream.Collectors.toList;
import static java.util.stream.Collectors.toSet;

import static diarsid.beam.core.base.control.io.base.interaction.Messages.error;
import static diarsid.beam.core.base.util.CollectionsUtils.nonEmpty;
import static diarsid.beam.core.base.util.Logs.logError;
import static diarsid.beam.core.base.util.SqlUtil.lowerWildcard;
import static diarsid.beam.core.base.util.StringIgnoreCaseUtil.replaceIgnoreCase;
import static diarsid.beam.core.base.util.StringUtils.lower;
import static diarsid.beam.core.modules.data.sql.daos.RowToEntityConversions.ROW_TO_LOCATION;
import static diarsid.jdbc.transactions.core.Params.params;


abstract class H2DaoLocationsV0 
        extends BeamCommonDao 
        implements DaoLocations {
    
    H2DaoLocationsV0(DataBase dataBase, InnerIoEngine ioEngine) {
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
            logError(H2DaoLocationsV0.class, ex);
            super.ioEngine().report(initiator, "is name free request failed.");
            return false;
        }
    }

    @Override
    public Optional<Location> getLocationByExactName(Initiator initiator, String exactName) {
        try {
            return super.openDisposableTransaction()
                    .doQueryAndStreamVarargParams(
                            ROW_TO_LOCATION,
                            "SELECT loc_name, loc_path " +
                            "FROM locations " +
                            "WHERE ( LOWER(loc_name) IS ? ) ",
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
        try (JdbcTransaction transact = super.openTransaction()) {
            
            String lowerLocationName = lower(locationName);
            this.removeLocationSubPathChoicesUsing(transact, lowerLocationName);
            
            int removed = transact
                    .doUpdateVarargParams(
                            "DELETE FROM locations " +
                            "WHERE LOWER(loc_name) IS ? ", 
                            lowerLocationName);
            
            return ( removed > 0 );            
        } catch (TransactionHandledSQLException|TransactionHandledException ex) {
            logError(this.getClass(), ex);
            super.ioEngine().report(
                    initiator, format("Location removing by '%s' failed.", locationName));
            return false;
        }
    }
    
    private void removeLocationSubPathChoicesUsing(
            JdbcTransaction transact, String lowerLocationName) 
            throws TransactionHandledException, TransactionHandledSQLException {
        transact
                .doUpdateVarargParams(
                        "DELETE FROM subpath_choices " +
                        "WHERE ( LOWER(loc_name) IS ? )", 
                        lowerLocationName);
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
            
            boolean nameIsNotFree = transact
                    .doesQueryHaveResultsVarargParams(
                            "SELECT loc_name " +
                            "FROM locations " +
                            "WHERE LOWER(loc_name) IS ? ", 
                            lower(newName));
            
            if ( nameIsNotFree ) {
                return false;
            } 
            
            String lowerLocationName = lower(locationName);
            this.removeLocationSubPathChoicesUsing(transact, lowerLocationName);
            
            int modified = transact
                    .doUpdateVarargParams(
                            "UPDATE locations " +
                            "SET loc_name = ? " +
                            "WHERE LOWER(loc_name) IS ? ", 
                            newName, lowerLocationName);
            
            if ( modified != 1 ) {
                transact
                    .rollbackAndProceed();
            }    
            
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
                            ROW_TO_LOCATION,     
                            "SELECT loc_name, loc_path " +
                            "FROM locations " +
                            "WHERE LOWER(loc_path) LIKE ? ", 
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
                            ROW_TO_LOCATION,
                            "SELECT loc_name, loc_path " +
                            "FROM locations")
                    .collect(toList());
        } catch (TransactionHandledSQLException|TransactionHandledException ex) {
            logError(this.getClass(), ex);
            super.ioEngine().report(initiator, "All locations obtaining failed.");
            return emptyList();
        }
    }
}