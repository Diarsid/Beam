/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package diarsid.beam.core.domain.inputparsing.locations;

import org.junit.BeforeClass;
import org.junit.Test;

import static java.util.Arrays.asList;

import static org.junit.Assert.assertEquals;

/**
 *
 * @author Diarsid
 */
public class LocationsInputParserTest {
    
    static final LocationsInputParser parser = new LocationsInputParser();

    public LocationsInputParserTest() {
    }

    @BeforeClass
    public static void setUpClass() {
    }

    /**
     * Test of parse method, of class LocationsInputParser.
     */
    @Test
    public void testParse_text_path() {
        LocationNameAndPath namePath = parser.parse(asList("content", "D:\\content"));
        assertEquals("D:\\content", namePath.getPath());
        assertEquals("content", namePath.getName());
    }
    
    @Test
    public void testParse_path_text() {
        LocationNameAndPath namePath = parser.parse(asList("D:\\content", "content"));
        assertEquals("D:\\content", namePath.getPath());
        assertEquals("content", namePath.getName());
    }
    
    @Test
    public void testParse_path() {
        LocationNameAndPath namePath = parser.parse(asList("D:\\content"));
        assertEquals("D:\\content", namePath.getPath());
        assertEquals("", namePath.getName());
    }
    
    @Test
    public void testParse_text() {
        LocationNameAndPath namePath = parser.parse(asList("content"));
        assertEquals("", namePath.getPath());
        assertEquals("content", namePath.getName());
    }

}