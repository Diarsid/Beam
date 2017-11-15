/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package diarsid.beam.core.base.control.io.base.console;

import diarsid.beam.core.base.control.io.base.actors.Initiator;

/**
 *
 * @author Diarsid
 */
public interface ConsoleBlockingExecutor {
    
    void blockingExecuteCommand(Initiator initiator, String commandLine) throws Exception;
}
