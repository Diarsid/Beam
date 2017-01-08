/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package diarsid.beam.core.modules.domain.keepers;

import java.util.List;
import java.util.Optional;

import diarsid.beam.core.control.io.base.Initiator;
import diarsid.beam.core.control.io.base.InnerIoEngine;
import diarsid.beam.core.control.io.base.VariantAnswer;
import diarsid.beam.core.control.io.base.VariantsQuestion;
import diarsid.beam.core.control.io.commands.EditEntityCommand;
import diarsid.beam.core.control.io.commands.FindEntityCommand;
import diarsid.beam.core.control.io.commands.RemoveEntityCommand;
import diarsid.beam.core.control.io.commands.creation.CreateLocationCommand;
import diarsid.beam.core.domain.entities.Location;
import diarsid.beam.core.modules.data.DaoLocations;
import diarsid.beam.core.modules.domain.CommandConsistencyChecker;
import diarsid.beam.core.modules.domain.LocationsKeeper;

import static diarsid.beam.core.control.io.interpreter.ControlKeys.hasWildcard;
import static diarsid.beam.core.util.CollectionsUtils.containsOne;
import static diarsid.beam.core.util.CollectionsUtils.getOne;
import static diarsid.beam.core.util.StringUtils.splitByWildcard;


public class LocationsKeeperWorker implements LocationsKeeper {
    
    private final DaoLocations dao;
    private final InnerIoEngine ioEngine;
    private final CommandConsistencyChecker consistencyChecker;
    
    public LocationsKeeperWorker(
            DaoLocations dao, 
            InnerIoEngine ioEngine, 
            CommandConsistencyChecker consistencyChecker) {
        this.dao = dao;
        this.ioEngine = ioEngine;
        this.consistencyChecker = consistencyChecker;
    }
    
    @Override
    public Optional<Location> getLocation(Initiator initiator, String locationNamePattern) {
        return this.findExactlyOneLocation(locationNamePattern, initiator);
    }

    @Override
    public Optional<Location> findLocation(Initiator initiator, FindEntityCommand command) {
        if ( this.consistencyChecker.check(command, initiator) ) {
            return this.findExactlyOneLocation(command.getArg(), initiator);
        } else {
            return Optional.empty();
        }
    }

    private Optional<Location> findExactlyOneLocation(
            String locationNamePattern, Initiator initiator) {
        List<Location> locations = this.getMatchingLocationsBy(locationNamePattern, initiator);
        if ( containsOne(locations) ) {
            return Optional.of(getOne(locations));
        } else if ( locations.size() > 1 ) {
            VariantAnswer answer = this.ioEngine.resolveVariants(
                    initiator, new VariantsQuestion("choose location", locations));
            if ( answer.isPresent() ) {
                return locations
                        .stream()
                        .filter(location -> location.getName().equals(answer.get().getText()))
                        .findFirst();
            } else {
                return Optional.empty();
            }
        } else {
            return Optional.empty();
        }
    }

    private List<Location> getMatchingLocationsBy(
            String locationNamePattern, Initiator initiator) {
        if ( hasWildcard(locationNamePattern) ) {
            return this.dao.getLocationsByNameParts(
                    initiator, splitByWildcard(locationNamePattern));
        } else {
            return this.dao.getLocationsByName(
                    initiator, locationNamePattern);
        }
    }

    @Override
    public boolean createLocation(Initiator initiator, CreateLocationCommand command) {
        if ( this.consistencyChecker.check(command, initiator) ) {
            return this.dao.saveNewLocation(
                    initiator, 
                    new Location(
                            command.getName(), 
                            command.getPath()));
        } else {
            return false;
        }
    }

    @Override
    public boolean removeLocation(Initiator initiator, RemoveEntityCommand command) {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    @Override
    public boolean editLocation(Initiator initiator, EditEntityCommand command) {
        if ( this.consistencyChecker.check(initiator, command) ) {
            Optional<Location> location = this.findExactlyOneLocation(
                    command.getName(), initiator);
            if ( location.isPresent() ) {
                switch ( command.getTarget() ) {
                    case TARGET_NAME : {
                        String newName = this.ioEngine.askForInput(initiator, "new name");
                        if ( newName.isEmpty() ) {
                            return false;
                        }
                        // todo validate name
                        return this.dao.editLocationName(
                                initiator, location.get().getName(), newName);
                    }
                    case TARGET_PATH : {
                        String newPath = this.ioEngine.askForInput(initiator, "new path");
                        if ( newPath.isEmpty() ) {
                            return false;
                        }
                        // todo validate path
                        return this.dao.editLocationPath(
                                initiator, location.get().getName(), newPath);
                    }
                    default : {
                        return false;
                    }
                }
            } else {
                return false;
            }            
        } else {
            return false;
        }
    }

    @Override
    public boolean replaceInPaths(Initiator initiator, String replaceable, String replacement) {
        
        return this.dao.replaceInPaths(initiator, replaceable, replacement);
    }

    @Override
    public List<Location> getAllLocations(Initiator initiator) {
        throw new UnsupportedOperationException("Not supported yet.");
    }
}
