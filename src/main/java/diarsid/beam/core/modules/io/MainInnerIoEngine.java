/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package diarsid.beam.core.modules.io;

import java.io.IOException;
import java.util.List;

import diarsid.beam.core.application.gui.Gui;
import diarsid.beam.core.base.analyze.variantsweight.WeightedVariants;
import diarsid.beam.core.base.control.io.base.actors.Initiator;
import diarsid.beam.core.base.control.io.base.actors.InnerIoEngine;
import diarsid.beam.core.base.control.io.base.actors.OuterIoEngine;
import diarsid.beam.core.base.control.io.base.actors.TimeMessagesIo;
import diarsid.beam.core.base.control.io.base.interaction.Answer;
import diarsid.beam.core.base.control.io.base.interaction.Choice;
import diarsid.beam.core.base.control.io.base.interaction.Help;
import diarsid.beam.core.base.control.io.base.interaction.HelpContext;
import diarsid.beam.core.base.control.io.base.interaction.HelpKey;
import diarsid.beam.core.base.control.io.base.interaction.Message;
import diarsid.beam.core.base.control.io.base.interaction.TaskMessage;
import diarsid.beam.core.base.control.io.base.interaction.TextMessage;
import diarsid.beam.core.base.control.io.base.interaction.VariantsQuestion;

import static java.util.Arrays.asList;

import static diarsid.beam.core.Beam.systemInitiator;
import static diarsid.beam.core.base.control.io.base.interaction.Answers.rejectedAnswer;
import static diarsid.beam.core.base.control.io.base.interaction.Choice.NOT_MADE;
import static diarsid.beam.core.base.control.io.base.interaction.Help.isHelpRequest;
import static diarsid.beam.core.base.control.io.base.interaction.Message.MessageType.ERROR;
import static diarsid.beam.core.base.control.io.base.interaction.Message.MessageType.INFO;
import static diarsid.beam.core.base.util.ConcurrencyUtil.asyncDo;
import static diarsid.beam.core.base.util.ConcurrencyUtil.awaitDo;
import static diarsid.beam.core.base.util.ConcurrencyUtil.awaitGet;
import static diarsid.beam.core.base.util.Logs.logError;

/**
 *
 * @author Diarsid
 */
public class MainInnerIoEngine 
        implements 
                InnerIoEngine,
                TimeMessagesIo {
    
    private final OuterIoEnginesHolder ioEnginesHolder;
    private final Gui gui;
    private final HelpContext helpContext;
    private final Initiator systemInitiator;
    
    public MainInnerIoEngine(
            OuterIoEnginesHolder ioEnginesHolder, Gui gui, HelpContext helpContext) {
        this.ioEnginesHolder = ioEnginesHolder;
        this.gui = gui;
        this.helpContext = helpContext;
        this.systemInitiator = systemInitiator();
    }

    @Override
    public Choice ask(Initiator initiator, String yesOrNoQuestion, Help help) {
        if ( this.ioEnginesHolder.hasEngine(initiator) ) {
            return awaitGet(() -> {
                try {
                    OuterIoEngine ioEngine = this.ioEnginesHolder.getEngine(initiator);
                    Choice choice = ioEngine.resolve(yesOrNoQuestion);
                    while ( choice.isHelpRequest() ) {
                        if ( help.isKey() ) {
                            ioEngine.report(this.helpContext.get(help.asKey()));
                        } else {
                            ioEngine.report(help.asInfo());
                        }                        
                        choice = ioEngine.resolve(yesOrNoQuestion);
                    }
                    return choice;
                } catch (IOException ex) {
                    logError(this.getClass(), ex);
                    this.ioEnginesHolder.deleteEngine(initiator);
                    return NOT_MADE;
                }
            }).orElse(NOT_MADE);            
        } else {
            return NOT_MADE;
        }
    }

    @Override
    public Answer ask(Initiator initiator, VariantsQuestion question, Help help) {
        if ( this.ioEnginesHolder.hasEngine(initiator) ) {
            return awaitGet(() -> {
                try {
                    OuterIoEngine ioEngine = this.ioEnginesHolder.getEngine(initiator);
                    Answer answer = ioEngine.resolve(question);
                    while ( answer.isHelpRequest() ) {
                        if ( help.isKey() ) {
                            ioEngine.report(this.helpContext.get(help.asKey()));
                        } else {
                            ioEngine.report(help.asInfo());
                        }
                        answer = ioEngine.resolve(question);
                    }
                    return answer; 
                } catch (IOException ex) {
                    logError(this.getClass(), ex);
                    this.ioEnginesHolder.deleteEngine(initiator);
                    return rejectedAnswer();
                }
            }).orElse(rejectedAnswer());            
        } else {
            return rejectedAnswer();
        }        
    }

    @Override
    public Answer chooseInWeightedVariants(
            Initiator initiator, WeightedVariants variants, Help help) {
        if ( this.ioEnginesHolder.hasEngine(initiator) ) {
            return awaitGet(() -> {
                try {
                    OuterIoEngine ioEngine = this.ioEnginesHolder.getEngine(initiator);
                    Answer answer = ioEngine.resolve(variants);
                    while ( answer.isHelpRequest() ) {
                        if ( help.isKey() ) {
                            ioEngine.report(this.helpContext.get(help.asKey()));
                        } else {
                            ioEngine.report(help.asInfo());
                        }
                        answer = ioEngine.resolve(variants);
                    }
                    return answer;        
                } catch (IOException ex) {
                    logError(this.getClass(), ex);
                    this.ioEnginesHolder.deleteEngine(initiator);
                    return rejectedAnswer();
                }
            }).orElse(rejectedAnswer());      
        } else {
            return rejectedAnswer();
        }  
    }
    
    @Override
    public String askInput(Initiator initiator, String inputQuestion, Help help) {
        if ( this.ioEnginesHolder.hasEngine(initiator) ) {
            return awaitGet(() -> {
                try {
                    OuterIoEngine ioEngine = this.ioEnginesHolder.getEngine(initiator);
                    String input = ioEngine.askForInput(inputQuestion); 
                    while ( isHelpRequest(input) ) {
                        if ( help.isKey() ) {
                            ioEngine.report(this.helpContext.get(help.asKey()));
                        } else {
                            ioEngine.report(help.asInfo());
                        }
                        input = ioEngine.askForInput(inputQuestion); 
                    }    
                    return input;
                } catch (IOException ex) {
                    logError(this.getClass(), ex);
                    this.ioEnginesHolder.deleteEngine(initiator);
                    return "";
                }
            }).orElse("");
        } else {
            return "";
        }
    }

    @Override
    public HelpKey addToHelpContext(String... help) {
        return this.helpContext.add(asList(help));
    }
    
    @Override
    public HelpKey addToHelpContext(List<String> help) {
        return this.helpContext.add(help);
    }

    @Override
    public void report(Initiator initiator, String string) {
        if ( this.ioEnginesHolder.hasEngine(initiator) ) {
            awaitDo(() -> {
                try {
                    this.ioEnginesHolder
                            .getEngine(initiator)
                            .report(string);
                } catch (IOException ex) {
                    logError(this.getClass(), ex);
                    this.ioEnginesHolder.deleteEngine(initiator);
                }
            });            
        } else if ( initiator.equals(this.systemInitiator) ) {
            this.gui.showMessage(new TextMessage(INFO, string));
        }    
    }
    
    @Override
    public void reportAndExitLater(Initiator initiator, String string) {
        this.ioEnginesHolder
                .all()
                .forEach(ioEngine -> {
                    asyncDo(() -> {
                        try {
                            ioEngine.report(string);
                        } catch (IOException ex) {
                            logError(this.getClass(), ex);
                        }
                    });                  
                });
        this.gui.showMessage(new TextMessage(ERROR, string));
        this.gui.exitAfterAllWindowsClosed();
    }

    @Override
    public void reportMessage(Initiator initiator, Message message) {
        if ( this.ioEnginesHolder.hasEngine(initiator) ) {
            awaitDo(() -> {
                try {
                    this.ioEnginesHolder
                            .getEngine(initiator)
                            .report(message);
                } catch (IOException ex) {
                    logError(this.getClass(), ex);
                    this.ioEnginesHolder.deleteEngine(initiator);
                }
            });            
        } else if ( initiator.equals(this.systemInitiator) ) {
            this.gui.showMessage(message);
        }
    }

    @Override
    public void reportMessageAndExitLater(Initiator initiator, Message message) {
        this.ioEnginesHolder
                .all()
                .forEach(ioEngine -> {
                    asyncDo(() -> {
                        try {
                            ioEngine.report(message);
                        } catch (IOException ex) {
                            logError(this.getClass(), ex);
                        }
                    });                    
                });
        this.gui.showMessage(message);
        this.gui.exitAfterAllWindowsClosed();
    }

    @Override
    public void show(TaskMessage task) {
        this.gui.showTask(task);
    }

    @Override
    public void showAll(List<TaskMessage> tasks) {
        tasks.stream().forEach(task -> this.gui.showTask(task));
    }

    @Override
    public void showTasksNotification(String periodOfNotification, List<TaskMessage> tasks) {
        this.gui.showTasks(periodOfNotification, tasks);
    }
}
