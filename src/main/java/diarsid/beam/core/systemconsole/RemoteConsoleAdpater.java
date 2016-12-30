/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package diarsid.beam.core.systemconsole;

import java.io.IOException;
import java.rmi.RemoteException;

import diarsid.beam.core.control.io.base.Choice;
import diarsid.beam.core.control.io.base.Initiator;
import diarsid.beam.core.control.io.base.Message;
import diarsid.beam.core.control.io.base.VariantAnswer;
import diarsid.beam.core.control.io.base.VariantsQuestion;
import diarsid.beam.core.rmi.RemoteOuterIoEngine;

import static diarsid.beam.core.control.io.base.Choice.CHOICE_NOT_MADE;
import static diarsid.beam.core.control.io.base.VariantAnswer.noAnswerFromVariants;
import static diarsid.beam.core.systemconsole.SystemConsole.exitSystemConsole;
import static diarsid.beam.core.util.Logs.logError;

/**
 *
 * @author Diarsid
 */
public class RemoteConsoleAdpater implements RemoteOuterIoEngine {
        
    private final ConsoleController console;
    
    public RemoteConsoleAdpater(ConsoleController console) {
        this.console = console;
    }

    @Override
    public void close() throws RemoteException {
        try {
            this.console.close();
        } catch (IOException e) {
            logError(this.getClass(), e);   
            exitSystemConsole();
        }
    }
    
    @Override
    public String askForInput(String inputRequest) throws RemoteException {
        try {
            return this.console.askForInput(inputRequest);
        } catch (IOException e) {
            logError(this.getClass(), e);
            exitSystemConsole();
            return "";
        }
    }
    
    @Override
    public Choice resolveYesOrNo(String yesOrNoQuestion) throws RemoteException {
        try {
            return this.console.resolveYesOrNo(yesOrNoQuestion);
        } catch (IOException e) {
            logError(this.getClass(), e);
            exitSystemConsole();
            return CHOICE_NOT_MADE;
        }
    }

    @Override
    public VariantAnswer resolveQuestion(VariantsQuestion question) throws RemoteException {
        try {
            return this.console.resolveQuestion(question);
        } catch (IOException e) {
            logError(this.getClass(), e);
            exitSystemConsole();
            return noAnswerFromVariants();
        }
    }

    @Override
    public void report(String string) throws RemoteException {
        try {
            this.console.report(string);
        } catch (IOException e) {
            logError(this.getClass(), e);
            exitSystemConsole();
        }
    }

    @Override
    public void reportMessage(Message message) throws RemoteException {
        try {
            this.console.reportMessage(message);
        } catch (IOException e) {
            logError(this.getClass(), e);
            exitSystemConsole();
        }
    }

    @Override
    public void acceptInitiator(Initiator initiator) throws RemoteException {
        try {
            this.console.acceptInitiator(initiator);
        } catch (IOException e) {
            logError(this.getClass(), e);
            exitSystemConsole();
        }
    }

    @Override
    public String getName() throws RemoteException {
        return this.console.getName();
    }
   
}
