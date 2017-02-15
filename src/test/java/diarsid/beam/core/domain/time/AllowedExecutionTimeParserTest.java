/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package diarsid.beam.core.domain.time;

import org.junit.Test;

import static org.junit.Assert.assertTrue;

import static diarsid.beam.core.domain.time.TimeParsing.allowedExecutionTimeParser;

/**
 *
 * @author Diarsid
 */
public class AllowedExecutionTimeParserTest {
    
    private final static AllowedExecutionTimeParser parser = allowedExecutionTimeParser();

    public AllowedExecutionTimeParserTest() {
    }

    /**
     * Test of parseAllowedHours method, of class AllowedExecutionTimeParser.
     */
    @Test
    public void testParseAllowedHours_1() {
        AllowedExecutionTime time = parser.parseAllowedHours("12, 13, 14, 18 - 20, 11, 23");
        assertTrue(time.hasHours());
        assertTrue(time.containsHours(11, 12, 13, 14, 18, 19, 23));
    }
    
    @Test
    public void testParseAllowedHours_2() {
        AllowedExecutionTime time = parser.parseAllowedHours("7-12, 15,18 - 20");
        assertTrue(time.hasHours());
        assertTrue(time.containsHours(7, 8, 9, 10, 11, 15, 18, 19));
    }

    /**
     * Test of parseAllowedDays method, of class AllowedExecutionTimeParser.
     */
    @Test
    public void testParseAllowedDays() {
        AllowedExecutionTime time = parser.parseAllowedDays("1, 3-5, 7");
        assertTrue(time.hasDays());
        assertTrue(time.containsDays(1, 3, 4, 5, 7));
    }

}