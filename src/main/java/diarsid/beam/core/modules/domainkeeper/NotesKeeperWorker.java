/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package diarsid.beam.core.modules.domainkeeper;

import java.io.IOException;
import java.util.List;

import diarsid.beam.core.application.environment.NotesCatalog;
import diarsid.beam.core.base.control.flow.VoidOperation;
import diarsid.beam.core.base.control.io.base.actors.Initiator;
import diarsid.beam.core.base.control.io.base.actors.InnerIoEngine;
import diarsid.beam.core.base.control.io.base.interaction.Answer;
import diarsid.beam.core.base.control.io.commands.ArgumentsCommand;
import diarsid.beam.core.base.control.io.commands.EmptyCommand;
import diarsid.beam.core.domain.patternsanalyze.WeightedVariants;

import static java.lang.String.format;
import static java.time.LocalDateTime.now;

import static diarsid.beam.core.base.control.flow.Operations.voidCompleted;
import static diarsid.beam.core.base.control.flow.Operations.voidOperationFail;
import static diarsid.beam.core.base.control.flow.Operations.voidOperationStopped;
import static diarsid.beam.core.base.control.io.base.interaction.Variants.stringsToVariants;
import static diarsid.beam.core.base.control.io.commands.CommandType.CREATE_NOTE;
import static diarsid.beam.core.base.control.io.commands.CommandType.OPEN_NOTES;
import static diarsid.beam.core.base.control.io.commands.CommandType.OPEN_PATH_IN_NOTES;
import static diarsid.beam.core.base.control.io.commands.CommandType.OPEN_TARGET_IN_NOTES;
import static diarsid.beam.core.base.util.CollectionsUtils.getOne;
import static diarsid.beam.core.base.util.CollectionsUtils.hasOne;
import static diarsid.beam.core.base.util.PathUtils.containsPathSeparator;
import static diarsid.beam.core.base.util.StringUtils.normalize;
import static diarsid.beam.core.domain.patternsanalyze.Analyze.weightVariants;

/**
 *
 * @author Diarsid
 */
class NotesKeeperWorker implements NotesKeeper {

    private final InnerIoEngine ioEngine;
    private final NotesCatalog notesCatalog;
    private final KeeperDialogHelper helper;
    
    // TODO 
    // 1 - fix creation by path (separator not allowed)
    // 2 - add extension adding .txt to any created file
    NotesKeeperWorker(
            InnerIoEngine ioEngine, NotesCatalog notesCatalog, KeeperDialogHelper helper) {
        this.ioEngine = ioEngine;
        this.notesCatalog = notesCatalog;
        this.helper = helper;
    }    

    private void reportAndOpen(Initiator initiator, String noteTarget) throws IOException {
        this.ioEngine.report(initiator, format("opening %s...", noteTarget));
        this.notesCatalog.open(noteTarget);
    }

    private void reportCreateAndOpen(Initiator initiator, String noteName) throws IOException {
        this.ioEngine.report(initiator, format("opening %s...", noteName));
        this.notesCatalog.createAndOpenNoteWithName(noteName);
    }

    @Override
    public VoidOperation openNotes(Initiator initiator, EmptyCommand command) {
        if ( command.type().isNot(OPEN_NOTES) ) {
            return voidOperationFail("wrong command type!");
        }
        
        try {
            this.ioEngine.report(initiator, "openinig Notes...");
            this.notesCatalog.open();
        } catch (IOException ex) {
            return voidOperationFail(ex.getMessage());
        }    
        return voidCompleted();
    }

    @Override
    public VoidOperation openTargetInNotes(Initiator initiator, ArgumentsCommand command) {
        if ( command.type().isNot(OPEN_TARGET_IN_NOTES) ) {
            return voidOperationFail("wrong command type!");
        }
        
        String noteName = command.joinedArguments();
        if ( noteName.isEmpty() ) {
            return voidOperationStopped();
        } 
        
        List<String> foundNotes = this.notesCatalog.findByNoteName(noteName);
        if ( foundNotes.isEmpty() ) {
            return voidOperationFail("not found.");
        }
        
        if ( hasOne(foundNotes) ) {
            try {
                this.reportAndOpen(initiator, getOne(foundNotes));                
                return voidCompleted();
            } catch (IOException ex) {
                return voidOperationFail(ex.getMessage());
            }
        } else {
            return this.processMultipleNotes(initiator, noteName, foundNotes);
        }
    }

    @Override
    public VoidOperation openPathInNotes(Initiator initiator, ArgumentsCommand command) {
        if ( command.type().isNot(OPEN_PATH_IN_NOTES) ) {
            return voidOperationFail("wrong command type!");
        }
        
        String pathToOpen = command.getFirstArg();
        if ( pathToOpen.isEmpty() ) {
            return voidOperationStopped();
        } 
        
        List<String> foundNotePaths = this.notesCatalog.findByPath(pathToOpen);
        if ( foundNotePaths.isEmpty() ) {
            return voidOperationFail("not found.");
        }
        
        if ( hasOne(foundNotePaths) ) {
            try {
                this.reportAndOpen(initiator, getOne(foundNotePaths));
                return voidCompleted();
            } catch (IOException ex) {
                return voidOperationFail(ex.getMessage());
            }    
        } else {            
            return this.processMultipleNotes(initiator, pathToOpen, foundNotePaths);
        }
    }    

    private VoidOperation processMultipleNotes(
            Initiator initiator, String noteTarget, List<String> foundNoteTargets) {
        WeightedVariants variants =
                weightVariants(noteTarget, stringsToVariants(foundNoteTargets));
        Answer answer = this.ioEngine.chooseInWeightedVariants(initiator, variants);
        if ( answer.isGiven() ) {
            try {
                this.reportAndOpen(initiator, answer.text());
                return voidCompleted();
            } catch (IOException ex) {
                return voidOperationFail(ex.getMessage());
            }
        } else {
            return voidOperationStopped();
        }
    }

    @Override
    public VoidOperation createNote(Initiator initiator, ArgumentsCommand command) {
        if ( command.type().isNot(CREATE_NOTE) ) {
            return voidOperationFail("wrong command type!");
        }
        
        String noteName = command.joinedArguments();
        if ( noteName.isEmpty() ) {
            noteName = now().toString();
        } else {
            noteName = normalize(noteName);
            if ( containsPathSeparator(noteName) ) {
                // TODO
                noteName = helper.validateEntityNameInteractively(initiator, noteName);
            } else {
                noteName = helper.validateEntityNameInteractively(initiator, noteName);
            }            
        }
        
        if ( noteName.isEmpty() ) {
            return voidOperationStopped();
        }
        
        try {
            this.reportCreateAndOpen(initiator, noteName);
            return voidCompleted();
        } catch (IOException ex) {
            return voidOperationFail(ex.getMessage());
        }
    }
}
