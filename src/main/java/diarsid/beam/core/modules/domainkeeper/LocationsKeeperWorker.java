/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and openAsync the template in the editor.
 */

package diarsid.beam.core.modules.domainkeeper;

import java.util.List;
import java.util.Optional;

import diarsid.beam.core.base.control.flow.ReturnOperation;
import diarsid.beam.core.base.control.flow.VoidOperation;
import diarsid.beam.core.base.control.io.base.actors.Initiator;
import diarsid.beam.core.base.control.io.base.actors.InnerIoEngine;
import diarsid.beam.core.base.control.io.base.interaction.Answer;
import diarsid.beam.core.base.control.io.base.interaction.Question;
import diarsid.beam.core.base.control.io.commands.ArgumentsCommand;
import diarsid.beam.core.domain.entities.Location;
import diarsid.beam.core.domain.entities.metadata.EntityProperty;
import diarsid.beam.core.domain.inputparsing.common.PropertyAndText;
import diarsid.beam.core.domain.inputparsing.common.PropertyAndTextParser;
import diarsid.beam.core.domain.inputparsing.locations.LocationNameAndPath;
import diarsid.beam.core.domain.inputparsing.locations.LocationsInputParser;
import diarsid.beam.core.modules.data.DaoLocations;

import static diarsid.beam.core.base.control.flow.Operations.ok;
import static diarsid.beam.core.base.control.flow.Operations.okWith;
import static diarsid.beam.core.base.control.flow.Operations.returnOperationFail;
import static diarsid.beam.core.base.control.flow.Operations.successEmpty;
import static diarsid.beam.core.base.control.flow.Operations.voidOperationFail;
import static diarsid.beam.core.base.control.flow.Operations.voidOperationStopped;
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
import static diarsid.beam.core.domain.entities.validation.ValidationRule.ENTITY_NAME;
import static diarsid.beam.core.domain.entities.validation.ValidationRule.LOCAL_DIRECTORY_PATH;



class LocationsKeeperWorker implements LocationsKeeper {
    
    private final DaoLocations dao;
    private final InnerIoEngine ioEngine;
    private final KeeperDialogHelper helper;
    private final LocationsInputParser locationInpurParser;
    private final PropertyAndTextParser propertyTextParser;
    
    LocationsKeeperWorker(
            DaoLocations dao, 
            InnerIoEngine ioEngine, 
            KeeperDialogHelper consistencyChecker,
            LocationsInputParser parser,
            PropertyAndTextParser propertyTextParser) {
        this.dao = dao;
        this.ioEngine = ioEngine;
        this.helper = consistencyChecker;
        this.locationInpurParser = parser;
        this.propertyTextParser = propertyTextParser;
    }
    
    @Override
    public Optional<Location> getLocationByExactName(
            Initiator initiator, String exactName) {
        return this.dao.getLocationByExactName(initiator, exactName);
    }
    
    @Override
    public List<Location> getLocationsByNamePattern(
            Initiator initiator, String namePattern) {
        return this.getMatchingLocationsBy(initiator, namePattern);
    }
    
    @Override
    public Optional<Location> getLocationByNamePattern(
            Initiator initiator, String locationNamePattern) {
        return this.findExactlyOneLocationByPattern(initiator, locationNamePattern);
    }
    
    @Override
    public ReturnOperation<Location> findLocation(
            Initiator initiator, ArgumentsCommand command) {
        if ( command.type().isNot(FIND_LOCATION) ) {
            return returnOperationFail("wrong command type!");
        }
        String namePattern = "";
        if ( command.hasArguments()) {
            namePattern = command.getFirstArg();
        } 
        
        namePattern = this.helper.validateInteractively(initiator, namePattern, "name", ENTITY_NAME);
        if ( namePattern.isEmpty() ) {
            return successEmpty();
        } else {
            return okWith(this.findExactlyOneLocationByPattern(initiator, namePattern));
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
            return Optional.of(locations.get(answer.index()));
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
    public VoidOperation createLocation(Initiator initiator, ArgumentsCommand command) {
        if ( command.type().isNot(CREATE_LOCATION) ) {
            return voidOperationFail("wrong command type!");
        }
        
        String name;
        String path;
        if ( command.hasArguments() ) { 
            LocationNameAndPath nameAndPath = this.locationInpurParser.parse(command.arguments());
            name = nameAndPath.getName();
            path = nameAndPath.getPath();
        } else {
            name = "";
            path = "";
        }
        
        name = this.helper.validateEntityNameInteractively(initiator, name);
        if ( name.isEmpty() ) {
            return voidOperationStopped();
        }
        
        path = this.helper.validateInteractively(initiator, path, "path", LOCAL_DIRECTORY_PATH);
        if ( path.isEmpty() ) {
            return voidOperationStopped();
        }
            
        boolean nameIsNotValidOrFree = true;
        while ( nameIsNotValidOrFree ) {
            if ( this.dao.isNameFree(initiator, name) ) {
                nameIsNotValidOrFree = false;
            } else {
                this.ioEngine.report(initiator, "this name is not free!");
                name = this.ioEngine.askInput(initiator, "name");
                if ( name.isEmpty() ) {
                    return voidOperationStopped();
                }
                name = this.helper.validateEntityNameInteractively(initiator, name);
                if ( name.isEmpty() ) {
                    return voidOperationStopped();
                } else {
                    nameIsNotValidOrFree = false;
                }                    
            }
        }

        if ( this.dao.saveNewLocation(initiator, new Location(name, path)) ) {
            return ok();
        } else {
            return voidOperationFail("DAO failed to save Location.");
        }     
    }

    @Override
    public VoidOperation removeLocation(Initiator initiator, ArgumentsCommand command) {
        if ( command.type().isNot(DELETE_LOCATION) ) {
            return voidOperationFail("wrong command type!");
        }
        
        String name;
        if ( command.hasArguments()) {
            name = command.getFirstArg();
        } else {
            name = "";
        }
        
        name = this.helper.validateEntityNameInteractively(initiator, name);
        if ( name.isEmpty() ) {
            return voidOperationStopped();
        }
        
        List<Location> locationsToRemove = this.getMatchingLocationsBy(initiator, name);
        if ( hasOne(locationsToRemove) ) {
            String locationName = getOne(locationsToRemove).getName();
            if ( this.dao.removeLocation(initiator, locationName) ) {
                return ok();
            } else {
                return voidOperationFail("DAO failed to remove location.");
            }
        } else if ( locationsToRemove.isEmpty() ) {
            return voidOperationFail("no such location.");
        } else {
            Question question = question("choose").withAnswerEntities(locationsToRemove);
            Answer answer = this.ioEngine.ask(initiator, question);
            if ( answer.isGiven() ) {
                String locationName = locationsToRemove.get(answer.index()).getName();
                if ( this.dao.removeLocation(initiator, locationName) ) {
                    return ok();
                } else {
                    return voidOperationFail("DAO failed to remove location.");
                }                    
            } else {
                return voidOperationStopped();
            }
        }        
    }

    @Override
    public VoidOperation editLocation(Initiator initiator, ArgumentsCommand command) {
        if ( command.type().isNot(EDIT_LOCATION) ) {
            return voidOperationFail("wrong command type!");
        }
        
        String name;
        EntityProperty property;
        if ( command.hasArguments() ) {
            PropertyAndText propText = this.propertyTextParser.parse(command.arguments());
            name = propText.text();
            property = propText.property();
        } else {
            name = "";
            property = PROPERTY_UNDEFINED;
        }        
        
        property = this.helper.validatePropertyInteractively(
                initiator, property, NAME, FILE_URL);
        if ( property.isNotDefined() ) {
            return voidOperationStopped();
        }
        
        name = this.helper.validateEntityNameInteractively(initiator, name);
        if ( name.isEmpty() ) {
            return voidOperationStopped();
        }
        
        Optional<Location> location = this.findExactlyOneLocationByPattern(initiator, name);
        if ( location.isPresent() ) {
            switch ( property ) {
                case NAME : {
                    String newName = this.ioEngine.askInput(initiator, "new name");                    
                    if ( newName.isEmpty() ) {
                        return voidOperationStopped();
                    }
                    newName = this.helper.validateEntityNameInteractively(initiator, newName);
                    if ( newName.isEmpty() ) {
                        return voidOperationStopped();
                    }
                    if ( this.dao.editLocationName(initiator, location.get().getName(), newName) ) {
                        return ok();
                    } else {
                        return voidOperationFail("DAO failed to edit name.");
                    }
                }
                case FILE_URL : {
                    String newPath = this.ioEngine.askInput(initiator, "new path");
                    if ( newPath.isEmpty() ) {
                        return voidOperationStopped();
                    }
                    newPath = this.helper.validateInteractively(
                            initiator, newPath, "new path", LOCAL_DIRECTORY_PATH);
                    if ( newPath.isEmpty() ) {
                        return voidOperationStopped();
                    }
                    if ( this.dao.editLocationPath(initiator, location.get().getName(), newPath) ) {
                        return ok();
                    } else {
                        return voidOperationFail("DAO failed to edit path.");
                    }
                }
                default : {
                    return voidOperationFail("unexpected property.");
                }
            }
        } else {
            return voidOperationFail("no such location.");
        }          
    }

    @Override
    public VoidOperation replaceInPaths(
            Initiator initiator, String replaceable, String replacement) {        
        if ( this.dao.replaceInPaths(initiator, replaceable, replacement) ) {
            return ok();
        } else {
            return voidOperationFail("DAO failed to replace path fragment.");
        }
    }

    @Override
    public List<Location> getAllLocations(Initiator initiator) {
        return this.dao.getAllLocations(initiator);
    }
}
