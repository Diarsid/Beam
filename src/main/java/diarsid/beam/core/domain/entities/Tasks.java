/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package diarsid.beam.core.domain.entities;

import java.time.LocalDateTime;
import java.util.Set;

/**
 *
 * @author Diarsid
 */
public class Tasks {    
    
    public static final String OUTPUT_TIME_PATTERN = "HH:mm (dd-MM-uuuu)";
    
    private Tasks() {
    }
    
    public static Task newTask(
            TaskRepeatType type, 
            LocalDateTime time,
            Set<Integer> days, 
            Set<Integer> hours,
            String... content) {        
        return new Task(type, time, content, days, hours);
    }
    
    public static Task restoreTask(
            int id, 
            TaskRepeatType type, 
            LocalDateTime time,
            boolean status, 
            Set<Integer> days, 
            Set<Integer> hours,
            String... content) {        
        return new Task(id, type, time, content, status, days, hours);
    }
}
