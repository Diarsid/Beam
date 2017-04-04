/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and openAsync the template in the editor.
 */

package diarsid.beam.core.modules.domainkeeper;

import java.util.List;
import java.util.Optional;
import java.util.Set;

import diarsid.beam.core.base.control.flow.ValueOperation;
import diarsid.beam.core.base.control.flow.VoidOperation;
import diarsid.beam.core.base.control.io.base.actors.Initiator;
import diarsid.beam.core.base.control.io.base.actors.InnerIoEngine;
import diarsid.beam.core.base.control.io.base.interaction.Answer;
import diarsid.beam.core.base.control.io.base.interaction.Question;
import diarsid.beam.core.base.control.io.commands.ArgumentsCommand;
import diarsid.beam.core.base.control.io.commands.CommandType;
import diarsid.beam.core.base.control.io.commands.InvocationEntityCommand;
import diarsid.beam.core.domain.entities.Location;
import diarsid.beam.core.domain.entities.metadata.EntityProperty;
import diarsid.beam.core.domain.inputparsing.common.PropertyAndText;
import diarsid.beam.core.domain.inputparsing.common.PropertyAndTextParser;
import diarsid.beam.core.domain.inputparsing.locations.LocationNameAndPath;
import diarsid.beam.core.domain.inputparsing.locations.LocationsInputParser;
import diarsid.beam.core.modules.data.DaoLocations;

import static java.lang.String.format;

import static diarsid.beam.core.base.control.flow.Operations.successEmpty;
import static diarsid.beam.core.base.control.flow.Operations.valueFound;
import static diarsid.beam.core.base.control.flow.Operations.valueOperationFail;
import static diarsid.beam.core.base.control.flow.Operations.voidCompleted;
import static diarsid.beam.core.base.control.flow.Operations.voidOperationFail;
import static diarsid.beam.core.base.control.flow.Operations.voidOperationStopped;
import static diarsid.beam.core.base.control.io.base.interaction.Question.question;
import static diarsid.beam.core.base.control.io.commands.CommandType.CREATE_LOCATION;
import static diarsid.beam.core.base.control.io.commands.CommandType.DELETE_LOCATION;
import static diarsid.beam.core.base.control.io.commands.CommandType.EDIT_LOCATION;
import static diarsid.beam.core.base.control.io.commands.CommandType.FIND_LOCATION;
import static diarsid.beam.core.base.control.io.commands.CommandType.OPEN_LOCATION;
import static diarsid.beam.core.base.control.io.commands.CommandType.OPEN_PATH;
import static diarsid.beam.core.base.control.io.interpreter.ControlKeys.hasWildcard;
import static diarsid.beam.core.base.util.CollectionsUtils.getOne;
import static diarsid.beam.core.base.util.CollectionsUtils.hasMany;
import static diarsid.beam.core.base.util.CollectionsUtils.hasOne;
import static diarsid.beam.core.base.util.CollectionsUtils.toSet;
import static diarsid.beam.core.base.util.StringUtils.splitByWildcard;
import static diarsid.beam.core.domain.entities.metadata.EntityProperty.FILE_URL;
import static diarsid.beam.core.domain.entities.metadata.EntityProperty.NAME;
import static diarsid.beam.core.domain.entities.metadata.EntityProperty.UNDEFINED_PROPERTY;
import static diarsid.beam.core.domain.entities.validation.ValidationRule.ENTITY_NAME_RULE;
import static diarsid.beam.core.domain.entities.validation.ValidationRule.LOCAL_DIRECTORY_PATH_RULE;



class LocationsKeeperWorker 
        implements 
                LocationsKeeper, 
                NamedEntitiesKeeper {
    
    private final DaoLocations dao;
    private final InnerIoEngine ioEngine;
    private final KeeperDialogHelper helper;
    private final LocationsInputParser locationInpurParser;
    private final PropertyAndTextParser propertyTextParser;
    private final Set<CommandType> subjectedCommandTypes;
    
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
        this.subjectedCommandTypes = toSet(OPEN_LOCATION, OPEN_PATH);
    }

    @Override
    public boolean isSubjectedTo(InvocationEntityCommand command) {
        return this.subjectedCommandTypes.contains(command.type());
    }
    
    @Override
    public Optional<Location> findByExactName(
            Initiator initiator, String exactName) {
        return this.dao.getLocationByExactName(initiator, exactName);
    }
    
    @Override
    public List<Location> getLocationsByNamePattern(
            Initiator initiator, String namePattern) {
        return this.getMatchingLocationsBy(initiator, namePattern);
    }
    
    @Override
    public Optional<Location> findByNamePattern(
            Initiator initiator, String locationNamePattern) {
        return this.findExactlyOneLocationByPattern(initiator, locationNamePattern);
    }
    
    @Override
    public ValueOperation<Location> findLocation(
            Initiator initiator, ArgumentsCommand command) {
        if ( command.type().isNot(FIND_LOCATION) ) {
            return valueOperationFail("wrong command type!");
        }
        String namePattern = "";
        if ( command.hasArguments()) {
            namePattern = command.getFirstArg();
        } 
        
        namePattern = this.helper.validateInteractively(initiator, namePattern, "name", ENTITY_NAME_RULE);
        if ( namePattern.isEmpty() ) {
            return successEmpty();
        } else {
            return valueFound(this.findExactlyOneLocationByPattern(initiator, namePattern));
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
        // TODO 
        // employ more sofisticated choice algorithm
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
        // TODO
        // employ advanced search algorithm
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
        
        path = this.helper.validateInteractively(initiator, path, "path", LOCAL_DIRECTORY_PATH_RULE);
        if ( path.isEmpty() ) {
            return voidOperationStopped();
        }        
        
        name = this.helper.validateEntityNameInteractively(initiator, name);
        if ( name.isEmpty() ) {
            return voidOperationStopped();
        }    
        nameDefining: while ( true ) {
            if ( this.dao.isNameFree(initiator, name) ) {
                break nameDefining;
            } else {
                this.ioEngine.report(initiator, "this name is not free!");
                name = this.ioEngine.askInput(initiator, "name");
                if ( name.isEmpty() ) {
                    return voidOperationStopped();
                }
                name = this.helper.validateEntityNameInteractively(initiator, name);
                if ( name.isEmpty() ) {
                    return voidOperationStopped();
                }                    
            }
        }

        if ( this.dao.saveNewLocation(initiator, new Location(name, path)) ) {
            return voidCompleted();
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
            String locationName = getOne(locationsToRemove).name();
            this.ioEngine.report(initiator, format("'%s' found.", locationName));
            if ( this.dao.removeLocation(initiator, locationName) ) {
                return voidCompleted();
            } else {
                return voidOperationFail("DAO failed to remove location.");
            }
        } else if ( locationsToRemove.isEmpty() ) {
            return voidOperationFail("no such location.");
        } else {
            Question question = question("choose").withAnswerEntities(locationsToRemove);
            Answer answer = this.ioEngine.ask(initiator, question);
            if ( answer.isGiven() ) {
                String locationName = locationsToRemove.get(answer.index()).name();
                if ( this.dao.removeLocation(initiator, locationName) ) {
                    return voidCompleted();
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
            property = UNDEFINED_PROPERTY;
        }        
        
        name = this.helper.validateEntityNameInteractively(initiator, name);
        if ( name.isEmpty() ) {
            return voidOperationStopped();
        }
        
        Optional<Location> location = this.findExactlyOneLocationByPattern(initiator, name);
        if ( location.isPresent() ) {            
            this.ioEngine.report(initiator, format("'%s' found.", location.get().name()));
            
            property = this.helper.validatePropertyInteractively(
                    initiator, property, NAME, FILE_URL);
            if ( property.isUndefined() ) {
                return voidOperationStopped();
            }
        
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
                    if ( this.dao.editLocationName(initiator, location.get().name(), newName) ) {
                        return voidCompleted();
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
                            initiator, newPath, "new path", LOCAL_DIRECTORY_PATH_RULE);
                    if ( newPath.isEmpty() ) {
                        return voidOperationStopped();
                    }
                    if ( this.dao.editLocationPath(initiator, location.get().name(), newPath) ) {
                        return voidCompleted();
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
            return voidCompleted();
        } else {
            return voidOperationFail("DAO failed to replace path fragment.");
        }
    }

    @Override
    public List<Location> getAllLocations(Initiator initiator) {
        return this.dao.getAllLocations(initiator);
    }
}
