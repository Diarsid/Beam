/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package diarsid.beam.core.modules.io;

import java.io.IOException;
import java.util.List;

import diarsid.beam.core.modules.io.gui.Gui;
import diarsid.beam.core.base.analyze.variantsweight.WeightedVariants;
import diarsid.beam.core.base.control.io.base.actors.Initiator;
import diarsid.beam.core.base.control.io.base.actors.InnerIoEngine;
import diarsid.beam.core.base.control.io.base.actors.OuterIoEngine;
import diarsid.beam.core.base.control.io.base.interaction.Answer;
import diarsid.beam.core.base.control.io.base.interaction.Choice;
import diarsid.beam.core.base.control.io.base.interaction.Help;
import diarsid.beam.core.base.control.io.base.interaction.ApplicationHelpContext;
import diarsid.beam.core.base.control.io.base.interaction.HelpKey;
import diarsid.beam.core.base.control.io.base.interaction.Message;
import diarsid.beam.core.base.control.io.base.interaction.VariantsQuestion;

import static java.util.Arrays.asList;

import static diarsid.beam.core.Beam.systemInitiator;
import static diarsid.beam.core.base.control.io.base.interaction.Answers.rejectedAnswer;
import static diarsid.beam.core.base.control.io.base.interaction.Choice.NOT_MADE;
import static diarsid.beam.core.base.control.io.base.interaction.Help.isHelpRequest;
import static diarsid.beam.core.base.control.io.base.interaction.Messages.error;
import static diarsid.beam.core.base.control.io.base.interaction.Messages.info;
import static diarsid.beam.core.base.util.ConcurrencyUtil.asyncDo;
import static diarsid.beam.core.base.util.ConcurrencyUtil.awaitDo;
import static diarsid.beam.core.base.util.ConcurrencyUtil.awaitGet;
import static diarsid.support.log.Logging.logFor;

/**
 *
 * @author Diarsid
 */
public class MainInnerIoEngine implements InnerIoEngine {
    
    private final OuterIoEnginesHolder ioEnginesHolder;
    private final Gui gui;
    private final ApplicationHelpContext helpContext;
    private final Initiator systemInitiator;
    
    public MainInnerIoEngine(
            OuterIoEnginesHolder ioEnginesHolder, Gui gui, ApplicationHelpContext helpContext) {
        this.ioEnginesHolder = ioEnginesHolder;
        this.gui = gui;
        this.helpContext = helpContext;
        this.systemInitiator = systemInitiator();
    }

    @Override
    public Choice ask(Initiator initiator, String yesOrNoQuestion, Help help) {
        if ( this.ioEnginesHolder.hasEngineBy(initiator) ) {
            return awaitGet(() -> {
                try {
                    OuterIoEngine ioEngine = this.ioEnginesHolder.getEngineBy(initiator);
                    Choice choice = ioEngine.resolve(yesOrNoQuestion);
                    while ( choice.isHelpRequest() ) {
                        this.reportHelpUsing(ioEngine, help);
                        choice = ioEngine.resolve(yesOrNoQuestion);
                    }
                    return choice;
                } catch (IOException ex) {
                    logFor(this).error(ex.getMessage(), ex);
                    this.ioEnginesHolder.processCloseRequestBy(initiator);
                    return NOT_MADE;
                }
            }).orElse(NOT_MADE);            
        } else {
            return NOT_MADE;
        }
    }

    @Override
    public Answer ask(Initiator initiator, VariantsQuestion question, Help help) {
        if ( this.ioEnginesHolder.hasEngineBy(initiator) ) {
            return awaitGet(() -> {
                try {
                    OuterIoEngine ioEngine = this.ioEnginesHolder.getEngineBy(initiator);
                    Answer answer = ioEngine.resolve(question);
                    while ( answer.isHelpRequest() ) {
                        this.reportHelpUsing(ioEngine, help);
                        answer = ioEngine.resolve(question);
                    }
                    return answer; 
                } catch (IOException ex) {
                    logFor(this).error(ex.getMessage(), ex);
                    this.ioEnginesHolder.processCloseRequestBy(initiator);
                    return rejectedAnswer();
                }
            }).orElse(rejectedAnswer());            
        } else {
            return rejectedAnswer();
        }        
    }

    @Override
    public Answer ask(
            Initiator initiator, WeightedVariants variants, Help help) {
        if ( this.ioEnginesHolder.hasEngineBy(initiator) ) {
            return awaitGet(() -> {
                try {
                    OuterIoEngine ioEngine = this.ioEnginesHolder.getEngineBy(initiator);
                    Answer answer = ioEngine.resolve(variants);
                    while ( answer.isHelpRequest() ) {
                        this.reportHelpUsing(ioEngine, help);
                        variants.setTraversingToPositionBefore(answer.index());
                        answer = ioEngine.resolve(variants);
                    }
                    return answer;        
                } catch (IOException ex) {
                    logFor(this).error(ex.getMessage(), ex);
                    this.ioEnginesHolder.processCloseRequestBy(initiator);
                    return rejectedAnswer();
                }
            }).orElse(rejectedAnswer());      
        } else {
            return rejectedAnswer();
        }  
    }
    
    @Override
    public String askInput(Initiator initiator, String inputQuestion, Help help) {
        if ( this.ioEnginesHolder.hasEngineBy(initiator) ) {
            return awaitGet(() -> {
                try {
                    OuterIoEngine ioEngine = this.ioEnginesHolder.getEngineBy(initiator);
                    String input = ioEngine.askInput(inputQuestion); 
                    while ( isHelpRequest(input) ) {
                        this.reportHelpUsing(ioEngine, help);
                        input = ioEngine.askInput(inputQuestion); 
                    }    
                    return input;
                } catch (IOException ex) {
                    logFor(this).error(ex.getMessage(), ex);
                    this.ioEnginesHolder.processCloseRequestBy(initiator);
                    return "";
                }
            }).orElse("");
        } else {
            return "";
        }
    }

    private void reportHelpUsing(OuterIoEngine ioEngine, Help help) throws IOException {
        if ( help.isKey() ) {
            ioEngine.report(this.helpContext.get(help.asKey()));
        } else {
            ioEngine.report(help.asInfo());
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
        if ( this.ioEnginesHolder.hasEngineBy(initiator) ) {
            awaitDo(() -> {
                try {
                    this.ioEnginesHolder
                            .getEngineBy(initiator)
                            .report(string);
                } catch (IOException ex) {
                    logFor(this).error(ex.getMessage(), ex);
                    this.ioEnginesHolder.processCloseRequestBy(initiator);
                }
            });            
        } else if ( initiator.equals(this.systemInitiator) ) {
            this.gui.messagesGui().show(info(string));
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
                            logFor(this).error(ex.getMessage(), ex);
                        }
                    });                  
                });
        this.gui.messagesGui().show(error(string));
        this.gui.exitAfterAllWindowsClosed();
    }

    @Override
    public void reportMessage(Initiator initiator, Message message) {
        if ( this.ioEnginesHolder.hasEngineBy(initiator) ) {
            awaitDo(() -> {
                try {
                    this.ioEnginesHolder
                            .getEngineBy(initiator)
                            .report(message);
                } catch (IOException ex) {
                    logFor(this).error(ex.getMessage(), ex);
                    this.ioEnginesHolder.processCloseRequestBy(initiator);
                }
            });            
        } else if ( initiator.equals(this.systemInitiator) ) {
            this.gui.messagesGui().show(message);
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
                            logFor(this).error(ex.getMessage(), ex);
                        }
                    });                    
                });
        this.gui.messagesGui().show(message);
        this.gui.exitAfterAllWindowsClosed();
    }

//    @Override
//    public void show(Message task) {
//        this.gui.show(task);
//    }
//
//    @Override
//    public void showAll(List<Message> messages) {
//        messages.stream().forEach(message -> this.gui.show(message));
//    }
//
//    @Override
//    public void showAllAsOne(String header, List<Message> messages) {
//        this.gui.showAsOne(header, messages);
//    }
}
