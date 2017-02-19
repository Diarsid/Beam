/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package diarsid.beam.core.domain.inputparsing.time;

import java.time.LocalDateTime;

import static java.time.LocalDateTime.now;
import static java.util.Arrays.stream;

import static diarsid.beam.core.domain.inputparsing.time.TasksTimeType.Relativeness.ABSOLUTE_TIME;
import static diarsid.beam.core.domain.inputparsing.time.TasksTimeType.Relativeness.RELATIVE_TIME;

/**
 *
 * @author Diarsid
 */
public enum TasksTimeType {
    
    PLUS_MINUTES (RELATIVE_TIME) {
        @Override
        public LocalDateTime leapToFutureAccordingToType(LocalDateTime time) {
            while ( time.isBefore(now()) ) {                
                time = time.plusHours(1);
            }
            return time;
        }
    },
    PLUS_HOURS (RELATIVE_TIME) {
        @Override
        public LocalDateTime leapToFutureAccordingToType(LocalDateTime time) {
            while ( time.isBefore(now()) ) {                
                time = time.plusHours(1);
            }
            return time;
        }
    },
    PLUS_HOURS_AND_MINUTES (RELATIVE_TIME) {
        @Override
        public LocalDateTime leapToFutureAccordingToType(LocalDateTime time) {
            while ( time.isBefore(now()) ) {                
                time = time.plusHours(1);
            }
            return time;
        }
    },
    NEXT_MINUTES (ABSOLUTE_TIME) {
        @Override
        public LocalDateTime leapToFutureAccordingToType(LocalDateTime time) {
            while ( time.isBefore(now()) ) {                
                time = time.plusHours(1);
            }
            return time;
        }
    },
    NEXT_HOURS (ABSOLUTE_TIME) {
        @Override
        public LocalDateTime leapToFutureAccordingToType(LocalDateTime time) {
            while ( time.isBefore(now()) ) {                
                time = time.plusDays(1);
            }
            return time;
        }
    },
    NEXT_HOURS_AND_MINUTES (ABSOLUTE_TIME) {
        @Override
        public LocalDateTime leapToFutureAccordingToType(LocalDateTime time) {
            while ( time.isBefore(now()) ) {                
                time = time.plusDays(1);
            }
            return time;
        }
    },
    NEXT_DAYS_HOURS_AND_MINUTES (ABSOLUTE_TIME) {
        @Override
        public LocalDateTime leapToFutureAccordingToType(LocalDateTime time) {
            while ( time.isBefore(now()) ) {                
                time = time.plusMonths(1);
            }
            return time;
        }
    },
    NEXT_MONTH_DAY_HOURS_AND_MINUTES (ABSOLUTE_TIME) {
        @Override
        public LocalDateTime leapToFutureAccordingToType(LocalDateTime time) {
            while ( time.isBefore(now()) ) {                
                time = time.plusYears(1);
            }
            return time;
        }
    },
    FULL_DATE (ABSOLUTE_TIME) {
        @Override
        public LocalDateTime leapToFutureAccordingToType(LocalDateTime time) {
            while ( time.isBefore(now()) ) {                
                time = time.plusYears(1);
            }
            return time;
        }
    };
    
    private final Relativeness quality;
    
    private TasksTimeType(Relativeness type) {
        this.quality = type;
    }
    
    public enum Relativeness {
        RELATIVE_TIME,
        ABSOLUTE_TIME
    }
    
    public abstract LocalDateTime leapToFutureAccordingToType(LocalDateTime pastTime);
    
    public boolean is(TasksTimeType other) {
        return this.equals(other);
    }
    
    public boolean isNot(TasksTimeType other) {
        return ! this.equals(other);
    }
    
    public boolean isRelative() {
        return this.quality.equals(RELATIVE_TIME);
    }
    
    public boolean isAbsolute() {
        return this.quality.equals(ABSOLUTE_TIME);
    }
    
    public boolean isOfQuality(Relativeness otherQality) {
        return this.quality.equals(otherQality);
    }
    
    public boolean isOneOf(TasksTimeType... others) {
        return stream(others).anyMatch(other -> this.equals(other));
    }
    
    public boolean isNotOneOf(TasksTimeType... others) {
        return stream(others).noneMatch(other -> this.equals(other));
    }
}
