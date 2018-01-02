/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package diarsid.beam.core.application.systemconsole;

import diarsid.beam.core.base.control.io.base.console.ConsoleBlockingExecutor;
import diarsid.beam.core.base.control.io.base.console.ConsoleIO;
import diarsid.beam.core.base.control.io.base.console.ConsolePlatform;

import static diarsid.beam.core.application.systemconsole.SystemConsole.exitSystemConsole;
import static diarsid.beam.core.application.systemconsole.SystemConsole.getPassport;
import static diarsid.beam.core.base.control.io.base.actors.OuterIoEngineType.REMOTE;


class SystemConsolePlatform extends ConsolePlatform {

    public SystemConsolePlatform(
            ConsoleIO consoleIo, 
            ConsoleBlockingExecutor blockingExecutor) {
        super(consoleIo, blockingExecutor, REMOTE);
    }
    
    @Override
    public String name() {
        return getPassport().getName();
    }

    @Override
    public void whenStopped() { 
        exitSystemConsole();
    }

    @Override
    public void whenInitiatorAccepted() {
        getPassport().setInitiatorId(super.initiator.identity());   
    }

    @Override
    public boolean isActiveWhenClosed() {
        return false;
    }
    
}
