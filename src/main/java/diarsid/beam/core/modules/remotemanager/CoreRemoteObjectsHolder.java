/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package diarsid.beam.core.modules.remotemanager;

import java.rmi.registry.Registry;

import diarsid.beam.core.base.rmi.RemoteCoreAccessEndpoint;

/**
 *
 * @author Diarsid
 */
class CoreRemoteObjectsHolder {
    
    /**
     * Java RMI mechanism requires that remote objects that have been exported 
     * by this JVM for an external usage by other JVM were saved in static variables.
     * Otherwise they will be collected by the GC and the RMI interaction through them will 
     * be impossible. Any attempt to use them after it will cause RemoteException.
     */
    static Registry holdedRegistry;
    static RemoteCoreAccessEndpoint holdedRemoteAccessEndpoint;
//    private static RmiExecutorInterface rmiExecutorInterface;
//    private static RmiTaskManagerInterface rmiTaskManagerInterface;
//    private static RmiLocationsHandlerInterface rmiLocationsHandlerInterface;
//    private static RmiWebPagesHandlerInterface rmiWebPageHandlerInterface;  
    
    private CoreRemoteObjectsHolder() {
    }
}
