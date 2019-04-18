/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package diarsid.beam.core.base.rmi;

import java.rmi.Remote;
import java.rmi.RemoteException;

import diarsid.beam.core.base.analyze.variantsweight.WeightedVariants;
import diarsid.beam.core.base.control.io.base.actors.Initiator;
import diarsid.beam.core.base.control.io.base.actors.OuterIoEngine;
import diarsid.beam.core.base.control.io.base.actors.OuterIoEngineType;
import diarsid.beam.core.base.control.io.base.interaction.Answer;
import diarsid.beam.core.base.control.io.base.interaction.Choice;
import diarsid.beam.core.base.control.io.base.interaction.Message;
import diarsid.beam.core.base.control.io.base.interaction.VariantsQuestion;

/**
 *
 * @author Diarsid
 */
public interface RemoteOuterIoEngine extends Remote, OuterIoEngine {
    
    @Override
    OuterIoEngineType type() throws RemoteException;
    
    @Override
    void close() throws RemoteException;
    
    @Override
    String askInput(String inputRequest) throws RemoteException;
    
    @Override
    Choice resolve(String yesOrNoQuestion) throws RemoteException;
    
    @Override
    Answer resolve(VariantsQuestion question) throws RemoteException;
    
    @Override
    Answer resolve(WeightedVariants variants) throws RemoteException;
    
    @Override
    void report(String string) throws RemoteException;
    
    @Override
    void report(Message message) throws RemoteException;
    
    @Override
    void accept(Initiator initiator) throws RemoteException;
    
    @Override
    String name() throws RemoteException;
} 
