/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package diarsid.beam.core.modules.domainkeeper;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.Set;

import diarsid.beam.core.base.control.flow.ValueOperation;
import diarsid.beam.core.base.control.flow.VoidOperation;
import diarsid.beam.core.base.control.io.base.actors.Initiator;
import diarsid.beam.core.base.control.io.base.actors.InnerIoEngine;
import diarsid.beam.core.base.control.io.base.interaction.Answer;
import diarsid.beam.core.base.control.io.base.interaction.VariantsQuestion;
import diarsid.beam.core.base.control.io.base.interaction.WebResponse;
import diarsid.beam.core.base.control.io.commands.ArgumentsCommand;
import diarsid.beam.core.base.control.io.commands.CommandType;
import diarsid.beam.core.base.control.io.commands.executor.BrowsePageCommand;
import diarsid.beam.core.base.control.io.commands.executor.InvocationCommand;
import diarsid.beam.core.base.exceptions.WorkflowBrokenException;
import diarsid.beam.core.domain.entities.WebDirectory;
import diarsid.beam.core.domain.entities.WebDirectoryPages;
import diarsid.beam.core.domain.entities.WebPage;
import diarsid.beam.core.domain.entities.WebPlace;
import diarsid.beam.core.domain.entities.metadata.EntityProperty;
import diarsid.beam.core.domain.entities.validation.ValidationResult;
import diarsid.beam.core.domain.inputparsing.common.PropertyAndText;
import diarsid.beam.core.domain.inputparsing.common.PropertyAndTextParser;
import diarsid.beam.core.domain.inputparsing.webpages.WebObjectsInputParser;
import diarsid.beam.core.domain.inputparsing.webpages.WebPageNameUrlAndPlace;
import diarsid.beam.core.domain.patternsanalyze.WeightedVariants;
import diarsid.beam.core.modules.data.DaoWebDirectories;
import diarsid.beam.core.modules.data.DaoWebPages;

import static java.lang.String.format;
import static java.util.Collections.sort;

import static diarsid.beam.core.base.control.flow.Operations.valueCompletedEmpty;
import static diarsid.beam.core.base.control.flow.Operations.valueCompletedWith;
import static diarsid.beam.core.base.control.flow.Operations.valueOperationFail;
import static diarsid.beam.core.base.control.flow.Operations.valueOperationStopped;
import static diarsid.beam.core.base.control.flow.Operations.voidCompleted;
import static diarsid.beam.core.base.control.flow.Operations.voidOperationFail;
import static diarsid.beam.core.base.control.flow.Operations.voidOperationStopped;
import static diarsid.beam.core.base.control.io.base.interaction.Variants.entitiesToVariants;
import static diarsid.beam.core.base.control.io.base.interaction.VariantsQuestion.question;
import static diarsid.beam.core.base.control.io.base.interaction.WebResponse.badRequestWithJson;
import static diarsid.beam.core.base.control.io.base.interaction.WebResponse.notFoundWithJson;
import static diarsid.beam.core.base.control.io.base.interaction.WebResponse.ok;
import static diarsid.beam.core.base.control.io.base.interaction.WebResponse.okWithJson;
import static diarsid.beam.core.base.control.io.commands.CommandType.BROWSE_WEBPAGE;
import static diarsid.beam.core.base.control.io.commands.CommandType.CREATE_PAGE;
import static diarsid.beam.core.base.control.io.commands.CommandType.DELETE_PAGE;
import static diarsid.beam.core.base.control.io.commands.CommandType.EDIT_PAGE;
import static diarsid.beam.core.base.control.io.commands.CommandType.FIND_PAGE;
import static diarsid.beam.core.base.control.io.commands.executor.InvocationCommandLifePhase.NEW;
import static diarsid.beam.core.base.control.io.commands.executor.InvocationCommandTargetState.TARGET_FOUND;
import static diarsid.beam.core.base.util.CollectionsUtils.getOne;
import static diarsid.beam.core.base.util.CollectionsUtils.hasMany;
import static diarsid.beam.core.base.util.CollectionsUtils.hasOne;
import static diarsid.beam.core.base.util.CollectionsUtils.toSet;
import static diarsid.beam.core.base.util.ConcurrencyUtil.asyncDo;
import static diarsid.beam.core.base.util.Logs.debug;
import static diarsid.beam.core.base.util.OptionalUtil.isNotPresent;
import static diarsid.beam.core.base.util.StringUtils.nonEmpty;
import static diarsid.beam.core.base.util.StringUtils.splitBySpacesToList;
import static diarsid.beam.core.domain.entities.Orderables.reorderAccordingToNewOrder;
import static diarsid.beam.core.domain.entities.WebDirectories.newDirectory;
import static diarsid.beam.core.domain.entities.WebPages.newWebPage;
import static diarsid.beam.core.domain.entities.WebPlace.UNDEFINED_PLACE;
import static diarsid.beam.core.domain.entities.WebPlace.WEBPANEL;
import static diarsid.beam.core.domain.entities.metadata.EntityProperty.NAME;
import static diarsid.beam.core.domain.entities.metadata.EntityProperty.ORDER;
import static diarsid.beam.core.domain.entities.metadata.EntityProperty.SHORTCUTS;
import static diarsid.beam.core.domain.entities.metadata.EntityProperty.UNDEFINED_PROPERTY;
import static diarsid.beam.core.domain.entities.metadata.EntityProperty.WEB_DIRECTORY;
import static diarsid.beam.core.domain.entities.metadata.EntityProperty.WEB_URL;
import static diarsid.beam.core.domain.entities.validation.ValidationRule.ENTITY_NAME_RULE;
import static diarsid.beam.core.domain.entities.validation.ValidationRule.WEB_URL_RULE;
import static diarsid.beam.core.domain.patternsanalyze.Analyze.weightVariants;


public class WebPagesKeeperWorker 
        extends 
                WebObjectsCommonKeeper 
        implements 
                WebPagesKeeper, 
                NamedEntitiesKeeper {
    
    private final DaoWebPages daoPages;
    private final DaoWebDirectories daoDirectories;
    private final CommandsMemoryKeeper commandsMemory;
    private final InnerIoEngine ioEngine;
    private final Initiator systemInitiator;
    private final KeeperDialogHelper helper;
    private final PropertyAndTextParser propetyTextParser;
    private final WebObjectsInputParser webObjectsParser;
    private final Set<CommandType> subjectedCommandTypes;
    private final WebDirectory defaultDirectory;
    
    public WebPagesKeeperWorker(
            DaoWebPages dao, 
            DaoWebDirectories daoDirectories,
            CommandsMemoryKeeper commandsMemory,
            InnerIoEngine ioEngine, 
            Initiator systemInitiator,
            KeeperDialogHelper helper,
            PropertyAndTextParser propetyTextParser,
            WebObjectsInputParser parser) {
        super(ioEngine);
        this.daoPages = dao;
        this.commandsMemory = commandsMemory;
        this.daoDirectories = daoDirectories;
        this.ioEngine = ioEngine;
        this.systemInitiator = systemInitiator;
        this.helper = helper;
        this.propetyTextParser = propetyTextParser;
        this.webObjectsParser = parser;
        this.subjectedCommandTypes = toSet(BROWSE_WEBPAGE);
        this.defaultDirectory = this.getOrCreateDefaultDirectory();                 
    }
    
    private WebDirectory getOrCreateDefaultDirectory() {
        Optional<WebDirectory> defaultDir = this.daoDirectories.getDirectoryByNameAndPlace(
                this.systemInitiator, "Common", WEBPANEL);
        if ( defaultDir.isPresent() ) {
            return defaultDir.get();
        } else {
            WebDirectory directory = newDirectory("Common", WEBPANEL);
            if ( this.daoDirectories.save(this.systemInitiator, directory) ) {
                return directory;
            } else {
                throw new WorkflowBrokenException("Cannot create default WebDirectory 'Common'.");
            }   
        }    
    }

    @Override
    public boolean isSubjectedTo(InvocationCommand command) {
        return this.subjectedCommandTypes.contains(command.type());
    }

    private void asyncRemoveCommandsForPage(Initiator initiator, WebPage page) {
        asyncDo(() -> {
            this.commandsMemory.removeByExactExtendedAndType(
                    initiator, page.name(), BROWSE_WEBPAGE);
        });
    }
    
    private void asyncChangeCommandsForPageNames(
            Initiator initiator, String pageOldName, String pageNewName) {
        asyncDo(() -> {
            this.commandsMemory.removeByExactExtendedAndType(
                    initiator, pageOldName, BROWSE_WEBPAGE);
            this.commandsMemory.save(
                    initiator, new BrowsePageCommand(pageNewName, pageNewName, NEW, TARGET_FOUND));
        });
    }
    
    private void asyncChangeCommandsForPageShortcuts(
            Initiator initiator, String pageName, String pageOldShorts, String pageNewShorts) {
        asyncDo(() -> {
            if ( nonEmpty(pageOldShorts) ) {
                splitBySpacesToList(pageOldShorts)
                        .stream()
                        .peek(alias -> debug("[WEB PAGES KEEPER] delete command by alias: " + alias))
                        .forEach(alias -> {
                            this.commandsMemory.removeByExactOriginalAndType(
                                    initiator, alias, BROWSE_WEBPAGE);
                        });
            }
            if ( nonEmpty(pageNewShorts) ) {
                splitBySpacesToList(pageNewShorts)
                        .stream()
                        .peek(alias -> debug("[WEB PAGES KEEPER] save alias as command: " + alias))
                        .forEach(alias -> {
                            this.commandsMemory.save(
                                    initiator, 
                                    new BrowsePageCommand(alias, pageName, NEW, TARGET_FOUND));
                        });
            }
        });
    }
    
    private void asyncAddCommandsForPage(Initiator initiator, WebPage page) {
        asyncDo(() -> {
            this.commandsMemory.save(
                    initiator, new BrowsePageCommand(page.name(), page.name(), NEW, TARGET_FOUND));
            if ( nonEmpty(page.shortcuts()) ) {
                splitBySpacesToList(page.shortcuts())
                        .stream()
                        .peek(alias -> debug("[WEB PAGES KEEPER] save alias as command: " + alias))
                        .forEach(alias -> {
                            this.commandsMemory.save(
                                    initiator, 
                                    new BrowsePageCommand(alias, page.name(), NEW, TARGET_FOUND));
                        });
            }
        });
    }
    
    @Override
    public ValueOperation<WebPage> findByExactName(
            Initiator initiator, String name) {
        return valueCompletedWith(this.daoPages.getByExactName(initiator, name));
    }
    
    @Override
    public ValueOperation<WebPage> findByNamePattern(
            Initiator initiator, String namePattern) {
        List<WebPage> foundPages = this.daoPages.findByPattern(initiator, namePattern);
        if ( hasOne(foundPages) ) {
            return valueCompletedWith(getOne(foundPages));
        } else if ( hasMany(foundPages) ) {
            return this.manageWithManyPages(initiator, namePattern, foundPages);
        } else {
            return valueCompletedEmpty();
        }
    }
    
    private ValueOperation<WebPage> manageWithManyPages(
            Initiator initiator, String pattern, List<WebPage> pages) {
        WeightedVariants variants = weightVariants(pattern, entitiesToVariants(pages));
        if ( variants.isEmpty() ) {
            return valueCompletedEmpty();
        }
        Answer answer = this.ioEngine.chooseInWeightedVariants(initiator, variants);
        if ( answer.isGiven() ) {
            return valueCompletedWith(pages.get(answer.index()));
        } else {
            if ( answer.isRejection() ) {
                return valueOperationStopped();
            } else if ( answer.variantsAreNotSatisfactory() ) {
                return valueCompletedEmpty();
            } else {
                return valueCompletedEmpty();
            }
        }
    }

    private String discussShortcuts(Initiator initiator) {
        String shortcuts = this.ioEngine.askInput(initiator, "shortcuts, if any");
        if ( nonEmpty(shortcuts) ) {
            ValidationResult shortcutsValidity = ENTITY_NAME_RULE.applyTo(shortcuts);
            validation: while ( shortcutsValidity.isFail() ) {
                shortcuts = this.ioEngine.askInput(initiator, "shortcuts");
                if ( shortcuts.isEmpty() ) {
                    break validation;
                } else {
                    shortcutsValidity = ENTITY_NAME_RULE.applyTo(shortcuts);
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
        VariantsQuestion question;
        directoryDefining: while ( pageNotDefined ) {  
            if ( pagePattern.isEmpty() ) {
                pagePattern = this.ioEngine.askInput(initiator, "page name");
            }            
            if ( pagePattern.isEmpty() ) {
                optPage = Optional.empty();
                pageNotDefined = false;
                continue;
            }
            pagePattern = this.helper.validateEntityNameInteractively(initiator, pagePattern);

            foundPages = this.daoPages.findByPattern(initiator, pagePattern);

            if ( foundPages.isEmpty() ) {
                this.ioEngine.report(initiator, format("page not found by '%s'.", pagePattern));
                pagePattern = "";
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
        VariantsQuestion question;        
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
        Optional<Integer> freeNameIndex = daoPages.findFreeNameNextIndex(initiator, name);
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
            this.ioEngine.report(
                    initiator, 
                    format("default directory '%s' will be used.", this.defaultDirectory.name()));
            optDirectory = Optional.of(this.defaultDirectory);
        } else {
            this.ioEngine.report(
                    initiator, format("directory found: '%s'", optDirectory.get().name()));
        }       
        
        WebPage page = newWebPage(name, shortcuts, url, optDirectory.get().id());
        if ( daoPages.save(initiator, page) ) {
            this.asyncAddCommandsForPage(initiator, page);
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
        this.ioEngine.report(initiator, format("'%s' found.", page.name()));
        
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
                return this.editPageShortcuts(initiator, page.name(), page.shortcuts());
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
            this.asyncChangeCommandsForPageNames(initiator, pageName, newName);
            return voidCompleted();
        } else {
            return voidOperationFail("DAO failed to rename page.");
        }
    }
    
    private VoidOperation editPageShortcuts(
            Initiator initiator, String pageName, String oldShortcuts) {
        String newShortcuts = this.discussShortcuts(initiator);
        if ( newShortcuts.isEmpty() ) {
            this.ioEngine.report(initiator, "removing shortcuts...");
        }
        if ( this.daoPages.editShortcuts(initiator, pageName, newShortcuts) ) {
            this.asyncChangeCommandsForPageShortcuts(
                    initiator, pageName, oldShortcuts, newShortcuts);
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
        
        Optional<WebPage> page = this.discussExistingPage(initiator, pagePattern);
        if ( page.isPresent() ) {
            this.ioEngine.report(initiator, format("'%s' found.", page.get().name()));            
        } else {
            return voidOperationStopped();
        }
        
        if ( this.daoPages.remove(initiator, page.get().name()) ) {
            this.asyncRemoveCommandsForPage(initiator, page.get());
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
            return valueCompletedWith(optPage.get());
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
    public WebResponse createWebPage(
            WebPlace place, String directoryName, String pageName, String pageUrl) {   
        ValidationResult urlValidity = WEB_URL_RULE.applyTo(pageUrl);
        if ( urlValidity.isFail() ) {
            return badRequestWithJson(urlValidity.getFailureMessage());
        }
        
        ValidationResult pageNameValidity = ENTITY_NAME_RULE.applyTo(pageName);
        if ( pageNameValidity.isFail() ) {
            return badRequestWithJson(pageNameValidity.getFailureMessage());
        }
        
        Optional<Integer> freeNameIndex = daoPages.findFreeNameNextIndex(
                this.systemInitiator, pageName);
        if ( ! freeNameIndex.isPresent() ) {
            return badRequestWithJson("Cannot get free name next index.");
        }
        if ( freeNameIndex.get() > 0 ) {
            pageName = format("%s (%d)", pageName, freeNameIndex.get());
        }
        
        Optional<Integer> optId =  this.daoDirectories.getDirectoryIdByNameAndPlace(
                this.systemInitiator, directoryName, place);
        if ( ! optId.isPresent() ) {
            return notFoundWithJson(
                    format("WebDirectory '%s' does not exist in %s", directoryName, place.name()));
        } 
        
        WebPage newPage = newWebPage(pageName, "", pageUrl, optId.get());
        boolean saved = this.daoPages.save(this.systemInitiator, newPage);
        if ( saved ) {
            this.asyncAddCommandsForPage(this.systemInitiator, newPage);
            return ok();
        } else {
            return badRequestWithJson(
                    format("page '%s' not saved in '%s'.", pageName, directoryName));
        }
    }

    @Override
    public WebResponse editWebPageName(
            WebPlace place, String directoryName, String pageOldName, String pageNewName) {
        Optional<WebDirectoryPages> webDirectoryPages = this.daoDirectories
                .getDirectoryPagesByNameAndPlace(this.systemInitiator, directoryName, place);
        if ( isNotPresent(webDirectoryPages) ) {
            return notFoundWithJson(format(
                    "Directory '%s' not found in %s!", directoryName, place.name()));
        }
        
        Optional<WebPage> page = webDirectoryPages.get().pages()
                .stream()
                .filter(webPage -> webPage.name().equalsIgnoreCase(pageOldName))
                .findFirst();
        
        if ( isNotPresent(page) ) {
            return notFoundWithJson(format(
                    "Page '%s' not found in %s!", pageOldName, directoryName));
        }
        
        ValidationResult newNameValidity = ENTITY_NAME_RULE.applyTo(pageNewName);
        if ( newNameValidity.isFail() ) {
            return badRequestWithJson(newNameValidity.getFailureMessage());
        }
        
        Optional<Integer> freeNameIndex = daoPages
                .findFreeNameNextIndex(this.systemInitiator, pageNewName);
        if ( ! freeNameIndex.isPresent() ) {
            return badRequestWithJson("Cannot get free name next index.");
        }
        if ( freeNameIndex.get() > 0 ) {
            pageNewName = format("%s (%d)", pageNewName, freeNameIndex.get());
        }
        
        boolean edited = this.daoPages
                .editName(this.systemInitiator, page.get().name(), pageNewName);
        if ( edited ) {
            this.asyncChangeCommandsForPageNames(this.systemInitiator, pageOldName, pageNewName);
            return ok();
        } else {
            return badRequestWithJson(format("Page '%s' is not renamed.", page.get().name()));
        }     
    }

    @Override
    public WebResponse getWebPage(WebPlace place, String directoryName, String pageName) {
        Optional<WebDirectoryPages> webDirectoryPages = this.daoDirectories
                .getDirectoryPagesByNameAndPlace(this.systemInitiator, directoryName, place);
        if ( isNotPresent(webDirectoryPages) ) {
            return notFoundWithJson(format(
                    "Directory '%s' not found in %s!", directoryName, place.name()));
        }
        
        Optional<WebPage> page = webDirectoryPages.get().pages()
                .stream()
                .filter(webPage -> webPage.name().equalsIgnoreCase(pageName))
                .findFirst();
        
        if ( page.isPresent() ) {
            return okWithJson(page.get());
        } else {
            return notFoundWithJson(format(
                    "Page '%s' not found in %s!", pageName, directoryName));
        }
    }

    @Override
    public WebResponse getWebPagesInDirectory(WebPlace place, String directoryName) {
        Optional<WebDirectoryPages> webDirectoryPages = this.daoDirectories
                .getDirectoryPagesByNameAndPlace(this.systemInitiator, directoryName, place);
        
        if ( webDirectoryPages.isPresent() ) {
            return okWithJson(webDirectoryPages.get().pages());
        } else {
            return notFoundWithJson(format(
                    "Directory '%s' not found in %s!", directoryName, place.name()));
        }
    }

    @Override
    public WebResponse deleteWebPage(WebPlace place, String directoryName, String pageName) {
        Optional<WebDirectoryPages> webDirectoryPages = this.daoDirectories
                .getDirectoryPagesByNameAndPlace(this.systemInitiator, directoryName, place);
        if ( isNotPresent(webDirectoryPages) ) {
            return notFoundWithJson(format(
                    "Directory '%s' not found in %s!", directoryName, place.name()));
        }
        
        Optional<WebPage> page = webDirectoryPages.get().pages()
                .stream()
                .filter(webPage -> webPage.name().equalsIgnoreCase(pageName))
                .findFirst();
        
        if ( isNotPresent(page) ) {
            return notFoundWithJson(format(
                    "Page '%s' not found in %s!", pageName, directoryName));
        }
        
        boolean deleted = this.daoPages.remove(this.systemInitiator, page.get().name());
        if ( deleted ) {
            this.asyncRemoveCommandsForPage(this.systemInitiator, page.get());
            return ok();
        } else {
            return badRequestWithJson(format("Page '%s' is not removed.", page.get().name()));
        } 
    }

    @Override
    public WebResponse editWebPageUrl(
            WebPlace place, String directoryName, String pageName, String pageUrl) {
        Optional<WebDirectoryPages> webDirectoryPages = this.daoDirectories
                .getDirectoryPagesByNameAndPlace(this.systemInitiator, directoryName, place);
        if ( isNotPresent(webDirectoryPages) ) {
            return notFoundWithJson(format(
                    "Directory '%s' not found in %s!", directoryName, place.name()));
        }
        
        Optional<WebPage> page = webDirectoryPages.get().pages()
                .stream()
                .filter(webPage -> webPage.name().equalsIgnoreCase(pageName))
                .findFirst();
        
        if ( isNotPresent(page) ) {
            return notFoundWithJson(format(
                    "Page '%s' not found in %s!", pageName, directoryName));
        }
        
        ValidationResult urlValidity = WEB_URL_RULE.applyTo(pageUrl);
        if ( urlValidity.isFail() ) {
            return badRequestWithJson(urlValidity.getFailureMessage());
        }
        
        boolean changed = this.daoPages.editUrl(this.systemInitiator, page.get().name(), pageUrl);
        if ( changed ) {
            return ok();
        } else {
            return badRequestWithJson(format("Page '%s' URL is not changed.", page.get().name()));
        }
    }

    @Override
    public WebResponse editWebPageDirectory(
            WebPlace place, String directoryName, String pageName, String newDirectoryName) {        
        return this.editWebPageDirectoryAndPlace(
                place, directoryName, pageName, place, newDirectoryName);
    }

    @Override
    public WebResponse editWebPageDirectoryAndPlace(
            WebPlace place, 
            String directoryName, 
            String pageName, 
            WebPlace newPlace, 
            String newDirectoryName) {
        Optional<WebDirectory> newDirectory = daoDirectories
                .getDirectoryByNameAndPlace(this.systemInitiator, newDirectoryName, newPlace);
        if ( isNotPresent(newDirectory) ) {
            return notFoundWithJson(format(
                    "Directory '%s' not found in %s!", newDirectoryName, newPlace.name()));
        }
        
        Optional<WebDirectoryPages> webDirectoryPages = this.daoDirectories
                .getDirectoryPagesByNameAndPlace(this.systemInitiator, directoryName, place);
        if ( isNotPresent(webDirectoryPages) ) {
            return notFoundWithJson(format(
                    "Directory '%s' not found in %s!", directoryName, place.name()));
        }
        
        Optional<WebPage> page = webDirectoryPages.get().pages()
                .stream()
                .filter(webPage -> webPage.name().equalsIgnoreCase(pageName))
                .findFirst();
        
        if ( isNotPresent(page) ) {
            return notFoundWithJson(format(
                    "Page '%s' not found in %s!", pageName, directoryName));
        }
        
        boolean moved = daoPages
                .movePageFromDirToDir(this.systemInitiator, page.get(), newDirectory.get().id());
        if ( moved ) {
            return ok();
        } else {
            return badRequestWithJson(format(
                    "Page '%s' directory is not changed.", page.get().name()));
        }
    }

    @Override
    public WebResponse editWebPageOrder(
            WebPlace place, String directoryName, String pageName, int newOrder) {
        Optional<WebDirectoryPages> webDirectoryPages = this.daoDirectories
                .getDirectoryPagesByNameAndPlace(this.systemInitiator, directoryName, place);
        if ( isNotPresent(webDirectoryPages) ) {
            return notFoundWithJson(format(
                    "Directory '%s' not found in %s!", directoryName, place.name()));
        }
        
        Optional<WebPage> page = webDirectoryPages.get().pages()
                .stream()
                .filter(webPage -> webPage.name().equalsIgnoreCase(pageName))
                .findFirst();
        
        if ( isNotPresent(page) ) {
            return notFoundWithJson(format(
                    "Page '%s' not found in %s!", pageName, directoryName));
        }
        
        List<WebPage> pages = new ArrayList<>(webDirectoryPages.get().pages());
        reorderAccordingToNewOrder(pages, page.get().order(), newOrder);
        sort(pages);
        boolean changed = this.daoPages.updatePageOrdersInDir(this.systemInitiator, pages);
        if ( changed ) {
            return ok();
        } else {
            return badRequestWithJson(format(
                    "Page '%s' order is not changed.", page.get().name()));
        }
    }
}
