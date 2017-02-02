/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package diarsid.beam.core.modules.tasks;

import java.time.Duration;
import java.time.LocalDateTime;

import static java.time.DayOfWeek.MONDAY;

/**
 *
 * @author Diarsid
 */
class TimeUtil {
    
    private TimeUtil() {
    }
    
    static LocalDateTime getThisWeekBeginning() {
        return LocalDateTime.now()
                .withHour(12)
                .withMinute(0)
                .withSecond(0)
                .withNano(0)
                .with(MONDAY);
    }

    static LocalDateTime getNextMonthBeginning() {
        return LocalDateTime.now()
                .withHour(12)
                .withMinute(0)
                .withSecond(0)
                .withNano(0)
                .plusMonths(1)
                .withDayOfMonth(1);
    }

    static LocalDateTime getNextWeekBeginning() {
        return LocalDateTime.now()
                .withHour(12)
                .withMinute(0)
                .withSecond(0)
                .withNano(0)
                .plusWeeks(1)
                .with(MONDAY);
    }
    
    static long getMinutesFromPastToNow(LocalDateTime past) {
        return Duration.between(past, LocalDateTime.now()).toMinutes();
    }
    
    static long getMillisFromNowToTime(LocalDateTime futureTime) {        
        return Duration.between(LocalDateTime.now(), futureTime).toMillis();
    }
}
