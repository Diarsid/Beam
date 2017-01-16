/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package diarsid.beam.core.modules.domainkeeper.keepers;

import java.util.List;
import java.util.Optional;

import diarsid.beam.core.control.io.base.Answer;
import diarsid.beam.core.control.io.base.Initiator;
import diarsid.beam.core.control.io.base.InnerIoEngine;
import diarsid.beam.core.control.io.commands.EditEntityCommand;
import diarsid.beam.core.control.io.commands.FindEntityCommand;
import diarsid.beam.core.control.io.commands.RemoveEntityCommand;
import diarsid.beam.core.control.io.commands.creation.CreateLocationCommand;
import diarsid.beam.core.domain.entities.Location;
import diarsid.beam.core.modules.data.DaoLocations;
import diarsid.beam.core.modules.domainkeeper.KeeperDialogHelper;
import diarsid.beam.core.modules.domainkeeper.LocationsKeeper;

import static diarsid.beam.core.control.io.base.Question.questionWithEntites;
import static diarsid.beam.core.control.io.commands.CommandType.DELETE_LOCATION;
import static diarsid.beam.core.control.io.commands.CommandType.EDIT_LOCATION;
import static diarsid.beam.core.control.io.commands.CommandType.FIND_LOCATION;
import static diarsid.beam.core.control.io.commands.EditableTarget.TARGET_NAME;
import static diarsid.beam.core.control.io.commands.EditableTarget.TARGET_PATH;
import static diarsid.beam.core.control.io.interpreter.ControlKeys.hasWildcard;
import static diarsid.beam.core.domain.entities.validation.ValidationRule.LOCAL_DIRECTORY_PATH;
import static diarsid.beam.core.util.CollectionsUtils.getOne;
import static diarsid.beam.core.util.CollectionsUtils.hasMany;
import static diarsid.beam.core.util.CollectionsUtils.hasOne;
import static diarsid.beam.core.util.StringUtils.splitByWildcard;


public class LocationsKeeperWorker implements LocationsKeeper {
    
    private final DaoLocations dao;
    private final InnerIoEngine ioEngine;
    private final KeeperDialogHelper helper;
    
    public LocationsKeeperWorker(
            DaoLocations dao, 
            InnerIoEngine ioEngine, 
            KeeperDialogHelper consistencyChecker) {
        this.dao = dao;
        this.ioEngine = ioEngine;
        this.helper = consistencyChecker;
    }

    private Optional<Location> getLocationUsingAnswer(
            List<Location> locationsToRemove, Answer answer) {
        return locationsToRemove
                .stream()
                .filter(location -> location.getName().equals(answer.getText()))
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
        if ( this.helper.checkFinding(initiator, command, FIND_LOCATION) ) {
            return this.findExactlyOneLocationByPattern(command.getArg(), initiator);
        } else {
            return Optional.empty();
        }
    }

    private Optional<Location> findExactlyOneLocationByPattern(
            String locationNamePattern, Initiator initiator) {
        List<Location> locations = this.getMatchingLocationsBy(locationNamePattern, initiator);
        if ( hasOne(locations) ) {
            return Optional.of(getOne(locations));
        } else if ( hasMany(locations) ) {
            return this.manageWithManyLocations(initiator, locations);
        } else {
            return Optional.empty();
        }
    }

    private Optional<Location> manageWithManyLocations(Initiator initiator, List<Location> locations) {
        Answer answer = this.ioEngine.ask(
                initiator, questionWithEntites("choose location", locations));
        if ( answer.isGiven() ) {
            return Optional.of(locations.get(answer.getIndex()));
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
        if ( this.helper.check(initiator, command) ) {
            
            boolean nameIsNotValidOrFree = true;
            String alternativeName = "";
            while ( nameIsNotValidOrFree ) {
                if ( this.dao.isNameFree(initiator, command.getName()) ) {
                    nameIsNotValidOrFree = false;
                } else {
                    this.ioEngine.report(initiator, "this name is not free!");
                    alternativeName = this.ioEngine.askInput(initiator, "name");
                    if ( alternativeName.isEmpty() ) {
                        return false;
                    }
                    alternativeName = this.helper.validateEntityNameInteractively(
                            initiator, alternativeName);
                    if ( alternativeName.isEmpty() ) {
                        return false;
                    } else {
                        command.resetName(alternativeName);
                    }                    
                }
            }
            
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
        if ( this.helper.checkDeletion(initiator, command, DELETE_LOCATION) ) {
            List<Location> locationsToRemove = 
                    this.getMatchingLocationsBy(command.getArg(), initiator);
            if ( hasOne(locationsToRemove) ) {
                return this.dao.removeLocation(initiator, getOne(locationsToRemove).getName());
            } else if ( locationsToRemove.isEmpty() ) {
                this.ioEngine.report(initiator, "no such locations.");
                return false;
            } else {
                Answer answer = this.ioEngine.ask(
                        initiator, questionWithEntites("choose location", locationsToRemove));
                if ( answer.isGiven() ) {
                    return this.dao.removeLocation(
                            initiator, 
                            locationsToRemove.get(answer.getIndex()).getName());                    
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
        if ( this.helper.checkEdition(
                initiator, command, EDIT_LOCATION, TARGET_NAME, TARGET_PATH) ) {
            Optional<Location> location = this.findExactlyOneLocationByPattern(
                    command.getName(), initiator);
            if ( location.isPresent() ) {
                switch ( command.getTarget() ) {
                    case TARGET_NAME : {
                        String newName = this.ioEngine.askInput(initiator, "new name");
                        if ( newName.isEmpty() ) {
                            return false;
                        }
                        newName = this.helper.validateEntityNameInteractively(
                                initiator, newName);
                        return this.dao.editLocationName(
                                initiator, location.get().getName(), newName);
                    }
                    case TARGET_PATH : {
                        String newPath = this.ioEngine.askInput(initiator, "new path");
                        if ( newPath.isEmpty() ) {
                            return false;
                        }
                        newPath = this.helper.validateInteractively(
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
