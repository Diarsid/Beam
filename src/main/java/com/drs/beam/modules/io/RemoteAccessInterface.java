/*
 * project: Beam
 * author: Diarsid
 */
package com.drs.beam.modules.io;

import java.rmi.Remote;
import java.rmi.RemoteException;

/*
 * Interface provides methods to control main program`s flow 
 * by external io engine from outside. Methods can be invoked by external io engine 
 * to force program to exit or change it`s behavior regarding it`s output activity.
 */
public interface RemoteAccessInterface extends Remote {
    public boolean hasExternalIOProcessor() throws RemoteException;
    public void acceptNewIOProcessor(String consoleRmiName, String consoleHost, int consolePort) throws RemoteException;
    public void useExternalShowTaskMethod() throws RemoteException;
    public void useNativeShowTaskMethod() throws RemoteException;
    public void exit() throws RemoteException;
    public void setDefaultIO() throws RemoteException;
}
