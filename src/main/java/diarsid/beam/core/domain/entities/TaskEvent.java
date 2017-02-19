/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package diarsid.beam.core.domain.entities;

import java.time.LocalDateTime;
import java.util.List;

import static diarsid.beam.core.domain.entities.TaskRepeat.MONTHLY_REPEAT;
import static diarsid.beam.core.domain.entities.TaskRepeat.YEARLY_REPEAT;

/**
 *
 * @author Diarsid
 */
class TaskEvent extends Task {
        
    TaskEvent(int id, LocalDateTime time, TaskRepeat type, List<String> text) {
        super(id, time, true, type, text);
        if ( type.isNot(MONTHLY_REPEAT, YEARLY_REPEAT) ) {
            throw new IllegalArgumentException(
                    "TaskRepeat type should be HOURLY_REPEAT or DAILY_REPEAT.");
        }
    }
    
    @Override
    public final synchronized void switchTime() {
        if ( super.isFuture() ) {
            // no need to switch.
            return;
        }
        typeSwitch: switch (super.type()) {            
            case MONTHLY_REPEAT : {
                // if task is monthly, its time should be reseted to
                // + 1 month from its current time and stored in DB.
                super.setTime(super.getTime().plusMonths(1));
                break typeSwitch;
            }
            case YEARLY_REPEAT : {
                // if task is yearly, its time should be reseted to
                // + 1 year from its current time and stored in DB.
                super.setTime(super.getTime().plusYears(1));
                break typeSwitch;
            }
            default: {
                throw new IllegalStateException(
                        "TaskRepeat type should be MONTHLY_REPEAT or YEARLY_REPEAT.");
            }
        }
    }
}
