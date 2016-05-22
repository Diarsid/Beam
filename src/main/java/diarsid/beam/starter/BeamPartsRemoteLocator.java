/*
 * project: Beam
 * author: Diarsid
 */

package diarsid.beam.starter;

import java.rmi.RemoteException;
import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;

import diarsid.beam.shared.modules.ConfigModule;
import diarsid.beam.shared.modules.config.Config;

import static diarsid.beam.shared.modules.config.Config.CORE_HOST;
import static diarsid.beam.shared.modules.config.Config.CORE_PORT;
import static diarsid.beam.shared.modules.config.Config.SYS_CONSOLE_HOST;
import static diarsid.beam.shared.modules.config.Config.SYS_CONSOLE_PORT;

/**
 *
 * @author Diarsid
 */
final class BeamPartsRemoteLocator {
    
    private final ConfigModule config;
    private final boolean shouldStartBeamCore;
    private final boolean shouldStartBeamSysConsole;
    
    BeamPartsRemoteLocator(ConfigModule config) {
        this.config = config;
        this.shouldStartBeamCore = ! this.isBeamCoreWorkingNow();
        this.shouldStartBeamSysConsole = ! this.isBeamSysConsoleWorkingNow();
    } 
    
    boolean shouldStartAnything() {
        return ( this.shouldStartBeamCore || this.shouldStartBeamSysConsole );
    }
    
    boolean shouldStartBeamCore() {
        return this.shouldStartBeamCore;
    }
    
    boolean shouldStartBeamSysConsole() {
        return this.shouldStartBeamSysConsole;
    }
    
    boolean isBeamCoreWorkingNow() {
        try {
            return this.isRmiAddressOccupied(CORE_HOST, CORE_PORT);
        } catch (RemoteException re){            
            return false;
        }
    }
    
    boolean isBeamSysConsoleWorkingNow(){
        try {
            return this.isRmiAddressOccupied(SYS_CONSOLE_HOST, SYS_CONSOLE_PORT);
        } catch (RemoteException re){            
            return false;
        }
    }

    boolean isRmiAddressOccupied(Config host, Config port) 
            throws RemoteException, NumberFormatException {
        
        Registry consoleRegistry = LocateRegistry.getRegistry(
                config.get(host),
                Integer.parseInt(config.get(port)));
        return ( consoleRegistry.list().length > 0 );
    }
}
