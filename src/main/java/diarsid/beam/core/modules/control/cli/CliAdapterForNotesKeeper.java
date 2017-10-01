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
import diarsid.beam.core.base.control.flow.VoidFlow;

/**
 *
 * @author Diarsid
 */
class CliAdapterForNotesKeeper extends AbstractCliAdapter{
    
    private final NotesKeeper notesKeeper;

    public CliAdapterForNotesKeeper(NotesKeeper notesKeeper, InnerIoEngine ioEngine) {
        super(ioEngine);
        this.notesKeeper = notesKeeper;
    }

    void createNoteAndReport(Initiator initiator, ArgumentsCommand command) {
        VoidFlow flow = this.notesKeeper.createNote(initiator, command);
        super.reportVoidFlow(initiator, flow, "created!");
    }
    
    void openNotesAndReport(Initiator initiator, EmptyCommand command) {
        VoidFlow flow = this.notesKeeper.openNotes(initiator, command);
        super.reportVoidFlow(initiator, flow);
    }
    
    void openTargetInNotesAndReport(Initiator initiator, ArgumentsCommand command) {
        VoidFlow flow = this.notesKeeper.openTargetInNotes(initiator, command);
        super.reportVoidFlow(initiator, flow);
    }
    
    void openPathInNotesAndReport(Initiator initiator, ArgumentsCommand command) {
        VoidFlow flow = this.notesKeeper.openPathInNotes(initiator, command);
        super.reportVoidFlow(initiator, flow);
    }
}
