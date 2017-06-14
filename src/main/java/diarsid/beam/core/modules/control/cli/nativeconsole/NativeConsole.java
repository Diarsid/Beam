/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package diarsid.beam.core.modules.control.cli.nativeconsole;

import java.io.IOException;
import java.util.logging.Level;
import java.util.logging.Logger;

import diarsid.beam.core.base.control.io.base.actors.Initiator;
import diarsid.beam.core.base.control.io.base.actors.OuterIoEngine;
import diarsid.beam.core.base.control.io.base.interaction.Answer;
import diarsid.beam.core.base.control.io.base.interaction.Choice;
import diarsid.beam.core.base.control.io.base.interaction.Message;
import diarsid.beam.core.base.control.io.base.interaction.VariantsQuestion;
import diarsid.beam.core.base.control.io.interpreter.CommandLineProcessor;
import diarsid.beam.core.domain.patternsanalyze.WeightedVariants;

import static java.util.Objects.nonNull;

import static diarsid.beam.core.base.control.io.base.interaction.Choice.NOT_MADE;
import static diarsid.beam.core.base.control.io.base.interaction.Answers.rejectedAnswer;

/**
 *
 * @author Diarsid
 */
public class NativeConsole 
        implements 
                OuterIoEngine, 
                Runnable {
    
    private final CommandLineProcessor cliProcessor;
    private final InputManager inputManager;
    private Initiator initiator;
    
    public NativeConsole(CommandLineProcessor commandLineProcessor, InputManager buffer) {
        this.cliProcessor = commandLineProcessor;
        this.inputManager = buffer;
    }
    
    @Override
    public void run() {
        String commandLine;
        while ( true ) {
            try {
                commandLine = this.inputManager.waitForCommand();
                this.inputManager.interactionBegins();
                if ( nonNull(this.initiator) ) {
                    this.cliProcessor.process(this.initiator, commandLine);
                }
                this.inputManager.interactionEnds();
            } catch (InterruptedException ex) {
                Logger.getLogger(NativeConsole.class.getName()).log(Level.SEVERE, null, ex);
            }
        }
    }
    
    @Override
    public String askForInput(String inputRequest) throws IOException {
        return "";
    }

    @Override
    public Choice resolve(String yesOrNoQuestion) {
        // ...
        return NOT_MADE;
    }

    @Override
    public Answer resolve(VariantsQuestion question) {
        // show question
        try {
            String answer = inputManager.waitForResponse();
        } catch (InterruptedException e) {
            return rejectedAnswer();
        }
        // ...
        return rejectedAnswer();
    }

    @Override
    public Answer resolve(WeightedVariants variants) throws IOException {
        // ...
        return rejectedAnswer();
    }

    @Override
    public void report(String string) {
        // ...
    }

    @Override
    public void report(Message message) {
        // ...
    }

    @Override
    public void close() {
        // ...
    }

    @Override
    public void accept(Initiator initiator) {
        this.initiator = initiator;
    }

    @Override
    public String name() {
        return "Native console";
    }
}
