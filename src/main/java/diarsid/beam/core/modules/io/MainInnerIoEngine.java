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
import diarsid.beam.core.base.control.io.base.actors.OuterIoEngine;
import diarsid.beam.core.base.control.io.base.actors.TimeMessagesIo;
import diarsid.beam.core.base.control.io.base.interaction.Answer;
import diarsid.beam.core.base.control.io.base.interaction.Choice;
import diarsid.beam.core.base.control.io.base.interaction.Message;
import diarsid.beam.core.base.control.io.base.interaction.Question;
import diarsid.beam.core.base.control.io.base.interaction.TaskMessage;
import diarsid.beam.core.base.control.io.base.interaction.TextMessage;

import static diarsid.beam.core.Beam.getSystemInitiator;
import static diarsid.beam.core.base.control.io.base.interaction.Answer.noAnswerFromVariants;
import static diarsid.beam.core.base.control.io.base.interaction.Choice.CHOICE_NOT_MADE;
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
    private final Initiator systemInitiator;
    
    public MainInnerIoEngine(OuterIoEnginesHolder ioEnginesHolder, Gui gui) {
        this.ioEnginesHolder = ioEnginesHolder;
        this.gui = gui;
        this.systemInitiator = getSystemInitiator();
    }

    @Override
    public Choice ask(Initiator initiator, String yesOrNoQuestion) {
        if ( this.ioEnginesHolder.hasEngine(initiator) ) {
            return awaitGet(() -> {
                try {
                    return this.ioEnginesHolder
                            .getEngine(initiator)
                            .resolveYesOrNo(yesOrNoQuestion);
                } catch (IOException ex) {
                    logError(this.getClass(), ex);
                    this.ioEnginesHolder.deleteEngine(initiator);
                    return CHOICE_NOT_MADE;
                }
            }).orElse(CHOICE_NOT_MADE);            
        } else {
            return CHOICE_NOT_MADE;
        }
    }

    @Override
    public Answer ask(Initiator initiator, Question question) {
        if ( this.ioEnginesHolder.hasEngine(initiator) ) {
            return awaitGet(() -> {
                try {
                    return this.ioEnginesHolder
                            .getEngine(initiator)
                            .resolveQuestion(question);
                } catch (IOException ex) {
                    logError(this.getClass(), ex);
                    this.ioEnginesHolder.deleteEngine(initiator);
                    return noAnswerFromVariants();
                }
            }).orElse(noAnswerFromVariants());            
        } else {
            return noAnswerFromVariants();
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
            OuterIoEngine ioEngine = this.ioEnginesHolder.getEngine(initiator);
            awaitDo(() -> {
                try {
                    ioEngine.reportMessage(message);
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
                            ioEngine.reportMessage(message);
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
