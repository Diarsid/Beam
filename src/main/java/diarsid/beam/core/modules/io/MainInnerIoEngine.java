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
import diarsid.beam.core.control.io.base.OuterIoEngine;
import diarsid.beam.core.control.io.base.Question;
import diarsid.beam.core.control.io.base.TextMessage;
import diarsid.beam.core.control.io.base.TimeScheduledIo;
import diarsid.beam.core.modules.tasks.TimeMessage;

import static diarsid.beam.core.control.io.base.Answer.noAnswer;
import static diarsid.beam.core.control.io.base.Choice.CHOICE_NOT_MADE;
import static diarsid.beam.core.control.io.base.TextMessage.IoMessageType.NORMAL;
import static diarsid.beam.core.util.ConcurrencyUtil.waitAndGet;
import static diarsid.beam.core.util.Logs.logError;

/**
 *
 * @author Diarsid
 */
public class MainInnerIoEngine 
        implements 
                InnerIoEngine,
                TimeScheduledIo {
    
    private final OuterIoEnginesHolder ioEnginesHolder;
    private final Gui gui;
    private final Initiator systemInitiator;
    
    public MainInnerIoEngine(OuterIoEnginesHolder ioEnginesHolder, Gui gui) {
        this.ioEnginesHolder = ioEnginesHolder;
        this.gui = gui;
        this.systemInitiator = new Initiator();
    }
    
    Initiator getSystemInitiator() {
        return this.systemInitiator;
    }

    @Override
    public Choice resolveYesOrNo(Initiator initiator, String yesOrNoQuestion) {
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
    public Answer resolveVariants(Initiator initiator, Question question) {
        if ( this.ioEnginesHolder.hasEngine(initiator) ) {
            OuterIoEngine ioEngine = this.ioEnginesHolder.getEngine(initiator);
            return waitAndGet(() -> {
                try {
                    return ioEngine.resolveQuestion(question);
                } catch (IOException ex) {
                    logError(this.getClass(), ex);
                    this.ioEnginesHolder.deleteEngine(initiator);
                    return noAnswer();
                }
            }).orElse(noAnswer());            
        } else {
            return noAnswer();
        }        
    }

    @Override
    public void report(Initiator initiator, String string) {
        if ( this.ioEnginesHolder.hasEngine(initiator) ) {
            try {
                this.ioEnginesHolder
                        .getEngine(initiator)
                        .report(string);
            } catch (IOException ex) {
                logError(this.getClass(), ex);
                this.ioEnginesHolder.deleteEngine(initiator);
            }
        } else if ( initiator.equals(this.systemInitiator) ) {
            this.gui.showMessage(new TextMessage(NORMAL, string));
        }    
    }
    
    @Override
    public void reportAndExitLater(Initiator initiator, String string) {
        this.ioEnginesHolder
                .all()
                .forEach(ioEngine -> {
                    try {
                        ioEngine.report(string);
                    } catch (IOException ex) {
                        logError(this.getClass(), ex);
                    }
                });
        this.gui.showMessage(new TextMessage(NORMAL, string));
        this.gui.exitAfterAllWindowsClosed();
    }

    @Override
    public void reportMessage(Initiator initiator, TextMessage message) {
        if ( this.ioEnginesHolder.hasEngine(initiator) ) {
            try {
                this.ioEnginesHolder
                        .getEngine(initiator)
                        .reportMessage(message);
            } catch (IOException ex) {
                logError(this.getClass(), ex);
                this.ioEnginesHolder.deleteEngine(initiator);
            }
        } else if ( initiator.equals(this.systemInitiator) ) {
            this.gui.showMessage(message);
        }
    }

    @Override
    public void reportMessageAndExitLater(Initiator initiator, TextMessage message) {
        this.ioEnginesHolder
                .all()
                .forEach(ioEngine -> {
                    try {
                        ioEngine.reportMessage(message);
                    } catch (IOException ex) {
                        logError(this.getClass(), ex);
                    }
                });
        this.gui.showMessage(message);
        this.gui.exitAfterAllWindowsClosed();
    }

    @Override
    public void showTask(TimeMessage task) {
        this.gui.showTask(task);
    }

    @Override
    public void showTasksNotification(String periodOfNotification, List<TimeMessage> tasks) {
        this.gui.showTasks(periodOfNotification, tasks);
    }
}
