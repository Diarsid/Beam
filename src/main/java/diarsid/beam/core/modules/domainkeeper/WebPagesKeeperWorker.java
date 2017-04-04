/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
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
import diarsid.beam.core.domain.entities.WebDirectory;
import diarsid.beam.core.domain.entities.WebPage;
import diarsid.beam.core.domain.entities.WebPlace;
import diarsid.beam.core.domain.entities.exceptions.DomainConsistencyException;
import diarsid.beam.core.domain.entities.exceptions.DomainOperationException;
import diarsid.beam.core.domain.entities.metadata.EntityProperty;
import diarsid.beam.core.domain.entities.validation.ValidationResult;
import diarsid.beam.core.domain.inputparsing.common.PropertyAndText;
import diarsid.beam.core.domain.inputparsing.common.PropertyAndTextParser;
import diarsid.beam.core.domain.inputparsing.webpages.WebObjectsInputParser;
import diarsid.beam.core.domain.inputparsing.webpages.WebPageNameUrlAndPlace;
import diarsid.beam.core.modules.data.DaoWebDirectories;
import diarsid.beam.core.modules.data.DaoWebPages;

import static java.lang.String.format;
import static java.util.Collections.sort;

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
import static diarsid.beam.core.base.control.io.commands.CommandType.SEE_WEBPAGE;
import static diarsid.beam.core.base.util.CollectionsUtils.getOne;
import static diarsid.beam.core.base.util.CollectionsUtils.hasMany;
import static diarsid.beam.core.base.util.CollectionsUtils.hasOne;
import static diarsid.beam.core.base.util.CollectionsUtils.toSet;
import static diarsid.beam.core.base.util.StringUtils.nonEmpty;
import static diarsid.beam.core.domain.entities.Orderables.reorderAccordingToNewOrder;
import static diarsid.beam.core.domain.entities.WebPages.newWebPage;
import static diarsid.beam.core.domain.entities.WebPlace.UNDEFINED_PLACE;
import static diarsid.beam.core.domain.entities.metadata.EntityProperty.NAME;
import static diarsid.beam.core.domain.entities.metadata.EntityProperty.ORDER;
import static diarsid.beam.core.domain.entities.metadata.EntityProperty.SHORTCUTS;
import static diarsid.beam.core.domain.entities.metadata.EntityProperty.UNDEFINED_PROPERTY;
import static diarsid.beam.core.domain.entities.metadata.EntityProperty.WEB_DIRECTORY;
import static diarsid.beam.core.domain.entities.metadata.EntityProperty.WEB_URL;
import static diarsid.beam.core.domain.entities.validation.ValidationResults.fail;
import static diarsid.beam.core.domain.entities.validation.ValidationRule.ENTITY_NAME_RULE;
import static diarsid.beam.core.domain.entities.validation.ValidationRule.WEB_URL_RULE;


public class WebPagesKeeperWorker 
        extends 
                WebObjectsCommonKeeper 
        implements 
                WebPagesKeeper, 
                NamedEntitiesKeeper {
    
    private final DaoWebPages daoPages;
    private final DaoWebDirectories daoDirectories;
    private final InnerIoEngine ioEngine;
    private final KeeperDialogHelper helper;
    private final PropertyAndTextParser propetyTextParser;
    private final WebObjectsInputParser webObjectsParser;
    private final Set<CommandType> subjectedCommandTypes;
    
    public WebPagesKeeperWorker(
            DaoWebPages dao, 
            DaoWebDirectories daoDirectories,
            InnerIoEngine ioEngine, 
            KeeperDialogHelper helper,
            PropertyAndTextParser propetyTextParser,
            WebObjectsInputParser parser) {
        super(ioEngine);
        this.daoPages = dao;
        this.daoDirectories = daoDirectories;
        this.ioEngine = ioEngine;
        this.helper = helper;
        this.propetyTextParser = propetyTextParser;
        this.webObjectsParser = parser;
        this.subjectedCommandTypes = toSet(SEE_WEBPAGE);
    }

    @Override
    public boolean isSubjectedTo(InvocationEntityCommand command) {
        return this.subjectedCommandTypes.contains(command.type());
    }

    @Override
    public Optional<WebPage> findByExactName(
            Initiator initiator, String name) {
        return this.daoPages.getByExactName(initiator, name);
    }
    
    @Override
    public Optional<WebPage> findByNamePattern(
            Initiator initiator, String namePattern) {
        List<WebPage> foundPages = this.daoPages.findByPattern(initiator, namePattern);
        if ( hasOne(foundPages) ) {
            return Optional.of(getOne(foundPages));
        } else if ( hasMany(foundPages) ) {
            return this.manageWithManyPages(initiator, foundPages);
        } else {
            return Optional.empty();
        }
    }
    
    private Optional<WebPage> manageWithManyPages(Initiator initiator, List<WebPage> pages) {
        // TODO
        // employ more sofisticated algorithm
        Question question = question("choose").withAnswerEntities(pages);
        Answer answer = this.ioEngine.ask(initiator, question);
        if ( answer.isGiven() ) {
            return Optional.of(pages.get(answer.index()));
        } else {
            return Optional.empty();
        }
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
    
    private Optional<WebPage> discussExistingPage(Initiator initiator, String pagePattern) {
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
    
    private String discussPageNewName(Initiator initiator) {
        String pageNewName = "";
        boolean newNameNotDefined = true;
        newNameDefining: while ( newNameNotDefined ) {            
            pageNewName = this.ioEngine.askInput(initiator, "new name");
            if ( pageNewName.isEmpty() ) {
                return "";
            }
            pageNewName = this.helper.validateEntityNameInteractively(initiator, pageNewName);
            if ( pageNewName.isEmpty() ) {
                return "";
            } else {
                return pageNewName;
            }
        }
        return pageNewName;
    }
    
    private Optional<WebDirectory> discussExistingWebDirectory(
            Initiator initiator, WebPlace place) {
        if ( place.isUndefined() ) {
            place = super.discussWebPlace(initiator);
        }        
        if ( place.isUndefined() ) {
            return Optional.empty();
        }
        
        String directoryName;
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
            if ( directoryName.isEmpty() ) {
                return Optional.empty();                
            }
            
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
            WebPageNameUrlAndPlace data = this.webObjectsParser.parseNameUrlAndPlace(command.arguments());
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
            this.ioEngine.report(initiator, format("page '%s' already exists.", name));
            name = format("%s (%d)", name, freeNameIndex.get());
            this.ioEngine.report(initiator, format("name '%s' will be saved instead.", name));
        }
        
        url = this.helper.validateInteractively(initiator, url, "url", WEB_URL_RULE);
        if ( url.isEmpty() ) {
            return voidOperationStopped();
        }
        
        String shortcuts = this.discussShortcuts(initiator);
        
        Optional<WebDirectory> optDirectory = this.discussExistingWebDirectory(initiator, place);
        if ( ! optDirectory.isPresent() ) {
            return voidOperationStopped();
        }        
        
        WebPage page = newWebPage(name, shortcuts, url, optDirectory.get().id());
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
        EntityProperty propertyToEdit;
        if ( command.hasArguments() ) {
            PropertyAndText propertyText = this.propetyTextParser.parse(command.arguments());
            propertyToEdit = propertyText.property();
            pagePattern = propertyText.text();
        } else {
            pagePattern = "";
            propertyToEdit = UNDEFINED_PROPERTY;
        }
        
        Optional<WebPage> optPage = this.discussExistingPage(initiator, pagePattern);
        if ( ! optPage.isPresent() ) {
            return voidOperationStopped();
        }
        WebPage page = optPage.get();
        
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
        String newName = this.discussPageNewName(initiator);
        if ( newName.isEmpty() ) {
            return voidOperationStopped();
        }
        if ( this.daoPages.editName(initiator, pageName, newName) ) {
            return voidCompleted();
        } else {
            return voidOperationFail("DAO failed to rename page.");
        }
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
                .allFromDirectory(initiator, page.directoryId());
        if ( pagesInDirectory.isEmpty() ) {
            return voidOperationFail("cannot find all pages from directory.");
        }
        int pageNewOrder = this.helper.discussIntInRange(
                initiator, 0, pagesInDirectory.size() - 1, "new order");
        if ( pageNewOrder < 0 ) {
            return voidOperationStopped();
        }
        reorderAccordingToNewOrder(pagesInDirectory, page.order(), pageNewOrder);
        sort(pagesInDirectory);
        if ( this.daoPages.updatePageOrdersInDir(initiator, pagesInDirectory) ) {
            return voidCompleted();
        } else {
            return voidOperationFail("DOA failed to reorder directory with new page order.");
        }
    }
    
    private VoidOperation editPageWebDirectory(Initiator initiator, WebPage page) {
        this.ioEngine.report(initiator, "choosing new page directory...");
        Optional<WebDirectory> optDirectory = 
                this.discussExistingWebDirectory(initiator, UNDEFINED_PLACE);
        if ( ! optDirectory.isPresent() ) {
            return voidOperationStopped();
        }
        
        boolean pageMoved = this.daoPages
                .movePageFromDirToDir(initiator, page, optDirectory.get().id());
        
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
        
        Optional<WebPage> optPage = this.discussExistingPage(initiator, pagePattern);
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
        
        Optional<WebPage> optPage = this.discussExistingPage(initiator, pagePattern);
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
    public boolean createWebPage(
            Initiator initiator, String name, String url, WebPlace place, String directory) 
            throws 
                    DomainConsistencyException, 
                    DomainOperationException {
        Optional<Integer> freeNameIndex = daoPages.freeNameNextIndex(initiator, name);
        if ( ! freeNameIndex.isPresent() ) {
            throw new DomainOperationException("Cannot get free name next index.");
        }
        if ( freeNameIndex.get() > 0 ) {
            name = format("%s (%d)", name, freeNameIndex.get());
        }
        
        ValidationResult urlValidity = WEB_URL_RULE.apply(url);
        if ( urlValidity.isFail() ) {
            throw new DomainConsistencyException(urlValidity.getFailureMessage());
        }
        
        ValidationResult dirNameValidity = ENTITY_NAME_RULE.apply(directory);
        if ( dirNameValidity.isFail() ) {
            throw new DomainConsistencyException(dirNameValidity.getFailureMessage());
        }
        
        Optional<Integer> optId = 
                this.daoDirectories.getDirectoryIdByNameAndPlace(initiator, name, place);
        if ( ! optId.isPresent() ) {
            throw new DomainConsistencyException(
                    format("%s does not exist in %s", directory, place.name()));
        }
        
        return this.daoPages.save(initiator, newWebPage(name, "", url, optId.get()));
    }

    @Override
    public boolean editWebPageName(
            Initiator initiator, String name, String newName) 
            throws 
                    DomainConsistencyException, 
                    DomainOperationException {
        Optional<Integer> freeNameIndex = daoPages.freeNameNextIndex(initiator, newName);
        if ( ! freeNameIndex.isPresent() ) {
            throw new DomainOperationException("Cannot get free name next index.");
        }
        if ( freeNameIndex.get() > 0 ) {
            newName = format("%s (%d)", newName, freeNameIndex.get());
        }
        
        ValidationResult newNameValidity = ENTITY_NAME_RULE.apply(newName);
        if ( newNameValidity.isFail() ) {
            throw new DomainConsistencyException(newNameValidity.getFailureMessage());
        }
        
        return this.daoPages.editName(initiator, name, newName);        
    }
}
