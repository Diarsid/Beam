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
import diarsid.beam.core.base.control.io.base.interaction.VariantsQuestion;
import diarsid.beam.core.base.control.io.base.interaction.WebResponse;
import diarsid.beam.core.base.control.io.commands.ArgumentsCommand;
import diarsid.beam.core.domain.entities.WebDirectory;
import diarsid.beam.core.domain.entities.WebDirectoryPages;
import diarsid.beam.core.domain.entities.WebPage;
import diarsid.beam.core.domain.entities.WebPlace;
import diarsid.beam.core.domain.entities.metadata.EntityProperty;
import diarsid.beam.core.domain.entities.validation.ValidationResult;
import diarsid.beam.core.domain.inputparsing.webpages.WebDirectoryNameAndPlace;
import diarsid.beam.core.domain.inputparsing.webpages.WebDirectoryNamePlaceAndProperty;
import diarsid.beam.core.domain.inputparsing.webpages.WebObjectsInputParser;
import diarsid.beam.core.modules.data.DaoWebDirectories;

import static java.lang.String.format;
import static java.util.Collections.sort;

import static diarsid.beam.core.base.control.flow.Flows.valueFlowCompletedWith;
import static diarsid.beam.core.base.control.flow.Flows.valueFlowFail;
import static diarsid.beam.core.base.control.flow.Flows.valueFlowStopped;
import static diarsid.beam.core.base.control.flow.Flows.voidFlowCompleted;
import static diarsid.beam.core.base.control.flow.Flows.voidFlowFail;
import static diarsid.beam.core.base.control.flow.Flows.voidFlowStopped;
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
import static diarsid.beam.core.domain.entities.validation.ValidationRule.ENTITY_NAME_RULE;


class WebDirectoriesKeeperWorker 
        extends WebObjectsCommonKeeper 
        implements WebDirectoriesKeeper {
    
    private final DaoWebDirectories daoDirectories;
    private final CommandsMemoryKeeper commandsMemory;
    private final InnerIoEngine ioEngine;
    private final Initiator systemInitiator;
    private final KeeperDialogHelper helper;
    private final WebObjectsInputParser webObjectsParser;
    private final Help chooseOneDirectoryHelp;
    private final Help withPagesChoiceHelp;
    private final Help deleteDirectoryConfirmationHelp;

    public WebDirectoriesKeeperWorker(
            DaoWebDirectories daoDirectories, 
            CommandsMemoryKeeper commandsMemory,
            InnerIoEngine ioEngine, 
            Initiator systemInitiator,
            KeeperDialogHelper helper, 
            WebObjectsInputParser webObjectsParser) {
        super(ioEngine);
        this.daoDirectories = daoDirectories;
        this.commandsMemory = commandsMemory;
        this.ioEngine = ioEngine;
        this.systemInitiator = systemInitiator;
        this.helper = helper;
        this.webObjectsParser = webObjectsParser;
        this.chooseOneDirectoryHelp = this.ioEngine.addToHelpContext(
                "Choose one WebDirectory.",
                "Use:",
                "   - number to choose WebDirectory",
                "   - WebDirectory name part to choose it",
                "   - n/no to see more variants, if any",
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
        
        name = this.helper.validateEntityNameInteractively(initiator, name);
        if ( name.isEmpty() ) {
            return voidFlowStopped();
        }
        
        Optional<Integer> freeNameIndex = this.daoDirectories.freeNameNextIndex(initiator, name, place);
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
    
    private Optional<WebDirectory> discussExistingDirectoryBy(
            Initiator initiator, String name, WebPlace place) {
        
        List<WebDirectory> foundDirs;
        do {
            name = this.helper.validateEntityNameInteractively(initiator, name);
            if ( name.isEmpty() ) {
                return Optional.empty();
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
            }
        } while ( foundDirs.isEmpty() );
        
        if ( hasOne(foundDirs) ) {
            WebDirectory toRemove;
            toRemove = getOne(foundDirs);
            this.ioEngine.report(initiator, format("'%s' found.", toRemove.name()));
            return Optional.of(toRemove);
        } else if ( hasMany(foundDirs) ) {
            VariantsQuestion question = question("choose").withAnswerEntities(foundDirs);
            Answer answer = this.ioEngine.ask(initiator, question, this.chooseOneDirectoryHelp);
            if ( answer.isGiven() ) {
                return Optional.of(foundDirs.get(answer.index()));
            } else {
                return Optional.empty();
            }
        } else {
            return Optional.empty();
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
        
        Optional<WebDirectory> directoryToRemove = 
                this.discussExistingDirectoryBy(initiator, name, place);
        if ( ! directoryToRemove.isPresent() ) {
            return voidFlowStopped();
        }
        
        this.ioEngine.report(initiator, "all WebPages in this WebDirectory will be removed also.");
        Choice choice = this.ioEngine.ask(
                initiator, "are you sure?", this.deleteDirectoryConfirmationHelp);
        
        Optional<WebDirectoryPages> pages = this.daoDirectories
                .getDirectoryPagesById(initiator, directoryToRemove.get().id());
        if ( choice.isPositive() ) {
            if ( daoDirectories.remove(
                    initiator, directoryToRemove.get().name(), directoryToRemove.get().place()) ) {
                this.asyncCleanCommandsMemory(initiator, pages.get().pages());
                return voidFlowCompleted();
            } else {
                return voidFlowFail("cannot remove WebDirectory.");
            }
        } else {
            return voidFlowStopped();
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
        
        Optional<WebDirectory> optEdited = this.discussExistingDirectoryBy(initiator, name, place);
        if ( ! optEdited.isPresent() ) {
            return voidFlowStopped();
        }
        
        propertyToEdit = this.helper.validatePropertyInteractively(
                initiator, propertyToEdit, ORDER, WEB_PLACE, NAME);
        if ( propertyToEdit.isUndefined() ) {
            return voidFlowStopped();
        }
        
        switch ( propertyToEdit ) {
            case ORDER : {
                return this.editWebDirectoryOrder(initiator, optEdited.get());
            }
            case WEB_PLACE : {
                return this.editWebDirectoryPlace(initiator, optEdited.get());
            }
            case NAME : {
                return this.editWebDirectoryName(initiator, optEdited.get());
            }
            default : {
                return voidFlowFail("undefined property.");
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
        String newName = this.helper.validateEntityNameInteractively(initiator, "");
        if ( newName.isEmpty() ) {
            return voidFlowStopped();
        }
        Optional<Integer> freeNameIndex = 
                this.daoDirectories.freeNameNextIndex(initiator, newName, directory.place());
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
    public ValueFlow<? extends WebDirectory> findWebDirectory(
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
        
        Optional<WebDirectory> searched = this.discussExistingDirectoryBy(initiator, name, place);
        if ( ! searched.isPresent() ) {
            return valueFlowStopped();
        }
        
        Choice needWithPages = this.ioEngine.ask(initiator, "with pages", this.withPagesChoiceHelp);   
        
        if ( needWithPages.isPositive() ) {
            Optional<WebDirectoryPages> directoryWithPages = 
                    this.daoDirectories.getDirectoryPagesById(initiator, searched.get().id());
            if ( directoryWithPages.isPresent() ) {
                return valueFlowCompletedWith(directoryWithPages);
            } else {
                return valueFlowFail("cannot get directory with pages.");
            }            
        } else {
            return valueFlowCompletedWith(searched);
        }
    }    

    @Override
    public WebResponse createWebDirectory(WebPlace place, String name) {
        ValidationResult nameValidity = ENTITY_NAME_RULE.applyTo(name);
        if ( nameValidity.isFail() ) {
            return badRequestWithJson(nameValidity.getFailureMessage());
        }
        
        Optional<Integer> freeNameIndex = this.daoDirectories
                .freeNameNextIndex(this.systemInitiator, name, place);
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
        
        ValidationResult newNameValidity = ENTITY_NAME_RULE.applyTo(newName);
        if ( newNameValidity.isFail() ) {
            return badRequestWithJson(newNameValidity.getFailureMessage());
        }
        
        Optional<Integer> freeNameIndex = this.daoDirectories
                .freeNameNextIndex(this.systemInitiator, newName, place);
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
