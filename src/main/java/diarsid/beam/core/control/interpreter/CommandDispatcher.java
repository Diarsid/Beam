/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package diarsid.beam.core.control.interpreter;

import diarsid.beam.core.control.Initiator;

/**
 *
 * @author Diarsid
 */
public interface CommandDispatcher {

    void dispatch(Initiator initiator, diarsid.beam.core.control.commands.Command operation);    
}
