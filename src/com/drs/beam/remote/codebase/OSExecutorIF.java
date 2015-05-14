/*
 * project: Beam
 * author: Diarsid
 */
package com.drs.beam.remote.codebase;

import java.rmi.Remote;
import java.rmi.RemoteException;

public interface OSExecutorIF extends Remote {
    public void open(String path) throws RemoteException;
    public void runExternalProgram(String ProgramName) throws RemoteException;
}
