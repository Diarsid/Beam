/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package diarsid.beam.core.modules.domainkeeper;

import java.io.IOException;
import java.util.List;

import diarsid.beam.core.application.environment.NotesCatalog;
import diarsid.beam.core.base.analyze.variantsweight.WeightedVariants;
import diarsid.beam.core.base.control.io.base.actors.Initiator;
import diarsid.beam.core.base.control.io.base.actors.InnerIoEngine;
import diarsid.beam.core.base.control.io.base.interaction.Answer;
import diarsid.beam.core.base.control.io.base.interaction.Help;
import diarsid.beam.core.base.control.io.commands.ArgumentsCommand;
import diarsid.beam.core.base.control.io.commands.EmptyCommand;

import static java.lang.String.format;
import static java.time.LocalDateTime.now;
import static java.time.format.DateTimeFormatter.ISO_LOCAL_DATE_TIME;

import static diarsid.beam.core.base.analyze.variantsweight.Analyze.weightVariants;
import static diarsid.beam.core.base.control.io.base.interaction.Variants.stringsToVariants;
import static diarsid.beam.core.base.control.io.commands.CommandType.CREATE_NOTE;
import static diarsid.beam.core.base.control.io.commands.CommandType.OPEN_NOTES;
import static diarsid.beam.core.base.control.io.commands.CommandType.OPEN_PATH_IN_NOTES;
import static diarsid.beam.core.base.control.io.commands.CommandType.OPEN_TARGET_IN_NOTES;
import static diarsid.beam.core.base.util.CollectionsUtils.getOne;
import static diarsid.beam.core.base.util.CollectionsUtils.hasOne;
import static diarsid.beam.core.base.util.PathUtils.containsPathSeparator;
import static diarsid.beam.core.base.util.StringUtils.normalizeSpaces;
import static diarsid.beam.core.domain.entities.validation.ValidationRule.SIMPLE_PATH_RULE;

import diarsid.beam.core.base.control.flow.VoidFlow;

import static diarsid.beam.core.base.control.flow.Flows.voidFlowCompleted;
import static diarsid.beam.core.base.control.flow.Flows.voidFlowCompleted;
import static diarsid.beam.core.base.control.flow.Flows.voidFlowCompleted;
import static diarsid.beam.core.base.control.flow.Flows.voidFlowCompleted;
import static diarsid.beam.core.base.control.flow.Flows.voidFlowStopped;
import static diarsid.beam.core.base.control.flow.Flows.voidFlowFail;

/**
 *
 * @author Diarsid
 */
class NotesKeeperWorker implements NotesKeeper {

    private final InnerIoEngine ioEngine;
    private final NotesCatalog notesCatalog;
    private final KeeperDialogHelper helper;
    private final Help chooseOneNoteHelp;
    
    // TODO ??? find closest path te/rea -> is tech/react/ ?
    NotesKeeperWorker(
            InnerIoEngine ioEngine, NotesCatalog notesCatalog, KeeperDialogHelper helper) {
        this.ioEngine = ioEngine;
        this.notesCatalog = notesCatalog;
        this.helper = helper;
        this.chooseOneNoteHelp = this.ioEngine.addToHelpContext(
                "Choose one note.",
                "Use:",
                "   - number to choose note",
                "   - name part of note to choose it",
                "   - n/no to see more variants, if any",
                "   - dot to break");
    }    

    private void reportAndOpen(Initiator initiator, String noteTarget) throws IOException {
        this.ioEngine.report(initiator, format("...opening %s", noteTarget));
        this.notesCatalog.open(noteTarget);
    }

    private void reportCreateAndOpen(Initiator initiator, String noteName) throws IOException {
        this.ioEngine.report(initiator, format("...opening %s", noteName));
        this.notesCatalog.createAndOpenNoteWithName(noteName);
    }

    @Override
    public VoidFlow openNotes(Initiator initiator, EmptyCommand command) {
        if ( command.type().isNot(OPEN_NOTES) ) {
            return voidFlowFail("wrong command type!");
        }
        
        try {
            this.ioEngine.report(initiator, "...openinig Notes");
            this.notesCatalog.open();
        } catch (IOException ex) {
            return voidFlowFail(ex.getMessage());
        }    
        return voidFlowCompleted();
    }

    @Override
    public VoidFlow openTargetInNotes(Initiator initiator, ArgumentsCommand command) {
        if ( command.type().isNot(OPEN_TARGET_IN_NOTES) ) {
            return voidFlowFail("wrong command type!");
        }
        
        String noteName = command.joinedArguments();
        if ( noteName.isEmpty() ) {
            return voidFlowStopped();
        } 
        
        List<String> foundNotes = this.notesCatalog.findByNoteName(noteName);
        if ( foundNotes.isEmpty() ) {
            return voidFlowFail("not found.");
        }
        
        if ( hasOne(foundNotes) ) {
            try {
                this.reportAndOpen(initiator, getOne(foundNotes));                
                return voidFlowCompleted();
            } catch (IOException ex) {
                return voidFlowFail(ex.getMessage());
            }
        } else {
            return this.processMultipleNotes(initiator, noteName, foundNotes);
        }
    }

    @Override
    public VoidFlow openPathInNotes(Initiator initiator, ArgumentsCommand command) {
        if ( command.type().isNot(OPEN_PATH_IN_NOTES) ) {
            return voidFlowFail("wrong command type!");
        }
        
        String pathToOpen = command.getFirstArg();
        if ( pathToOpen.isEmpty() ) {
            return voidFlowStopped();
        } 
        
        List<String> foundNotePaths = this.notesCatalog.findByPath(pathToOpen);
        if ( foundNotePaths.isEmpty() ) {
            return voidFlowFail("not found.");
        }
        
        if ( hasOne(foundNotePaths) ) {
            try {
                this.reportAndOpen(initiator, getOne(foundNotePaths));
                return voidFlowCompleted();
            } catch (IOException ex) {
                return voidFlowFail(ex.getMessage());
            }    
        } else {            
            return this.processMultipleNotes(initiator, pathToOpen, foundNotePaths);
        }
    }    

    private VoidFlow processMultipleNotes(
            Initiator initiator, String noteTarget, List<String> foundNoteTargets) {
        WeightedVariants variants =
                weightVariants(noteTarget, stringsToVariants(foundNoteTargets));
        if ( variants.isEmpty() ) {
            return voidFlowFail("not found.");
        }
        Answer answer = this.ioEngine.chooseInWeightedVariants(
                initiator, variants, this.chooseOneNoteHelp);
        if ( answer.isGiven() ) {
            try {
                this.reportAndOpen(initiator, answer.text());
                return voidFlowCompleted();
            } catch (IOException ex) {
                return voidFlowFail(ex.getMessage());
            }
        } else {
            return voidFlowStopped();
        }
    }

    @Override
    public VoidFlow createNote(Initiator initiator, ArgumentsCommand command) {
        if ( command.type().isNot(CREATE_NOTE) ) {
            return voidFlowFail("wrong command type!");
        }
        
        String noteName = command.joinedArguments();
        if ( noteName.isEmpty() ) {
            noteName = now().format(ISO_LOCAL_DATE_TIME).replace(':', '-');
        } else {
            noteName = normalizeSpaces(noteName);
            if ( containsPathSeparator(noteName) ) {
                noteName = helper.validateInteractively(
                        initiator, noteName, "note path", SIMPLE_PATH_RULE);
            } else {
                noteName = helper.validateEntityNameInteractively(initiator, noteName);
            }            
        }
        
        if ( noteName.isEmpty() ) {
            return voidFlowStopped();
        }
        
        try {
            this.reportCreateAndOpen(initiator, noteName);
            return voidFlowCompleted();
        } catch (IOException ex) {
            return voidFlowFail(ex.getMessage());
        }
    }
}
