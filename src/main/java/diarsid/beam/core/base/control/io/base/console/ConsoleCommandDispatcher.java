/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package diarsid.beam.core.base.control.io.base.console;

import diarsid.beam.core.base.control.io.base.actors.Initiator;
import diarsid.beam.core.base.control.io.commands.Command;

/**
 *
 * @author Diarsid
 */
public interface ConsoleCommandDispatcher {

    void dispatch(Initiator initiator, Command command);    
}
