/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package diarsid.beam.core.domain.inputparsing.time;


import org.junit.Test;

import static org.junit.Assert.assertTrue;

import static diarsid.beam.core.domain.inputparsing.time.TimeParsing.allowedTimePeriodsParser;


/**
 *
 * @author Diarsid
 */
public class AllowedExecutionTimeParserTest {
    
    private final static AllowedTimePeriodsParser parser = allowedTimePeriodsParser();

    public AllowedExecutionTimeParserTest() {
    }

    /**
     * Test of parseAllowedHours method, of class AllowedTimePeriodsParser.
     */
    @Test
    public void testParseAllowedHours_1() {
        AllowedTimePeriod time = parser.parseAllowedHours("12, 13, 14, 18 - 20, 11, 23");
        assertTrue(time.hasHours());
        assertTrue(time.containsHours(11, 12, 13, 14, 18, 19, 23));
    }
    
    @Test
    public void testParseAllowedHours_2() {
        AllowedTimePeriod time = parser.parseAllowedHours("7-12, 15,18 - 20");
        assertTrue(time.hasHours());
        assertTrue(time.containsHours(7, 8, 9, 10, 11, 15, 18, 19));
    }
    
    @Test
    public void testParseAllowedHours_3() {
        AllowedTimePeriod time = parser.parseAllowedHours("15,18 - 24");
        assertTrue(time.hasHours());
        assertTrue(time.containsHours(15, 18, 19, 21, 22, 23));
    }

    /**
     * Test of parseAllowedDays method, of class AllowedTimePeriodsParser.
     */
    @Test
    public void testParseAllowedDays_1() {
        AllowedTimePeriod time = parser.parseAllowedDays("1, 3-5, 7");
        assertTrue(time.hasDays());
        assertTrue(time.containsDays(1, 3, 4, 5, 7));
    }
    
    @Test
    public void testParseAllowedDays_2() {
        AllowedTimePeriod time = parser.parseAllowedDays("1, 7, 3 - 5");
        assertTrue(time.hasDays());
        assertTrue(time.containsDays(1, 3, 4, 5, 7));
    }

}