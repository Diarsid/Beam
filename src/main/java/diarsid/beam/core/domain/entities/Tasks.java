/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package diarsid.beam.core.domain.entities;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Set;

import static java.lang.Integer.MIN_VALUE;
import static java.lang.String.join;
import static java.time.format.DateTimeFormatter.ofPattern;
import static java.util.Arrays.asList;
import static java.util.Arrays.stream;
import static java.util.Collections.emptySet;
import static java.util.stream.Collectors.joining;
import static java.util.stream.Collectors.toSet;

import static diarsid.beam.core.base.util.StringUtils.nonEmpty;

/**
 *
 * @author Diarsid
 */
public class Tasks {    
    
    static final int NEW_TASK_ID;
    public static final DateTimeFormatter TIME_PRINT_FORMAT;
    static {
        NEW_TASK_ID = MIN_VALUE;
        TIME_PRINT_FORMAT = ofPattern("HH:mm (dd.MM.uuuu)");
    }
    
    private Tasks() {
    }
    
    public static String stringifyTimePeriods(Set<Integer> times) {
        if ( times.isEmpty() ) {
            return "";
        } else {
            return times
                    .stream()
                    .map(time -> time.toString())
                    .collect(joining(" "));
        }        
    }
    
    static Set<Integer> collectTimePeriods(String timePeriodsString) {
        if ( timePeriodsString.isEmpty() ) {
            return emptySet();
        } else {
            return stream(timePeriodsString.split(" "))
                    .filter(timeString -> nonEmpty(timeString))
                    .map(time -> Integer.parseInt(time))
                    .collect(toSet());
        }        
    }
    
    public static String stringifyTaskText(List<String> text) {
        return join(" \\ ", text);
    }
    
    static List<String> collectTaskText(String textString) {
        return asList(textString.split(" \\\\ "));
    }
    
    public static Task newInstantTask(
            TaskRepeat type, 
            LocalDateTime time,
            List<String> text) {
        return new TaskInstant(NEW_TASK_ID, time, true, text);
    }
    
    public static Task newReminderTask(
            TaskRepeat type, 
            LocalDateTime time,
            Set<Integer> days, 
            Set<Integer> hours,
            List<String> text) {        
        return new TaskReminder(NEW_TASK_ID, time, days, hours, type, text);
    }
    
    public static Task newEventTask(
            TaskRepeat type, 
            LocalDateTime time,
            List<String> text) {
        return new TaskEvent(NEW_TASK_ID, time, type, text);
    }
    
    public static Task restoreTask(
            int id, 
            TaskRepeat type, 
            LocalDateTime time,
            boolean status, 
            String daysString, 
            String hoursString,
            String textString) {    
        switch ( type ) {
            case NO_REPEAT : {
                return new TaskInstant(id, time, status, collectTaskText(textString));
            }
            case HOURLY_REPEAT :
            case DAILY_REPEAT : {
                return new TaskReminder(
                        id, 
                        time, 
                        collectTimePeriods(daysString), 
                        collectTimePeriods(hoursString), 
                        type, 
                        collectTaskText(textString));
            }
            case MONTHLY_REPEAT :
            case YEARLY_REPEAT : {
                return new TaskEvent(id, time, type, collectTaskText(textString));
            }
            case UNDEFINED_REPEAT:
            default : {
                throw new IllegalArgumentException("Unknown or undefined TaskRepeat.");
            }            
        }
    }
}
