/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package com.drs.beam.core.entities;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

import diarsid.beam.core.modules.tasks.Task;

import org.junit.Before;
import org.junit.Test;

import static org.junit.Assert.assertEquals;

/**
 *
 * @author Diarsid
 */
public class TaskTest {
    Task task;
    
    String[] content;
    
    LocalDateTime time;
    
    @Before
    public void createTaskObjects(){
        /**
         * Parameters:
         * year - the year to represent, from MIN_YEAR to MAX_YEAR
         * month - the month-of-year to represent, from 1 (January) to 12 (December)
         * dayOfMonth - the day-of-month to represent, from 1 to 31
         * hour - the hour-of-day to represent, from 0 to 23
         * minute - the minute-of-hour to represent, from 0 to 59
         */
        time = LocalDateTime.of(2015, 1, 1, 1, 1);
        content = new String[]{"first_line", "second_line", "third_line"};
                
        //task = new Task(Task.USUAL_TASK, time, content);
    }
    
    /*
    @Test
    public void testTaskConstructor(){  
        String contentString = String.join(Task.DB_TASK_DELIMITER, content);
        Task taskTwo = new Task(Task.USUAL_TASK, time, contentString);
        assertEquals(task, taskTwo);        
    }
    
    @Test
    public void testGetContentForStoring(){
        String result = String.join(Task.DB_TASK_DELIMITER, content);
        String contentString = task.getContentForStoring();
        assertEquals(result, contentString);
    }
    
    @Test
    public void testGetTimeOutputString(){
        String result = this.time.format(DateTimeFormatter.ofPattern(Task.OUTPUT_TIME_PATTERN));
        String timeString = task.getTimeOutputString();
        assertEquals(result, timeString);
    }
    
    @Test
    public void testGetTimeDBString(){
        String result = this.time.format(DateTimeFormatter.ofPattern(Task.DB_TIME_PATTERN));
        String timeString = task.getTimeDBString();
        assertEquals(result, timeString);
    }
    */
}
