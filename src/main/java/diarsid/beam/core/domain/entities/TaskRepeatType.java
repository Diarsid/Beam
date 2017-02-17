/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package diarsid.beam.core.domain.entities;

import static java.util.Arrays.stream;

/**
 *
 * @author Diarsid
 */
public enum TaskRepeatType {
    
    NO_REPEAT,
    HOURLY_REPEAT,
    DAILY_REPEAT,
    MONTHLY_REPEAT,
    YEARLY_REPEAT;    
    
    public boolean isNot(TaskRepeatType other) {
        return ! this.equals(other);
    }
    
    public boolean isNot(TaskRepeatType... others) {
        return stream(others)
                .noneMatch(other -> this.equals(other));
    }
}
