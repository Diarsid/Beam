/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package diarsid.beam.core.application.systemconsole;

import diarsid.beam.core.base.control.io.base.actors.Initiator;
import diarsid.beam.core.base.control.io.base.console.ConsoleBlockingExecutor;
import diarsid.beam.core.base.rmi.RemoteCoreAccessEndpoint;

/**
 *
 * @author Diarsid
 */
class SystemConsoleBlockingExecutor implements ConsoleBlockingExecutor {

    private static RemoteCoreAccessEndpoint remoteAccess;  

    SystemConsoleBlockingExecutor(RemoteCoreAccessEndpoint access) {
        remoteAccess = access;
    }
    
    @Override
    public void blockingExecuteCommand(Initiator initiator, String commandLine) throws Exception {
        remoteAccess.executeCommand(initiator, commandLine);
    }
    
}
