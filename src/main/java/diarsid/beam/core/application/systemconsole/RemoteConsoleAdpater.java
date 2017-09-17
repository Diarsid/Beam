/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package diarsid.beam.core.application.systemconsole;

import diarsid.beam.core.base.control.io.console.ConsoleController;

import java.rmi.RemoteException;

import diarsid.beam.core.base.control.io.base.actors.Initiator;
import diarsid.beam.core.base.control.io.base.interaction.Answer;
import diarsid.beam.core.base.control.io.base.interaction.Choice;
import diarsid.beam.core.base.control.io.base.interaction.Message;
import diarsid.beam.core.base.control.io.base.interaction.VariantsQuestion;
import diarsid.beam.core.base.rmi.RemoteOuterIoEngine;
import diarsid.beam.core.base.analyze.variantsweight.WeightedVariants;

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
            this.console.close();
    }
    
    @Override
    public String askForInput(String inputRequest) throws RemoteException {        
        return this.console.askForInput(inputRequest);
    }
    
    @Override
    public Choice resolve(String yesOrNoQuestion) throws RemoteException {        
        return this.console.resolve(yesOrNoQuestion);
    }

    @Override
    public Answer resolve(VariantsQuestion question) throws RemoteException {
        return this.console.resolve(question);
    }

    @Override
    public Answer resolve(WeightedVariants variants) throws RemoteException {
        return this.console.resolve(variants);
    }

    @Override
    public void report(String string) throws RemoteException {
        this.console.report(string);
    }

    @Override
    public void report(Message message) throws RemoteException {
        this.console.report(message);
    }

    @Override
    public void accept(Initiator initiator) throws RemoteException {
        this.console.accept(initiator);
    }

    @Override
    public String name() throws RemoteException {
        return this.console.name();
    }
   
}
