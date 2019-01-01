/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package diarsid.beam.core.modules.domainkeeper;

import java.util.List;
import java.util.Optional;

import diarsid.beam.core.base.control.flow.ValueFlow;
import diarsid.beam.core.base.control.flow.VoidFlow;
import diarsid.beam.core.base.control.io.base.actors.Initiator;
import diarsid.beam.core.base.control.io.base.actors.InnerIoEngine;
import diarsid.beam.core.base.control.io.base.interaction.Answer;
import diarsid.beam.core.base.control.io.base.interaction.Choice;
import diarsid.beam.core.base.control.io.base.interaction.Help;
import diarsid.beam.core.base.control.io.base.interaction.Message;
import diarsid.beam.core.base.control.io.base.interaction.VariantsQuestion;
import diarsid.beam.core.base.control.io.base.interaction.WebResponse;
import diarsid.beam.core.base.control.io.commands.ArgumentsCommand;
import diarsid.beam.core.domain.entities.WebDirectory;
import diarsid.beam.core.domain.entities.WebDirectoryPages;
import diarsid.beam.core.domain.entities.WebPage;
import diarsid.beam.core.domain.entities.WebPlace;
import diarsid.beam.core.domain.entities.metadata.EntityProperty;
import diarsid.beam.core.domain.inputparsing.webpages.WebDirectoryNameAndPlace;
import diarsid.beam.core.domain.inputparsing.webpages.WebDirectoryNamePlaceAndProperty;
import diarsid.beam.core.domain.inputparsing.webpages.WebObjectsInputParser;
import diarsid.beam.core.modules.responsivedata.ResponsiveDaoWebDirectories;

import static java.lang.String.format;
import static java.lang.String.join;
import static java.util.Collections.sort;
import static java.util.stream.Collectors.toList;

import static diarsid.beam.core.base.control.flow.Flows.valueFlowCompletedWith;
import static diarsid.beam.core.base.control.flow.Flows.valueFlowFail;
import static diarsid.beam.core.base.control.flow.Flows.valueFlowStopped;
import static diarsid.beam.core.base.control.flow.Flows.voidFlowCompleted;
import static diarsid.beam.core.base.control.flow.Flows.voidFlowFail;
import static diarsid.beam.core.base.control.flow.Flows.voidFlowStopped;
import static diarsid.beam.core.base.control.io.base.interaction.Messages.linesToOptionalMessageWithHeader;
import static diarsid.beam.core.base.control.io.base.interaction.VariantsQuestion.question;
import static diarsid.beam.core.base.control.io.base.interaction.WebResponse.badRequestWithJson;
import static diarsid.beam.core.base.control.io.base.interaction.WebResponse.notFoundWithJson;
import static diarsid.beam.core.base.control.io.base.interaction.WebResponse.ok;
import static diarsid.beam.core.base.control.io.base.interaction.WebResponse.okWithJson;
import static diarsid.beam.core.base.control.io.commands.CommandType.BROWSE_WEBPAGE;
import static diarsid.beam.core.base.control.io.commands.CommandType.CREATE_WEB_DIR;
import static diarsid.beam.core.base.control.io.commands.CommandType.DELETE_WEB_DIR;
import static diarsid.beam.core.base.control.io.commands.CommandType.EDIT_WEB_DIR;
import static diarsid.beam.core.base.control.io.commands.CommandType.FIND_WEBDIRECTORY;
import static diarsid.beam.core.base.control.io.interpreter.ControlKeys.UNACCEPTABLE_DOMAIN_CHARS;
import static diarsid.beam.core.base.util.CollectionsUtils.getOne;
import static diarsid.beam.core.base.util.CollectionsUtils.hasMany;
import static diarsid.beam.core.base.util.CollectionsUtils.hasOne;
import static diarsid.beam.core.base.util.ConcurrencyUtil.asyncDo;
import static diarsid.beam.core.base.util.OptionalUtil.isNotPresent;
import static diarsid.beam.core.base.util.StringUtils.lower;
import static diarsid.beam.core.domain.entities.Orderables.reorderAccordingToNewOrder;
import static diarsid.beam.core.domain.entities.WebDirectories.newDirectory;
import static diarsid.beam.core.domain.entities.WebPlace.UNDEFINED_PLACE;
import static diarsid.beam.core.domain.entities.metadata.EntityProperty.NAME;
import static diarsid.beam.core.domain.entities.metadata.EntityProperty.ORDER;
import static diarsid.beam.core.domain.entities.metadata.EntityProperty.UNDEFINED_PROPERTY;
import static diarsid.beam.core.domain.entities.metadata.EntityProperty.WEB_PLACE;
import static diarsid.beam.core.domain.entities.validation.DomainValidationRule.ENTITY_NAME_RULE;
import static diarsid.support.objects.Pools.giveBackToPool;
import static diarsid.support.objects.Pools.takeFromPool;

import diarsid.beam.core.domain.entities.validation.Validity;


class WebDirectoriesKeeperWorker 
        extends WebObjectsCommonKeeper 
        implements WebDirectoriesKeeper {
    
    private final Object allDirectoriesConsistencyLock;
    private final ResponsiveDaoWebDirectories daoDirectories;
    private final CommandsMemoryKeeper commandsMemory;
    private final InnerIoEngine ioEngine;
    private final Initiator systemInitiator;
    private final KeeperDialogHelper helper;
    private final WebObjectsInputParser webObjectsParser;
    private final Help enterExistingDirectoryNameHelp;
    private final Help enterNewDirectoryNameHelp;
    private final Help applyFreeIndexToNameHelp;
    private final Help chooseOneDirectoryHelp;
    private final Help withPagesChoiceHelp;
    private final Help deleteDirectoryConfirmationHelp;

    public WebDirectoriesKeeperWorker(
            ResponsiveDaoWebDirectories daoDirectories, 
            CommandsMemoryKeeper commandsMemory,
            InnerIoEngine ioEngine, 
            Initiator systemInitiator,
            KeeperDialogHelper helper, 
            WebObjectsInputParser webObjectsParser) {
        super(ioEngine);
        this.allDirectoriesConsistencyLock = new Object();
        this.daoDirectories = daoDirectories;
        this.commandsMemory = commandsMemory;
        this.ioEngine = ioEngine;
        this.systemInitiator = systemInitiator;
        this.helper = helper;
        this.webObjectsParser = webObjectsParser;        
        this.enterExistingDirectoryNameHelp = this.ioEngine.addToHelpContext(
                "Enter existing WebDirectory name or name pattern.",
                "Name cannot contain following chars: " + join("", UNACCEPTABLE_DOMAIN_CHARS)
        );
        this.enterNewDirectoryNameHelp = this.ioEngine.addToHelpContext(
                "Enter WebDirectory name.",
                "Name cannot contain following chars: " + join("", UNACCEPTABLE_DOMAIN_CHARS)
        );
        this.chooseOneDirectoryHelp = this.ioEngine.addToHelpContext(
                "Choose one WebDirectory.",
                "Use:",
                "   - number to choose WebDirectory",
                "   - WebDirectory name part to choose it",
                "   - n/no to see more variants, if any",
                "   - dot to break"
        );        
        this.applyFreeIndexToNameHelp = this.ioEngine.addToHelpContext(
                "This means that such name already exists. Thus you can choose ", 
                "to save directory name with index (+1) to avoid name duplication.",
                "Specify whether you want to save new name with given free index.",
                "Use: ",
                "   - y/yes/+ to agree",
                "   - n/no to enter other name",
                "   - dot to break"
        );
        this.withPagesChoiceHelp = this.ioEngine.addToHelpContext(
                "Choose whether you want to find all WebPages",
                "this WebDirectory contains or not.",
                "Use:",
                "   - y/yes/+ to find WebDirectory's pages",
                "   - n/no/. if not"
        );
        this.deleteDirectoryConfirmationHelp = this.ioEngine.addToHelpContext(
                "Choose wheter you want to remove entire WebDirectory. ",
                "That will remove all WebPages it contains too.",
                "This operation cannot be undone.",
                "Use:",
                "   - y/yes/+ to confirm removing",
                "   - n/no/. to cancel");
    }
    
    private void asyncCleanCommandsMemory(Initiator initiator, List<WebPage> pages) {
        asyncDo(() -> {
            pages.stream().forEach(page -> {
                this.commandsMemory.removeByExactExtendedAndType(
                        initiator, page.name(), BROWSE_WEBPAGE);
            });            
        });
    }
    
    private String discussWebDirectoryNewName(Initiator initiator, WebPlace place, String name) {
        if ( place.isUndefined() ) {
            return "";
        }
        KeeperLoopValidationDialog dialog = takeFromPool(KeeperLoopValidationDialog.class);
        Optional<Integer> freeNameIndex;
        Choice applyIndexChoice;
        String applyIndexQuestion;
        
        try { 
            dialog
                    .withRule(ENTITY_NAME_RULE)
                    .withInputSource(() -> {
                        return this.ioEngine.askInput(
                                initiator, "name", this.enterNewDirectoryNameHelp);
                    })
                    .withOutputDestination((validationFail) -> {
                        this.ioEngine.report(initiator, validationFail);
                    });

            nameDiscussing: while ( true ) {
                name = dialog
                        .withInitialArgument(name)
                        .validateAndGet();
                
                if ( name.isEmpty() ) {
                    return name;
                }                
                
                freeNameIndex = daoDirectories.findFreeNameNextIndex(initiator, name, place);
                if ( isNotPresent(freeNameIndex) ) {
                    this.ioEngine.report(initiator, "DAO failed to get free name index.");
                    return "";
                }
                if ( freeNameIndex.get() > 0 ) {
                    this.ioEngine.report(initiator, format("directory '%s' already exists.", name));
                    applyIndexQuestion = format(
                            "name directory as '%s (%s)'", name, freeNameIndex.get());
                    applyIndexChoice = this.ioEngine.ask(
                            initiator, applyIndexQuestion, this.applyFreeIndexToNameHelp);
                    if ( applyIndexChoice.isPositive() ) {
                        name = format("%s (%d)", name, freeNameIndex.get());
                        this.ioEngine.report(
                                initiator, format("name '%s' will be saved instead.", name));
                        break nameDiscussing;
                    } else if ( applyIndexChoice.isRejected() ) {
                        return "";
                    } else if ( applyIndexChoice.isNegative() ) {
                        name = "";
                        continue nameDiscussing;
                    }                    
                }
            }

            return name;
        } finally {
            giveBackToPool(dialog);
        }
    }
    
    @Override
    public VoidFlow createWebDirectory(
            Initiator initiator, ArgumentsCommand command) {
        if ( command.type().isNot(CREATE_WEB_DIR) ) {
            return voidFlowFail("wrong command type!");
        }
        
        WebPlace place;
        String name;
        if ( command.hasArguments() ) {
            WebDirectoryNameAndPlace namePlace = 
                    this.webObjectsParser.parseNameAndPlace(command.arguments());
            place = namePlace.place();
            name = namePlace.name();
        } else {
            place = UNDEFINED_PLACE;
            name = "";
        }
        
        if ( place.isUndefined() ) {
            place = super.discussWebPlace(initiator);
            if ( place.isUndefined() ) {
                return voidFlowStopped();
            }
        }
        
        synchronized ( this.allDirectoriesConsistencyLock ) {
            name = this.discussWebDirectoryNewName(initiator, place, name);
            if ( name.isEmpty() ) {
                return voidFlowStopped();
            }

            Optional<Integer> freeNameIndex = this.daoDirectories.findFreeNameNextIndex(initiator, name, place);
            if ( ! freeNameIndex.isPresent() ) {
                return voidFlowFail("cannot obtain free name index.");
            } 
            if ( freeNameIndex.get() > 0 ) {
                this.ioEngine.report(initiator, 
                        format("directory '%s' already exists in %s.", name, lower(place.name())));
                name = format("%s (%d)", name, freeNameIndex.get());
                this.ioEngine.report(initiator, format("name '%s' will be saved instead.", name));
            }

            WebDirectory directory = newDirectory(name, place);
            if ( this.daoDirectories.save(initiator, directory) ) {
                return voidFlowCompleted();
            } else {
                return voidFlowFail("cannot save new directory.");
            }
        }
    }
    
    private ValueFlow<WebDirectory> findExistingExistingDirectoryInternally(
            Initiator initiator, String name, WebPlace place) {
        
        List<WebDirectory> foundDirs;
        KeeperLoopValidationDialog dialog = takeFromPool(KeeperLoopValidationDialog.class);
        
        synchronized ( this.allDirectoriesConsistencyLock ) {
            try {
                dialog
                        .withInitialArgument(name)
                        .withRule(ENTITY_NAME_RULE)
                        .withInputSource(() -> {
                            return this.ioEngine.askInput(
                                    initiator, "name", this.enterExistingDirectoryNameHelp);
                        })
                        .withOutputDestination((validationFail) -> {
                            this.ioEngine.report(initiator, validationFail);
                        });
                
                directoriesSearching: while ( true ) {
                    name = dialog
                            .withInitialArgument(name)
                            .validateAndGet();
                    if ( name.isEmpty() ) {
                        return valueFlowStopped();
                    }        

                    if ( place.isDefined() ) {
                        foundDirs = this.daoDirectories
                                .findDirectoriesByPatternInPlace(initiator, name, place);                
                    } else {
                        foundDirs = this.daoDirectories
                                .findDirectoriesByPatternInAnyPlace(initiator, name);            
                    }
                    
                    if ( foundDirs.isEmpty() ) {
                        this.ioEngine.report(initiator, "not found.");
                        name = "";
                        continue directoriesSearching;
                    }

                    if ( hasOne(foundDirs) ) {
                        WebDirectory dir = getOne(foundDirs);
                        this.ioEngine.report(initiator, format("'%s' found.", dir.name()));
                        return valueFlowCompletedWith(dir);
                    } else if ( hasMany(foundDirs) ) {
                        VariantsQuestion question = question("choose").withAnswerEntities(foundDirs);
                        Answer answer = this.ioEngine.ask(initiator, question, this.chooseOneDirectoryHelp);
                        if ( answer.isGiven() ) {
                            return valueFlowCompletedWith(foundDirs.get(answer.index()));
                        } else if ( answer.isRejection() ) {
                            return valueFlowStopped();
                        } else if ( answer.variantsAreNotSatisfactory() ) {
                            name = "";
                            continue directoriesSearching;
                        } else {
                            this.ioEngine.report(initiator, "cannot determine your answer.");
                            return valueFlowStopped();
                        }
                    } else {
                        this.ioEngine.report(initiator, format("not found by '%s'", name));
                        name = "";
                        continue directoriesSearching;
                    } 
                }
            } finally {
                giveBackToPool(dialog);
            }
        }    
    }

    @Override
    public VoidFlow deleteWebDirectory(
            Initiator initiator, ArgumentsCommand command) {
        if ( command.type().isNot(DELETE_WEB_DIR) ) {
            return voidFlowFail("wrong command type!");
        }
        
        WebPlace place;
        String name;
        if ( command.hasArguments() ) {
            WebDirectoryNameAndPlace namePlace = 
                    this.webObjectsParser.parseNameAndPlace(command.arguments());
            place = namePlace.place();
            name = namePlace.name();
        } else {
            place = UNDEFINED_PLACE;
            name = "";
        }
        
        synchronized ( this.allDirectoriesConsistencyLock ) {
            ValueFlow<WebDirectory> directoryFlow = 
                    this.findExistingExistingDirectoryInternally(initiator, name, place);
            if ( directoryFlow.isNotCompletedWithValue() ) {
                return directoryFlow.toVoid();
            }

            WebDirectory directory = directoryFlow.asComplete().orThrow();
            this.ioEngine.report(
                    initiator, "all WebPages in this WebDirectory will be removed also.");
            Choice choice = this.ioEngine.ask(
                    initiator, "are you sure?", this.deleteDirectoryConfirmationHelp);

            Optional<WebDirectoryPages> pages = this.daoDirectories
                    .getDirectoryPagesById(initiator, directory.id());
            if ( choice.isPositive() ) {
                if ( daoDirectories.remove(
                        initiator, directory.name(), directory.place()) ) {
                    this.asyncCleanCommandsMemory(initiator, pages.get().pages());
                    return voidFlowCompleted();
                } else {
                    return voidFlowFail("cannot remove WebDirectory.");
                }
            } else {
                return voidFlowStopped();
            }    
        }    
    }

    @Override
    public VoidFlow editWebDirectory(
            Initiator initiator, ArgumentsCommand command) {
        if ( command.type().isNot(EDIT_WEB_DIR) ) {
            return voidFlowFail("wrong command type!");
        }
        
        String name;
        WebPlace place;
        EntityProperty propertyToEdit;
        if ( command.hasArguments() ) {
            WebDirectoryNamePlaceAndProperty input = 
                    this.webObjectsParser.parseNamePlaceAndProperty(command.arguments());
            name = input.name();
            place = input.place();
            propertyToEdit = input.property();
        } else {
            name = "";
            place = UNDEFINED_PLACE;
            propertyToEdit = UNDEFINED_PROPERTY;
        }
        
        synchronized ( this.allDirectoriesConsistencyLock ) {
            ValueFlow<WebDirectory> directoryFlow = 
                    this.findExistingExistingDirectoryInternally(initiator, name, place);
            if ( directoryFlow.isNotCompletedWithValue() ) {
                return directoryFlow.toVoid();
            }

            WebDirectory directory = directoryFlow.asComplete().orThrow();

            propertyToEdit = this.helper.validatePropertyInteractively(
                    initiator, propertyToEdit, ORDER, WEB_PLACE, NAME);
            if ( propertyToEdit.isUndefined() ) {
                return voidFlowStopped();
            }

            switch ( propertyToEdit ) {
                case ORDER : {
                    return this.editWebDirectoryOrder(initiator, directory);
                }
                case WEB_PLACE : {
                    return this.editWebDirectoryPlace(initiator, directory);
                }
                case NAME : {
                    return this.editWebDirectoryName(initiator, directory);
                }
                default : {
                    return voidFlowFail("undefined property.");
                }
            }
        }
    }
    
    private VoidFlow editWebDirectoryOrder(
            Initiator initiator, WebDirectory directory) {
        List<WebDirectory> directories = 
                this.daoDirectories.getAllDirectoriesInPlace(initiator, directory.place());
        if ( directories.isEmpty() ) {
            return voidFlowFail(format(
                    "there are no directories in '%s'.", directory.place()));
        }
        int newOrder = this.helper.discussIntInRange(
                initiator, 0, directories.size() - 1, "new order");
        reorderAccordingToNewOrder(directories, directory.order(), newOrder);
        sort(directories);
        if ( this.daoDirectories.updateWebDirectoryOrders(initiator, directories) ) {
            return voidFlowCompleted();
        } else {
            return voidFlowFail("cannot save reordered directories.");
        }        
    }
    
    private VoidFlow editWebDirectoryPlace(
            Initiator initiator, WebDirectory directory) {
        WebPlace destinationPlace = super.discussWebPlace(initiator);
        if ( destinationPlace.isUndefined() ) {
            return voidFlowStopped();
        }
        boolean moved = this.daoDirectories.moveDirectoryToPlace(
                initiator, directory.name(), directory.place(), destinationPlace);
        if ( moved ) {
            return voidFlowCompleted();
        } else {
            return voidFlowFail("cannot move directory to new place.");
        }
    }
    
    private VoidFlow editWebDirectoryName(
            Initiator initiator, WebDirectory directory) {
        String newName = this.discussWebDirectoryNewName(initiator, directory.place(), "");
        if ( newName.isEmpty() ) {
            return voidFlowStopped();
        }
        Optional<Integer> freeNameIndex = 
                this.daoDirectories.findFreeNameNextIndex(initiator, newName, directory.place());
        if ( ! freeNameIndex.isPresent() ) {
            return voidFlowStopped();
        }
        if ( freeNameIndex.get() > 0 ) {
            this.ioEngine.report(initiator, format(
                    "directory '%s' already exists in %s.", 
                    newName, 
                    lower(directory.place().name())));
            newName = format("%s (%d)", newName, freeNameIndex.get());
            this.ioEngine.report(initiator, format("name '%s' will be saved instead.", newName));
        }
        boolean renamed = this.daoDirectories
                .editDirectoryName(initiator, directory.name(), directory.place(), newName);
        if ( renamed ) {
            return voidFlowCompleted();
        } else {
            return voidFlowFail("cannot rename directory.");
        }
    }

    @Override
    public ValueFlow<WebDirectory> findWebDirectory(
            Initiator initiator, ArgumentsCommand command) {
        if ( command.type().isNot(FIND_WEBDIRECTORY) ) {
            return valueFlowFail("wrong command type!");
        }
        
        String name;
        WebPlace place;
        if ( command.hasArguments() ) {
            WebDirectoryNameAndPlace namePlace = 
                    this.webObjectsParser.parseNameAndPlace(command.arguments());
            name = namePlace.name();
            place = namePlace.place();
        } else {
            name = "";
            place = UNDEFINED_PLACE;
        }     
        
        ValueFlow<WebDirectory> directoryFlow = 
                this.findExistingExistingDirectoryInternally(initiator, name, place);
        if ( directoryFlow.isNotCompletedWithValue() ) {
            return directoryFlow;
        }
        
        Choice needWithPages = this.ioEngine.ask(initiator, "with pages", this.withPagesChoiceHelp);   
        
        if ( needWithPages.isPositive() ) {
            int dirId = directoryFlow.asComplete().orThrow().id();
            Optional<WebDirectoryPages> directoryWithPages = 
                    this.daoDirectories.getDirectoryPagesById(initiator, dirId);
            if ( directoryWithPages.isPresent() ) {
                return valueFlowCompletedWith(directoryWithPages.get());
            } else {
                return valueFlowFail("cannot get directory with pages.");
            }            
        } else {
            return directoryFlow;
        }
    }    

    @Override
    public ValueFlow<Message> showAll(Initiator initiator) {
        List<String> dirs = this.daoDirectories
                .getAllDirectories(initiator)
                .stream()
                .sorted()
                .map(webDir -> format("%s > %s", webDir.place().displayName(), webDir.name()))
                .collect(toList());
        return valueFlowCompletedWith(linesToOptionalMessageWithHeader(
                "all WebDirectories:", dirs));
    }
            
    @Override
    public WebResponse createWebDirectory(WebPlace place, String name) {
        Validity nameValidity = ENTITY_NAME_RULE.applyTo(name);
        if ( nameValidity.isFail() ) {
            return badRequestWithJson(nameValidity.getFailureMessage());
        }
        
        Optional<Integer> freeNameIndex = this.daoDirectories
                .findFreeNameNextIndex(this.systemInitiator, name, place);
        if ( ! freeNameIndex.isPresent() ) {
            return badRequestWithJson("Cannot get free name index.");
        } 
        if ( freeNameIndex.get() > 0 ) {
            name = format("%s (%d)", name, freeNameIndex.get());
        }
        
        WebDirectory directory = newDirectory(name, place);        
        boolean saved = this.daoDirectories.save(this.systemInitiator, directory);
        if ( saved ) {
            return ok();
        } else {
            return badRequestWithJson(format("Directory '%s' not saved in %s", name, place.name()));
        }
    }

    @Override
    public WebResponse deleteWebDirectory(WebPlace place, String name) {
        boolean removed = this.daoDirectories.remove(this.systemInitiator, name, place);
        if ( removed ) {
            return ok();
        } else {
            return badRequestWithJson(
                    format("Directory '%s' not removed from %s", name, place.name()));
        }
    }

    @Override
    public WebResponse editWebDirectoryName(WebPlace place, String name, String newName) {
        Optional<WebDirectory> directory = this.daoDirectories
                .getDirectoryByNameAndPlace(this.systemInitiator, name, place);
        
        if ( isNotPresent(directory) ) {
            return notFoundWithJson(format("Directory '%s' not found in %s", name, place.name()));
        } 
        
        Validity newNameValidity = ENTITY_NAME_RULE.applyTo(newName);
        if ( newNameValidity.isFail() ) {
            return badRequestWithJson(newNameValidity.getFailureMessage());
        }
        
        Optional<Integer> freeNameIndex = this.daoDirectories
                .findFreeNameNextIndex(this.systemInitiator, newName, place);
        if ( ! freeNameIndex.isPresent() ) {
            return badRequestWithJson("Cannot get free name index.");
        } 
        if ( freeNameIndex.get() > 0 ) {
            newName = format("%s (%d)", newName, freeNameIndex.get());
        }
        
        boolean edited = this.daoDirectories
                .editDirectoryName(this.systemInitiator, name, place, newName);
        if ( edited ) {
            return ok();
        } else {
            return badRequestWithJson(format(
                    "Directory '%s' name not changed", directory.get().name(), place.name()));
        }
    }

    @Override
    public WebResponse editWebDirectoryPlace(WebPlace place, String name, WebPlace newPlace) {
        if ( place.is(newPlace) ) {
            return ok();
        }
        
        Optional<WebDirectory> directory = this.daoDirectories
                .getDirectoryByNameAndPlace(this.systemInitiator, name, place);
        
        if ( isNotPresent(directory) ) {
            return notFoundWithJson(format("Directory '%s' not found in %s", name, place.name()));
        } 
        
        boolean moved = this.daoDirectories.moveDirectoryToPlace(
                this.systemInitiator, directory.get().name(), place, newPlace);
        if ( moved ) {
            return ok();
        } else {
            return badRequestWithJson(format(
                    "Directory '%s' not moved to %s", directory.get().name(), newPlace.name()));
        }
    }

    @Override
    public WebResponse editWebDirectoryOrder(WebPlace place, String name, int newOrder) {
        List<WebDirectory> directories = this.daoDirectories
                .getAllDirectoriesInPlace(this.systemInitiator, place);
        
        Optional<WebDirectory> directory = directories
                .stream()
                .filter(dir -> dir.name().equalsIgnoreCase(name))
                .findFirst();
        
        if ( isNotPresent(directory) ) {
            return notFoundWithJson(format("Directory '%s' not found in %s", name, place.name()));
        } 
        
        reorderAccordingToNewOrder(directories, directory.get().order(), newOrder);
        sort(directories);
        
        boolean changed = this.daoDirectories
                .updateWebDirectoryOrders(this.systemInitiator, directories);
        if ( changed ) {
            return ok();
        } else {
            return badRequestWithJson(format(
                    "Directory '%s' not moved to %s order", directory.get().name(), newOrder));
        }
    }

    @Override
    public WebResponse getWebDirectory(WebPlace place, String name) {
        Optional<WebDirectory> directory = this.daoDirectories
                .getDirectoryByNameAndPlace(this.systemInitiator, name, place);
        
        if ( directory.isPresent() ) {
            return okWithJson(directory.get());
        } else {
            return notFoundWithJson(format("Directory '%s' not found in %s", name, place.name()));
        }
    }

    @Override
    public WebResponse getWebDirectoryPages(WebPlace place, String name) {
        Optional<WebDirectoryPages> directory = this.daoDirectories
                .getDirectoryPagesByNameAndPlace(this.systemInitiator, name, place);
        
        if ( directory.isPresent() ) {
            return okWithJson(directory.get());
        } else {
            return notFoundWithJson(format("Directory '%s' not found in %s", name, place.name()));
        }
    }

    @Override
    public WebResponse getAllDirectoriesInPlace(WebPlace place) {
        List<WebDirectoryPages> directories = this.daoDirectories
                .getAllDirectoriesPagesInPlace(this.systemInitiator, place);
        
        return okWithJson(directories);
    }
}
