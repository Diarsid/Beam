/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package diarsid.beam.core.modules.control.cli;

import diarsid.beam.core.base.control.io.base.actors.Initiator;
import diarsid.beam.core.base.control.io.base.actors.InnerIoEngine;
import diarsid.beam.core.base.control.io.commands.ArgumentsCommand;
import diarsid.beam.core.base.control.io.commands.EmptyCommand;
import diarsid.beam.core.modules.domainkeeper.NotesKeeper;

/**
 *
 * @author Diarsid
 */
class CliAdapterForNotesKeeper {
    
    private final NotesKeeper notesKeeper;
    private final InnerIoEngine ioEngine;

    public CliAdapterForNotesKeeper(NotesKeeper notesKeeper, InnerIoEngine ioEngine) {
        this.notesKeeper = notesKeeper;
        this.ioEngine = ioEngine;
    }    
    
    void openNotesAndReport(Initiator initiator, EmptyCommand emptyCommand) {
        // TODO
    }
    
    void openTargetInNotesAndReport(Initiator initiator, ArgumentsCommand command) {
        // TODO
    }
    
    void openPathInNotesAndReport(Initiator initiator, ArgumentsCommand command) {
        // TODO
    }
}
