package com.drs.beam.remote.codebase;

import java.rmi.Remote;
import java.rmi.RemoteException;

/**
 * Org by Diarsid
 * Time: 13:08 - 25.02.15
 * IDE: IntelliJ IDEA 12
 */

public interface OSExecutorIF extends Remote {
    public void open(String path) throws RemoteException;
    public void runExternalProgram(String ProgramName) throws RemoteException;
}
