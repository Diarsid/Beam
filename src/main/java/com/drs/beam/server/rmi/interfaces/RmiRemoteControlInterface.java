/*
 * project: Beam
 * author: Diarsid
 */
package com.drs.beam.server.rmi.interfaces;

import java.rmi.NotBoundException;
import java.rmi.Remote;
import java.rmi.RemoteException;

/*
 * Interface provides methods to control main program`s flow 
 * by external io engine from outside. Methods can be invoked by external io engine 
 * to force program to exit or change it`s behavior regarding it`s output activity.
 */
public interface RmiRemoteControlInterface extends Remote {
    boolean hasExternalIOProcessor() throws RemoteException;
    void acceptNewIOProcessor(String consoleRmiName, String consoleHost, int consolePort)
            throws RemoteException, NotBoundException;
    void useExternalShowTaskMethod() throws RemoteException;
    void useNativeShowTaskMethod() throws RemoteException;
    void exit() throws RemoteException;
    void setDefaultIO() throws RemoteException;
}
