/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package diarsid.beam.core.domain.entities;

import java.util.List;

import static java.util.Arrays.stream;

import static diarsid.beam.core.base.util.CollectionsUtils.toUnmodifiableList;
import static diarsid.beam.core.base.util.StringIgnoreCaseUtil.containsWordInIgnoreCase;

/**
 *
 * @author Diarsid
 */
public enum TimePeriod {    
        
    SECONDS {
        @Override
        public boolean isAppropriateFormat(String timeString) {
            return containsWordInIgnoreCase(SECONDS_PATTERNS, timeString);
        }
    },
    MINUTES {
        @Override
        public boolean isAppropriateFormat(String timeString) {
            return containsWordInIgnoreCase(MINUTES_PATTERNS, timeString);
        }
    },
    HOURS {
        @Override
        public boolean isAppropriateFormat(String timeString) {
            return containsWordInIgnoreCase(HOURS_PATTERNS, timeString);
        }
    },
    DAYS {
        @Override
        public boolean isAppropriateFormat(String timeString) {
            return containsWordInIgnoreCase(DAYS_PATTERNS, timeString);
        }
    },
    WEEKS {
        @Override
        public boolean isAppropriateFormat(String timeString) {
            return containsWordInIgnoreCase(WEEKS_PATTERNS, timeString);
        }
    },
    MONTHS {
        @Override
        public boolean isAppropriateFormat(String timeString) {
            return containsWordInIgnoreCase(MONTHES_PATTERNS, timeString);
        }
    },
    YEARS {
        @Override
        public boolean isAppropriateFormat(String timeString) {
            return containsWordInIgnoreCase(YEARS_PATTERNS, timeString);
        }
    },
    UNDEFINED {
        @Override
        public boolean isAppropriateFormat(String timeString) {
            return false;
        }
    };
    
    private static final List<String> SECONDS_PATTERNS;
    private static final List<String> MINUTES_PATTERNS;
    private static final List<String> HOURS_PATTERNS;    
    private static final List<String> DAYS_PATTERNS;
    private static final List<String> WEEKS_PATTERNS;
    private static final List<String> MONTHES_PATTERNS;
    private static final List<String> YEARS_PATTERNS;
    private static final List<String> ALL_PATTERNS;
    static {
        SECONDS_PATTERNS = toUnmodifiableList("s", "sec", "second", "seconds");
        MINUTES_PATTERNS = toUnmodifiableList("m", "min", "minute", "minutes");
        HOURS_PATTERNS = toUnmodifiableList("h", "hour", "hours");
        DAYS_PATTERNS = toUnmodifiableList("d", "day", "days");
        WEEKS_PATTERNS = toUnmodifiableList("w", "week", "weeks");
        MONTHES_PATTERNS = toUnmodifiableList("mo", "month", "months");
        YEARS_PATTERNS = toUnmodifiableList("y", "yy", "years", "year");
        ALL_PATTERNS = toUnmodifiableList(
                SECONDS_PATTERNS, 
                MINUTES_PATTERNS, 
                HOURS_PATTERNS, 
                DAYS_PATTERNS, 
                WEEKS_PATTERNS, 
                MONTHES_PATTERNS, 
                YEARS_PATTERNS);
    }
    
    public abstract boolean isAppropriateFormat(String timeString);
    
    public boolean is(TimePeriod other) {
        return this.equals(other);
    }
    
    public boolean isNot(TimePeriod other) {
        return ! this.equals(other);
    }
    
    public boolean isDefined() {
        return ! this.equals(UNDEFINED);
    }
    
    public static TimePeriod parseTimePeriodFrom(String timeString) {
        return stream(values())
                .filter(timePeriod -> timePeriod.isAppropriateFormat(timeString))
                .findFirst()
                .orElse(UNDEFINED);
    }
    
    public static boolean isAppropriateAsTimePeriod(String s) {
        return containsWordInIgnoreCase(ALL_PATTERNS, s);
    }
}
