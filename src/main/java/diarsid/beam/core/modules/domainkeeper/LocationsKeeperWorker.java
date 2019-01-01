/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and openAsync the template in the editor.
 */

package diarsid.beam.core.modules.domainkeeper;

import java.util.List;
import java.util.Optional;
import java.util.Set;

import diarsid.beam.core.base.analyze.variantsweight.WeightedVariant;
import diarsid.beam.core.base.analyze.variantsweight.WeightedVariants;
import diarsid.beam.core.base.control.flow.ValueFlow;
import diarsid.beam.core.base.control.flow.VoidFlow;
import diarsid.beam.core.base.control.io.base.actors.Initiator;
import diarsid.beam.core.base.control.io.base.actors.InnerIoEngine;
import diarsid.beam.core.base.control.io.base.interaction.Answer;
import diarsid.beam.core.base.control.io.base.interaction.Help;
import diarsid.beam.core.base.control.io.base.interaction.Message;
import diarsid.beam.core.base.control.io.commands.ArgumentsCommand;
import diarsid.beam.core.base.control.io.commands.CommandType;
import diarsid.beam.core.base.control.io.commands.executor.InvocationCommand;
import diarsid.beam.core.base.control.io.commands.executor.OpenLocationCommand;
import diarsid.beam.core.domain.entities.Location;
import diarsid.beam.core.domain.entities.LocationSubPath;
import diarsid.beam.core.domain.entities.metadata.EntityProperty;
import diarsid.beam.core.domain.entities.validation.UndefinedValidity;
import diarsid.beam.core.domain.entities.validation.Validity;
import diarsid.beam.core.domain.inputparsing.common.PropertyAndText;
import diarsid.beam.core.domain.inputparsing.common.PropertyAndTextParser;
import diarsid.beam.core.domain.inputparsing.locations.LocationNameAndPath;
import diarsid.beam.core.domain.inputparsing.locations.LocationsInputParser;
import diarsid.beam.core.modules.responsivedata.ResponsiveDaoLocationSubPaths;
import diarsid.beam.core.modules.responsivedata.ResponsiveDaoLocations;
import diarsid.beam.core.modules.responsivedata.ResponsiveDaoPatternChoices;

import static java.lang.String.format;
import static java.util.stream.Collectors.joining;

import static diarsid.beam.core.base.analyze.variantsweight.Analyze.isEntitySatisfiable;
import static diarsid.beam.core.base.analyze.variantsweight.Analyze.isNameSatisfiable;
import static diarsid.beam.core.base.analyze.variantsweight.Analyze.weightVariants;
import static diarsid.beam.core.base.analyze.variantsweight.WeightEstimate.PERFECT;
import static diarsid.beam.core.base.control.flow.Flows.valueFlowCompletedEmpty;
import static diarsid.beam.core.base.control.flow.Flows.valueFlowCompletedWith;
import static diarsid.beam.core.base.control.flow.Flows.valueFlowFail;
import static diarsid.beam.core.base.control.flow.Flows.valueFlowStopped;
import static diarsid.beam.core.base.control.flow.Flows.voidFlowCompleted;
import static diarsid.beam.core.base.control.flow.Flows.voidFlowFail;
import static diarsid.beam.core.base.control.flow.Flows.voidFlowStopped;
import static diarsid.beam.core.base.control.io.base.interaction.Messages.entitiesToOptionalMessageWithHeader;
import static diarsid.beam.core.base.control.io.base.interaction.Variants.entitiesToVariants;
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
import static diarsid.beam.core.base.util.PathUtils.containsPathSeparator;
import static diarsid.beam.core.domain.entities.metadata.EntityProperty.FILE_URL;
import static diarsid.beam.core.domain.entities.metadata.EntityProperty.NAME;
import static diarsid.beam.core.domain.entities.metadata.EntityProperty.UNDEFINED_PROPERTY;
import static diarsid.beam.core.domain.entities.metadata.EntityProperty.argToProperty;
import static diarsid.beam.core.domain.entities.validation.DomainValidationRule.ENTITY_NAME_RULE;
import static diarsid.beam.core.domain.entities.validation.DomainValidationRule.LOCAL_DIRECTORY_PATH_RULE;
import static diarsid.beam.core.domain.entities.validation.UndefinedValidity.undefinedValidityMutableOnce;
import static diarsid.beam.core.domain.entities.validation.Validities.validationFailsWith;
import static diarsid.beam.core.domain.entities.validation.Validities.validationOk;
import static diarsid.support.objects.Pools.giveBackToPool;
import static diarsid.support.objects.Pools.takeFromPool;



class LocationsKeeperWorker implements LocationsKeeper {
    
    private final Object allLocationsConsistencyLock;
    private final ResponsiveDaoLocations daoLocations;
    private final ResponsiveDaoLocationSubPaths daoLocationSubPaths;
    private final ResponsiveDaoPatternChoices daoPatternChoices;
    private final CommandsMemoryKeeper commandsMemory;
    private final InnerIoEngine ioEngine;
    private final LocationsInputParser locationInpurParser;
    private final PropertyAndTextParser propertyTextParser;
    private final Set<CommandType> subjectedCommandTypes;
    private final Help chooseOneLocationHelp;
    private final Help chooseOneSubPathHelp;
    private final Help enterLocationNameHelp;
    private final Help enterLocationPathHelp;
    private final Help enterPropertyToEditHelp;
    
    LocationsKeeperWorker(
            ResponsiveDaoLocations daoLocations,
            ResponsiveDaoLocationSubPaths daoLocationSubPaths,
            ResponsiveDaoPatternChoices daoPatternChoices,
            CommandsMemoryKeeper commandsMemoryKeeper,
            InnerIoEngine ioEngine, 
            LocationsInputParser parser,
            PropertyAndTextParser propertyTextParser) {
        this.allLocationsConsistencyLock = new Object();
        this.daoLocations = daoLocations;
        this.daoLocationSubPaths = daoLocationSubPaths;
        this.daoPatternChoices = daoPatternChoices;
        this.commandsMemory = commandsMemoryKeeper;
        this.ioEngine = ioEngine;
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
        this.chooseOneSubPathHelp = this.ioEngine.addToHelpContext(
                "Choose one path from given variants.",
                "Use:",
                "   - number to choose path",
                "   - part of path to choose it",
                "   - n/no to see more found paths if any",
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
        this.enterPropertyToEditHelp = this.ioEngine.addToHelpContext(
                "Enter Location's property to edit.",
                "Can be name or path."
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
                    initiator, new OpenLocationCommand(
                            locationName, locationName, NEW, TARGET_FOUND));
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
    
    private ValueFlow<Location> findExistingLocationInternally(Initiator initiator, String name) {
        List<Location> foundLocations;     
        WeightedVariants weightedLocations;
        Answer answer;
        KeeperLoopValidationDialog dialog = takeFromPool(KeeperLoopValidationDialog.class)
                .withInitialArgument(name)
                .withRule(ENTITY_NAME_RULE)
                .withInputSource(() -> {
                    return this.ioEngine.askInput(
                            initiator, "name", this.enterLocationNameHelp);
                })
                .withOutputDestination((validationFail) -> {
                    this.ioEngine.report(initiator, validationFail);
                });
        
        try {
            locationFinding: while ( true ) {            

                name = dialog
                        .withInitialArgument(name)
                        .validateAndGet();            

                if (name.isEmpty()) {
                    return valueFlowStopped();
                }

                foundLocations = this.daoLocations.getLocationsByNamePattern(initiator, name);
                if ( hasOne(foundLocations) ) {
                    Location location = getOne(foundLocations);
                    if ( ! name.equalsIgnoreCase(location.name()) ) {
                        this.ioEngine.report(initiator, format("'%s' found.", location.name()));
                    }                    
                    return valueFlowCompletedWith(location);
                } else if ( hasMany(foundLocations) ) {
                    weightedLocations = weightVariants(name, entitiesToVariants(foundLocations));
                    if ( weightedLocations.isEmpty() ) {
                        this.ioEngine.report(initiator, format("not found by '%s'", name));
                        name = "";
                        continue locationFinding;
                    }
                    answer = this.ioEngine.chooseInWeightedVariants(
                            initiator, weightedLocations, this.chooseOneLocationHelp);
                    if ( answer.isGiven() ) {
                        return valueFlowCompletedWith(foundLocations.get(answer.index()));
                    } else if ( answer.isRejection() ) {
                        return valueFlowStopped();
                    } else if ( answer.variantsAreNotSatisfactory() ) {
                        name = "";
                        continue locationFinding;
                    } else {
                        this.ioEngine.report(initiator, "cannot determine your answer.");
                        return valueFlowStopped();
                    }
                } else {
                    this.ioEngine.report(initiator, format("not found by '%s'", name));
                    name = "";
                    continue locationFinding;
                }
            }
        } finally {
            giveBackToPool(dialog);
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
        List<LocationSubPath> locationSubPaths = this.daoLocationSubPaths.getSubPathesByPattern(
                initiator, namePattern);
        locations.addAll(locationSubPaths);
        if ( hasOne(locations) ) {
            Location location = getOne(locations);
            if ( location.name().equalsIgnoreCase(namePattern) ) {
                return valueFlowCompletedWith(location);
            } else {
                if ( isEntitySatisfiable(namePattern, location) ) {
                    return valueFlowCompletedWith(location);
                } else {
                    return valueFlowCompletedEmpty();
                }
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
        
        return this.findExistingLocationInternally(initiator, namePattern);
    }

    private ValueFlow<Location> manageWithManyLocations(
            Initiator initiator, String pattern, List<Location> locations) {
        WeightedVariants variants = weightVariants(pattern, entitiesToVariants(locations));
        if ( variants.isEmpty() ) {
            return valueFlowCompletedEmpty();
        }
        
        WeightedVariant bestVariant = variants.best();
        if ( bestVariant.text().equalsIgnoreCase(pattern) || 
             bestVariant.hasEqualOrBetterWeightThan(PERFECT) ) {
            return valueFlowCompletedWith(locations.get(bestVariant.index()));
        } else {
            boolean hasMatch = this.daoPatternChoices
                    .hasMatchOf(initiator, pattern, bestVariant.text(), variants);
            if ( hasMatch ) {
                return valueFlowCompletedWith(locations.get(bestVariant.index()));
            } else {
                return this.askUserForLocationAndSaveChoice(
                        initiator, pattern, variants, locations);
            }            
        }        
    }    
    
    private ValueFlow<Location> askUserForLocationAndSaveChoice(
            Initiator initiator, 
            String pattern, 
            WeightedVariants variants, 
            List<Location> locations) {
        Answer answer = this.ioEngine.chooseInWeightedVariants(
                initiator, variants, this.chooseOneLocationHelp);
        if ( answer.isGiven() ) {
            Location location = locations.get(answer.index());
            asyncDo(() -> {
                this.daoPatternChoices.save(initiator, pattern, location.name(), variants);
            });
            return valueFlowCompletedWith(location);
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
        
        synchronized ( this.allLocationsConsistencyLock ) {
            path = this.discussLocationNewPath(initiator, path);

            if ( path.isEmpty() ) {
                return voidFlowStopped();
            }

            name = this.discussLocationNewName(initiator, name); 
            
            if ( name.isEmpty() ) {
                return voidFlowStopped();
            }
        }
        
        if ( this.daoLocations.saveNewLocation(initiator, new Location(name, path)) ) {
            this.asyncAddCommand(initiator, name);
            return voidFlowCompleted();
        } else {
            return voidFlowFail("DAO failed to save Location.");
        }     
    }

    private String discussLocationNewName(Initiator initiator, String name) {
        KeeperLoopValidationDialog dialog = takeFromPool(KeeperLoopValidationDialog.class);
        
        name = dialog
                .withInitialArgument(name)
                .withRule(ENTITY_NAME_RULE)
                .withRule((newName) -> {
                    if ( this.daoLocations.isNameFree(initiator, newName) ) {
                        return validationOk();
                    } else {
                        return validationFailsWith("this name is not free!");
                    }
                })
                .withInputSource(() -> {
                    return this.ioEngine.askInput(
                            initiator, "name", this.enterLocationNameHelp);
                })
                .withOutputDestination((validationFail) -> {
                    this.ioEngine.report(initiator, validationFail);
                })
                .validateAndGet();
        
        giveBackToPool(dialog);
        
        return name;
    }

    private String discussLocationNewPath(Initiator initiator, String path) {
        KeeperLoopValidationDialog dialog = takeFromPool(KeeperLoopValidationDialog.class);
        
        path = dialog
                .withInitialArgument(path)
                .withRule((newLocationPath) -> {
                    return this.validateLocationNewPath(initiator, newLocationPath, dialog);
                })
                .withInputSource(() -> {
                    return this.ioEngine.askInput(
                            initiator, "path", this.enterLocationPathHelp);
                })
                .withOutputDestination((validationFail) -> {
                    this.ioEngine.report(initiator, validationFail);
                })
                .validateAndGet();
        
        giveBackToPool(dialog);
        
        return path;
    }
    
    private Validity validateLocationNewPath(
            Initiator initiator, String newPath, KeeperLoopValidationDialog dialog) {
        if ( ! containsPathSeparator(newPath) ) {
            return validationFailsWith("must be path!");
        }

        UndefinedValidity validity = undefinedValidityMutableOnce();

        if ( LOCAL_DIRECTORY_PATH_RULE.applyTo(newPath).isOk() ) {
            Optional<Location> locationFoundByPath = this.daoLocations
                    .getLocationByPath(initiator, newPath);
            if ( locationFoundByPath.isPresent() ) {
                String reason = format(
                        "location '%s' already has this path.", 
                        locationFoundByPath.get().name());
                validity.failsWith(reason);
            } else {
                validity.ok();
            }            
        } else {                    
            List<LocationSubPath> subPathes = this.daoLocationSubPaths
                    .getSubPathesByPattern(initiator, newPath);

            if ( hasOne(subPathes) ) {
                LocationSubPath locationSubPath = getOne(subPathes);
                String locationAndSubPath = locationSubPath.name();
                boolean pathFound = 
                        locationAndSubPath.equalsIgnoreCase(newPath) ||
                        isNameSatisfiable(newPath, locationAndSubPath);
                if ( pathFound ) {
                    String realFoundPath = locationSubPath.fullPath();
                    validity.set(defineNewPathValidityUsing(initiator, realFoundPath, dialog));   
                } else {
                    validity.failsWith(format("'%s' not found", newPath));
                }
            } else if ( hasMany(subPathes) ) {
                WeightedVariants variants = weightVariants(
                        newPath, entitiesToVariants(subPathes));
                if ( variants.isEmpty() ) {
                    validity.failsWith(format("'%s' not found", newPath));
                } else {
                    WeightedVariant best = variants.best();
                    boolean bestIsGoodEnough = 
                            best.text().equalsIgnoreCase(newPath) || 
                            best.hasEqualOrBetterWeightThan(PERFECT);
                    if ( bestIsGoodEnough ) {
                        String realFoundPath = subPathes.get(best.index()).fullPath();
                        validity.set(defineNewPathValidityUsing(initiator, realFoundPath, dialog));   
                    } else {
                        boolean hasMatch = this.daoPatternChoices
                                .hasMatchOf(initiator, newPath, best.text(), variants);
                        if ( hasMatch ) {
                            String realFoundPath = subPathes.get(best.index()).fullPath();
                            validity.set(defineNewPathValidityUsing(initiator, realFoundPath, dialog));
                        } else {
                            Answer answer = this.ioEngine.chooseInWeightedVariants(
                                    initiator, variants, this.chooseOneSubPathHelp);
                            if ( answer.isGiven() ) {
                                LocationSubPath foundSubPath = subPathes.get(answer.index());
                                asyncDo(() -> {
                                    this.daoPatternChoices.save(
                                            initiator, newPath, foundSubPath.name(), variants);
                                });
                                String realFoundPath = foundSubPath.fullPath();
                                validity.set(defineNewPathValidityUsing(initiator, realFoundPath, dialog));
                            } else {
                                validity.fails();
                            }
                        }            
                    }
                }                
            } else {
                validity.failsWith(format("'%s' not found", newPath));
            }
        }

        return validity.get();
    }

    private Validity defineNewPathValidityUsing(
            Initiator initiator, String realFoundPath, KeeperLoopValidationDialog dialog) {
        Optional<Location> locationFoundByPath = this.daoLocations
                .getLocationByPath(initiator, realFoundPath);
        if ( locationFoundByPath.isPresent() ) {
            String reason = format(
                    "location '%s' already has path %s.",
                    locationFoundByPath.get().name(),
                    realFoundPath);
            return validationFailsWith(reason);
        } else {
            if ( LOCAL_DIRECTORY_PATH_RULE.applyTo(realFoundPath).isOk() ) {
                dialog.replaceInput(realFoundPath);
                return validationOk();
            } else {
                return validationFailsWith("path not found");
            }
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
        
        synchronized ( this.allLocationsConsistencyLock ) {
            ValueFlow<Location> locationFlow = this.findExistingLocationInternally(initiator, name);
            
            switch ( locationFlow.result() ) {
                case COMPLETE:
                    if ( locationFlow.asComplete().hasValue() ) {
                        Location location = locationFlow.asComplete().orThrow();
                        return this.removeLocationByName(initiator, location.name());
                    } else {
                        return voidFlowFail("no such location.");
                    }
                case STOP:
                    return voidFlowStopped();
                case FAIL:
                    return locationFlow.toVoid();
                default:
                    return voidFlowFail("unknown ValueFlow result.");
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
        EntityProperty propertyToEdit;
        if ( command.hasArguments() ) {
            PropertyAndText propText = this.propertyTextParser.parse(command.arguments());
            name = propText.text();
            propertyToEdit = propText.property();
        } else {
            name = "";
            propertyToEdit = UNDEFINED_PROPERTY;
        }  
        
        synchronized ( this.allLocationsConsistencyLock ) {
            Location location;
            ValueFlow<Location> locationFlow = this.findExistingLocationInternally(initiator, name);
            switch ( locationFlow.result() ) {
                case COMPLETE : {
                    if ( locationFlow.asComplete().hasValue() ) {
                        location = locationFlow.asComplete().orThrow();
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
            
            KeeperLoopValidationDialog dialog = takeFromPool(KeeperLoopValidationDialog.class);
            String propertyName = dialog
                    .withInitialArgument(propertyToEdit.name())
                    .withRule((prop) -> {
                        EntityProperty property = argToProperty(prop);
                        if ( property.isUndefined() ) {
                            return validationFailsWith("not a property");
                        }
                        if ( property.isNotOneOf(NAME, FILE_URL) ) {
                            return validationFailsWith("name or url is expected");
                        }
                        return validationOk();
                    })
                    .withInputSource(() -> {
                        return this.ioEngine.askInput(
                                initiator, "property", this.enterPropertyToEditHelp);
                    })
                    .withOutputDestination((validationFail) -> {
                        this.ioEngine.report(initiator, validationFail);
                    })
                    .validateAndGet();

            propertyToEdit = argToProperty(propertyName);
            if ( propertyToEdit.isUndefined() ) {
                return voidFlowStopped();
            }

            switch ( propertyToEdit ) {
                case NAME : {
                    return this.editLocationName(initiator, location);
                }
                case FILE_URL : {
                    return this.edilLocationPath(initiator, location);
                }
                default : {
                    return voidFlowFail("unexpected property.");
                }
            } 
        }        
    }

    private VoidFlow edilLocationPath(Initiator initiator, Location location) {
        String newPath = this.discussLocationNewPath(initiator, "");
        
        if ( newPath.isEmpty() ) {
            return voidFlowStopped();
        }
        
        if ( this.daoLocations.editLocationPath(initiator, location.name(), newPath) ) {
            return voidFlowCompleted();
        } else {
            return voidFlowFail("DAO failed to edit path.");
        }
    }

    private VoidFlow editLocationName(Initiator initiator, Location location) {
        String newName = this.discussLocationNewName(initiator, "");
        
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
    public ValueFlow<Message> findAll(Initiator initiator) {
        return valueFlowCompletedWith(entitiesToOptionalMessageWithHeader(
                    "all Locations:", this.daoLocations.getAllLocations(initiator)));
    }
}
