/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package diarsid.beam.core.systemconsole;

import java.io.IOException;
import java.rmi.RemoteException;

import diarsid.beam.core.control.io.base.Answer;
import diarsid.beam.core.control.io.base.Choice;
import diarsid.beam.core.control.io.base.Initiator;
import diarsid.beam.core.control.io.base.TextMessage;
import diarsid.beam.core.control.io.base.Question;
import diarsid.beam.core.rmi.RemoteOuterIoEngine;

import static diarsid.beam.core.control.io.base.Answer.noAnswer;
import static diarsid.beam.core.control.io.base.Choice.CHOICE_NOT_MADE;

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
        }
    }

    @Override
    public Choice resolveYesOrNo(String yesOrNoQuestion) throws RemoteException {
        try {
            return this.console.resolveYesOrNo(yesOrNoQuestion);
        } catch (IOException e) {
            e.printStackTrace();
            return CHOICE_NOT_MADE;
        }
    }

    @Override
    public Answer resolveQuestion(Question question) throws RemoteException {
        try {
            return this.console.resolveQuestion(question);
        } catch (IOException e) {
            e.printStackTrace();
            return noAnswer();
        }
    }

    @Override
    public void report(String string) throws RemoteException {
        try {
            this.console.report(string);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @Override
    public void reportMessage(TextMessage message) throws RemoteException {
        try {
            this.console.reportMessage(message);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @Override
    public void acceptInitiator(Initiator initiator) throws RemoteException {
        try {
            this.console.acceptInitiator(initiator);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @Override
    public String getName() throws RemoteException {
        return this.console.getName();
    }
}
