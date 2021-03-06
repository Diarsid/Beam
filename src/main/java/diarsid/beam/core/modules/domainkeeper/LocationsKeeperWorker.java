/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and openAsync the template in the editor.
 */

package diarsid.beam.core.modules.domainkeeper;

import java.util.List;
import java.util.Optional;
import java.util.Set;

import diarsid.beam.core.base.analyze.variantsweight.Variant;
import diarsid.beam.core.base.analyze.variantsweight.Variants;
import diarsid.beam.core.base.analyze.variantsweight.WeightAnalyze;
import diarsid.beam.core.base.control.flow.ValueFlow;
import diarsid.beam.core.base.control.flow.ValueFlowDone;
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
import diarsid.beam.core.base.os.treewalking.advanced.Walker;
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
import diarsid.support.objects.Pool;

import static java.lang.String.format;
import static java.util.stream.Collectors.joining;

import static diarsid.beam.core.base.analyze.variantsweight.WeightEstimate.PERFECT;
import static diarsid.beam.core.base.control.flow.Flows.valueFlowDoneEmpty;
import static diarsid.beam.core.base.control.flow.Flows.valueFlowDoneWith;
import static diarsid.beam.core.base.control.flow.Flows.valueFlowFail;
import static diarsid.beam.core.base.control.flow.Flows.valueFlowStopped;
import static diarsid.beam.core.base.control.flow.Flows.voidFlowDone;
import static diarsid.beam.core.base.control.flow.Flows.voidFlowFail;
import static diarsid.beam.core.base.control.flow.Flows.voidFlowStopped;
import static diarsid.beam.core.base.control.io.base.interaction.Messages.entitiesToOptionalMessageWithHeader;
import static diarsid.beam.core.base.control.io.base.interaction.VariantConversions.entitiesToVariants;
import static diarsid.beam.core.base.control.io.commands.CommandType.CREATE_LOCATION;
import static diarsid.beam.core.base.control.io.commands.CommandType.DELETE_LOCATION;
import static diarsid.beam.core.base.control.io.commands.CommandType.EDIT_LOCATION;
import static diarsid.beam.core.base.control.io.commands.CommandType.FIND_LOCATION;
import static diarsid.beam.core.base.control.io.commands.CommandType.OPEN_LOCATION;
import static diarsid.beam.core.base.control.io.commands.CommandType.OPEN_LOCATION_TARGET;
import static diarsid.beam.core.base.control.io.commands.executor.InvocationCommandLifePhase.NEW;
import static diarsid.beam.core.base.control.io.commands.executor.InvocationCommandTargetState.TARGET_FOUND;
import static diarsid.beam.core.base.control.io.interpreter.ControlKeys.UNACCEPTABLE_DOMAIN_CHARS;
import static diarsid.beam.core.base.os.treewalking.base.FileSearchMode.FOLDERS_ONLY;
import static diarsid.beam.core.base.util.CollectionsUtils.getOne;
import static diarsid.beam.core.base.util.CollectionsUtils.hasMany;
import static diarsid.beam.core.base.util.CollectionsUtils.hasOne;
import static diarsid.beam.core.base.util.CollectionsUtils.toSet;
import static diarsid.beam.core.base.util.ConcurrencyUtil.asyncDo;
import static diarsid.beam.core.base.util.PathUtils.containsPathSeparator;
import static diarsid.beam.core.base.util.PathUtils.extractLocationFromPath;
import static diarsid.beam.core.base.util.PathUtils.extractTargetFromPath;
import static diarsid.beam.core.base.util.PathUtils.joinToPath;
import static diarsid.beam.core.domain.entities.metadata.EntityProperty.FILE_PATH;
import static diarsid.beam.core.domain.entities.metadata.EntityProperty.NAME;
import static diarsid.beam.core.domain.entities.metadata.EntityProperty.UNDEFINED_PROPERTY;
import static diarsid.beam.core.domain.entities.metadata.EntityProperty.argToProperty;
import static diarsid.beam.core.domain.entities.validation.DomainValidationRule.ENTITY_NAME_RULE;
import static diarsid.beam.core.domain.entities.validation.DomainValidationRule.LOCAL_DIRECTORY_PATH_RULE;
import static diarsid.beam.core.domain.entities.validation.UndefinedValidity.undefinedValidityMutableOnce;
import static diarsid.beam.core.domain.entities.validation.Validities.validationFailsWith;
import static diarsid.beam.core.domain.entities.validation.Validities.validationOk;



class LocationsKeeperWorker implements LocationsKeeper {
    
    private final Object allLocationsConsistencyLock;
    private final ResponsiveDaoLocations daoLocations;
    private final ResponsiveDaoLocationSubPaths daoLocationSubPaths;
    private final ResponsiveDaoPatternChoices daoPatternChoices;
    private final CommandsMemoryKeeper commandsMemory;
    private final WeightAnalyze analyze;
    private final Pool<KeeperLoopValidationDialog> dialogPool;
    private final InnerIoEngine ioEngine;
    private final Walker walker;
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
            WeightAnalyze analyze, 
            Pool<KeeperLoopValidationDialog> dialogPool,
            InnerIoEngine ioEngine, 
            Walker walker,
            LocationsInputParser parser,
            PropertyAndTextParser propertyTextParser) {
        this.allLocationsConsistencyLock = new Object();
        this.daoLocations = daoLocations;
        this.daoLocationSubPaths = daoLocationSubPaths;
        this.daoPatternChoices = daoPatternChoices;
        this.commandsMemory = commandsMemoryKeeper;
        this.analyze = analyze;
        this.dialogPool = dialogPool;
        this.ioEngine = ioEngine;
        this.walker = walker;
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
                "Name must be unique and must contain neither path separators",
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
        Location foundLocation;
        List<Location> foundLocations;     
        Variants weightedLocations;
        Answer answer;
        
        try (KeeperLoopValidationDialog dialog = this.dialogPool.give()) {
            dialog
                    .withInitialArgument(name)
                    .withRule(ENTITY_NAME_RULE)
                    .withInputSource(() -> {
                        return this.ioEngine.askInput(
                                initiator, "name", this.enterLocationNameHelp);
                    })
                    .withOutputDestination((validationFail) -> {
                        this.ioEngine.report(initiator, validationFail);
                    });
        
        
            locationFinding: while ( true ) {            

                name = dialog
                        .withInitialArgument(name)
                        .validateAndGet();            

                if (name.isEmpty()) {
                    return valueFlowStopped();
                }

                foundLocations = this.daoLocations.getLocationsByNamePattern(initiator, name);
                if ( hasOne(foundLocations) ) {
                    foundLocation = getOne(foundLocations);
                    this.reportThatLocationFound(initiator, name, foundLocation);                    
                    return valueFlowDoneWith(foundLocation);
                } else if ( hasMany(foundLocations) ) {
                    weightedLocations = this.analyze.weightVariants(
                            name, entitiesToVariants(foundLocations));
                    if ( weightedLocations.isEmpty() ) {
                        this.ioEngine.report(initiator, format("not found by '%s'", name));
                        name = "";
                        continue locationFinding;
                    }
                    
                    Variant bestLocation = weightedLocations.best();
                    if ( bestLocation.value().equalsIgnoreCase(name) || 
                         bestLocation.hasEqualOrBetterWeightThan(PERFECT) ) {
                        foundLocation = foundLocations.get(bestLocation.index());
                        this.reportThatLocationFound(initiator, name, foundLocation);
                        return valueFlowDoneWith(foundLocation);
                    } else {
                        boolean hasMatch = this.daoPatternChoices.hasMatchOf(
                                initiator, name, bestLocation.value(), weightedLocations);
                        if ( hasMatch ) {
                            foundLocation = foundLocations.get(bestLocation.index());
                            this.reportThatLocationFound(initiator, name, foundLocation);
                            return valueFlowDoneWith(foundLocation);
                        } else {
                            answer = this.ioEngine.ask(
                                    initiator, weightedLocations, this.chooseOneLocationHelp);
                            if ( answer.isGiven() ) {
                                Location location = foundLocations.get(answer.index());
                                String pattern = name;
                                Variants variants = weightedLocations;
                                asyncDo(() -> {
                                    this.daoPatternChoices.save(
                                            initiator, pattern, location.name(), variants);
                                });
                                foundLocation = foundLocations.get(answer.index());
                                this.reportThatLocationFound(initiator, name, foundLocation);
                                return valueFlowDoneWith(foundLocation);
                            } else if ( answer.isRejection() ) {
                                return valueFlowStopped();
                            } else if ( answer.variantsAreNotSatisfactory() ) {
                                name = "";
                                continue locationFinding;
                            } else {
                                this.ioEngine.report(initiator, "cannot determine your answer.");
                                return valueFlowStopped();
                            }
                        }            
                    }
                } else {
                    this.ioEngine.report(initiator, format("not found by '%s'", name));
                    name = "";
                    continue locationFinding;
                }
            }
        } 
    }

    private void reportThatLocationFound(Initiator initiator, String name, Location location) {
        if ( ! name.equalsIgnoreCase(location.name()) ) {
            this.ioEngine.report(initiator, format("'%s' found.", location.name()));
        }
    }

    @Override
    public boolean isSubjectedTo(InvocationCommand command) {
        return this.subjectedCommandTypes.contains(command.type());
    }
    
    @Override
    public ValueFlow<Location> findByExactName(
            Initiator initiator, String exactName) {
        return valueFlowDoneWith(this.daoLocations.getLocationByExactName(initiator, exactName));
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
                return valueFlowDoneWith(location);
            } else {
                if ( this.analyze.isSatisfiable(namePattern, location.name()) ) {
                    return valueFlowDoneWith(location);
                } else {
                    return valueFlowDoneEmpty();
                }
            }                        
        } else if ( hasMany(locations) ) {
            return this.manageWithManyLocations(initiator, namePattern, locations);
        } else {
            return valueFlowDoneEmpty();
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
        Variants variants = this.analyze.weightVariants(pattern, entitiesToVariants(locations));
        if ( variants.isEmpty() ) {
            return valueFlowDoneEmpty();
        }
        
        Variant bestVariant = variants.best();
        if ( bestVariant.value().equalsIgnoreCase(pattern) || 
             bestVariant.hasEqualOrBetterWeightThan(PERFECT) ) {
            return valueFlowDoneWith(locations.get(bestVariant.index()));
        } else {
            boolean hasMatch = this.daoPatternChoices
                    .hasMatchOf(initiator, pattern, bestVariant.value(), variants);
            if ( hasMatch ) {
                return valueFlowDoneWith(locations.get(bestVariant.index()));
            } else {
                return this.askUserForLocationAndSaveChoice(
                        initiator, pattern, variants, locations);
            }            
        }        
    }    
    
    private ValueFlow<Location> askUserForLocationAndSaveChoice(
            Initiator initiator, 
            String pattern, 
            Variants variants, 
            List<Location> locations) {
        Answer answer = this.ioEngine.ask(
                initiator, variants, this.chooseOneLocationHelp);
        if ( answer.isGiven() ) {
            Location location = locations.get(answer.index());
            asyncDo(() -> {
                this.daoPatternChoices.save(initiator, pattern, location.name(), variants);
            });
            return valueFlowDoneWith(location);
        } else if ( answer.isRejection() ) {
            return valueFlowStopped();
        } else if ( answer.variantsAreNotSatisfactory() ) {
            return valueFlowDoneEmpty();
        } else {
            return valueFlowDoneEmpty();
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
            return voidFlowDone();
        } else {
            return voidFlowFail("DAO failed to save Location.");
        }     
    }

    private String discussLocationNewName(Initiator initiator, String name) {
        try (KeeperLoopValidationDialog dialog = this.dialogPool.give()) {
            return dialog
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
        }  
    }

    private String discussLocationNewPath(Initiator initiator, String path) {        
        try (KeeperLoopValidationDialog dialog = this.dialogPool.give()) {
            return dialog
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
        } 
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
            String possibleLocationName = extractLocationFromPath(newPath);
            String possibleTarget = extractTargetFromPath(newPath);
            Optional<Location> locationByName = this.daoLocations
                    .getLocationByExactName(initiator, possibleLocationName);
            if ( locationByName.isPresent() ) {
                this.searchIn(initiator, locationByName.get(), possibleTarget, validity, dialog);
            } else {
                ValueFlow<Location> locationFlow = findByNamePattern(
                        initiator, possibleLocationName);
                switch ( locationFlow.result() ) {
                    case DONE:
                        ValueFlowDone<Location> doneLocationFlow = locationFlow.asDone();
                        if ( doneLocationFlow.hasValue() ) {
                            Location location = doneLocationFlow.orThrow();
                            this.searchIn(initiator, location, possibleTarget, validity, dialog);
                        } else {
                            if ( doneLocationFlow.hasMessage() ) {
                                this.ioEngine.report(initiator, doneLocationFlow.message());
                            }
                            this.searchInLocationSubPathes(initiator, newPath, validity, dialog);
                        }
                        break;
                    case FAIL:
                        validity.failsWith(locationFlow.asFail().reason());  
                        break;     
                    case STOP:
                        dialog.stop();
                        validity.fails();
                        break;
                    default:
                        validity.failsWith("unkown result during locatiob by pattern search");           
                }
            }
        }

        return validity.get();
    }
    
    private void searchIn(
            Initiator initiator, 
            Location location, 
            String target, 
            UndefinedValidity validity, 
            KeeperLoopValidationDialog dialog) {
        ValueFlow<String> pathFlow = this.walker
                .lookingFor(FOLDERS_ONLY)
                .walkToFind(target)
                .in(location)
                .by(initiator)
                .andGetResult();
        
        switch ( pathFlow.result() ) {
            case DONE:
                ValueFlowDone<String> donePathFlow = pathFlow.asDone();
                if ( donePathFlow.hasValue() ) {
                    validity.ok();
                    dialog.replaceInput(joinToPath(location.path(), donePathFlow.orThrow()));
                } else {
                    String message = format("cannot find %s in %s", target, location.name());
                    validity.failsWith(message);
                }
                break;
            case FAIL:
                validity.failsWith(pathFlow.asFail().reason());  
                break;
            case STOP:
                dialog.stop();
                validity.fails();
                break;
            default:
                validity.failsWith("unkown result during locatiob by pattern search");
        }
    }

    private void searchInLocationSubPathes(
            Initiator initiator, 
            String newPath, 
            UndefinedValidity validity, 
            KeeperLoopValidationDialog dialog) {
        List<LocationSubPath> subPathes = this.daoLocationSubPaths
                .getSubPathesByPattern(initiator, newPath);
        
        if ( hasOne(subPathes) ) {
            LocationSubPath locationSubPath = getOne(subPathes);
            String locationAndSubPath = locationSubPath.name();
            boolean pathFound =
                    locationAndSubPath.equalsIgnoreCase(newPath) ||
                        this.analyze.isSatisfiable(newPath, locationAndSubPath);
            if ( pathFound ) {
                String realFoundPath = locationSubPath.fullPath();
                validity.set(defineNewPathValidityUsing(initiator, realFoundPath, dialog));
            } else {
                validity.failsWith(format("'%s' not found", newPath));
            }
        } else if ( hasMany(subPathes) ) {
            Variants variants = this.analyze.weightVariants(
                    newPath, entitiesToVariants(subPathes));
            if ( variants.isEmpty() ) {
                validity.failsWith(format("'%s' not found", newPath));
            } else {
                Variant best = variants.best();
                boolean bestIsGoodEnough =
                        best.value().equalsIgnoreCase(newPath) || 
                        best.hasEqualOrBetterWeightThan(PERFECT);
                if ( bestIsGoodEnough ) {
                    String realFoundPath = subPathes.get(best.index()).fullPath();
                    validity.set(defineNewPathValidityUsing(initiator, realFoundPath, dialog));
                } else {
                    boolean hasMatch = this.daoPatternChoices.hasMatchOf(
                            initiator, newPath, best.value(), variants);
                    if ( hasMatch ) {
                        String realFoundPath = subPathes.get(best.index()).fullPath();
                        validity.set(defineNewPathValidityUsing(   
                                initiator, realFoundPath, dialog));
                    } else {
                        Answer answer = this.ioEngine.ask(
                                initiator, variants, this.chooseOneSubPathHelp);
                        if ( answer.isGiven() ) {
                            LocationSubPath foundSubPath = subPathes.get(answer.index());
                            asyncDo(() -> {
                                this.daoPatternChoices.save(
                                        initiator, newPath, foundSubPath.name(), variants);
                            });
                            String realFoundPath = foundSubPath.fullPath();
                            validity.set(defineNewPathValidityUsing(
                                    initiator, realFoundPath, dialog));
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
                case DONE:
                    if ( locationFlow.asDone().hasValue() ) {
                        Location location = locationFlow.asDone().orThrow();
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
            return voidFlowDone();
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
                case DONE : {
                    if ( locationFlow.asDone().hasValue() ) {
                        location = locationFlow.asDone().orThrow();
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
            
            String propertyName;
            try (KeeperLoopValidationDialog dialog = this.dialogPool.give()) {
                if ( propertyToEdit.isDefined() ) {
                    dialog.withInitialArgument(propertyToEdit.name());
                }
                propertyName = dialog
                        .withRule((prop) -> {
                            EntityProperty property = argToProperty(prop);
                            if ( property.isUndefined() ) {
                                return validationFailsWith("not a property");
                            }
                            if ( property.isNotOneOf(NAME, FILE_PATH) ) {
                                return validationFailsWith("name or file path is expected");
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
            }
            
            propertyToEdit = argToProperty(propertyName);
            if ( propertyToEdit.isUndefined() ) {
                return voidFlowStopped();
            }

            switch ( propertyToEdit ) {
                case NAME : {
                    return this.editLocationName(initiator, location);
                }
                case FILE_PATH : {
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
            return voidFlowDone();
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
            return voidFlowDone();
        } else {
            return voidFlowFail("DAO failed to edit name.");
        }
    }

    @Override
    public VoidFlow replaceInPaths(
            Initiator initiator, String replaceable, String replacement) {        
        if ( this.daoLocations.replaceInPaths(initiator, replaceable, replacement) ) {
            return voidFlowDone();
        } else {
            return voidFlowFail("DAO failed to replace path fragment.");
        }
    }

    @Override
    public ValueFlow<Message> findAll(Initiator initiator) {
        return valueFlowDoneWith(entitiesToOptionalMessageWithHeader(
                    "all Locations:", this.daoLocations.getAllLocations(initiator)));
    }
}
