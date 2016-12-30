/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package diarsid.beam.core.modules.corecontrol.cli.nativeconsole;

import diarsid.beam.core.control.io.base.VariantAnswer;
import diarsid.beam.core.control.io.base.Choice;
import diarsid.beam.core.control.io.base.Initiator;
import diarsid.beam.core.control.io.base.Message;
import diarsid.beam.core.control.io.base.OuterIoEngine;
import diarsid.beam.core.control.io.base.VariantsQuestion;
import diarsid.beam.core.control.io.interpreter.CommandLineProcessor;

import static java.util.Objects.nonNull;

import static diarsid.beam.core.control.io.base.Choice.CHOICE_NOT_MADE;
import static diarsid.beam.core.control.io.base.VariantAnswer.noAnswerFromVariants;

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
    public Choice resolveYesOrNo(String yesOrNoQuestion) {
        this.buffer.consoleIsWaitingForAnswer();
        // ...
        return CHOICE_NOT_MADE;
    }

    @Override
    public VariantAnswer resolveQuestion(VariantsQuestion question) {
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
