/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package diarsid.beam.core.modules.domainkeeper;

import diarsid.beam.core.base.control.io.base.actors.Initiator;
import diarsid.beam.core.base.control.io.commands.ArgumentsCommand;
import diarsid.beam.core.base.control.io.commands.EmptyCommand;
import diarsid.beam.core.base.control.flow.VoidFlow;

/**
 *
 * @author Diarsid
 */
public interface NotesKeeper {
    
    VoidFlow openNotes(Initiator initiator, EmptyCommand command);
    
    VoidFlow openTargetInNotes(Initiator initiator, ArgumentsCommand command);
    
    VoidFlow openPathInNotes(Initiator initiator, ArgumentsCommand command);
    
    VoidFlow createNote(Initiator initiator, ArgumentsCommand command);
}
