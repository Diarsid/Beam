/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package diarsid.beam.core.modules.domainkeeper;

import java.util.List;
import java.util.Optional;

import diarsid.beam.core.base.control.flow.ValueOperation;
import diarsid.beam.core.base.control.flow.VoidOperation;
import diarsid.beam.core.base.control.io.base.actors.Initiator;
import diarsid.beam.core.base.control.io.base.actors.InnerIoEngine;
import diarsid.beam.core.base.control.io.base.interaction.Answer;
import diarsid.beam.core.base.control.io.base.interaction.Choice;
import diarsid.beam.core.base.control.io.base.interaction.Question;
import diarsid.beam.core.base.control.io.commands.ArgumentsCommand;
import diarsid.beam.core.domain.entities.WebDirectory;
import diarsid.beam.core.domain.entities.WebDirectoryPages;
import diarsid.beam.core.domain.entities.WebPlace;
import diarsid.beam.core.domain.entities.metadata.EntityProperty;
import diarsid.beam.core.domain.inputparsing.webpages.WebDirectoryNameAndPlace;
import diarsid.beam.core.domain.inputparsing.webpages.WebDirectoryNamePlaceAndProperty;
import diarsid.beam.core.domain.inputparsing.webpages.WebObjectsInputParser;
import diarsid.beam.core.modules.data.DaoWebDirectories;

import static java.lang.String.format;

import static diarsid.beam.core.base.control.flow.Operations.valueFound;
import static diarsid.beam.core.base.control.flow.Operations.valueOperationFail;
import static diarsid.beam.core.base.control.flow.Operations.valueOperationStopped;
import static diarsid.beam.core.base.control.flow.Operations.voidCompleted;
import static diarsid.beam.core.base.control.flow.Operations.voidOperationFail;
import static diarsid.beam.core.base.control.flow.Operations.voidOperationStopped;
import static diarsid.beam.core.base.control.io.base.interaction.Question.question;
import static diarsid.beam.core.base.control.io.commands.CommandType.CREATE_WEB_DIR;
import static diarsid.beam.core.base.control.io.commands.CommandType.DELETE_WEB_DIR;
import static diarsid.beam.core.base.control.io.commands.CommandType.EDIT_WEB_DIR;
import static diarsid.beam.core.base.control.io.commands.CommandType.FIND_WEBDIRECTORY;
import static diarsid.beam.core.base.util.CollectionsUtils.getOne;
import static diarsid.beam.core.base.util.CollectionsUtils.hasMany;
import static diarsid.beam.core.base.util.CollectionsUtils.hasOne;
import static diarsid.beam.core.base.util.StringUtils.lower;
import static diarsid.beam.core.domain.entities.Orderables.reorderAccordingToNewOrder;
import static diarsid.beam.core.domain.entities.WebDirectories.newDirectory;
import static diarsid.beam.core.domain.entities.WebPlace.UNDEFINED_PLACE;
import static diarsid.beam.core.domain.entities.metadata.EntityProperty.NAME;
import static diarsid.beam.core.domain.entities.metadata.EntityProperty.ORDER;
import static diarsid.beam.core.domain.entities.metadata.EntityProperty.UNDEFINED_PROPERTY;
import static diarsid.beam.core.domain.entities.metadata.EntityProperty.WEB_PLACE;


class WebDirectoriesKeeperWorker 
        extends WebObjectsCommonKeeper 
        implements WebDirectoriesKeeper {
    
    private final DaoWebDirectories daoDirectories;
    private final InnerIoEngine ioEngine;
    private final KeeperDialogHelper helper;
    private final WebObjectsInputParser webObjectsParser;

    public WebDirectoriesKeeperWorker(
            DaoWebDirectories daoDirectories, 
            InnerIoEngine ioEngine, 
            KeeperDialogHelper helper, 
            WebObjectsInputParser webObjectsParser) {
        super(ioEngine);
        this.daoDirectories = daoDirectories;
        this.ioEngine = ioEngine;
        this.helper = helper;
        this.webObjectsParser = webObjectsParser;
    }
    
    @Override
    public VoidOperation createWebDirectory(
            Initiator initiator, ArgumentsCommand command) {
        if ( command.type().isNot(CREATE_WEB_DIR) ) {
            return voidOperationFail("wrong command type!");
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
                return voidOperationStopped();
            }
        }
        
        name = this.helper.validateEntityNameInteractively(initiator, name);
        if ( name.isEmpty() ) {
            return voidOperationStopped();
        }
        
        Optional<Integer> freeNameIndex = this.daoDirectories.freeNameNextIndex(initiator, name, place);
        if ( ! freeNameIndex.isPresent() ) {
            return voidOperationFail("cannot obtain free name index.");
        } 
        if ( freeNameIndex.get() > 0 ) {
            this.ioEngine.report(initiator, 
                    format("directory '%s' already exists in %s.", name, lower(place.name())));
            name = format("%s (%d)", name, freeNameIndex.get());
            this.ioEngine.report(initiator, format("name '%s' will be saved instead.", name));
        }
        
        WebDirectory directory = newDirectory(name, place);
        if ( this.daoDirectories.save(initiator, directory) ) {
            return voidCompleted();
        } else {
            return voidOperationFail("cannot save new directory.");
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
            this.ioEngine.report(initiator, "not found.");
        } while ( foundDirs.isEmpty() );
        
        if ( hasOne(foundDirs) ) {
            WebDirectory toRemove;
            toRemove = getOne(foundDirs);
            this.ioEngine.report(initiator, format("'%s' found.", toRemove.name()));
            return Optional.of(toRemove);
        } else if ( hasMany(foundDirs) ) {
            Question question = question("choose").withAnswerEntities(foundDirs);
            Answer answer = this.ioEngine.ask(initiator, question);
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
    public VoidOperation deleteWebDirectory(
            Initiator initiator, ArgumentsCommand command) {
        if ( command.type().isNot(DELETE_WEB_DIR) ) {
            return voidOperationFail("wrong command type!");
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
        
        Optional<WebDirectory> removed = this.discussExistingDirectoryBy(initiator, name, place);
        if ( ! removed.isPresent() ) {
            return voidOperationStopped();
        }
        
        this.ioEngine.report(initiator, "all pages in this directory will be removed also.");
        Choice choice = this.ioEngine.ask(initiator, "are you sure?");
        
        if ( choice.isPositive() ) {
            if ( daoDirectories.remove(initiator, removed.get().name(), removed.get().place()) ) {
                return voidCompleted();
            } else {
                return voidOperationFail("cannot remove directory.");
            }
        } else {
            return voidOperationStopped();
        }       
    }

    @Override
    public VoidOperation editWebDirectory(
            Initiator initiator, ArgumentsCommand command) {
        if ( command.type().isNot(EDIT_WEB_DIR) ) {
            return voidOperationFail("wrong command type!");
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
            return voidOperationStopped();
        }
        
        propertyToEdit = this.helper.validatePropertyInteractively(
                initiator, propertyToEdit, ORDER, WEB_PLACE, NAME);
        if ( propertyToEdit.isUndefined() ) {
            return voidOperationStopped();
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
                return voidOperationFail("undefined property.");
            }
        }
    }
    
    private VoidOperation editWebDirectoryOrder(
            Initiator initiator, WebDirectory directory) {
        List<WebDirectory> directories = 
                this.daoDirectories.getAllDirectoriesInPlace(initiator, directory.place());
        if ( directories.isEmpty() ) {
            return voidOperationFail(format(
                    "there are no directories in '%s'.", directory.place()));
        }
        int newOrder = this.helper.discussIntInRange(
                initiator, 0, directories.size() - 1, "new order");
        reorderAccordingToNewOrder(directories, directory.order(), newOrder);
        if ( this.daoDirectories.updateWebDirectoryOrders(initiator, directories) ) {
            return voidCompleted();
        } else {
            return voidOperationFail("cannot save reordered directories.");
        }        
    }
    
    private VoidOperation editWebDirectoryPlace(
            Initiator initiator, WebDirectory directory) {
        WebPlace destinationPlace = super.discussWebPlace(initiator);
        if ( destinationPlace.isUndefined() ) {
            return voidOperationStopped();
        }
        boolean moved = this.daoDirectories.moveDirectoryToPlace(
                initiator, directory.name(), directory.place(), destinationPlace);
        if ( moved ) {
            return voidCompleted();
        } else {
            return voidOperationFail("cannot move directory to new place.");
        }
    }
    
    private VoidOperation editWebDirectoryName(
            Initiator initiator, WebDirectory directory) {
        String newName = this.helper.validateEntityNameInteractively(initiator, "");
        if ( newName.isEmpty() ) {
            return voidOperationStopped();
        }
        Optional<Integer> freeNameIndex = 
                this.daoDirectories.freeNameNextIndex(initiator, newName, directory.place());
        if ( ! freeNameIndex.isPresent() ) {
            return voidOperationStopped();
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
            return voidCompleted();
        } else {
            return voidOperationFail("cannot rename directory.");
        }
    }

    @Override
    public ValueOperation<? extends WebDirectory> findWebDirectory(
            Initiator initiator, ArgumentsCommand command) {
        if ( command.type().isNot(FIND_WEBDIRECTORY) ) {
            return valueOperationFail("wrong command type!");
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
        
        Choice needWithPages = this.ioEngine.ask(initiator, "with pages");        
        
        Optional<WebDirectory> searched = this.discussExistingDirectoryBy(initiator, name, place);
        if ( ! searched.isPresent() ) {
            return valueOperationStopped();
        }
        
        if ( needWithPages.isPositive() ) {
            Optional<WebDirectoryPages> directoryWithPages = this.daoDirectories
                    .getDirectoryPagesByNameInPlace(
                            initiator, searched.get().name(), searched.get().place());
            if ( directoryWithPages.isPresent() ) {
                return valueFound(directoryWithPages);
            } else {
                return valueOperationFail("cannot get directory with pages.");
            }            
        } else {
            return valueFound(searched.get());
        }
    }    
}
