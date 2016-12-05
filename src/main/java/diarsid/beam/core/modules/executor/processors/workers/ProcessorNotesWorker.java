/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package diarsid.beam.core.modules.executor.processors.workers;

import java.util.List;

import diarsid.beam.core.domain.entities.Location;
import diarsid.beam.core.modules.executor.OS;
import diarsid.beam.core.modules.executor.processors.ProcessorNotes;

/**
 *
 * @author Diarsid
 */
class ProcessorNotesWorker implements ProcessorNotes {
    
    private final OS system;
    private final Location notes;
    
    ProcessorNotesWorker(OS sys, Location notes) {
        this.system = sys;
        this.notes = notes;
    }
    
    @Override
    public void openNotes() {
        this.system.openLocation(this.notes);
    }
    
    @Override
    public void openNote(List<String> commandParams) {
        if (commandParams.size() < 2) {
            this.system.createAndOpenTxtFileIn("", this.notes);
        } else {
            String name = String.join(" ", commandParams.subList(1, commandParams.size()));
            this.system.openFileInLocation(name, this.notes);
//            OperationResult openFile = this.system.openFileInLocation(name, this.notes);
//            if ( ! openFile.ifSuccess() ) {
//                this.system.createAndOpenTxtFileIn(name, this.notes);
//            }
        }
    }
}
