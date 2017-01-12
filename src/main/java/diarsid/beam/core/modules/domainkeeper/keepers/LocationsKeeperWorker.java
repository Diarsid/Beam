/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package diarsid.beam.core.modules.domainkeeper.keepers;

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
import diarsid.beam.core.modules.domainkeeper.KeeperDialogHelper;
import diarsid.beam.core.modules.domainkeeper.LocationsKeeper;

import static diarsid.beam.core.control.io.commands.CommandType.DELETE_LOCATION;
import static diarsid.beam.core.control.io.commands.CommandType.EDIT_LOCATION;
import static diarsid.beam.core.control.io.commands.CommandType.FIND_LOCATION;
import static diarsid.beam.core.control.io.commands.EditableTarget.TARGET_NAME;
import static diarsid.beam.core.control.io.commands.EditableTarget.TARGET_PATH;
import static diarsid.beam.core.control.io.interpreter.ControlKeys.hasWildcard;
import static diarsid.beam.core.domain.entities.validation.ValidationRule.ENTITY_NAME;
import static diarsid.beam.core.domain.entities.validation.ValidationRule.LOCAL_DIRECTORY_PATH;
import static diarsid.beam.core.util.CollectionsUtils.containsOne;
import static diarsid.beam.core.util.CollectionsUtils.getOne;
import static diarsid.beam.core.util.StringUtils.splitByWildcard;


public class LocationsKeeperWorker implements LocationsKeeper {
    
    private final DaoLocations dao;
    private final InnerIoEngine ioEngine;
    private final KeeperDialogHelper checker;
    
    public LocationsKeeperWorker(
            DaoLocations dao, 
            InnerIoEngine ioEngine, 
            KeeperDialogHelper consistencyChecker) {
        this.dao = dao;
        this.ioEngine = ioEngine;
        this.checker = consistencyChecker;
    }

    private Optional<Location> getLocationUsingAnswer(
            List<Location> locationsToRemove, VariantAnswer answer) {
        return locationsToRemove
                .stream()
                .filter(location -> location.getName().equals(answer.get().getText()))
                .findFirst();
    }
    
    @Override
    public Optional<Location> getLocationByExactName(Initiator initiator, String exactName) {
        return this.dao.getLocationByExactName(initiator, exactName);
    }
    
    @Override
    public List<Location> getLocationsByNamePattern(Initiator initiator, String namePattern) {
        return this.getMatchingLocationsBy(namePattern, initiator);
    }
    
    @Override
    public Optional<Location> getLocationByNamePattern(Initiator initiator, String locationNamePattern) {
        return this.findExactlyOneLocationByPattern(locationNamePattern, initiator);
    }

    @Override
    public Optional<Location> findLocation(Initiator initiator, FindEntityCommand command) {
        if ( this.checker.checkFinding(initiator, command, FIND_LOCATION) ) {
            return this.findExactlyOneLocationByPattern(command.getArg(), initiator);
        } else {
            return Optional.empty();
        }
    }

    private Optional<Location> findExactlyOneLocationByPattern(
            String locationNamePattern, Initiator initiator) {
        List<Location> locations = this.getMatchingLocationsBy(locationNamePattern, initiator);
        if ( containsOne(locations) ) {
            return Optional.of(getOne(locations));
        } else if ( locations.size() > 1 ) {
            VariantAnswer answer = this.ioEngine.resolveVariants(
                    initiator, new VariantsQuestion("choose location", locations));
            if ( answer.isPresent() ) {
                return this.getLocationUsingAnswer(locations, answer);
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
            return this.dao.getLocationsByNamePatternParts(
                    initiator, splitByWildcard(locationNamePattern));
        } else {
            return this.dao.getLocationsByNamePattern(
                    initiator, locationNamePattern);
        }
    }

    @Override
    public boolean createLocation(Initiator initiator, CreateLocationCommand command) {
        if ( this.checker.check(initiator, command) ) {
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
        if ( this.checker.checkDeletion(initiator, command, DELETE_LOCATION) ) {
            List<Location> locationsToRemove = 
                    this.getMatchingLocationsBy(command.getArg(), initiator);
            if ( containsOne(locationsToRemove) ) {
                return this.dao.removeLocation(initiator, getOne(locationsToRemove).getName());
            } else if ( locationsToRemove.isEmpty() ) {
                this.ioEngine.report(initiator, "no such locations.");
                return false;
            } else {
                VariantAnswer answer = this.ioEngine.resolveVariants(
                        initiator, new VariantsQuestion("choose location", locationsToRemove));
                if ( answer.isPresent() ) {
                    return this.dao.removeLocation(
                            initiator, 
                            this.getLocationUsingAnswer(locationsToRemove, answer)
                                    .get().getName());                    
                } else {
                    return false;
                }
            }
        } else {
            return false;
        }
    }

    @Override
    public boolean editLocation(Initiator initiator, EditEntityCommand command) {
        if ( this.checker.checkEdition(
                initiator, command, EDIT_LOCATION, TARGET_NAME, TARGET_PATH) ) {
            Optional<Location> location = this.findExactlyOneLocationByPattern(
                    command.getName(), initiator);
            if ( location.isPresent() ) {
                switch ( command.getTarget() ) {
                    case TARGET_NAME : {
                        String newName = this.ioEngine.askForInput(initiator, "new name");
                        if ( newName.isEmpty() ) {
                            return false;
                        }
                        newName = this.checker.checkArgumentValidity(
                                initiator, newName, "new name", ENTITY_NAME);
                        return this.dao.editLocationName(
                                initiator, location.get().getName(), newName);
                    }
                    case TARGET_PATH : {
                        String newPath = this.ioEngine.askForInput(initiator, "new path");
                        if ( newPath.isEmpty() ) {
                            return false;
                        }
                        newPath = this.checker.checkArgumentValidity(
                                initiator, newPath, "new path", LOCAL_DIRECTORY_PATH);
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
        return this.dao.getAllLocations(initiator);
    }
}
