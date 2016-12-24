/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package diarsid.beam.core.control.io.interpreter;

import diarsid.beam.core.control.io.base.Initiator;
import diarsid.beam.core.control.io.commands.Command;

/**
 *
 * @author Diarsid
 */
public interface CommandDispatcher {

    void dispatch(Initiator initiator, Command operation);    
}
