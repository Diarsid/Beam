/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package diarsid.beam.core.modules.domainkeeper;

import diarsid.beam.core.application.environment.NotesCatalog;
import diarsid.beam.core.base.control.flow.VoidOperation;
import diarsid.beam.core.base.control.io.base.actors.Initiator;
import diarsid.beam.core.base.control.io.base.actors.InnerIoEngine;
import diarsid.beam.core.base.control.io.commands.ArgumentsCommand;
import diarsid.beam.core.base.control.io.commands.EmptyCommand;

/**
 *
 * @author Diarsid
 */
class NotesKeeperWorker implements NotesKeeper {

    private final InnerIoEngine ioEngine;
    private final NotesCatalog notesCatalog;
    
    NotesKeeperWorker(InnerIoEngine ioEngine, NotesCatalog notesCatalog) {
        this.ioEngine = ioEngine;
        this.notesCatalog = notesCatalog;
    }

    @Override
    public VoidOperation openNotes(Initiator initiator, EmptyCommand command) {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    @Override
    public VoidOperation openTargetInNotes(Initiator initiator, ArgumentsCommand command) {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    @Override
    public VoidOperation openPathInNotes(Initiator initiator, ArgumentsCommand command) {
        throw new UnsupportedOperationException("Not supported yet.");
    }
    
}
