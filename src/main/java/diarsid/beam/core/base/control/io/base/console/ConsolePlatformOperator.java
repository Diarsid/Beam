/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package diarsid.beam.core.base.control.io.base.console;

import java.util.List;

import diarsid.beam.core.base.analyze.variantsweight.Variant;
import diarsid.beam.core.base.control.io.base.actors.Initiator;
import diarsid.beam.core.base.control.io.base.actors.OuterIoEngineType;
import diarsid.beam.core.base.control.io.base.interaction.HelpInfo;
import diarsid.beam.core.base.control.io.base.interaction.Message;
import diarsid.beam.core.base.control.io.base.interaction.VariantsQuestion;


class ConsolePlatformOperator {
        
    private final ConsolePlatform consolePlatform;
    private final ConsoleIO io;     

    ConsolePlatformOperator(ConsolePlatform consolePlatform) {
        this.consolePlatform = consolePlatform;
        this.io = consolePlatform.io();
    }
    
    OuterIoEngineType type() {
        return this.consolePlatform.type();
    }
    
    String name() {
        return this.consolePlatform.name();
    }
    
    boolean isActiveWhenClosed() {
        return this.consolePlatform.isActiveWhenClosed();
    }
    
    String read() {
        try {
            return this.io.readLine();
        } catch (Exception e) {
            this.consolePlatform.reportException(e);
            return "";
        }
    }
    
    String readyAndWaitForLine() {
        try {
            this.io.printReadyForNewCommandLine();
            return this.io.readLine();
        } catch (Exception e) {
            this.consolePlatform.reportException(e);
            return "";
        }
    }
    
    void print(String report) {
        try {
            if ( this.consolePlatform.isInteractionLasts() ) {
                this.io.printDuringInteraction(report);
            } else {
                this.io.printNonDuringInteraction(report);
            }
        } catch (Exception e) {
            this.consolePlatform.reportException(e);
        }
    }
    
    void print(Message message) {
        try {
            if ( this.consolePlatform.isInteractionLasts() ) {
                this.io.printDuringInteraction(message);
            } else {
                this.io.printNonDuringInteraction(message);
            }
        } catch (Exception e) {
            this.consolePlatform.reportException(e);
        }
    }
    
    void print(HelpInfo help) {
        try {
            if ( this.consolePlatform.isInteractionLasts() ) {
                this.io.printDuringInteraction(help);
            } else {
                this.io.printNonDuringInteraction(help);
            }
        } catch (Exception e) {
            this.consolePlatform.reportException(e);
        }
    }
    
    void print(VariantsQuestion question) {
        try {
            this.io.print(question);
        } catch (Exception e) {
            this.consolePlatform.reportException(e);
        }
    }
    
    void print(List<Variant> мariants) {
        try {
            this.io.print(мariants);
        } catch (Exception e) {
            this.consolePlatform.reportException(e);
        }
    }
    
    void printYesNoQuestion(String question) {
        try {
            this.io.printYesNoQuestion(question);
        } catch (Exception e) {
            this.consolePlatform.reportException(e);
        }    
    }
    
    void printInvite(String inviteMessage) {
        try {
            this.io.printInDialogInviteLine(inviteMessage);
        } catch (Exception e) {
            this.consolePlatform.reportException(e);
        }
    }
    
    boolean isWorking() {
        return this.consolePlatform.isWorking();
    }
    
    void interactionBegins() {
        this.consolePlatform.interactionBegins();
    }
    
    void interactionEnds() {
        this.consolePlatform.interactionEnds();
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
        this.consolePlatform.stop();
    }    
}
