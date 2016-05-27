/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package diarsid.beam.core.modules.executor;

import java.util.List;

import diarsid.beam.core.entities.Location;

/**
 *
 * @author Diarsid
 */
class ProcessorNotes {
    
    private final OS system;
    private final Location notes;
    
    ProcessorNotes(OS sys, Location notes) {
        this.system = sys;
        this.notes = notes;
    }
    
    void openNotes() {
        this.system.openLocation(this.notes);
    }
    
    void openNote(List<String> commandParams) {
        if (commandParams.size() < 2) {
            this.system.createAndOpenTxtFileIn("", this.notes);
        } else {
            String name = String.join(" ", commandParams.subList(1, commandParams.size()));
            OperationResult openFile = this.system.openFileInLocation(name, this.notes);
            if ( ! openFile.ifOperationWasSuccessful() ) {
                this.system.createAndOpenTxtFileIn(name, this.notes);
            }
        }
    }
}
