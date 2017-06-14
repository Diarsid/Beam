/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package diarsid.beam.core.base.control.io.console;

import java.io.IOException;
import java.util.List;
import java.util.concurrent.atomic.AtomicBoolean;

import diarsid.beam.core.base.control.io.base.actors.Initiator;
import diarsid.beam.core.base.control.io.base.interaction.Message;
import diarsid.beam.core.base.control.io.base.interaction.VariantsQuestion;
import diarsid.beam.core.domain.patternsanalyze.WeightedVariant;


class ConsoleEngine {
        
    private final ConsolePlatform consolePlatform;
    private final ConsolePrinter printer;         
    private final ConsoleReader reader;           
    private final AtomicBoolean isInteractionLasts;   
    private final AtomicBoolean isWorking;  

    ConsoleEngine(ConsolePlatform consolePlatform) {
        this.consolePlatform = consolePlatform;
        this.reader = consolePlatform.reader();
        this.printer = consolePlatform.printer();        
        this.isInteractionLasts = new AtomicBoolean(false);
        this.isWorking = new AtomicBoolean(true);
    }
    
    String name() {
        return this.consolePlatform.name();
    }
    
    String read() {
        try {
            return this.reader.readLine();
        } catch (IOException e) {
            this.consolePlatform.reportException(e);
            return "";
        }
    }
    
    String readyAndWaitForLine() {
        try {
            this.printer.printReadyForNewCommandLine();
            return this.reader.readLine();
        } catch (IOException e) {
            this.consolePlatform.reportException(e);
            return "";
        }
    }
    
    void print(String report) {
        try {
            if ( this.isInteractionLasts.get() ) {
                this.printer.printDuringInteraction(report);
            } else {
                this.printer.printNonDuringInteraction(report);
            }
        } catch (IOException e) {
            this.consolePlatform.reportException(e);
        }
    }
    
    void print(Message message) {
        try {
            if ( this.isInteractionLasts.get() ) {
                this.printer.printDuringInteraction(message);
            } else {
                this.printer.printNonDuringInteraction(message);
            }
        } catch (IOException e) {
            this.consolePlatform.reportException(e);
        }
    }
    
    void print(VariantsQuestion question) {
        try {
            this.printer.print(question);
        } catch (IOException e) {
            this.consolePlatform.reportException(e);
        }
    }
    
    void print(List<WeightedVariant> weightedVariants) {
        try {
            this.printer.print(weightedVariants);
        } catch (IOException e) {
            this.consolePlatform.reportException(e);
        }
    }
    
    void printYesNoQuestion(String question) {
        try {
            this.printer.printYesNoQuestion(question);
        } catch (IOException e) {
            this.consolePlatform.reportException(e);
        }    
    }
    
    void printInvite(String inviteMessage) {
        try {
            this.printer.printInDialogInviteLine(inviteMessage);
        } catch (IOException e) {
            this.consolePlatform.reportException(e);
        }
    }
    
    boolean isWorking() {
        return this.isWorking.get();
    }
    
    void interactionBegins() {
        this.isInteractionLasts.set(true);
    }
    
    void interactionEnds() {
        this.isInteractionLasts.set(false);
    }
    
    void execute(String commandLine) {
        this.consolePlatform.executeCommand(commandLine);
    }
    
    void acceptInitiator(Initiator initiator) {
        this.consolePlatform.acceptInitiator(initiator);
    }
    
    void stop() {
        this.isWorking.set(false);
        this.consolePlatform.stop();
    }    
}
