/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package diarsid.beam.core.systemconsole;

import java.io.IOException;
import java.rmi.RemoteException;

import diarsid.beam.core.control.io.base.Initiator;
import diarsid.beam.core.control.io.base.IoChoice;
import diarsid.beam.core.control.io.base.IoMessage;
import diarsid.beam.core.control.io.base.IoQuestion;
import diarsid.beam.core.rmi.RemoteOuterIoEngine;

import static diarsid.beam.core.control.io.base.IoChoice.choiceNotMade;

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
    public boolean resolveYesOrNo(String yesOrNoQuestion) throws RemoteException {
        try {
            return this.console.resolveYesOrNo(yesOrNoQuestion);
        } catch (IOException e) {
            e.printStackTrace();
            return false;
        }
    }

    @Override
    public IoChoice resolveVariants(IoQuestion question) throws RemoteException {
        try {
            return this.console.resolveVariants(question);
        } catch (IOException e) {
            e.printStackTrace();
            return choiceNotMade();
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
    public void reportMessage(IoMessage message) throws RemoteException {
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
