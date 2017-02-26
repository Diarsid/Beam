/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package diarsid.beam.core.modules.control.cli.nativeconsole;

import java.io.IOException;

import diarsid.beam.core.base.control.io.base.interaction.Choice;
import diarsid.beam.core.base.control.io.base.actors.Initiator;
import diarsid.beam.core.base.control.io.base.interaction.Message;
import diarsid.beam.core.base.control.io.base.actors.OuterIoEngine;
import diarsid.beam.core.base.control.io.base.interaction.Answer;
import diarsid.beam.core.base.control.io.base.interaction.Question;
import diarsid.beam.core.base.control.io.interpreter.CommandLineProcessor;

import static java.util.Objects.nonNull;

import static diarsid.beam.core.base.control.io.base.interaction.Choice.CHOICE_NOT_MADE;
import static diarsid.beam.core.base.control.io.base.interaction.Answer.noAnswerFromVariants;

/**
 *
 * @author Diarsid
 */
public class NativeConsole 
        implements 
                OuterIoEngine, 
                Runnable {
    
    private final CommandLineProcessor cliProcessor;
    private Initiator initiator;
    private final InputBlockingBuffer buffer;
    
    public NativeConsole(CommandLineProcessor commandLineProcessor, InputBlockingBuffer buffer) {
        this.cliProcessor = commandLineProcessor;
        this.buffer = buffer;
    }
    
    @Override
    public void run() {
        String commandLine;
        while ( true ) {
            commandLine = this.buffer.waitForCommand();
            if ( nonNull(this.initiator) ) {
                this.cliProcessor.process(this.initiator, commandLine);
            }
        }
    }
    
    @Override
    public String askForInput(String inputRequest) throws IOException {
        return "";
    }

    @Override
    public Choice resolveYesOrNo(String yesOrNoQuestion) {
        this.buffer.consoleIsWaitingForAnswer();
        // ...
        return CHOICE_NOT_MADE;
    }

    @Override
    public Answer resolveQuestion(Question question) {
        this.buffer.consoleIsWaitingForAnswer();
        // ...
        return noAnswerFromVariants();
    }

    @Override
    public void report(String string) {
        // do nothing
    }

    @Override
    public void reportMessage(Message message) {
        // do nothing
    }

    @Override
    public void close() {
        // do nothing
    }

    @Override
    public void acceptInitiator(Initiator initiator) {
        this.initiator = initiator;
    }

    @Override
    public String getName() {
        return "Native console";
    }
}