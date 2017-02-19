/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package diarsid.beam.core.domain.entities;

import java.time.LocalDateTime;
import java.util.List;

import static diarsid.beam.core.domain.entities.TaskRepeat.NO_REPEAT;

/**
 *
 * @author Diarsid
 */
class TaskInstant extends Task {

    TaskInstant(int id, LocalDateTime time, boolean activeStatus, List<String> text) {
        super(id, time, activeStatus, NO_REPEAT, text);
    }
    
    @Override
    public final synchronized void switchTime() {
        if ( super.isFuture() ) {
            // no need to switch.
            return;
        }
        typeSwitch: switch (super.type()) {
            case NO_REPEAT : {
                // if task is usual one-off task, it will be only 
                // disabled and stored in DB as non-active past task.
                super.setStatus(false);
                break typeSwitch;
            }
            default : {
                throw new IllegalStateException("TaskRepeat type should be NO_REPEAT.");
            }
        }  
    }
}
