/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package diarsid.beam.core.domain.inputparsing.time;

import java.time.LocalDateTime;
import java.util.Objects;

import diarsid.beam.core.domain.entities.TaskRepeatType;

import static java.time.LocalDateTime.now;

/**
 *
 * @author Diarsid
 */
public class TasksTime {
    
    private final LocalDateTime time;
    private final TasksTimeType timeType;

    TasksTime(LocalDateTime time, TasksTimeType timeType) {
        this.time = time;
        this.timeType = timeType;
    }

    public LocalDateTime actualizedTime() {
        if ( this.time.isBefore(now()) ) {
            return this.timeType.leapToFutureAccordingToType(this.time);
        } else {
            return this.time;
        }        
    }
    
    public TaskRepeatType defineTasksType() {
        return this.timeType.getAppropriateTaskType();
    }

    @Override
    public int hashCode() {
        int hash = 5;
        hash = 67 * hash + Objects.hashCode(this.time);
        hash = 67 * hash + Objects.hashCode(this.timeType);
        return hash;
    }

    @Override
    public boolean equals(Object obj) {
        if ( this == obj ) {
            return true;
        }
        if ( obj == null ) {
            return false;
        }
        if ( getClass() != obj.getClass() ) {
            return false;
        }
        final TasksTime other = ( TasksTime ) obj;
        if ( !Objects.equals(this.time, other.time) ) {
            return false;
        }
        if ( this.timeType != other.timeType ) {
            return false;
        }
        return true;
    }
}
