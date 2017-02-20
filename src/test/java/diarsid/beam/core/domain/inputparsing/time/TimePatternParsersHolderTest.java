/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package diarsid.beam.core.domain.inputparsing.time;

import java.time.LocalDateTime;
import java.util.Optional;

import org.junit.BeforeClass;
import org.junit.Test;

import static java.time.LocalDateTime.now;
import static java.time.Year.isLeap;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import static diarsid.beam.core.domain.inputparsing.time.TimeParsing.timePatternParsersHolder;


/**
 *
 * @author Diarsid
 */
public class TimePatternParsersHolderTest {
    
    private final static TimePatternParsersHolder parsersHolder = timePatternParsersHolder();

    public TimePatternParsersHolderTest() {
    }

    @BeforeClass
    public static void setUpClass() {
    }

    @Test
    public void parse_plusMinutesM() {
        String time = "+3m";
        Optional<TaskTime> optTime = parsersHolder.parse(time);
        assertTrue(optTime.isPresent());
        LocalDateTime expected = now()
                .plusMinutes(3)
                .withSecond(0)
                .withNano(0);
        assertEquals(expected, optTime.get().actualizedTime());
    }
    
    @Test
    public void parse_plusMinutes() {
        String time = "+3";
        Optional<TaskTime> optTime = parsersHolder.parse(time);
        assertTrue(optTime.isPresent());
        LocalDateTime expected = now()
                .plusMinutes(3)
                .withSecond(0)
                .withNano(0);
        assertEquals(expected, optTime.get().actualizedTime());
    }
    
    @Test
    public void parse_plusHours() {
        String time = "+3h";
        Optional<TaskTime> optTime = parsersHolder.parse(time);
        assertTrue(optTime.isPresent());
        LocalDateTime expected = now()
                .plusHours(3)
                .withSecond(0)
                .withNano(0);
        assertEquals(expected, optTime.get().actualizedTime());
    }
    
    @Test
    public void parse_plusHoursMinutesH() {
        String time = "+3h:12";
        Optional<TaskTime> optTime = parsersHolder.parse(time);
        assertTrue(optTime.isPresent());
        LocalDateTime expected = now()
                .plusHours(3)
                .plusMinutes(12)
                .withSecond(0)
                .withNano(0);
        assertEquals(expected, optTime.get().actualizedTime());
    }
    
    @Test
    public void parse_plusHoursMinutesHM() {
        String time = "+3h:12m";
        Optional<TaskTime> optTime = parsersHolder.parse(time);
        assertTrue(optTime.isPresent());
        LocalDateTime expected = now()
                .plusHours(3)
                .plusMinutes(12)
                .withSecond(0)
                .withNano(0);
        assertEquals(expected, optTime.get().actualizedTime());
    }
    
    @Test
    public void parse_plusHoursMinutes() {
        String time = "+3:12";
        Optional<TaskTime> optTime = parsersHolder.parse(time);
        assertTrue(optTime.isPresent());
        LocalDateTime expected = now()
                .plusHours(3)
                .plusMinutes(12)
                .withSecond(0)
                .withNano(0);
        assertEquals(expected, optTime.get().actualizedTime());
    }
    
    @Test
    public void parse_plusHoursMinutes_wrongMinutes() {
        String time = "+3:61";
        Optional<TaskTime> optTime = parsersHolder.parse(time);
        assertFalse(optTime.isPresent());
    }
    
    @Test
    public void parse_nextHoursMinutes() {
        String time = "3:12";
        Optional<TaskTime> optTime = parsersHolder.parse(time);
        assertTrue(optTime.isPresent());
        LocalDateTime expected = now()
                .withHour(3)
                .withMinute(12)
                .withSecond(0)
                .withNano(0);
        if ( expected.isBefore(now()) ) {
            expected = expected.plusDays(1);
        }
        assertEquals(expected, optTime.get().actualizedTime());
    }
    
    @Test
    public void parse_nextHoursMinutesH() {
        String time = "3h:12";
        Optional<TaskTime> optTime = parsersHolder.parse(time);
        assertTrue(optTime.isPresent());
        LocalDateTime expected = now()
                .withHour(3)
                .withMinute(12)
                .withSecond(0)
                .withNano(0);
        if ( expected.isBefore(now()) ) {
            expected = expected.plusDays(1);
        }
        assertEquals(expected, optTime.get().actualizedTime());
    }
    
    @Test
    public void parse_nextHoursMinutesM() {
        String time = "3:12m";
        Optional<TaskTime> optTime = parsersHolder.parse(time);
        assertTrue(optTime.isPresent());
        LocalDateTime expected = now()
                .withHour(3)
                .withMinute(12)
                .withSecond(0)
                .withNano(0);
        if ( expected.isBefore(now()) ) {
            expected = expected.plusDays(1);
        }
        assertEquals(expected, optTime.get().actualizedTime());
    }
    
    @Test
    public void parse_nextHoursMinutesHM() {
        String time = "3h:12m";
        Optional<TaskTime> optTime = parsersHolder.parse(time);
        assertTrue(optTime.isPresent());
        LocalDateTime expected = now()
                .withHour(3)
                .withMinute(12)
                .withSecond(0)
                .withNano(0);
        if ( expected.isBefore(now()) ) {
            expected = expected.plusDays(1);
        }
        assertEquals(expected, optTime.get().actualizedTime());
    }
    
    @Test
    public void parse_nextHoursMinutes_wrongMinutes() {
        String time = "3:70";
        Optional<TaskTime> optTime = parsersHolder.parse(time);
        assertFalse(optTime.isPresent());
    }
    
    @Test
    public void parse_nextDaysHoursMinutes_leapDaysCase() {
        String time = "31 13:10";
        Optional<TaskTime> optTime = parsersHolder.parse(time);
        if ( now().getMonth().length(isLeap(now().getYear())) == 31 ) {
            assertTrue(optTime.isPresent());
            LocalDateTime expected = now()
                    .withDayOfMonth(31)
                    .withHour(13)
                    .withMinute(10)
                    .withSecond(0)
                    .withNano(0);
            if ( expected.isBefore(now()) ) {
                expected = expected.plusMonths(1);
            }
            assertEquals(expected, optTime.get().actualizedTime());
        } else {
            assertFalse(optTime.isPresent());
        }        
    }
    
    @Test
    public void parse_nextDaysHoursMinutes() {
        String time = "24 13:10";
        Optional<TaskTime> optTime = parsersHolder.parse(time);
        assertTrue(optTime.isPresent());
        LocalDateTime expected = now()
                .withDayOfMonth(24)
                .withHour(13)
                .withMinute(10)
                .withSecond(0)
                .withNano(0);
        if ( expected.isBefore(now()) ) {
            expected = expected.plusMonths(1);
        }
        assertEquals(expected, optTime.get().actualizedTime());        
    }
    
    @Test 
    public void parse_nextMinuteM() {
        String time = "35m";
        Optional<TaskTime> optTime = parsersHolder.parse(time);
        assertTrue(optTime.isPresent());
        LocalDateTime expected = now()
                .withMinute(35)
                .withSecond(0)
                .withNano(0);
        if ( expected.isBefore(now()) ) {
            expected = expected.plusHours(1);
        }
        assertEquals(expected, optTime.get().actualizedTime());
    }
    
    @Test 
    public void parse_nextMinute() {
        String time = "35";
        Optional<TaskTime> optTime = parsersHolder.parse(time);
        assertTrue(optTime.isPresent());
        LocalDateTime expected = now()
                .withMinute(35)
                .withSecond(0)
                .withNano(0);
        if ( expected.isBefore(now()) ) {
            expected = expected.plusHours(1);
        }
        assertEquals(expected, optTime.get().actualizedTime());
    }
    
    @Test 
    public void parse_nextHourH() {
        String time = "14h";
        Optional<TaskTime> optTime = parsersHolder.parse(time);
        assertTrue(optTime.isPresent());
        LocalDateTime expected = now()
                .withHour(14)
                .withSecond(0)
                .withNano(0);
        if ( expected.isBefore(now()) ) {
            expected = expected.plusDays(1);
        }
        assertEquals(expected, optTime.get().actualizedTime());
    }
    
    @Test
    public void parse_nextMonthDayHourMinute_dot() {
        String time = "25.10 12:30";
        Optional<TaskTime> optTime = parsersHolder.parse(time);
        assertTrue(optTime.isPresent());
        LocalDateTime expected = now()
                .withMonth(10)
                .withDayOfMonth(25)
                .withHour(12)
                .withMinute(30)
                .withSecond(0)
                .withNano(0);
        if ( expected.isBefore(now()) ) {
            expected = expected.plusYears(1);
        }
        assertEquals(expected, optTime.get().actualizedTime());
    }
    
    @Test
    public void parse_nextMonthDayHourMinute_dash() {
        String time = "25-10 12:30";
        Optional<TaskTime> optTime = parsersHolder.parse(time);
        assertTrue(optTime.isPresent());
        LocalDateTime expected = now()
                .withMonth(10)
                .withDayOfMonth(25)
                .withHour(12)
                .withMinute(30)
                .withSecond(0)
                .withNano(0);
        if ( expected.isBefore(now()) ) {
            expected = expected.plusYears(1);
        }
        assertEquals(expected, optTime.get().actualizedTime());
    }
    
    @Test
    public void parse_fullDate_dash_naturalOrder() {
        String time = "25-10-2017 12:30";
        Optional<TaskTime> optTime = parsersHolder.parse(time);
        assertTrue(optTime.isPresent());
        LocalDateTime expected = now()
                .withYear(2017)
                .withMonth(10)
                .withDayOfMonth(25)
                .withHour(12)
                .withMinute(30)
                .withSecond(0)
                .withNano(0);
        if ( expected.isBefore(now()) ) {
            expected = expected.plusYears(1);
        }
        assertEquals(expected, optTime.get().actualizedTime());
    }
    
    @Test
    public void parse_fullDate_dot_naturalOrder() {
        String time = "25.10.2017 12:30";
        Optional<TaskTime> optTime = parsersHolder.parse(time);
        assertTrue(optTime.isPresent());
        LocalDateTime expected = now()
                .withYear(2017)
                .withMonth(10)
                .withDayOfMonth(25)
                .withHour(12)
                .withMinute(30)
                .withSecond(0)
                .withNano(0);
        if ( expected.isBefore(now()) ) {
            expected = expected.plusYears(1);
        }
        assertEquals(expected, optTime.get().actualizedTime());
    }
    
    @Test
    public void parse_fullDate_dash_computerOrder() {
        String time = "2017-10-25 12:30";
        Optional<TaskTime> optTime = parsersHolder.parse(time);
        assertTrue(optTime.isPresent());
        LocalDateTime expected = now()
                .withYear(2017)
                .withMonth(10)
                .withDayOfMonth(25)
                .withHour(12)
                .withMinute(30)
                .withSecond(0)
                .withNano(0);
        if ( expected.isBefore(now()) ) {
            expected = expected.plusYears(1);
        }
        assertEquals(expected, optTime.get().actualizedTime());
    }
    
    @Test
    public void parse_fullDate_dot_computerOrder() {
        String time = "2017.10.25 12:30";
        Optional<TaskTime> optTime = parsersHolder.parse(time);
        assertTrue(optTime.isPresent());
        LocalDateTime expected = now()
                .withYear(2017)
                .withMonth(10)
                .withDayOfMonth(25)
                .withHour(12)
                .withMinute(30)
                .withSecond(0)
                .withNano(0);
        if ( expected.isBefore(now()) ) {
            expected = expected.plusYears(1);
        }
        assertEquals(expected, optTime.get().actualizedTime());
    }

}