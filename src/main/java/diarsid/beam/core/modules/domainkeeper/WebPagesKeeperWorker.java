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

import diarsid.beam.core.application.gui.InteractionGui;
import diarsid.beam.core.base.analyze.variantsweight.WeightedVariant;
import diarsid.beam.core.base.analyze.variantsweight.WeightedVariants;
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
import diarsid.beam.core.base.control.io.commands.CommandType;
import diarsid.beam.core.base.control.io.commands.EmptyCommand;
import diarsid.beam.core.base.control.io.commands.executor.BrowsePageCommand;
import diarsid.beam.core.base.control.io.commands.executor.InvocationCommand;
import diarsid.beam.core.base.exceptions.WorkflowBrokenException;
import diarsid.beam.core.domain.entities.Picture;
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
import diarsid.beam.core.modules.data.DaoPatternChoices;
import diarsid.beam.core.modules.data.DaoPictures;
import diarsid.beam.core.modules.data.DaoWebDirectories;
import diarsid.beam.core.modules.data.DaoWebPages;

import static java.lang.String.format;
import static java.lang.String.join;
import static java.util.Collections.sort;
import static java.util.stream.Collectors.toList;

import static diarsid.beam.core.base.analyze.variantsweight.Analyze.isEntitySatisfiable;
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
import static diarsid.beam.core.base.control.io.base.interaction.Messages.info;
import static diarsid.beam.core.base.control.io.base.interaction.Variants.entitiesToVariants;
import static diarsid.beam.core.base.control.io.base.interaction.VariantsQuestion.question;
import static diarsid.beam.core.base.control.io.base.interaction.WebResponse.badRequestWithJson;
import static diarsid.beam.core.base.control.io.base.interaction.WebResponse.notFoundWithJson;
import static diarsid.beam.core.base.control.io.base.interaction.WebResponse.ok;
import static diarsid.beam.core.base.control.io.base.interaction.WebResponse.okWithJson;
import static diarsid.beam.core.base.control.io.base.interaction.WebResponse.optionalOkWithBinary;
import static diarsid.beam.core.base.control.io.commands.CommandType.BROWSE_WEBPAGE;
import static diarsid.beam.core.base.control.io.commands.CommandType.CAPTURE_PAGE_IMAGE;
import static diarsid.beam.core.base.control.io.commands.CommandType.CREATE_PAGE;
import static diarsid.beam.core.base.control.io.commands.CommandType.DELETE_PAGE;
import static diarsid.beam.core.base.control.io.commands.CommandType.EDIT_PAGE;
import static diarsid.beam.core.base.control.io.commands.CommandType.FIND_WEBPAGE;
import static diarsid.beam.core.base.control.io.commands.CommandType.SHOW_BOOKMARKS;
import static diarsid.beam.core.base.control.io.commands.CommandType.SHOW_WEBPANEL;
import static diarsid.beam.core.base.control.io.commands.executor.InvocationCommandLifePhase.NEW;
import static diarsid.beam.core.base.control.io.commands.executor.InvocationCommandTargetState.TARGET_FOUND;
import static diarsid.beam.core.base.control.io.interpreter.ControlKeys.UNACCEPTABLE_DOMAIN_CHARS;
import static diarsid.beam.core.base.events.BeamEventRuntime.fireAsync;
import static diarsid.beam.core.base.objects.Pools.giveBackToPool;
import static diarsid.beam.core.base.objects.Pools.takeFromPool;
import static diarsid.beam.core.base.util.CollectionsUtils.getOne;
import static diarsid.beam.core.base.util.CollectionsUtils.hasMany;
import static diarsid.beam.core.base.util.CollectionsUtils.hasOne;
import static diarsid.beam.core.base.util.CollectionsUtils.toSet;
import static diarsid.beam.core.base.util.ConcurrencyUtil.asyncDo;
import static diarsid.beam.core.base.util.ConcurrencyUtil.awaitGetFlow;
import static diarsid.beam.core.base.util.OptionalUtil.isNotPresent;
import static diarsid.beam.core.base.util.StringUtils.nonEmpty;
import static diarsid.beam.core.base.util.StringUtils.splitBySpacesToList;
import static diarsid.beam.core.domain.entities.Orderables.reorderAccordingToNewOrder;
import static diarsid.beam.core.domain.entities.WebDirectories.newDirectory;
import static diarsid.beam.core.domain.entities.WebPages.newWebPage;
import static diarsid.beam.core.domain.entities.WebPlace.BOOKMARKS;
import static diarsid.beam.core.domain.entities.WebPlace.UNDEFINED_PLACE;
import static diarsid.beam.core.domain.entities.WebPlace.WEBPANEL;
import static diarsid.beam.core.domain.entities.metadata.EntityProperty.NAME;
import static diarsid.beam.core.domain.entities.metadata.EntityProperty.ORDER;
import static diarsid.beam.core.domain.entities.metadata.EntityProperty.SHORTCUTS;
import static diarsid.beam.core.domain.entities.metadata.EntityProperty.UNDEFINED_PROPERTY;
import static diarsid.beam.core.domain.entities.metadata.EntityProperty.WEB_DIRECTORY;
import static diarsid.beam.core.domain.entities.metadata.EntityProperty.WEB_URL;
import static diarsid.beam.core.domain.entities.validation.DomainValidationRule.ENTITY_NAME_RULE;
import static diarsid.beam.core.domain.entities.validation.DomainValidationRule.WEB_URL_RULE;
import static diarsid.beam.core.domain.entities.validation.ValidationResults.validationFailsWith;
import static diarsid.beam.core.domain.entities.validation.ValidationResults.validationOk;


public class WebPagesKeeperWorker 
        extends 
                WebObjectsCommonKeeper 
        implements 
                WebPagesKeeper {
    
    private final Object allPagesConsistencyLock;
    private final DaoWebPages daoPages;
    private final DaoWebDirectories daoDirectories;
    private final DaoPatternChoices daoPatternChoices;
    private final DaoPictures daoPictures;
    private final CommandsMemoryKeeper commandsMemory;
    private final InnerIoEngine ioEngine;
    private final InteractionGui interactionGui;
    private final Initiator systemInitiator;
    private final KeeperDialogHelper helper;
    private final PropertyAndTextParser propetyTextParser;
    private final WebObjectsInputParser webObjectsParser;
    private final Set<CommandType> subjectedCommandTypes;
    private final WebDirectory defaultDirectory;
    private final Help chooseOnePageHelp;
    private final Help enterNewPageNameHelp;
    private final Help enterNewPageUrlHelp;
    private final Help applyFreeIndexToNameHelp;
    private final Help enterExistingPageNameHelp;
    private final Help chooseOneDirectoryHelp;
    private final Help enterDirectoryNameHelp;
    private final Help enterShortcutsHelp;
    
    public WebPagesKeeperWorker(
            DaoWebPages dao, 
            DaoWebDirectories daoDirectories,
            DaoPictures daoPictures,
            CommandsMemoryKeeper commandsMemory,
            DaoPatternChoices daoPatternChoices,
            InnerIoEngine ioEngine, 
            InteractionGui interactionGui,
            Initiator systemInitiator,
            KeeperDialogHelper helper,
            PropertyAndTextParser propetyTextParser,
            WebObjectsInputParser parser) {
        super(ioEngine);
        this.allPagesConsistencyLock = new Object();
        this.daoPages = dao;
        this.commandsMemory = commandsMemory;
        this.daoDirectories = daoDirectories;
        this.daoPictures = daoPictures;
        this.daoPatternChoices = daoPatternChoices;
        this.ioEngine = ioEngine;
        this.interactionGui = interactionGui;
        this.systemInitiator = systemInitiator;
        this.helper = helper;
        this.propetyTextParser = propetyTextParser;
        this.webObjectsParser = parser;
        this.subjectedCommandTypes = toSet(BROWSE_WEBPAGE);
        this.defaultDirectory = this.getOrCreateDefaultDirectory();   
        this.chooseOnePageHelp = this.ioEngine.addToHelpContext(
                "Choose one WebPage.",
                "Use:",
                "   - WebPage number to choose it",
                "   - WebPage name part to choose it",
                "   - n/no to see more WebPages, if any",
                "   - dot to break"
        );
        this.enterNewPageNameHelp = this.ioEngine.addToHelpContext(
                "Enter WebPage name.",
                "Name cannot contain following chars: " + join("", UNACCEPTABLE_DOMAIN_CHARS)
        );
        this.enterNewPageUrlHelp = this.ioEngine.addToHelpContext(
                "Enter WebPage URL.",
                "It must be a valid URL."
        );
        this.applyFreeIndexToNameHelp = this.ioEngine.addToHelpContext(
                "This means that such name already exists. Thus you can choose ", 
                "to save page name with index (+1) to avoid name duplication.",
                "Specify whether you want to save new name with given free index.",
                "Use: ",
                "   - y/yes/+ to agree",
                "   - n/no to enter other name",
                "   - dot to break"
        );
        this.enterExistingPageNameHelp = this.ioEngine.addToHelpContext(
                "Enter existing WebPage name or name pattern.",
                "Name cannot contain following chars: " + join("", UNACCEPTABLE_DOMAIN_CHARS)
        );
        this.chooseOneDirectoryHelp = this.ioEngine.addToHelpContext(
                "Choose one WebDirectory.",
                "Use:",
                "   - WebDirectory number to choose it",
                "   - WebDirectory name part to choose it",
                "   - n/no to see more WebDirectories, if any",
                "   - dot to break"
        );
        this.enterDirectoryNameHelp = this.ioEngine.addToHelpContext(
                "Enter existing WebDirectory name or name pattern.",
                "Name cannot contain following chars: " + join("", UNACCEPTABLE_DOMAIN_CHARS)
        );
        this.enterShortcutsHelp = this.ioEngine.addToHelpContext(
                "Enter WebPage shortcuts. Shortcuts is a name",
                "substitution and can be used to search or invoke",
                "the WebPage by its shortcuts as by its name.",
                "Shorcuts can contain words separated by spaces and",
                "cannot contain following chars: " + join("", UNACCEPTABLE_DOMAIN_CHARS) +".",
                "Shortcuts can be empty."
        );
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
                        .forEach(alias -> {
                            this.commandsMemory.removeByExactOriginalAndType(
                                    initiator, alias, BROWSE_WEBPAGE);
                        });
            }
            if ( nonEmpty(pageNewShorts) ) {
                splitBySpacesToList(pageNewShorts)
                        .stream()
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
                        .forEach(alias -> {
                            this.commandsMemory.save(
                                    initiator, 
                                    new BrowsePageCommand(alias, page.name(), NEW, TARGET_FOUND));
                        });
            }
        });
    }
    
    @Override
    public ValueFlow<WebPage> findByExactName(
            Initiator initiator, String name) {
        return valueFlowCompletedWith(this.daoPages.getByExactName(initiator, name));
    }
    
    @Override
    public ValueFlow<WebPage> findByNamePattern(
            Initiator initiator, String namePattern) {
        List<WebPage> foundPages = this.daoPages.findByPattern(initiator, namePattern);
        if ( hasOne(foundPages) ) {
            WebPage page = getOne(foundPages);
            if ( page.name().equalsIgnoreCase(namePattern) ||
                 isEntitySatisfiable(namePattern, page) ) {
                return valueFlowCompletedWith(page);
            } else {
                return valueFlowCompletedEmpty();
            }
        } else if ( hasMany(foundPages) ) {
            return this.manageWithManyPages(initiator, namePattern, foundPages);
        } else {
            return valueFlowCompletedEmpty();
        }
    }
    
    private ValueFlow<WebPage> manageWithManyPages(
            Initiator initiator, String pattern, List<WebPage> pages) {
        WeightedVariants variants = weightVariants(pattern, entitiesToVariants(pages));
        if ( variants.isEmpty() ) {
            return valueFlowCompletedEmpty();
        }
        
        WeightedVariant bestVariant = variants.best();
        if ( bestVariant.text().equalsIgnoreCase(pattern) || 
             bestVariant.hasEqualOrBetterWeightThan(PERFECT) ) {
            return valueFlowCompletedWith(pages.get(bestVariant.index()));
        } else {
            if ( this.daoPatternChoices.hasMatchOf(pattern, bestVariant.text(), variants) ) {
                return valueFlowCompletedWith(pages.get(bestVariant.index()));
            } else {
                return this.askUserForPageAndSaveChoice(
                        initiator, pattern, variants, pages);
            }            
        }
    }   
    
    private ValueFlow<WebPage> askUserForPageAndSaveChoice(
            Initiator initiator, 
            String pattern, 
            WeightedVariants variants, 
            List<WebPage> pages) {
        Answer answer = this.ioEngine.chooseInWeightedVariants(
                initiator, variants, this.chooseOnePageHelp);
        if ( answer.isGiven() ) {
            asyncDo(() -> {
                this.daoPatternChoices.save(
                        pattern, pages.get(answer.index()).name(), variants);
            });
            return valueFlowCompletedWith(pages.get(answer.index()));
        } else if ( answer.isRejection() ) {
            return valueFlowStopped();
        } else if ( answer.variantsAreNotSatisfactory() ) {
            return valueFlowCompletedEmpty();
        } else {
            return valueFlowCompletedEmpty();
        }
    }

    private String discussShortcuts(Initiator initiator) {
        
        String shortcuts = this.ioEngine.askInput(
                initiator, "shortcuts, if any", this.enterShortcutsHelp);
        if ( nonEmpty(shortcuts) ) {
            ValidationResult shortcutsValidity = ENTITY_NAME_RULE.applyTo(shortcuts);
            validation: while ( shortcutsValidity.isFail() ) {
                shortcuts = this.ioEngine.askInput(initiator, "shortcuts", this.enterShortcutsHelp);
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
    
    private ValueFlow<WebPage> findExistingPageInternally(Initiator initiator, String pagePattern) {
        List<WebPage> foundPages;
        VariantsQuestion question;
        KeeperLoopValidationDialog dialog = takeFromPool(KeeperLoopValidationDialog.class);                
               
        synchronized ( this.allPagesConsistencyLock ) {
            try { 
                dialog
                        .withRule(ENTITY_NAME_RULE)
                        .withInputSource(() -> {
                            return this.ioEngine.askInput(
                                    initiator, "name", this.enterExistingPageNameHelp);
                        })
                        .withOutputDestination((validationFail) -> {
                            this.ioEngine.report(initiator, validationFail);
                        });
                pageFinding : while ( true ) {  

                    pagePattern = dialog
                            .withInitialArgument(pagePattern)
                            .validateAndGet();

                    if ( pagePattern.isEmpty() ) {
                        return valueFlowStopped();
                    }

                    foundPages = this.daoPages.findByPattern(initiator, pagePattern);

                    if ( foundPages.isEmpty() ) {
                        this.ioEngine.report(
                                initiator, format("page not found by '%s'.", pagePattern));
                        pagePattern = "";
                    } else if ( hasOne(foundPages) ) {
                        return valueFlowCompletedWith(getOne(foundPages));
                    } else {
                        question = question("choose page").withAnswerEntities(foundPages);
                        Answer answer = this.ioEngine.ask(
                                initiator, question, this.chooseOnePageHelp);
                        if ( answer.isGiven() ) {
                            return valueFlowCompletedWith(foundPages.get(answer.index()));
                        } else if ( answer.isRejection() ) {
                            return valueFlowStopped();
                        } else if ( answer.variantsAreNotSatisfactory() ) {
                            pagePattern = "";
                            continue pageFinding;
                        } else {
                            this.ioEngine.report(initiator, "cannot determine your answer.");
                            return valueFlowStopped();
                        }
                    }
                }
            } finally {
                giveBackToPool(dialog);
            }
        }
    }

    private String discussPageNewName(Initiator initiator, String name) {
        KeeperLoopValidationDialog dialog = takeFromPool(KeeperLoopValidationDialog.class);
        Optional<Integer> freeNameIndex;
        Choice applyIndexChoice;
        String applyIndexQuestion;
        
        try { 
            dialog
                    .withRule(ENTITY_NAME_RULE)
                    .withInputSource(() -> {
                        return this.ioEngine.askInput(initiator, "name", this.enterNewPageNameHelp);
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
                
                freeNameIndex = daoPages.findFreeNameNextIndex(initiator, name);
                if ( isNotPresent(freeNameIndex) ) {
                    this.ioEngine.report(initiator, "DAO failed to get free name index.");
                    return "";
                }
                if ( freeNameIndex.get() > 0 ) {
                    this.ioEngine.report(initiator, format("page '%s' already exists.", name));
                    applyIndexQuestion = format(
                            "name page as '%s (%s)'", name, freeNameIndex.get());
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
    
    private String discussPageNewUrl(Initiator initiator, String url) {
        KeeperLoopValidationDialog dialog = takeFromPool(KeeperLoopValidationDialog.class);
        
        try { 
            url = dialog
                    .withInitialArgument(url)
                    .withRule(WEB_URL_RULE)
                    .withRule((newUrl) -> {
                        Optional<WebPage> page = this.daoPages.getByUrl(initiator, newUrl);
                        if ( page.isPresent() ) {
                            return validationFailsWith(
                                    format("Page '%s' already has given URL", page.get().name()));
                        }
                        return validationOk();
                    })
                    .withInputSource(() -> {
                        return this.ioEngine.askInput(initiator, "url", this.enterNewPageUrlHelp);
                    })
                    .withOutputDestination((validationFail) -> {
                        this.ioEngine.report(initiator, validationFail);
                    })
                    .validateAndGet();

            return url;
        } finally {
            giveBackToPool(dialog);
        }
    }
    
    private ValueFlow<WebDirectory> findExistingWebDirectoryInternally(
            Initiator initiator, WebPlace place) {
        if ( place.isUndefined() ) {
            place = super.discussWebPlace(initiator);
        }        
        if ( place.isUndefined() ) {
            return valueFlowStopped();
        }
        
        String directoryName;
        List<WebDirectory> foundDirectories;
        VariantsQuestion question;    
        KeeperLoopValidationDialog dialog = takeFromPool(KeeperLoopValidationDialog.class)
                .withInitialArgument("")
                .withRule(ENTITY_NAME_RULE)
                .withInputSource(() -> {
                    return this.ioEngine.askInput(
                            initiator, "directory name", this.enterDirectoryNameHelp);
                })
                .withOutputDestination((validationFail) -> {
                    this.ioEngine.report(initiator, validationFail);
                });
         
        try {
            directoryDefining: while ( true ) {            
                directoryName = dialog
                        .withInitialArgument("")
                        .validateAndGet();

                foundDirectories = this.daoDirectories
                        .findDirectoriesByPatternInPlace(initiator, directoryName, place);

                if ( foundDirectories.isEmpty() ) {
                    this.ioEngine.report(initiator, "directory not found.");
                } else if ( hasOne(foundDirectories) ) {
                    return valueFlowCompletedWith(getOne(foundDirectories));
                } else {
                    question = question("choose directory").withAnswerEntities(foundDirectories);
                    Answer answer = this.ioEngine.ask(initiator, question, this.chooseOneDirectoryHelp);
                    if ( answer.isGiven() ) {
                        return valueFlowCompletedWith(foundDirectories.get(answer.index()));
                    } else if ( answer.isRejection() ) {
                        return valueFlowStopped();
                    } else if ( answer.variantsAreNotSatisfactory() ) {
                        continue directoryDefining;
                    } else {
                        return valueFlowFail("unknown flow");
                    }
                }
            }
        } finally {
            giveBackToPool(dialog);
        }    
    }

    @Override
    public VoidFlow createWebPage(
            Initiator initiator, ArgumentsCommand command) {
        if ( command.type().isNot(CREATE_PAGE) ) {
            return voidFlowFail("wrong command type!");
        }
        
        WebPlace place;
        String name;
        String url;
        if ( command.hasArguments() ) {
            WebPageNameUrlAndPlace data = this.webObjectsParser
                    .parseNameUrlAndPlace(command.arguments());
            place = data.place();
            name = data.name();
            url = data.url();
        } else {
            place = UNDEFINED_PLACE;
            name = "";
            url = "";
        }
        
        synchronized ( this.allPagesConsistencyLock ) {
            
            name = this.discussPageNewName(initiator, name);
            if ( name.isEmpty() ) {
                return voidFlowStopped();
            }

            url = this.discussPageNewUrl(initiator, url);
            if ( url.isEmpty() ) {
                return voidFlowStopped();
            }

            String shortcuts = this.discussShortcuts(initiator);

            ValueFlow<WebDirectory> directoryFlow = 
                    this.findExistingWebDirectoryInternally(initiator, place);
            switch ( directoryFlow.result() ) {
                case COMPLETE :
                    String message;
                    if ( directoryFlow.asComplete().hasValue() ) {
                        message = format("directory found: '%s'", 
                                         directoryFlow.asComplete().orThrow().name());
                        
                    } else {
                        message = format("default directory '%s' will be used.", 
                                         this.defaultDirectory.name());
                        directoryFlow = valueFlowCompletedWith(this.defaultDirectory);
                    }
                    this.ioEngine.report(initiator, message);
                    break;
                case STOP :
                    return voidFlowStopped();
                case FAIL :
                    return directoryFlow.toVoid();
                default :
                    return voidFlowFail("unkown flow result");
            } 

            int dirId = directoryFlow.asComplete().orThrow().id();
            WebPage page = newWebPage(name, shortcuts, url, dirId);
            if ( daoPages.save(initiator, page) ) {
                this.asyncAddCommandsForPage(initiator, page);
                return voidFlowCompleted();
            } else {
                return voidFlowFail("DAO failed to save new page.");
            }
        }    
    }

    @Override
    public VoidFlow editWebPage(
            Initiator initiator, ArgumentsCommand command) {
        if ( command.type().isNot(EDIT_PAGE) ) {
            return voidFlowFail("wrong command type!");
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
        
        synchronized ( this.allPagesConsistencyLock ) {
            ValueFlow<WebPage> pageFlow = this.findExistingPageInternally(initiator, pagePattern);
            if ( pageFlow.isNotCompletedWithValue() ) {
                return pageFlow.toVoid();
            }
            
            WebPage page = pageFlow.asComplete().orThrow();
            this.ioEngine.report(initiator, format("'%s' found.", page.name()));

            propertyToEdit = this.helper.validatePropertyInteractively(
                    initiator, propertyToEdit, SHORTCUTS, WEB_URL, NAME, ORDER, WEB_DIRECTORY);
            if ( propertyToEdit.isUndefined() ) {
                return voidFlowStopped();
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
                    return voidFlowFail("undefined property.");
                }
            } 
        }    
    }
    
    private VoidFlow editPageName(Initiator initiator, String pageName) {
        String newName = this.discussPageNewName(initiator, "");
        
        if ( newName.isEmpty() ) {
            return voidFlowStopped();
        }
        
        if ( this.daoPages.editName(initiator, pageName, newName) ) {
            this.asyncChangeCommandsForPageNames(initiator, pageName, newName);
            return voidFlowCompleted();
        } else {
            return voidFlowFail("DAO failed to rename page.");
        }
    }
    
    private VoidFlow editPageShortcuts(
            Initiator initiator, String pageName, String oldShortcuts) {
        String newShortcuts = this.discussShortcuts(initiator);
        if ( newShortcuts.isEmpty() ) {
            this.ioEngine.report(initiator, "removing shortcuts...");
        }
        if ( this.daoPages.editShortcuts(initiator, pageName, newShortcuts) ) {
            this.asyncChangeCommandsForPageShortcuts(
                    initiator, pageName, oldShortcuts, newShortcuts);
            return voidFlowCompleted();
        } else {
            return voidFlowFail("DAO failed to change shortcuts.");
        }
    }
    
    private VoidFlow editPageUrl(Initiator initiator, String pageName) {
        String url = this.discussPageNewUrl(initiator, "");
        
        if ( url.isEmpty() ) {
            return voidFlowStopped();
        }
        
        if ( this.daoPages.editUrl(initiator, pageName, url) ) {
            return voidFlowCompleted();
        } else {
            return voidFlowFail("DAO failed to change url.");
        }
    }
    
    private VoidFlow editPageOrder(Initiator initiator, WebPage page) {
        List<WebPage> pagesInDirectory = this.daoPages
                .getAllFromDirectory(initiator, page.directoryId());
        if ( pagesInDirectory.isEmpty() ) {
            return voidFlowFail("cannot find all pages from directory.");
        }
        int pageNewOrder = this.helper.discussIntInRange(
                initiator, 0, pagesInDirectory.size() - 1, "new order");
        if ( pageNewOrder < 0 ) {
            return voidFlowStopped();
        }
        reorderAccordingToNewOrder(pagesInDirectory, page.order(), pageNewOrder);
        sort(pagesInDirectory);
        if ( this.daoPages.updatePageOrdersInDir(initiator, pagesInDirectory) ) {
            return voidFlowCompleted();
        } else {
            return voidFlowFail("DOA failed to reorder directory with new page order.");
        }
    }
    
    private VoidFlow editPageWebDirectory(Initiator initiator, WebPage page) {
        this.ioEngine.report(initiator, "choosing new page directory...");
        ValueFlow<WebDirectory> directoryFlow = 
                this.findExistingWebDirectoryInternally(initiator, UNDEFINED_PLACE);
        if ( directoryFlow.isNotCompletedWithValue() ) {
            return voidFlowStopped();
        }
        
        WebDirectory directory = directoryFlow.asComplete().orThrow();
        boolean pageMoved = this.daoPages.movePageFromDirToDir(initiator, page, directory.id());
        
        if ( pageMoved ) {
            return voidFlowCompleted();
        } else {
            return voidFlowFail("DAO failed to move page to new directory.");
        }
    }

    @Override
    public VoidFlow removeWebPage(
            Initiator initiator, ArgumentsCommand command) {
        if ( command.type().isNot(DELETE_PAGE) ) {
            return voidFlowFail("wrong command type!");
        }
        
        String pagePattern;
        if ( command.hasArguments() ) {
            pagePattern = command.joinedArguments();
        } else {
            pagePattern = "";
        }
        
        synchronized ( this.allPagesConsistencyLock ) {
            ValueFlow<WebPage> pageFlow = this.findExistingPageInternally(initiator, pagePattern);
            if ( pageFlow.isNotCompletedWithValue() ) {
                return pageFlow.toVoid();                  
            } 

            WebPage page = pageFlow.asComplete().orThrow();
            this.ioEngine.report(initiator, format("'%s' found.", page.name()));              

            if ( this.daoPages.remove(initiator, page.name()) ) {
                this.asyncRemoveCommandsForPage(initiator, page);
                return voidFlowCompleted();
            } else {
                return voidFlowFail("DAO failed to remove page.");
            }
        }        
    }
    
    @Override
    public VoidFlow captureImage(
            Initiator initiator, ArgumentsCommand command) {
        if ( command.type().isNot(CAPTURE_PAGE_IMAGE) ) {
            return voidFlowFail("wrong command type!");
        }
        
        String pageNamePattern;
        if ( command.hasArguments() ) {
            pageNamePattern = command.joinedArguments();
        } else {
            pageNamePattern = "";
        }
        
        ValueFlow<WebPage> pageFlow = this.findExistingPageInternally(initiator, pageNamePattern);
        switch ( pageFlow.result() ) {
            case COMPLETE :
                if ( pageFlow.asComplete().isEmpty() ) {
                    return voidFlowStopped();
                } else {
                    break;
                }                
            case STOP :
                return voidFlowStopped();
            case FAIL :
                return pageFlow.toVoid();
            default :
                return voidFlowFail("unexpected flow result.");          
        }
        
        WebPage page = pageFlow.asComplete().orThrow();
        this.ioEngine.report(initiator, format("'%s' found.", page.name()));
        
        ValueFlow<Picture> pictureFlow = awaitGetFlow(() -> {     
            return this.interactionGui.capturePictureOnScreen(page.name());
        });
        
        switch ( pictureFlow.result() ) {
            case COMPLETE : {
                if ( ! pictureFlow.asComplete().hasValue() ) {
                    return pictureFlow.toVoid();
                } 
                break;
            }    
            case FAIL : 
            case STOP : {
                return pictureFlow.toVoid();
            }    
            default : {
                this.ioEngine.report(
                        initiator, "unexpected picture flow result " + pictureFlow.result().name());
                return voidFlowStopped();
            }    
        }
        
        Picture picture = pictureFlow.asComplete().orThrow();
        
        boolean saved = this.daoPictures.save(initiator, picture);
        if ( saved ) {
            fireAsync("image_saved", picture.name());
            return voidFlowCompleted("captured!");
        } else {
            return voidFlowFail("image not saved.");
        }        
    }

    @Override
    public ValueFlow<WebPage> findWebPageByPattern(
            Initiator initiator, ArgumentsCommand command) {
        if ( command.type().isNot(FIND_WEBPAGE) ) {
            return valueFlowFail("wrong command type!");
        }
        
        String pagePattern;
        if ( command.hasArguments() ) {
            pagePattern = command.joinedArguments();
        } else {
            pagePattern = "";
        }
        
        return this.findExistingPageInternally(initiator, pagePattern);
    }

    @Override
    public List<WebPage> findWebPagesByPattern(
            Initiator initiator, String pattern) {
        return this.daoPages.findByPattern(initiator, pattern);
    }
    
    @Override
    public ValueFlow<Message> getWebPlace(
            Initiator initiator, EmptyCommand command) {
        if ( command.type().isNot(SHOW_WEBPANEL) && command.type().isNot(SHOW_BOOKMARKS) ) {
            return valueFlowFail("wrong command type!");
        }
        
        WebPlace place = this.commandTypeToPlace(command.type());
        if ( place.isUndefined() ) {
            return valueFlowFail("cannot define WebPlace.");
        }
        
        List<WebDirectoryPages> directories = this.daoDirectories
                .getAllDirectoriesPagesInPlace(initiator, place);
        
        if ( directories.isEmpty() ) {
            return valueFlowCompletedWith(info(place.displayName() + " is empty."));
        }
        
        Message message = info(directories
                .stream()
                .sorted()
                .flatMap(directory -> directory.toMessage().allLines().stream())                
                .collect(toList()));
        message.addHeader(place.displayName());
        
        return valueFlowCompletedWith(message);
    }
    
    private WebPlace commandTypeToPlace(CommandType type) {
        if ( type.is(SHOW_WEBPANEL) ) {
            return WEBPANEL;
        }
        if ( type.is(SHOW_BOOKMARKS) ) {
            return BOOKMARKS;
        }
        return UNDEFINED_PLACE;
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
        
        synchronized ( this.allPagesConsistencyLock ) {
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
    }

    @Override
    public WebResponse editWebPageName(
            WebPlace place, String directoryName, String pageOldName, String pageNewName) {
        synchronized ( this.allPagesConsistencyLock ) {
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
    public WebResponse getWebPageImage(
            WebPlace place, String directoryName, String pageName) {
        Optional<WebDirectoryPages> webDirectoryPages = this.daoDirectories
                .getDirectoryPagesByNameAndPlace(this.systemInitiator, directoryName, place);
        if ( isNotPresent(webDirectoryPages) ) {
            return notFoundWithJson(format(
                    "Directory '%s' not found in %s!", directoryName, place.name()));
        }
        
        boolean pageExists = webDirectoryPages.get().pages()
                .stream()
                .anyMatch(webPage -> webPage.name().equalsIgnoreCase(pageName));
        
        if ( pageExists ) {
            return optionalOkWithBinary(this.daoPictures.getByName(this.systemInitiator, pageName));
        } else {
            return notFoundWithJson(format(
                    "Page '%s' not found in %s!", pageName, directoryName));
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
        synchronized ( this.allPagesConsistencyLock ) {
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

    @Override
    public ValueFlow<Message> findAll(Initiator initiator) {
        return valueFlowCompletedWith(entitiesToOptionalMessageWithHeader(
                    "all WebPages:", this.daoPages.getAll(initiator)));
    }
}
