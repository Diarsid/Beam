/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package diarsid.beam.core.application.systemconsole;

import java.io.IOException;
import java.rmi.RemoteException;

import diarsid.beam.core.base.control.io.base.actors.Initiator;
import diarsid.beam.core.base.control.io.base.interaction.Answer;
import diarsid.beam.core.base.control.io.base.interaction.Choice;
import diarsid.beam.core.base.control.io.base.interaction.Message;
import diarsid.beam.core.base.control.io.base.interaction.VariantsQuestion;
import diarsid.beam.core.base.rmi.RemoteOuterIoEngine;
import diarsid.beam.core.domain.patternsanalyze.WeightedVariants;

import static diarsid.beam.core.application.systemconsole.SystemConsole.exitSystemConsole;
import static diarsid.beam.core.base.control.io.base.interaction.Answer.noAnswerFromVariants;
import static diarsid.beam.core.base.control.io.base.interaction.Choice.NOT_MADE;
import static diarsid.beam.core.base.util.Logs.logError;

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
    public Choice resolve(String yesOrNoQuestion) throws RemoteException {
        try {
            return this.console.resolve(yesOrNoQuestion);
        } catch (IOException e) {
            logError(this.getClass(), e);
            exitSystemConsole();
            return NOT_MADE;
        }
    }

    @Override
    public Answer resolve(VariantsQuestion question) throws RemoteException {
        try {
            return this.console.resolve(question);
        } catch (IOException e) {
            logError(this.getClass(), e);
            exitSystemConsole();
            return noAnswerFromVariants();
        }
    }

    @Override
    public Answer resolve(WeightedVariants variants) throws RemoteException {
        try {
            return this.console.resolve(variants);
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
    public void report(Message message) throws RemoteException {
        try {
            this.console.report(message);
        } catch (IOException e) {
            logError(this.getClass(), e);
            exitSystemConsole();
        }
    }

    @Override
    public void accept(Initiator initiator) throws RemoteException {
        try {
            this.console.accept(initiator);
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
