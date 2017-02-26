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
import diarsid.beam.core.base.control.io.base.interaction.Question;
import diarsid.beam.core.base.control.io.commands.ArgumentsCommand;
import diarsid.beam.core.domain.entities.WebDirectory;
import diarsid.beam.core.domain.entities.WebPage;
import diarsid.beam.core.domain.entities.WebPlace;
import diarsid.beam.core.domain.entities.metadata.EntityProperty;
import diarsid.beam.core.domain.entities.validation.ValidationResult;
import diarsid.beam.core.domain.inputparsing.webpages.WebObjectsInputParser;
import diarsid.beam.core.domain.inputparsing.webpages.WebPageNameUrlAndPlace;
import diarsid.beam.core.modules.data.DaoWebDirectories;
import diarsid.beam.core.modules.data.DaoWebPages;

import static java.lang.String.format;

import static diarsid.beam.core.base.control.flow.Operations.valueFound;
import static diarsid.beam.core.base.control.flow.Operations.valueOperationFail;
import static diarsid.beam.core.base.control.flow.Operations.valueOperationStopped;
import static diarsid.beam.core.base.control.flow.Operations.voidCompleted;
import static diarsid.beam.core.base.control.flow.Operations.voidOperationFail;
import static diarsid.beam.core.base.control.flow.Operations.voidOperationStopped;
import static diarsid.beam.core.base.control.io.base.interaction.Question.question;
import static diarsid.beam.core.base.control.io.commands.CommandType.CREATE_PAGE;
import static diarsid.beam.core.base.control.io.commands.CommandType.DELETE_PAGE;
import static diarsid.beam.core.base.control.io.commands.CommandType.EDIT_PAGE;
import static diarsid.beam.core.base.control.io.commands.CommandType.FIND_PAGE;
import static diarsid.beam.core.base.util.CollectionsUtils.getOne;
import static diarsid.beam.core.base.util.CollectionsUtils.hasOne;
import static diarsid.beam.core.base.util.StringUtils.nonEmpty;
import static diarsid.beam.core.domain.entities.Orderables.reorderAccordingToNewOrder;
import static diarsid.beam.core.domain.entities.WebPages.newWebPage;
import static diarsid.beam.core.domain.entities.WebPlace.UNDEFINED_PLACE;
import static diarsid.beam.core.domain.entities.WebPlace.parsePlace;
import static diarsid.beam.core.domain.entities.metadata.EntityProperty.NAME;
import static diarsid.beam.core.domain.entities.metadata.EntityProperty.ORDER;
import static diarsid.beam.core.domain.entities.metadata.EntityProperty.SHORTCUTS;
import static diarsid.beam.core.domain.entities.metadata.EntityProperty.UNDEFINED_PROPERTY;
import static diarsid.beam.core.domain.entities.metadata.EntityProperty.WEB_DIRECTORY;
import static diarsid.beam.core.domain.entities.metadata.EntityProperty.WEB_URL;
import static diarsid.beam.core.domain.entities.validation.ValidationResults.fail;
import static diarsid.beam.core.domain.entities.validation.ValidationRule.ENTITY_NAME_RULE;
import static diarsid.beam.core.domain.entities.validation.ValidationRule.WEB_URL_RULE;


public class WebPagesKeeperWorker implements WebPagesKeeper {
    
    private final DaoWebPages daoPages;
    private final DaoWebDirectories daoDirectories;
    private final InnerIoEngine ioEngine;
    private final KeeperDialogHelper helper;
    private final WebObjectsInputParser parser;
    
    public WebPagesKeeperWorker(
            DaoWebPages dao, 
            DaoWebDirectories daoDirectories,
            InnerIoEngine ioEngine, 
            KeeperDialogHelper helper,
            WebObjectsInputParser parser) {
        this.daoPages = dao;
        this.daoDirectories = daoDirectories;
        this.ioEngine = ioEngine;
        this.helper = helper;
        this.parser = parser;
    }
    
    private WebPlace discussWebPlace(Initiator initiator) {
        String placeInput;
        WebPlace place = UNDEFINED_PLACE;
        placeDefining: while ( place.isUndefined() ) {     
            placeInput = this.ioEngine.askInput(initiator, "place");
            if ( placeInput.isEmpty() ) {
                break placeDefining;
            }
            place = parsePlace(placeInput);
        }
        return place;
    }

    private String discussShortcuts(Initiator initiator) {
        String shortcuts = this.ioEngine.askInput(initiator, "shortcuts, if any");
        if ( nonEmpty(shortcuts) ) {
            ValidationResult shortcutsValidity = fail();
            validation: while ( shortcutsValidity.isFail() ) {
                shortcuts = this.ioEngine.askInput(initiator, "shortcuts");
                if ( shortcuts.isEmpty() ) {
                    break validation;
                } else {
                    shortcutsValidity = ENTITY_NAME_RULE.apply(shortcuts);
                    if ( shortcutsValidity.isFail() ) {
                        this.ioEngine.report(initiator, shortcutsValidity.getFailureMessage());
                    }
                }                
            }
        }
        return shortcuts;
    }
    
    private Optional<WebPage> discussPage(Initiator initiator, String pagePattern) {
        Optional<WebPage> optPage = Optional.empty();
        List<WebPage> foundPages;
        boolean pageNotDefined = true;
        Question question;
        directoryDefining: while ( pageNotDefined ) {  
            if ( pagePattern.isEmpty() ) {
                pagePattern = this.ioEngine.askInput(initiator, "page name");
            }            
            if ( pagePattern.isEmpty() ) {
                optPage = Optional.empty();
                pageNotDefined = false;
            }
            pagePattern = this.helper.validateEntityNameInteractively(initiator, pagePattern);

            foundPages = this.daoPages.findByPattern(initiator, pagePattern);

            if ( foundPages.isEmpty() ) {
                this.ioEngine.report(initiator, "page not found.");
            } else if ( hasOne(foundPages) ) {
                optPage = Optional.of(getOne(foundPages));
                pageNotDefined = false;
            } else {
                question = question("choose page").withAnswerEntities(foundPages);
                Answer answer = this.ioEngine.ask(initiator, question);
                if ( answer.isGiven() ) {
                    optPage = foundPages
                            .stream()
                            .filter(page -> page.name().equalsIgnoreCase(answer.text()))
                            .findFirst();
                } else {
                    optPage = Optional.empty();
                }
                pageNotDefined = false;
            }
        }
        return optPage;
    }
    
    private String discussDirectoryName(Initiator initiator, WebPlace place) {
        String directoryName = "";
        List<WebDirectory> foundDirectories;
        boolean directoryNotDefined = true;
        Question question;
        Answer answer;
        directoryDefining: while ( directoryNotDefined ) {            
            directoryName = this.ioEngine.askInput(initiator, "directory name");
            if ( directoryName.isEmpty() ) {
                return "";
            }
            directoryName = this.helper.validateEntityNameInteractively(initiator, directoryName);

            foundDirectories = this.daoDirectories
                    .findDirectoriesByPatternInPlace(initiator, directoryName, place);

            if ( foundDirectories.isEmpty() ) {
                this.ioEngine.report(initiator, "directory not found.");
            } else if ( hasOne(foundDirectories) ) {
                directoryName = getOne(foundDirectories).name();
                directoryNotDefined = false;
            } else {
                question = question("choose directory").withAnswerEntities(foundDirectories);
                answer = this.ioEngine.ask(initiator, question);
                if ( answer.isGiven() ) {
                    directoryName = answer.text();
                } else {
                    directoryName = "";
                }
                directoryNotDefined = false;
            }
        }
        return directoryName;
    }
    
    private Optional<WebDirectory> discussWebDirectory(Initiator initiator) {
        WebPlace place = this.discussWebPlace(initiator);
        if ( place.isUndefined() ) {
            return Optional.empty();
        }
        
        String directoryName = "";
        Optional<WebDirectory> optDirectory = Optional.empty();
        List<WebDirectory> foundDirectories;
        boolean directoryNotDefined = true;
        Question question;        
        directoryDefining: while ( directoryNotDefined ) {            
            directoryName = this.ioEngine.askInput(initiator, "directory name");
            if ( directoryName.isEmpty() ) {
                return Optional.empty();                
            }
            directoryName = this.helper.validateEntityNameInteractively(initiator, directoryName);

            foundDirectories = this.daoDirectories
                    .findDirectoriesByPatternInPlace(initiator, directoryName, place);

            if ( foundDirectories.isEmpty() ) {
                this.ioEngine.report(initiator, "directory not found.");
            } else if ( hasOne(foundDirectories) ) {
                optDirectory = Optional.of(getOne(foundDirectories));
                directoryNotDefined = false;
            } else {
                question = question("choose directory").withAnswerEntities(foundDirectories);
                Answer answer = this.ioEngine.ask(initiator, question);
                if ( answer.isGiven() ) {
                    optDirectory = foundDirectories
                            .stream()
                            .filter(dir -> dir.name().equalsIgnoreCase(answer.text()))
                            .findFirst();
                } else {
                    return Optional.empty();
                }
                directoryNotDefined = false;
            }
        }
        return optDirectory;
    }

    @Override
    public VoidOperation createWebPage(
            Initiator initiator, ArgumentsCommand command) {
        if ( command.type().isNot(CREATE_PAGE) ) {
            return voidOperationFail("wrong command type!");
        }
        
        WebPlace place;
        String name;
        String url;
        if ( command.hasArguments() ) {
            WebPageNameUrlAndPlace data = this.parser.parseNameUrlAndPlace(command.arguments());
            place = data.place();
            name = data.name();
            url = data.url();
        } else {
            place = UNDEFINED_PLACE;
            name = "";
            url = "";
        }
        
        name = this.helper.validateEntityNameInteractively(initiator, name);
        if ( name.isEmpty() ) {
            return voidOperationStopped();
        }
        Optional<Integer> freeNameIndex = daoPages.freeNameNextIndex(initiator, name);
        if ( ! freeNameIndex.isPresent() ) {
            return voidOperationFail("DAO failed to get free name index.");
        }
        if ( freeNameIndex.get() > 0 ) {
            name = format("%s (%d)", name, freeNameIndex.get());
        }
        
        url = this.helper.validateInteractively(initiator, url, "url", WEB_URL_RULE);
        if ( url.isEmpty() ) {
            return voidOperationStopped();
        }
        
        if ( place.isUndefined() ) {
            place = this.discussWebPlace(initiator);
            if ( place.isUndefined() ) {
                return voidOperationStopped();
            }
        }
        
        String shortcuts = this.discussShortcuts(initiator);
        
        String directoryName = this.discussDirectoryName(initiator, place);
        if ( directoryName.isEmpty() ) {
            return voidOperationStopped();
        }        
        
        WebPage page = newWebPage(name, shortcuts, url, place, directoryName);
        if ( daoPages.save(initiator, page) ) {
            return voidCompleted();
        } else {
            return voidOperationFail("DAO failed to save new page.");
        }
    }

    @Override
    public VoidOperation editWebPage(
            Initiator initiator, ArgumentsCommand command) {
        if ( command.type().isNot(EDIT_PAGE) ) {
            return voidOperationFail("wrong command type!");
        }
        
        String pagePattern;
        if ( command.hasArguments() ) {
            pagePattern = command.joinedArguments();
        } else {
            pagePattern = "";
        }
        
        Optional<WebPage> optPage = this.discussPage(initiator, pagePattern);
        if ( ! optPage.isPresent() ) {
            return voidOperationStopped();
        }
        WebPage page = optPage.get();
        
        EntityProperty propertyToEdit = UNDEFINED_PROPERTY;
        propertyToEdit = this.helper.validatePropertyInteractively(
                initiator, propertyToEdit, SHORTCUTS, WEB_URL, NAME, ORDER, WEB_DIRECTORY);
        if ( propertyToEdit.isUndefined() ) {
            return voidOperationStopped();
        }
        
        switch ( propertyToEdit ) {
            case NAME : {
                return this.editPageName(initiator, page.name());
            }
            case SHORTCUTS : {
                return this.editPageShortcuts(initiator, page.name());
            }
            case WEB_URL : {
                return this.editPageUrl(initiator, page.name());
            }
            case ORDER : {
                return this.editPageOrder(initiator, page);
            }
            case WEB_DIRECTORY : {
                return this.editPageWebDirectory(initiator, page);
            }
            default : {
                return voidOperationFail("undefined property.");
            }
        }        
    }
    
    private VoidOperation editPageName(Initiator initiator, String pageName) {
        
    }
    
    private VoidOperation editPageShortcuts(Initiator initiator, String pageName) {
        String shortcuts = this.discussShortcuts(initiator);
        if ( shortcuts.isEmpty() ) {
            return voidOperationStopped();
        }
        if ( this.daoPages.editShortcuts(initiator, pageName, shortcuts) ) {
            return voidCompleted();
        } else {
            return voidOperationFail("DAO failed to change shortcuts.");
        }
    }
    
    private VoidOperation editPageUrl(Initiator initiator, String pageName) {
        String url = "";
        url = this.helper.validateInteractively(initiator, url, "url", WEB_URL_RULE);
        if ( url.isEmpty() ) {
            return voidOperationStopped();
        }
        if ( this.daoPages.editUrl(initiator, pageName, url) ) {
            return voidCompleted();
        } else {
            return voidOperationFail("DAO failed to change url.");
        }
    }
    
    private VoidOperation editPageOrder(Initiator initiator, WebPage page) {
        List<WebPage> pagesInDirectory = this.daoPages
                .allFromDirectory(initiator, page.directoryName(), page.place());
        int pageNewOrder = this.helper.discussIntInRange(
                initiator, 0, pagesInDirectory.size() - 1, "new order");
        if ( pageNewOrder < 0 ) {
            return voidOperationStopped();
        }
        reorderAccordingToNewOrder(pagesInDirectory, page.order(), pageNewOrder);
        if ( this.daoPages.updatePageOrdersInDir(initiator, pagesInDirectory) ) {
            return voidCompleted();
        } else {
            return voidOperationFail("DOA failed to reorder directory with new page order.");
        }
    }
    
    private VoidOperation editPageWebDirectory(Initiator initiator, WebPage page) {
        this.ioEngine.report(initiator, "choosing new page directory...");
        Optional<WebDirectory> optDirectory = this.discussWebDirectory(initiator);
        if ( ! optDirectory.isPresent() ) {
            return voidOperationStopped();
        }
        
        boolean pageMoved = this.daoPages.movePageFromDirToDir(
                initiator, 
                page.name(), 
                page.directoryName(), 
                page.place(), 
                optDirectory.get().name(), 
                optDirectory.get().place());
        
        if ( pageMoved ) {
            return voidCompleted();
        } else {
            return voidOperationFail("DAO failed to move page to new directory.");
        }
    }

    @Override
    public VoidOperation removeWebPage(
            Initiator initiator, ArgumentsCommand command) {
        if ( command.type().isNot(DELETE_PAGE) ) {
            return voidOperationFail("wrong command type!");
        }
        
        String pagePattern;
        if ( command.hasArguments() ) {
            pagePattern = command.joinedArguments();
        } else {
            pagePattern = "";
        }
        
        Optional<WebPage> optPage = this.discussPage(initiator, pagePattern);
        if ( ! optPage.isPresent() ) {
            return voidOperationStopped();
        }
        
        if ( this.daoPages.remove(initiator, optPage.get().name()) ) {
            return voidCompleted();
        } else {
            return voidOperationFail("DAO failed to remove page.");
        }
    }

    @Override
    public ValueOperation<WebPage> findWebPageByPattern(
            Initiator initiator, ArgumentsCommand command) {
        if ( command.type().isNot(FIND_PAGE) ) {
            return valueOperationFail("wrong command type!");
        }
        
        String pagePattern;
        if ( command.hasArguments() ) {
            pagePattern = command.joinedArguments();
        } else {
            pagePattern = "";
        }
        
        Optional<WebPage> optPage = this.discussPage(initiator, pagePattern);
        if ( optPage.isPresent() ) {
            return valueFound(optPage.get());
        } else {
            return valueOperationStopped();
        }
    }

    @Override
    public List<WebPage> findWebPagesByPattern(
            Initiator initiator, String pattern) {
        return this.daoPages.findByPattern(initiator, pattern);
    }

    @Override
    public Optional<WebPage> getWebPageByName(
            Initiator initiator, String name) {
        return this.daoPages.getByName(initiator, name);
    }

    @Override
    public boolean createWebPage(
            Initiator initiator, String name, String url, WebPlace place, String directory) {
        Optional<Integer> freeNameIndex = daoPages.freeNameNextIndex(initiator, name);
        if ( ! freeNameIndex.isPresent() ) {
            // throw
        }
        if ( freeNameIndex.get() > 0 ) {
            name = format("%s (%d)", name, freeNameIndex.get());
        }
        
        ValidationResult urlValidity = WEB_URL_RULE.apply(url);
        if ( urlValidity.isFail() ) {
            // throw
        }
        
        ValidationResult dirNameValidity = ENTITY_NAME_RULE.apply(directory);
        if ( dirNameValidity.isFail() ) {
            // throw
        }
        
        if ( ! this.daoDirectories.exists(initiator, directory, place) ) {
            // throw
        }
        
        return this.daoPages.save(initiator, newWebPage(name, "", url, place, directory));
    }

    @Override
    public boolean editWebPageName(
            Initiator initiator, String name, String newName) {
        throw new UnsupportedOperationException("Not supported yet.");
    }
}