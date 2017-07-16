/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package diarsid.beam.core.modules.io;

import java.io.IOException;
import java.util.List;

import diarsid.beam.core.base.control.io.base.actors.Initiator;
import diarsid.beam.core.base.control.io.base.actors.InnerIoEngine;
import diarsid.beam.core.base.control.io.base.actors.TimeMessagesIo;
import diarsid.beam.core.base.control.io.base.interaction.Answer;
import diarsid.beam.core.base.control.io.base.interaction.Choice;
import diarsid.beam.core.base.control.io.base.interaction.Message;
import diarsid.beam.core.base.control.io.base.interaction.TaskMessage;
import diarsid.beam.core.base.control.io.base.interaction.TextMessage;
import diarsid.beam.core.base.control.io.base.interaction.VariantsQuestion;
import diarsid.beam.core.domain.patternsanalyze.WeightedVariants;

import static diarsid.beam.core.base.control.io.base.interaction.Choice.NOT_MADE;
import static diarsid.beam.core.base.control.io.base.interaction.Message.MessageType.ERROR;
import static diarsid.beam.core.base.control.io.base.interaction.Message.MessageType.INFO;
import static diarsid.beam.core.base.util.ConcurrencyUtil.asyncDo;
import static diarsid.beam.core.base.util.ConcurrencyUtil.awaitDo;
import static diarsid.beam.core.base.util.ConcurrencyUtil.awaitGet;
import static diarsid.beam.core.base.util.Logs.logError;
import static diarsid.beam.core.base.control.io.base.interaction.Answers.rejectedAnswer;
import static diarsid.beam.core.Beam.systemInitiator;

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
    private final Initiator systemInitiator;
    
    public MainInnerIoEngine(OuterIoEnginesHolder ioEnginesHolder, Gui gui) {
        this.ioEnginesHolder = ioEnginesHolder;
        this.gui = gui;
        this.systemInitiator = systemInitiator();
    }

    @Override
    public Choice ask(Initiator initiator, String yesOrNoQuestion) {
        if ( this.ioEnginesHolder.hasEngine(initiator) ) {
            return awaitGet(() -> {
                try {
                    return this.ioEnginesHolder
                            .getEngine(initiator)
                            .resolve(yesOrNoQuestion);
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
    public Answer ask(Initiator initiator, VariantsQuestion question) {
        if ( this.ioEnginesHolder.hasEngine(initiator) ) {
            return awaitGet(() -> {
                try {
                    return this.ioEnginesHolder
                            .getEngine(initiator)
                            .resolve(question);
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
    public Answer chooseInWeightedVariants(Initiator initiator, WeightedVariants variants) {
        if ( this.ioEnginesHolder.hasEngine(initiator) ) {
            return awaitGet(() -> {
                try {
                    return this.ioEnginesHolder
                            .getEngine(initiator)
                            .resolve(variants);
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
    public String askInput(Initiator initiator, String inputQuestion) {
        if ( this.ioEnginesHolder.hasEngine(initiator) ) {
            return awaitGet(() -> {
                try {
                    return this.ioEnginesHolder
                            .getEngine(initiator)
                            .askForInput(inputQuestion);
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
