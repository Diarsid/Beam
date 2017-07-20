/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and openAsync the template in the editor.
 */

package diarsid.beam.core.modules.domainkeeper;

import java.util.List;
import java.util.Set;

import diarsid.beam.core.base.control.flow.ValueOperation;
import diarsid.beam.core.base.control.flow.VoidOperation;
import diarsid.beam.core.base.control.io.base.actors.Initiator;
import diarsid.beam.core.base.control.io.base.actors.InnerIoEngine;
import diarsid.beam.core.base.control.io.base.interaction.Answer;
import diarsid.beam.core.base.control.io.base.interaction.VariantsQuestion;
import diarsid.beam.core.base.control.io.commands.ArgumentsCommand;
import diarsid.beam.core.base.control.io.commands.CommandType;
import diarsid.beam.core.base.control.io.commands.executor.InvocationCommand;
import diarsid.beam.core.base.control.io.commands.executor.OpenLocationCommand;
import diarsid.beam.core.domain.entities.Location;
import diarsid.beam.core.domain.entities.metadata.EntityProperty;
import diarsid.beam.core.domain.inputparsing.common.PropertyAndText;
import diarsid.beam.core.domain.inputparsing.common.PropertyAndTextParser;
import diarsid.beam.core.domain.inputparsing.locations.LocationNameAndPath;
import diarsid.beam.core.domain.inputparsing.locations.LocationsInputParser;
import diarsid.beam.core.domain.patternsanalyze.WeightedVariants;
import diarsid.beam.core.modules.data.DaoLocations;

import static java.lang.String.format;

import static diarsid.beam.core.base.control.flow.Operations.valueCompletedEmpty;
import static diarsid.beam.core.base.control.flow.Operations.valueCompletedWith;
import static diarsid.beam.core.base.control.flow.Operations.valueOperationFail;
import static diarsid.beam.core.base.control.flow.Operations.valueOperationStopped;
import static diarsid.beam.core.base.control.flow.Operations.voidCompleted;
import static diarsid.beam.core.base.control.flow.Operations.voidOperationFail;
import static diarsid.beam.core.base.control.flow.Operations.voidOperationStopped;
import static diarsid.beam.core.base.control.io.base.interaction.Variants.entitiesToVariants;
import static diarsid.beam.core.base.control.io.base.interaction.VariantsQuestion.question;
import static diarsid.beam.core.base.control.io.commands.CommandType.CREATE_LOCATION;
import static diarsid.beam.core.base.control.io.commands.CommandType.DELETE_LOCATION;
import static diarsid.beam.core.base.control.io.commands.CommandType.EDIT_LOCATION;
import static diarsid.beam.core.base.control.io.commands.CommandType.FIND_LOCATION;
import static diarsid.beam.core.base.control.io.commands.CommandType.OPEN_LOCATION;
import static diarsid.beam.core.base.control.io.commands.CommandType.OPEN_LOCATION_TARGET;
import static diarsid.beam.core.base.control.io.commands.executor.InvocationCommandLifePhase.NEW;
import static diarsid.beam.core.base.control.io.commands.executor.InvocationCommandTargetState.TARGET_FOUND;
import static diarsid.beam.core.base.util.CollectionsUtils.getOne;
import static diarsid.beam.core.base.util.CollectionsUtils.hasMany;
import static diarsid.beam.core.base.util.CollectionsUtils.hasOne;
import static diarsid.beam.core.base.util.CollectionsUtils.toSet;
import static diarsid.beam.core.base.util.ConcurrencyUtil.asyncDo;
import static diarsid.beam.core.domain.entities.metadata.EntityProperty.FILE_URL;
import static diarsid.beam.core.domain.entities.metadata.EntityProperty.NAME;
import static diarsid.beam.core.domain.entities.metadata.EntityProperty.UNDEFINED_PROPERTY;
import static diarsid.beam.core.domain.entities.validation.ValidationRule.LOCAL_DIRECTORY_PATH_RULE;
import static diarsid.beam.core.domain.patternsanalyze.Analyze.weightVariants;



class LocationsKeeperWorker 
        implements 
                LocationsKeeper, 
                NamedEntitiesKeeper {
    
    private final DaoLocations dao;
    private final CommandsMemoryKeeper commandsMemory;
    private final InnerIoEngine ioEngine;
    private final KeeperDialogHelper helper;
    private final LocationsInputParser locationInpurParser;
    private final PropertyAndTextParser propertyTextParser;
    private final Set<CommandType> subjectedCommandTypes;
    
    LocationsKeeperWorker(
            DaoLocations dao, 
            CommandsMemoryKeeper commandsMemoryKeeper,
            InnerIoEngine ioEngine, 
            KeeperDialogHelper consistencyChecker,
            LocationsInputParser parser,
            PropertyAndTextParser propertyTextParser) {
        this.dao = dao;
        this.commandsMemory = commandsMemoryKeeper;
        this.ioEngine = ioEngine;
        this.helper = consistencyChecker;
        this.locationInpurParser = parser;
        this.propertyTextParser = propertyTextParser;
        this.subjectedCommandTypes = toSet(OPEN_LOCATION, OPEN_LOCATION_TARGET);
    }
    
    private void asyncCleanCommandsMemory(Initiator initiator, String extended) {
        asyncDo(() -> {
            this.commandsMemory.removeByExactExtendedAndType(initiator, extended, OPEN_LOCATION);
            this.commandsMemory.removeByExactExtendedLocationPrefixInPath(initiator, extended);
        });
    }
    
    private void asyncAddCommand(Initiator initiator, String locationName) {
        asyncDo(() -> {
            this.commandsMemory.save(
                    initiator, new OpenLocationCommand(locationName, locationName, NEW, TARGET_FOUND));
        });
    }
    
    private void asyncChangeCommandsMemory(
            Initiator initiator, String locationOldName, String locationNewName) {
        asyncDo(() -> {
            this.commandsMemory.removeByExactExtendedAndType(
                    initiator, locationOldName, OPEN_LOCATION);
            this.commandsMemory.removeByExactExtendedLocationPrefixInPath(
                    initiator, locationOldName);
            this.commandsMemory.save(
                    initiator, new OpenLocationCommand(
                            locationNewName, locationNewName, NEW, TARGET_FOUND));
        });
    }
    
    private ValueOperation<Location> discussExistingLocation(Initiator initiator, String name) {
        List<Location> foundLocations;     
        WeightedVariants weightedLocations;
        Answer answer;
        locationDiscussing: while ( true ) {            
            name = this.helper.validateEntityNameInteractively(initiator, name);
            if (name.isEmpty()) {
                return valueOperationStopped();
            }

            foundLocations = this.dao.getLocationsByNamePattern(initiator, name);
            if ( hasOne(foundLocations) ) {
                this.ioEngine.report(
                        initiator, format("'%s' found.", getOne(foundLocations).name()));
                return valueCompletedWith(getOne(foundLocations));
            } else if ( hasMany(foundLocations) ) {
                weightedLocations = weightVariants(name, entitiesToVariants(foundLocations));
                if ( weightedLocations.isEmpty() ) {
                    this.ioEngine.report(initiator, format("not found by '%s'", name));
                    name = "";
                    continue locationDiscussing;
                }
                answer = this.ioEngine.chooseInWeightedVariants(initiator, weightedLocations);
                if ( answer.isGiven() ) {
                    return valueCompletedWith(foundLocations.get(answer.index()));
                } else if ( answer.isRejection() ) {
                    return valueOperationStopped();
                } else if ( answer.variantsAreNotSatisfactory() ) {
                    name = "";
                    continue locationDiscussing;
                } else {
                    this.ioEngine.report(initiator, "cannot determine your answer.");
                    return valueOperationStopped();
                }
            } else {
                this.ioEngine.report(initiator, format("not found by '%s'", name));
                name = "";
                continue locationDiscussing;
            }
        }
    }

    @Override
    public boolean isSubjectedTo(InvocationCommand command) {
        return this.subjectedCommandTypes.contains(command.type());
    }
    
    @Override
    public ValueOperation<Location> findByExactName(
            Initiator initiator, String exactName) {
        return valueCompletedWith(this.dao.getLocationByExactName(initiator, exactName));
    }
    
    @Override
    public List<Location> getLocationsByNamePattern(
            Initiator initiator, String namePattern) {
        return this.dao.getLocationsByNamePattern(initiator, namePattern);
    }
    
    @Override
    public ValueOperation<Location> findByNamePattern(
            Initiator initiator, String namePattern) {
        List<Location> locations = this.dao.getLocationsByNamePattern(initiator, namePattern);
        if ( hasOne(locations) ) {
            return valueCompletedWith(getOne(locations));
        } else if ( hasMany(locations) ) {
            return this.manageWithManyLocations(initiator, namePattern, locations);
        } else {
            return valueCompletedEmpty();
        }
    }
    
    @Override
    public ValueOperation<Location> findLocation(
            Initiator initiator, ArgumentsCommand command) {
        if ( command.type().isNot(FIND_LOCATION) ) {
            return valueOperationFail("wrong command type!");
        }
        String namePattern = "";
        if ( command.hasArguments() ) {
            namePattern = command.getFirstArg();
        } 
        
        return this.discussExistingLocation(initiator, namePattern);
    }

    private ValueOperation<Location> manageWithManyLocations(
            Initiator initiator, String pattern, List<Location> locations) {
        WeightedVariants variants = weightVariants(pattern, entitiesToVariants(locations));
        if ( variants.isEmpty() ) {
            return valueCompletedEmpty();
        }
        Answer answer = this.ioEngine.chooseInWeightedVariants(initiator, variants);
        if ( answer.isGiven() ) {
            return valueCompletedWith(locations.get(answer.index()));
        } else if ( answer.isRejection() ) {
            return valueOperationStopped();
        } else if ( answer.variantsAreNotSatisfactory() ) {
            return valueCompletedEmpty();
        } else {
            return valueCompletedEmpty();
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
            this.asyncAddCommand(initiator, name);
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
        
        List<Location> locationsToRemove = this.dao.getLocationsByNamePattern(initiator, name);
        if ( hasOne(locationsToRemove) ) {
            String locationName = getOne(locationsToRemove).name();
            this.ioEngine.report(initiator, format("'%s' found.", locationName));
            if ( this.dao.removeLocation(initiator, locationName) ) {
                this.asyncCleanCommandsMemory(initiator, locationName);
                return voidCompleted();
            } else {
                return voidOperationFail("DAO failed to remove location.");
            }
        } else if ( locationsToRemove.isEmpty() ) {
            return voidOperationFail("no such location.");
        } else {
            VariantsQuestion question = question("choose").withAnswerEntities(locationsToRemove);
            Answer answer = this.ioEngine.ask(initiator, question);
            if ( answer.isGiven() ) {
                String locationName = locationsToRemove.get(answer.index()).name();
                if ( this.dao.removeLocation(initiator, locationName) ) {
                    this.asyncCleanCommandsMemory(initiator, locationName);
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
        
        Location location;
        ValueOperation<Location> locationFlow = this.discussExistingLocation(initiator, name);
        switch ( locationFlow.result() ) {
            case COMPLETE : {
                if ( locationFlow.asComplete().hasValue() ) {
                    location = locationFlow.asComplete().getOrThrow();
                } else {
                    return voidOperationFail("no such location.");
                }
                break; 
            }
            case FAIL : {
                return voidOperationFail(locationFlow.asFail().reason());
            }
            case STOP : {
                return voidOperationStopped();
            }
            default : {
                return voidOperationFail("unknown ValueOperation result.");
            }
        }

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
                if ( this.dao.editLocationName(initiator, location.name(), newName) ) {
                    this.asyncChangeCommandsMemory(initiator, location.name(), newName);
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
                if ( this.dao.editLocationPath(initiator, location.name(), newPath) ) {
                    return voidCompleted();
                } else {
                    return voidOperationFail("DAO failed to edit path.");
                }
            }
            default : {
                return voidOperationFail("unexpected property.");
            }
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
