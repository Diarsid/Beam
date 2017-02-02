/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package diarsid.beam.core.modules.io;

import java.io.IOException;
import java.util.List;

import diarsid.beam.core.control.io.base.Answer;
import diarsid.beam.core.control.io.base.Choice;
import diarsid.beam.core.control.io.base.Initiator;
import diarsid.beam.core.control.io.base.InnerIoEngine;
import diarsid.beam.core.control.io.base.Message;
import diarsid.beam.core.control.io.base.OuterIoEngine;
import diarsid.beam.core.control.io.base.Question;
import diarsid.beam.core.control.io.base.TextMessage;
import diarsid.beam.core.control.io.base.TimeMessagesIo;
import diarsid.beam.core.control.io.base.TimeMessage;

import static java.util.concurrent.CompletableFuture.runAsync;

import static diarsid.beam.core.Beam.getSystemInitiator;
import static diarsid.beam.core.control.io.base.Answer.noAnswerFromVariants;
import static diarsid.beam.core.control.io.base.Choice.CHOICE_NOT_MADE;
import static diarsid.beam.core.control.io.base.Message.MessageType.ERROR;
import static diarsid.beam.core.control.io.base.Message.MessageType.INFO;
import static diarsid.beam.core.util.ConcurrencyUtil.waitAndDo;
import static diarsid.beam.core.util.ConcurrencyUtil.waitAndGet;
import static diarsid.beam.core.util.Logs.logError;

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
            OuterIoEngine ioEngine = this.ioEnginesHolder.getEngine(initiator);
            return waitAndGet(() -> {
                try {
                    return ioEngine.resolveYesOrNo(yesOrNoQuestion);
                } catch (Exception ex) {
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
            OuterIoEngine ioEngine = this.ioEnginesHolder.getEngine(initiator);
            return waitAndGet(() -> {
                try {
                    return ioEngine.resolveQuestion(question);
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
            OuterIoEngine ioEngine = this.ioEnginesHolder.getEngine(initiator);
            return waitAndGet(() -> {
                try {
                    return ioEngine.askForInput(inputQuestion);
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
            OuterIoEngine ioEngine = this.ioEnginesHolder.getEngine(initiator);
            waitAndDo(() -> {
                try {
                    ioEngine.report(string);
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
                    runAsync(() -> {
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
            waitAndDo(() -> {
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
                    runAsync(() -> {
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
    public void show(TimeMessage task) {
        this.gui.showTask(task);
    }

    @Override
    public void showAll(List<TimeMessage> tasks) {
        tasks.stream().forEach(task -> this.gui.showTask(task));
    }

    @Override
    public void showTasksNotification(String periodOfNotification, List<TimeMessage> tasks) {
        this.gui.showTasks(periodOfNotification, tasks);
    }
}
