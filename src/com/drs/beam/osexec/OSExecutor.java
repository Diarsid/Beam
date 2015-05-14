/*
 * project: Beam
 * author: Diarsid
 */
package com.drs.beam.osexec;

import com.drs.beam.io.BeamIO;
import com.drs.beam.io.InnerIOIF;
import com.drs.beam.remote.codebase.OSExecutorIF;
import java.rmi.RemoteException;


public class OSExecutor implements OSExecutorIF {
    // Fields ---------------------------------------------------------------------------------
    private InnerIOIF ioEngine;

    // Constructors ---------------------------------------------------------------------------
    public OSExecutor() {
        this.ioEngine = BeamIO.getInnerIO();
    }

    // Methods --------------------------------------------------------------------------------

    public static void main(String[] args) {
    }

    public void open(String path) throws RemoteException{
        // logic
    }
    public void runExternalProgram(String ProgramName) throws RemoteException{
        // logic
    }

    private void openFile(String path){
        // logic
    }
    private void openDirectory(String path){
        // logic
    }

}