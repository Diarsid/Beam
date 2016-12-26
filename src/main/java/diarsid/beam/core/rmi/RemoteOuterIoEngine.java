/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package diarsid.beam.core.rmi;

import java.rmi.Remote;
import java.rmi.RemoteException;

import diarsid.beam.core.control.io.base.Answer;
import diarsid.beam.core.control.io.base.Choice;
import diarsid.beam.core.control.io.base.Initiator;
import diarsid.beam.core.control.io.base.TextMessage;
import diarsid.beam.core.control.io.base.OuterIoEngine;
import diarsid.beam.core.control.io.base.Question;

/**
 *
 * @author Diarsid
 */
public interface RemoteOuterIoEngine extends Remote, OuterIoEngine {
    
    @Override
    void close() throws RemoteException;
    
    @Override
    Choice resolveYesOrNo(String yesOrNoQuestion) throws RemoteException;
    
    @Override
    Answer resolveQuestion(Question question) throws RemoteException;
    
    @Override
    void report(String string) throws RemoteException;
    
    @Override
    void reportMessage(TextMessage message) throws RemoteException;
    
    @Override
    void acceptInitiator(Initiator initiator) throws RemoteException;
    
    @Override
    String getName() throws RemoteException;
} 
