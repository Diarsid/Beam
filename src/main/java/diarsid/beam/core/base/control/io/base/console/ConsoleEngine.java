/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package diarsid.beam.core.base.control.io.base.console;

import java.util.List;
import java.util.concurrent.atomic.AtomicBoolean;

import diarsid.beam.core.base.analyze.variantsweight.WeightedVariant;
import diarsid.beam.core.base.control.io.base.actors.Initiator;
import diarsid.beam.core.base.control.io.base.actors.OuterIoEngineType;
import diarsid.beam.core.base.control.io.base.interaction.HelpInfo;
import diarsid.beam.core.base.control.io.base.interaction.Message;
import diarsid.beam.core.base.control.io.base.interaction.VariantsQuestion;


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
    
    OuterIoEngineType type() {
        return this.consolePlatform.type();
    }
    
    String name() {
        return this.consolePlatform.name();
    }
    
    String read() {
        try {
            return this.reader.readLine();
        } catch (Exception e) {
            this.consolePlatform.reportException(e);
            return "";
        }
    }
    
    String readyAndWaitForLine() {
        try {
            this.printer.printReadyForNewCommandLine();
            return this.reader.readLine();
        } catch (Exception e) {
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
        } catch (Exception e) {
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
        } catch (Exception e) {
            this.consolePlatform.reportException(e);
        }
    }
    
    void print(HelpInfo help) {
        try {
            if ( this.isInteractionLasts.get() ) {
                this.printer.printDuringInteraction(help);
            } else {
                this.printer.printNonDuringInteraction(help);
            }
        } catch (Exception e) {
            this.consolePlatform.reportException(e);
        }
    }
    
    void print(VariantsQuestion question) {
        try {
            this.printer.print(question);
        } catch (Exception e) {
            this.consolePlatform.reportException(e);
        }
    }
    
    void print(List<WeightedVariant> weightedVariants) {
        try {
            this.printer.print(weightedVariants);
        } catch (Exception e) {
            this.consolePlatform.reportException(e);
        }
    }
    
    void printYesNoQuestion(String question) {
        try {
            this.printer.printYesNoQuestion(question);
        } catch (Exception e) {
            this.consolePlatform.reportException(e);
        }    
    }
    
    void printInvite(String inviteMessage) {
        try {
            this.printer.printInDialogInviteLine(inviteMessage);
        } catch (Exception e) {
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
    
    void blockingExecute(String commandLine) {
        try {
            this.consolePlatform.blockingExecuteCommand(commandLine);
        } catch (Exception e) {
            this.consolePlatform.reportException(e);
        }
    }
    
    void acceptInitiator(Initiator initiator) {
        this.consolePlatform.acceptInitiator(initiator);
    }
    
    void stop() {
        this.isWorking.set(false);
        this.consolePlatform.stop();
    }    
}
