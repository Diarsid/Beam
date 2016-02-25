/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package diarsid.beam.core.modules.tasks;

import java.time.DateTimeException;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;

import diarsid.beam.core.modules.data.DaoTasks;

import diarsid.beam.core.modules.tasks.exceptions.TaskTimeFormatInvalidException;
import diarsid.beam.core.modules.tasks.exceptions.TaskTimeInvalidException;

/**
 *
 * @author Diarsid
 */
class TaskTimeFormatter {    
    
    TaskTimeFormatter() { 
    }
    
    /*
     * Parses LocalDateTime according to appropriate format.
     * Boolean mustBeFuture == true means parsed time value 
     * cannot be before current moment of time.
     */
    LocalDateTime ofFormat(String timeString, boolean mustBeFuture) 
            throws 
            DateTimeException,
            DateTimeParseException, 
            NumberFormatException, 
            TaskTimeInvalidException, 
            TaskTimeFormatInvalidException {
        
        LocalDateTime time = null;
        // get length of incoming string to define it's format
        parsing: switch (timeString.length()){
            // time format: uuuu-MM-dd HH:mm - 16 chars
            // full format
            case (16) : {                    
                time = LocalDateTime.parse(
                        timeString,
                        DateTimeFormatter.ofPattern(DaoTasks.DB_TIME_PATTERN));
                break parsing;
            }
            // time format: +MM - 3 chars
            // specifies time in minutes, which is added to current time-date like timer
            case (3) : {                    
                time = LocalDateTime.now().withSecond(00).withNano(000)
                        .plusMinutes(Integer.parseInt(timeString.substring(1,3)));
                break parsing;
            }
            // time format: HH:MM - 5 chars
            // specifies today's hours and minutes
            case (5) : {                    
                time = LocalDateTime.now().withSecond(00).withNano(000)
                        .withHour(Integer.parseInt(timeString.substring(0,2)))
                        .withMinute(Integer.parseInt(timeString.substring(3,5)));
                break parsing;
            }
            // time format: +HH:MM - 6 chars
            // specifies time in hours and minutes, which is added to current time-date like timer
            case (6) : {                    
                time = LocalDateTime.now().withSecond(00).withNano(000)
                        .plusHours(Integer.parseInt(timeString.substring(1,3)))
                        .plusMinutes(Integer.parseInt(timeString.substring(4,6)));
                break parsing;
            }
            // time format: dd HH:MM - 8 chars
            // specifies hours, minutes and day of current month
            case (8) : {                    
                time = LocalDateTime.now().withSecond(00).withNano(000)
                        .withDayOfMonth(Integer.parseInt(timeString.substring(0,2)))
                        .withHour(Integer.parseInt(timeString.substring(3,5)))
                        .withMinute(Integer.parseInt(timeString.substring(6,8)));
                break parsing;
            }
            // time format: dd-mm HH:MM - 11 chars
            // specifies hours, minutes, day and month of current year
            case (11) : {                    
                time = LocalDateTime.now().withSecond(00).withNano(000)
                        .withDayOfMonth(Integer.parseInt(timeString.substring(0,2)))
                        .withMonth(Integer.parseInt(timeString.substring(3,5)))
                        .withHour(Integer.parseInt(timeString.substring(6,8)))
                        .withMinute(Integer.parseInt(timeString.substring(9,11)));
                break parsing;
            }
            default: {
                // Unrecognizable time format.
                throw new TaskTimeFormatInvalidException();
            }
        }
        
        /*
        * If given time format is OK and time was parsed correctly, it is required to test
        * this time whether it is future.
        *
        * If argument mustBeFuture is TRUE, method should return NULL instead of parsed
        * time if it represents past date. If mustBeFuture is FALSE, it is allowed to return
        * time which represents past date.
        */
        if (mustBeFuture && LocalDateTime.now().isAfter(time)){
            // Time must be future.
            throw new TaskTimeInvalidException();
        } else {
            return time;
        }       
    }
    
    String outputTimePatternFormat(LocalDateTime time){
        return time.format(DateTimeFormatter.ofPattern(Task.OUTPUT_TIME_PATTERN));
    }
}
