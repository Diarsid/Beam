/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and openAsync the template in the editor.
 */

package diarsid.beam.core.modules.domainkeeper;

import java.util.List;
import java.util.Set;

import diarsid.beam.core.base.analyze.variantsweight.WeightedVariants;
import diarsid.beam.core.base.control.flow.ValueFlow;
import diarsid.beam.core.base.control.flow.VoidFlow;
import diarsid.beam.core.base.control.io.base.actors.Initiator;
import diarsid.beam.core.base.control.io.base.actors.InnerIoEngine;
import diarsid.beam.core.base.control.io.base.interaction.Answer;
import diarsid.beam.core.base.control.io.base.interaction.Help;
import diarsid.beam.core.base.control.io.base.interaction.Message;
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
import diarsid.beam.core.modules.data.DaoLocations;

import static java.lang.String.format;
import static java.util.stream.Collectors.joining;

import static diarsid.beam.core.base.analyze.variantsweight.Analyze.entityIsSatisfiable;
import static diarsid.beam.core.base.analyze.variantsweight.Analyze.weightVariants;
import static diarsid.beam.core.base.control.flow.Flows.valueFlowCompletedEmpty;
import static diarsid.beam.core.base.control.flow.Flows.valueFlowCompletedWith;
import static diarsid.beam.core.base.control.flow.Flows.valueFlowFail;
import static diarsid.beam.core.base.control.flow.Flows.valueFlowStopped;
import static diarsid.beam.core.base.control.flow.Flows.voidFlowCompleted;
import static diarsid.beam.core.base.control.flow.Flows.voidFlowFail;
import static diarsid.beam.core.base.control.flow.Flows.voidFlowStopped;
import static diarsid.beam.core.base.control.io.base.interaction.Messages.entitiesToOptionalMessageWithHeader;
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
import static diarsid.beam.core.base.control.io.interpreter.ControlKeys.UNACCEPTABLE_DOMAIN_CHARS;
import static diarsid.beam.core.base.util.CollectionsUtils.getOne;
import static diarsid.beam.core.base.util.CollectionsUtils.hasMany;
import static diarsid.beam.core.base.util.CollectionsUtils.hasOne;
import static diarsid.beam.core.base.util.CollectionsUtils.toSet;
import static diarsid.beam.core.base.util.ConcurrencyUtil.asyncDo;
import static diarsid.beam.core.domain.entities.metadata.EntityProperty.FILE_URL;
import static diarsid.beam.core.domain.entities.metadata.EntityProperty.NAME;
import static diarsid.beam.core.domain.entities.metadata.EntityProperty.UNDEFINED_PROPERTY;
import static diarsid.beam.core.domain.entities.validation.ValidationRule.LOCAL_DIRECTORY_PATH_RULE;



class LocationsKeeperWorker implements LocationsKeeper {
    
    private final DaoLocations daoLocations;
    private final CommandsMemoryKeeper commandsMemory;
    private final InnerIoEngine ioEngine;
    private final KeeperDialogHelper helper;
    private final LocationsInputParser locationInpurParser;
    private final PropertyAndTextParser propertyTextParser;
    private final Set<CommandType> subjectedCommandTypes;
    private final Help chooseOneLocationHelp;
    private final Help enterLocationNameHelp;
    private final Help enterLocationPathHelp;
    
    LocationsKeeperWorker(
            DaoLocations daoLocations, 
            CommandsMemoryKeeper commandsMemoryKeeper,
            InnerIoEngine ioEngine, 
            KeeperDialogHelper consistencyChecker,
            LocationsInputParser parser,
            PropertyAndTextParser propertyTextParser) {
        this.daoLocations = daoLocations;
        this.commandsMemory = commandsMemoryKeeper;
        this.ioEngine = ioEngine;
        this.helper = consistencyChecker;
        this.locationInpurParser = parser;
        this.propertyTextParser = propertyTextParser;
        this.subjectedCommandTypes = toSet(OPEN_LOCATION, OPEN_LOCATION_TARGET);
        this.chooseOneLocationHelp = this.ioEngine.addToHelpContext(
                "Choose one Location from given variants.",
                "Use:",
                "   - number to choose Location",
                "   - part of Location name to choose it",
                "   - n/no to see more found Locations if any",
                "   - dot to break"
        );
        this.enterLocationNameHelp = this.ioEngine.addToHelpContext(
                "Enter Location's name.",
                "Name must be unique and must not contain path separators",
                "nor following characters: " + 
                        UNACCEPTABLE_DOMAIN_CHARS.stream().collect(joining())
        );
        this.enterLocationPathHelp = this.ioEngine.addToHelpContext(
                "Enter Location's path.",
                "Path must point to existing directory."
        );
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
    
    private ValueFlow<Location> discussExistingLocation(Initiator initiator, String name) {
        List<Location> foundLocations;     
        WeightedVariants weightedLocations;
        Answer answer;
        locationDiscussing: while ( true ) {            
            name = this.helper.validateEntityNameInteractively(initiator, name);
            if (name.isEmpty()) {
                return valueFlowStopped();
            }

            foundLocations = this.daoLocations.getLocationsByNamePattern(initiator, name);
            if ( hasOne(foundLocations) ) {
                this.ioEngine.report(
                        initiator, format("'%s' found.", getOne(foundLocations).name()));
                return valueFlowCompletedWith(getOne(foundLocations));
            } else if ( hasMany(foundLocations) ) {
                weightedLocations = weightVariants(name, entitiesToVariants(foundLocations));
                if ( weightedLocations.isEmpty() ) {
                    this.ioEngine.report(initiator, format("not found by '%s'", name));
                    name = "";
                    continue locationDiscussing;
                }
                answer = this.ioEngine.chooseInWeightedVariants(
                        initiator, weightedLocations, this.chooseOneLocationHelp);
                if ( answer.isGiven() ) {
                    return valueFlowCompletedWith(foundLocations.get(answer.index()));
                } else if ( answer.isRejection() ) {
                    return valueFlowStopped();
                } else if ( answer.variantsAreNotSatisfactory() ) {
                    name = "";
                    continue locationDiscussing;
                } else {
                    this.ioEngine.report(initiator, "cannot determine your answer.");
                    return valueFlowStopped();
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
    public ValueFlow<Location> findByExactName(
            Initiator initiator, String exactName) {
        return valueFlowCompletedWith(this.daoLocations.getLocationByExactName(initiator, exactName));
    }
    
    @Override
    public List<Location> getLocationsByNamePattern(
            Initiator initiator, String namePattern) {
        return this.daoLocations.getLocationsByNamePattern(initiator, namePattern);
    }
    
    @Override
    public ValueFlow<Location> findByNamePattern(
            Initiator initiator, String namePattern) {
        List<Location> locations = this.daoLocations.getLocationsByNamePattern(
                initiator, namePattern);
        if ( hasOne(locations) ) {
            Location location = getOne(locations);
            if ( entityIsSatisfiable(namePattern, location) ) {
                return valueFlowCompletedWith(location);
            } else {
                return valueFlowCompletedEmpty();
            }            
        } else if ( hasMany(locations) ) {
            return this.manageWithManyLocations(initiator, namePattern, locations);
        } else {
            return valueFlowCompletedEmpty();
        }
    }
    
    @Override
    public ValueFlow<Location> findLocation(
            Initiator initiator, ArgumentsCommand command) {
        if ( command.type().isNot(FIND_LOCATION) ) {
            return valueFlowFail("wrong command type!");
        }
        String namePattern = "";
        if ( command.hasArguments() ) {
            namePattern = command.getFirstArg();
        } 
        
        return this.discussExistingLocation(initiator, namePattern);
    }

    private ValueFlow<Location> manageWithManyLocations(
            Initiator initiator, String pattern, List<Location> locations) {
        WeightedVariants variants = weightVariants(pattern, entitiesToVariants(locations));
        if ( variants.isEmpty() ) {
            return valueFlowCompletedEmpty();
        }
        Answer answer = this.ioEngine.chooseInWeightedVariants(
                initiator, variants, this.chooseOneLocationHelp);
        if ( answer.isGiven() ) {
            return valueFlowCompletedWith(locations.get(answer.index()));
        } else if ( answer.isRejection() ) {
            return valueFlowStopped();
        } else if ( answer.variantsAreNotSatisfactory() ) {
            return valueFlowCompletedEmpty();
        } else {
            return valueFlowCompletedEmpty();
        }
    }    

    @Override
    public VoidFlow createLocation(Initiator initiator, ArgumentsCommand command) {
        if ( command.type().isNot(CREATE_LOCATION) ) {
            return voidFlowFail("wrong command type!");
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
            return voidFlowStopped();
        }        
        
        name = this.helper.validateEntityNameInteractively(initiator, name);
        if ( name.isEmpty() ) {
            return voidFlowStopped();
        }    
        nameDefining: while ( true ) {
            if ( this.daoLocations.isNameFree(initiator, name) ) {
                break nameDefining;
            } else {
                this.ioEngine.report(initiator, "this name is not free!");
                name = this.ioEngine.askInput(initiator, "name", this.enterLocationNameHelp);
                if ( name.isEmpty() ) {
                    return voidFlowStopped();
                }
                name = this.helper.validateEntityNameInteractively(initiator, name);
                if ( name.isEmpty() ) {
                    return voidFlowStopped();
                }                    
            }
        }

        if ( this.daoLocations.saveNewLocation(initiator, new Location(name, path)) ) {
            this.asyncAddCommand(initiator, name);
            return voidFlowCompleted();
        } else {
            return voidFlowFail("DAO failed to save Location.");
        }     
    }

    @Override
    public VoidFlow removeLocation(Initiator initiator, ArgumentsCommand command) {
        if ( command.type().isNot(DELETE_LOCATION) ) {
            return voidFlowFail("wrong command type!");
        }
        
        String name;
        if ( command.hasArguments()) {
            name = command.getFirstArg();
        } else {
            name = "";
        }
        
        name = this.helper.validateEntityNameInteractively(initiator, name);
        if ( name.isEmpty() ) {
            return voidFlowStopped();
        }
        
        List<Location> locationsToRemove = this.daoLocations.getLocationsByNamePattern(
                initiator, name);
        if ( hasOne(locationsToRemove) ) {
            String locationName = getOne(locationsToRemove).name();
            this.ioEngine.report(initiator, format("'%s' found.", locationName));
            return this.removeLocationByName(initiator, locationName);
        } else if ( locationsToRemove.isEmpty() ) {
            return voidFlowFail("no such location.");
        } else {
            VariantsQuestion question = question("choose").withAnswerEntities(locationsToRemove);
            Answer answer = this.ioEngine.ask(initiator, question, this.chooseOneLocationHelp);
            if ( answer.isGiven() ) {
                return this.removeLocationByName(
                        initiator, locationsToRemove.get(answer.index()).name());                    
            } else {
                return voidFlowStopped();
            }
        }        
    }

    private VoidFlow removeLocationByName(Initiator initiator, String locationName) {
        if ( this.daoLocations.removeLocation(initiator, locationName) ) {
            this.asyncCleanCommandsMemory(initiator, locationName);
            return voidFlowCompleted();
        } else {
            return voidFlowFail("DAO failed to remove location.");
        }
    }

    @Override
    public VoidFlow editLocation(Initiator initiator, ArgumentsCommand command) {
        if ( command.type().isNot(EDIT_LOCATION) ) {
            return voidFlowFail("wrong command type!");
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
            return voidFlowStopped();
        }
        
        Location location;
        ValueFlow<Location> locationFlow = this.discussExistingLocation(initiator, name);
        switch ( locationFlow.result() ) {
            case COMPLETE : {
                if ( locationFlow.asComplete().hasValue() ) {
                    location = locationFlow.asComplete().getOrThrow();
                } else {
                    return voidFlowFail("no such location.");
                }
                break; 
            }
            case FAIL : {
                return voidFlowFail(locationFlow.asFail().reason());
            }
            case STOP : {
                return voidFlowStopped();
            }
            default : {
                return voidFlowFail("unknown ValueFlow result.");
            }
        }

        property = this.helper.validatePropertyInteractively(
                initiator, property, NAME, FILE_URL);
        if ( property.isUndefined() ) {
            return voidFlowStopped();
        }

        switch ( property ) {
            case NAME : {
                String newName = this.ioEngine.askInput(
                        initiator, "new name", this.enterLocationNameHelp);                    
                if ( newName.isEmpty() ) {
                    return voidFlowStopped();
                }
                newName = this.helper.validateEntityNameInteractively(initiator, newName);
                if ( newName.isEmpty() ) {
                    return voidFlowStopped();
                }
                if ( this.daoLocations.editLocationName(initiator, location.name(), newName) ) {
                    this.asyncChangeCommandsMemory(initiator, location.name(), newName);
                    return voidFlowCompleted();
                } else {
                    return voidFlowFail("DAO failed to edit name.");
                }
            }
            case FILE_URL : {
                String newPath = this.ioEngine.askInput(
                        initiator, "new path", this.enterLocationPathHelp);
                if ( newPath.isEmpty() ) {
                    return voidFlowStopped();
                }
                newPath = this.helper.validateInteractively(
                        initiator, newPath, "new path", LOCAL_DIRECTORY_PATH_RULE);
                if ( newPath.isEmpty() ) {
                    return voidFlowStopped();
                }
                if ( this.daoLocations.editLocationPath(initiator, location.name(), newPath) ) {
                    return voidFlowCompleted();
                } else {
                    return voidFlowFail("DAO failed to edit path.");
                }
            }
            default : {
                return voidFlowFail("unexpected property.");
            }
        }          
    }

    @Override
    public VoidFlow replaceInPaths(
            Initiator initiator, String replaceable, String replacement) {        
        if ( this.daoLocations.replaceInPaths(initiator, replaceable, replacement) ) {
            return voidFlowCompleted();
        } else {
            return voidFlowFail("DAO failed to replace path fragment.");
        }
    }

    @Override
    public ValueFlow<Message> showAll(Initiator initiator) {
        return valueFlowCompletedWith(entitiesToOptionalMessageWithHeader(
                    "all Locations:", this.daoLocations.getAllLocations(initiator)));
    }
}
