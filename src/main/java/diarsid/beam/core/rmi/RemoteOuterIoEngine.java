/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package diarsid.beam.core.rmi;

import java.rmi.Remote;
import java.rmi.RemoteException;

import diarsid.beam.core.control.io.base.Initiator;
import diarsid.beam.core.control.io.base.IoChoice;
import diarsid.beam.core.control.io.base.IoMessage;
import diarsid.beam.core.control.io.base.IoQuestion;
import diarsid.beam.core.control.io.base.OuterIoEngine;

/**
 *
 * @author Diarsid
 */
public interface RemoteOuterIoEngine extends Remote, OuterIoEngine {
    
    @Override
    void close() throws RemoteException;
    
    @Override
    boolean resolveYesOrNo(String yesOrNoQuestion) throws RemoteException;
    
    @Override
    IoChoice resolveVariants(IoQuestion question) throws RemoteException;
    
    @Override
    void report(String string) throws RemoteException;
    
    @Override
    void reportMessage(IoMessage message) throws RemoteException;
    
    @Override
    void acceptInitiator(Initiator initiator) throws RemoteException;
} 
