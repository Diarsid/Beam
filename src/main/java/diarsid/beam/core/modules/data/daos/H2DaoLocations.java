/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package diarsid.beam.core.modules.data.daos;

import java.util.List;

import diarsid.beam.core.control.io.base.Initiator;
import diarsid.beam.core.control.io.base.InnerIoEngine; 
import diarsid.beam.core.domain.entities.Location;
import diarsid.beam.core.modules.data.DaoLocations;
import diarsid.beam.core.modules.data.DataBase;
import diarsid.jdbc.transactions.JdbcTransaction;
import diarsid.jdbc.transactions.PerRowConversion;
import diarsid.jdbc.transactions.exceptions.TransactionHandledSQLException;

import static java.lang.String.format;
import static java.lang.String.join;
import static java.util.Arrays.stream;
import static java.util.Collections.emptyList;
import static java.util.stream.Collectors.toList;
import static java.util.stream.Collectors.toSet;

import static diarsid.beam.core.control.io.base.Messages.error;
import static diarsid.beam.core.util.CollectionsUtils.nonEmpty;
import static diarsid.beam.core.util.Logs.logError;
import static diarsid.beam.core.util.SqlUtil.SqlOperator.AND;
import static diarsid.beam.core.util.SqlUtil.lowerWildcard;
import static diarsid.beam.core.util.SqlUtil.lowerWildcardList;
import static diarsid.beam.core.util.SqlUtil.multipleLowerLike;
import static diarsid.beam.core.util.StringIgnoreCaseUtil.replaceIgnoreCase;
import static diarsid.jdbc.transactions.core.Params.params;


public class H2DaoLocations implements DaoLocations {
    
    private final DataBase dataBase;                
    private final InnerIoEngine ioEngine;
    private final PerRowConversion<Location> rowToLocationConversion;
    
    public H2DaoLocations(DataBase dataBase, InnerIoEngine ioEngine) {
        this.dataBase = dataBase;
        this.ioEngine = ioEngine;
        this.rowToLocationConversion = (row) -> {
            return new Location(
                    (String) row.get("loc_name"),
                    (String) row.get("loc_path"));
        };
    }

    private JdbcTransaction getDisposableTransaction() 
            throws TransactionHandledSQLException {
        return this.dataBase
                .transactionFactory()
                .createDisposableTransaction();
    }

    private JdbcTransaction getTransaction() 
            throws TransactionHandledSQLException {
        return this.dataBase.transactionFactory().createTransaction();
    }

    @Override
    public List<Location> getLocationsByName(
            Initiator initiator, String locationName) {
        try {
            return this.getDisposableTransaction()
                    .doQueryAndStreamVarargParams(
                            "SELECT loc_name, loc_path " +
                            "FROM locations " +
                            "WHERE ( LOWER(loc_name) LIKE ? ) ", 
                            this.rowToLocationConversion,
                            Location.class, 
                            lowerWildcard(locationName))
                    .collect(toList());
        } catch (TransactionHandledSQLException ex) {
            logError(this.getClass(), ex);
            this.ioEngine.report(
                    initiator, format("location search by name '%s' failed.", locationName));
            return emptyList();
        }
    }

    @Override
    public List<Location> getLocationsByNameParts(
            Initiator initiator, List<String> nameParts) {
        
        try {
            return this.getDisposableTransaction()
                    .ifTrue( nonEmpty(nameParts) )
                    .doQueryAndStream(
                            "SELECT loc_name, loc_path " +
                            "FROM locations " +
                            "WHERE " + multipleLowerLike("loc_name", nameParts.size(), AND), 
                            this.rowToLocationConversion,
                            Location.class, 
                            lowerWildcardList(nameParts))                    
                    .collect(toList());
        } catch (TransactionHandledSQLException ex) {
            logError(this.getClass(), ex);
            this.ioEngine.reportMessage(initiator, error(
                    "location search by name parts: ", 
                    "   " + join(" + ", nameParts),
                    "failed."));
            return emptyList();
        }
    }

    @Override
    public boolean saveNewLocation(
            Initiator initiator, Location location) {
        try {
            int updated = this.getDisposableTransaction()
                    .doUpdateVarargParams(
                            "INSERT INTO locations (loc_name, loc_path) " +
                            "VALUES ( ?, ? ) ", 
                            location.getName(), location.getPath());
            return ( updated == 1 );
        } catch (TransactionHandledSQLException ex) {
            logError(this.getClass(), ex);
            this.ioEngine.reportMessage(initiator, error(
                    "Location saving failed:",
                    "   name: " + location.getName(),
                    "   path: " + location.getPath()));
            return false;
        }
    }

    @Override
    public boolean removeLocation(
            Initiator initiator, String locationName) {
        try {
            int removed = this.getDisposableTransaction()
                    .doUpdateVarargParams(
                            "DELETE FROM locations " +
                            "WHERE loc_name IS ? ", 
                            locationName);
            return ( removed > 0 );
        } catch (TransactionHandledSQLException ex) {
            logError(this.getClass(), ex);
            this.ioEngine.report(
                    initiator, format("Location removing by '%s' failed.", locationName));
            return false;
        }
    }

    @Override
    public boolean editLocationPath(
            Initiator initiator, String locationName, String newPath) {
        try (JdbcTransaction transact = this.getTransaction()) {
            
            int modified = transact
                    .doUpdateVarargParams(
                            "UPDATE locations " +
                            "SET loc_path = ? " +
                            "WHERE loc_name IS ? ", 
                            newPath, locationName);
            
            transact
                    .ifTrue( modified != 1 )
                    .rollbackAndProceed();
            
            return ( modified == 1 );
        } catch (TransactionHandledSQLException ex) {
            logError(this.getClass(), ex);
            this.ioEngine.reportMessage(
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
        try (JdbcTransaction transact = this.getTransaction()) {
            
            int modified = transact
                    .doUpdateVarargParams(
                            "UPDATE locations " +
                            "SET loc_name = ? " +
                            "WHERE loc_name IS ? ", 
                            newName, locationName);
            
            transact
                    .ifTrue( modified != 1 )
                    .rollbackAndProceed();
            
            return ( modified == 1 );
        } catch (TransactionHandledSQLException ex) {
            logError(this.getClass(), ex);
            this.ioEngine.reportMessage(
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
        try (JdbcTransaction transact = this.getTransaction()) {
            
            List<Location> locationsToModify = transact
                    .doQueryAndStreamVarargParams(
                            "SELECT loc_name, loc_path " +
                            "FROM locations " +
                            "WHERE LOWER(loc_path) LIKE ? ", 
                            this.rowToLocationConversion, 
                            Location.class, 
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
                                            location.getName()))
                                    .collect(toSet()))
            ).sum();
                
            return ( modified > 0 );            
        } catch (TransactionHandledSQLException ex) {
            logError(this.getClass(), ex);
            this.ioEngine.reportMessage(
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
            return this.getDisposableTransaction()
                    .doQueryAndStream(
                            "SELECT loc_name, loc_path " +
                            "FROM locations", 
                            this.rowToLocationConversion, 
                            Location.class)
                    .collect(toList());
        } catch (TransactionHandledSQLException ex) {
            logError(this.getClass(), ex);
            this.ioEngine.report(initiator, "All locations obtaining failed.");
            return emptyList();
        }
    }
}
