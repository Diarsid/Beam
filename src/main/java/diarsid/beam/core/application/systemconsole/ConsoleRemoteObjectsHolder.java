/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package diarsid.beam.core.application.systemconsole;

import java.rmi.registry.Registry;

import diarsid.beam.core.base.rmi.RemoteCoreAccessEndpoint;
import diarsid.beam.core.base.rmi.RemoteOuterIoEngine;

/**
 *
 * @author Diarsid
 */
class ConsoleRemoteObjectsHolder {
        
    static Registry holdedRegistry;
    static RemoteOuterIoEngine holdedRemoteConsole;
    static RemoteOuterIoEngine holdedRemoteConsoleExported;
    static RemoteCoreAccessEndpoint holdedRemoteCoreAccess;
    
    private ConsoleRemoteObjectsHolder() {
    }
}
