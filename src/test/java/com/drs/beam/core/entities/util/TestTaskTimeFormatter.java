/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package com.drs.beam.core.entities.util;

import java.time.LocalDateTime;
import java.time.format.DateTimeParseException;

import com.drs.beam.core.entities.util.TaskTimeFormatter;
import com.drs.beam.core.entities.util.exceptions.TaskTimeFormatInvalidException;
import com.drs.beam.core.entities.util.exceptions.TaskTimeInvalidException;

import org.junit.Before;
import org.junit.Test;

import static org.junit.Assert.assertEquals;

/**
 *
 * @author Diarsid
 */
public class TestTaskTimeFormatter {
    // Fields =============================================================================
    TaskTimeFormatter formatter;

    // Constructors =======================================================================

    // Methods ============================================================================
    
    @Before
    public void init(){
        formatter = new TaskTimeFormatter();
    }
    
    @Test
    public void testOfFormatPlusMinutes() throws Exception{
        LocalDateTime time = LocalDateTime.now().plusMinutes(3).withSecond(00).withNano(000);
        LocalDateTime result = formatter.ofFormat("+03", true);
        assertEquals(time, result);        
    }
    
    @Test
    public void testOfFormatPlusMinutesAndHoures() throws Exception{
        LocalDateTime time = LocalDateTime.now().withSecond(00).withNano(000)
                .plusHours(01)
                .plusMinutes(01);
        LocalDateTime result = formatter.ofFormat("+01:01", true);
        assertEquals(time, result);
    }
    
    @Test
    public void testOfFormatOfHouresAndMinutes() throws Exception{
        LocalDateTime time = LocalDateTime.now().withSecond(00).withNano(000)
                .withHour(01)
                .withMinute(01);
        LocalDateTime result = formatter.ofFormat("01:01", false);
        assertEquals(time, result);
    }
    
    @Test
    public void testOfFormatOfHouresAndMinutesAndDay() throws Exception{
        LocalDateTime time = LocalDateTime.now().withSecond(00).withNano(000)
                .withDayOfMonth(01)
                .withHour(01)
                .withMinute(01);
        LocalDateTime result = formatter.ofFormat("01 01:01", false);
        assertEquals(time, result);
    }
    
    @Test
    public void testOfFormatOfHouresAndMinutesAndDayAndMonth() throws Exception{
        LocalDateTime time = LocalDateTime.now().withSecond(00).withNano(000)
                .withMonth(01)
                .withDayOfMonth(01)
                .withHour(01)
                .withMinute(01);
        LocalDateTime result = formatter.ofFormat("01-01 01:01", false);
        assertEquals(time, result);
    }
    
    @Test
    public void testOfFormatFullDate() throws Exception{
        LocalDateTime time = LocalDateTime.now().withSecond(00).withNano(000)
                .withYear(2001)
                .withMonth(01)
                .withDayOfMonth(01)
                .withHour(01)
                .withMinute(01);
        LocalDateTime result = formatter.ofFormat("2001-01-01 01:01", false);
        assertEquals(time, result);
    }
    
    @Test(expected = DateTimeParseException.class)
    public void testDateTimeParseException() throws Exception{
        // wrong date format: day-year-month
        formatter.ofFormat("01-2012-01 01:01", false);
    }
    
    @Test(expected = NumberFormatException.class)
    public void testNumberFormatException() throws Exception{
        // wrong format: not a number characters present, month is 1s
        formatter.ofFormat("01-1s 01:01", false);
    }
    
    @Test(expected = TaskTimeInvalidException.class)
    public void testTaskTimeInvalidException() throws Exception{
        // wrong time specified: expected future, given past
        formatter.ofFormat("1995-01-01 01:01", true);
    }
    
    @Test(expected = TaskTimeFormatInvalidException.class)
    public void testTaskTimeFormatInvalidException() throws Exception{
        // wrong format: month is 114
        formatter.ofFormat("01-114 01:01", false);
    }    
}
