/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and openAsync the template in the editor.
 */

package diarsid.beam.core.modules.domainkeeper;

import java.util.List;
import java.util.Optional;

import diarsid.beam.core.base.control.flow.OperationFlow;
import diarsid.beam.core.base.control.io.base.actors.Initiator;
import diarsid.beam.core.base.control.io.base.actors.InnerIoEngine;
import diarsid.beam.core.base.control.io.base.interaction.Answer;
import diarsid.beam.core.base.control.io.base.interaction.Question;
import diarsid.beam.core.base.control.io.commands.MultiStringCommand;
import diarsid.beam.core.base.control.io.commands.SingleStringCommand;
import diarsid.beam.core.domain.entities.Location;
import diarsid.beam.core.domain.entities.metadata.EntityProperty;
import diarsid.beam.core.domain.inputparsing.locations.LocationNameAndPath;
import diarsid.beam.core.domain.inputparsing.locations.LocationsInputParser;
import diarsid.beam.core.modules.data.DaoLocations;

import static diarsid.beam.core.base.control.flow.Operations.operationFailedWith;
import static diarsid.beam.core.base.control.flow.Operations.operationStopped;
import static diarsid.beam.core.base.control.flow.Operations.success;
import static diarsid.beam.core.base.control.io.base.interaction.Question.question;
import static diarsid.beam.core.base.control.io.commands.CommandType.CREATE_LOCATION;
import static diarsid.beam.core.base.control.io.commands.CommandType.DELETE_LOCATION;
import static diarsid.beam.core.base.control.io.commands.CommandType.EDIT_LOCATION;
import static diarsid.beam.core.base.control.io.commands.CommandType.FIND_LOCATION;
import static diarsid.beam.core.base.control.io.interpreter.ControlKeys.hasWildcard;
import static diarsid.beam.core.base.util.CollectionsUtils.getOne;
import static diarsid.beam.core.base.util.CollectionsUtils.hasMany;
import static diarsid.beam.core.base.util.CollectionsUtils.hasOne;
import static diarsid.beam.core.base.util.StringUtils.splitByWildcard;
import static diarsid.beam.core.domain.entities.metadata.EntityProperty.FILE_URL;
import static diarsid.beam.core.domain.entities.metadata.EntityProperty.NAME;
import static diarsid.beam.core.domain.entities.metadata.EntityProperty.PROPERTY_UNDEFINED;
import static diarsid.beam.core.domain.entities.metadata.EntityProperty.argToProperty;
import static diarsid.beam.core.domain.entities.validation.ValidationRule.ENTITY_NAME;
import static diarsid.beam.core.domain.entities.validation.ValidationRule.LOCAL_DIRECTORY_PATH;



class LocationsKeeperWorker implements LocationsKeeper {
    
    private final DaoLocations dao;
    private final InnerIoEngine ioEngine;
    private final KeeperDialogHelper helper;
    private final LocationsInputParser parser;
    
    LocationsKeeperWorker(
            DaoLocations dao, 
            InnerIoEngine ioEngine, 
            KeeperDialogHelper consistencyChecker,
            LocationsInputParser parser) {
        this.dao = dao;
        this.ioEngine = ioEngine;
        this.helper = consistencyChecker;
        this.parser = parser;
    }
    
    @Override
    public Optional<Location> getLocationByExactName(Initiator initiator, String exactName) {
        return this.dao.getLocationByExactName(initiator, exactName);
    }
    
    @Override
    public List<Location> getLocationsByNamePattern(Initiator initiator, String namePattern) {
        return this.getMatchingLocationsBy(initiator, namePattern);
    }
    
    @Override
    public Optional<Location> getLocationByNamePattern(Initiator initiator, String locationNamePattern) {
        return this.findExactlyOneLocationByPattern(initiator, locationNamePattern);
    }
    
    @Override
    public Optional<Location> findLocation(Initiator initiator, SingleStringCommand command) {
        if ( command.type().isNot(FIND_LOCATION) ) {
            return Optional.empty();
        }
        String namePattern = "";
        if ( command.hasArg() ) {
            namePattern = command.getArg();
        } 
        
        namePattern = this.helper.validateInteractively(initiator, namePattern, "name", ENTITY_NAME);
        if ( namePattern.isEmpty() ) {
            return Optional.empty();
        } else {
            return this.findExactlyOneLocationByPattern(initiator, namePattern);
        }
    }

    private Optional<Location> findExactlyOneLocationByPattern(
            Initiator initiator, String locationNamePattern) {
        List<Location> locations = this.getMatchingLocationsBy(initiator, locationNamePattern);
        if ( hasOne(locations) ) {
            return Optional.of(getOne(locations));
        } else if ( hasMany(locations) ) {
            return this.manageWithManyLocations(initiator, locations);
        } else {
            return Optional.empty();
        }
    }

    private Optional<Location> manageWithManyLocations(
            Initiator initiator, List<Location> locations) {
        Answer answer = this.ioEngine.ask(
                initiator, question("choose").withAnswerEntities(locations));
        if ( answer.isGiven() ) {
            return Optional.of(locations.get(answer.getIndex()));
        } else {
            return Optional.empty();
        }
    }

    private List<Location> getMatchingLocationsBy(
            Initiator initiator, String locationNamePattern) {
        if ( hasWildcard(locationNamePattern) ) {
            return this.dao.getLocationsByNamePatternParts(
                    initiator, splitByWildcard(locationNamePattern));
        } else {
            return this.dao.getLocationsByNamePattern(
                    initiator, locationNamePattern);
        }
    }

    @Override
    public OperationFlow createLocation(Initiator initiator, MultiStringCommand command) {
        if ( command.type().isNot(CREATE_LOCATION) ) {
            return operationFailedWith("wrong command type!");
        }
        
        String name;
        String path;
        if ( command.hasArguments() ) { 
            LocationNameAndPath nameAndPath = this.parser.parse(command.arguments());
            name = nameAndPath.getName();
            path = nameAndPath.getPath();
        } else {
            name = "";
            path = "";
        }
        
        name = this.helper.validateEntityNameInteractively(initiator, name);
        if ( name.isEmpty() ) {
            return operationStopped();
        }
        
        path = this.helper.validateInteractively(initiator, path, "path", LOCAL_DIRECTORY_PATH);
        if ( path.isEmpty() ) {
            return operationStopped();
        }
            
        boolean nameIsNotValidOrFree = true;
        while ( nameIsNotValidOrFree ) {
            if ( this.dao.isNameFree(initiator, name) ) {
                nameIsNotValidOrFree = false;
            } else {
                this.ioEngine.report(initiator, "this name is not free!");
                name = this.ioEngine.askInput(initiator, "name");
                if ( name.isEmpty() ) {
                    return operationStopped();
                }
                name = this.helper.validateEntityNameInteractively(initiator, name);
                if ( name.isEmpty() ) {
                    return operationStopped();
                } else {
                    nameIsNotValidOrFree = false;
                }                    
            }
        }

        if ( this.dao.saveNewLocation(initiator, new Location(name, path)) ) {
            return success();
        } else {
            return operationFailedWith("DAO failed to save Location.");
        }     
    }

    @Override
    public OperationFlow removeLocation(Initiator initiator, SingleStringCommand command) {
        if ( command.type().isNot(DELETE_LOCATION) ) {
            return operationFailedWith("wrong command type!");
        }
        
        String name;
        if ( command.hasArg() ) {
            name = command.getArg();
        } else {
            name = "";
        }
        
        name = this.helper.validateEntityNameInteractively(initiator, name);
        if ( name.isEmpty() ) {
            return operationStopped();
        }
        
        List<Location> locationsToRemove = this.getMatchingLocationsBy(initiator, name);
        if ( hasOne(locationsToRemove) ) {
            String locationName = getOne(locationsToRemove).getName();
            if ( this.dao.removeLocation(initiator, locationName) ) {
                return success();
            } else {
                return operationFailedWith("DAO failed to remove location.");
            }
        } else if ( locationsToRemove.isEmpty() ) {
            return operationFailedWith("no such location.");
        } else {
            Question question = question("choose").withAnswerEntities(locationsToRemove);
            Answer answer = this.ioEngine.ask(initiator, question);
            if ( answer.isGiven() ) {
                String locationName = locationsToRemove.get(answer.getIndex()).getName();
                if ( this.dao.removeLocation(initiator, locationName) ) {
                    return success();
                } else {
                    return operationFailedWith("DAO failed to remove location.");
                }                    
            } else {
                return operationStopped();
            }
        }        
    }

    @Override
    public OperationFlow editLocation(Initiator initiator, SingleStringCommand command) {
        if ( command.type().isNot(EDIT_LOCATION) ) {
            return operationFailedWith("wrong command type!");
        }
        
        String name;
        EntityProperty property = PROPERTY_UNDEFINED;
        if ( command.hasArg() ) {
            property = argToProperty(command.getArg());
            if ( property.isNotDefined() ) {
                name = command.getArg();
            } else {
                name = "";
            }            
        } else {
            name = "";
        }        
        
        property = this.helper.validatePropertyInteractively(
                initiator, property, NAME, FILE_URL);
        if ( property.isNotDefined() ) {
            return operationStopped();
        }
        
        name = this.helper.validateEntityNameInteractively(initiator, name);
        if ( name.isEmpty() ) {
            return operationStopped();
        }
        
        Optional<Location> location = this.findExactlyOneLocationByPattern(initiator, name);
        if ( location.isPresent() ) {
            switch ( property ) {
                case NAME : {
                    String newName = this.ioEngine.askInput(initiator, "new name");                    
                    if ( newName.isEmpty() ) {
                        return operationStopped();
                    }
                    newName = this.helper.validateEntityNameInteractively(initiator, newName);
                    if ( newName.isEmpty() ) {
                        return operationStopped();
                    }
                    if ( this.dao.editLocationName(initiator, location.get().getName(), newName) ) {
                        return success();
                    } else {
                        return operationFailedWith("DAO failed to edit name.");
                    }
                }
                case FILE_URL : {
                    String newPath = this.ioEngine.askInput(initiator, "new path");
                    if ( newPath.isEmpty() ) {
                        return operationStopped();
                    }
                    newPath = this.helper.validateInteractively(
                            initiator, newPath, "new path", LOCAL_DIRECTORY_PATH);
                    if ( newPath.isEmpty() ) {
                        return operationStopped();
                    }
                    if ( this.dao.editLocationPath(initiator, location.get().getName(), newPath) ) {
                        return success();
                    } else {
                        return operationFailedWith("DAO failed to edit path.");
                    }
                }
                default : {
                    return operationFailedWith("unexpected property.");
                }
            }
        } else {
            return operationFailedWith("no such location.");
        }          
    }

    @Override
    public OperationFlow replaceInPaths(
            Initiator initiator, String replaceable, String replacement) {        
        if ( this.dao.replaceInPaths(initiator, replaceable, replacement) ) {
            return success();
        } else {
            return operationFailedWith("DAO failed to replace path fragment.");
        }
    }

    @Override
    public List<Location> getAllLocations(Initiator initiator) {
        return this.dao.getAllLocations(initiator);
    }
}
