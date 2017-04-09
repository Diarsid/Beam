/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package diarsid.beam.core.modules.domainkeeper;

import diarsid.beam.core.base.control.flow.VoidOperation;
import diarsid.beam.core.base.control.io.base.actors.Initiator;
import diarsid.beam.core.base.control.io.commands.ArgumentsCommand;
import diarsid.beam.core.base.control.io.commands.EmptyCommand;

/**
 *
 * @author Diarsid
 */
public interface NotesKeeper {
    
    VoidOperation openNotes(Initiator initiator, EmptyCommand command);
    
    VoidOperation openTargetInNotes(Initiator initiator, ArgumentsCommand command);
    
    VoidOperation openPathInNotes(Initiator initiator, ArgumentsCommand command);
    
}
