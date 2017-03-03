/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package diarsid.beam.core.domain.inputparsing.common;

import org.junit.BeforeClass;
import org.junit.Test;

import static java.util.Arrays.asList;

import static org.junit.Assert.assertEquals;

import static diarsid.beam.core.domain.entities.metadata.EntityProperty.FILE_URL;
import static diarsid.beam.core.domain.entities.metadata.EntityProperty.NAME;
import static diarsid.beam.core.domain.entities.metadata.EntityProperty.UNDEFINED_PROPERTY;

/**
 *
 * @author Diarsid
 */
public class PropertyAndTextParserTest {
    
    static PropertyAndTextParser parser = new PropertyAndTextParser();

    public PropertyAndTextParserTest() {
    }

    @BeforeClass
    public static void setUpClass() {
    }

    /**
     * Test of parse method, of class PropertyAndTextParser.
     */
    @Test
    public void testParse_property() {
        PropertyAndText pt = parser.parse(asList("name"));
        assertEquals(NAME, pt.property());
        assertEquals("", pt.text());
    }
    
    @Test
    public void testParse_text() {
        PropertyAndText pt = parser.parse(asList("content"));
        assertEquals(UNDEFINED_PROPERTY, pt.property());
        assertEquals("content", pt.text());
    }
    
    @Test
    public void testParse_property_text() {
        PropertyAndText pt = parser.parse(asList("content", "path"));
        assertEquals(FILE_URL, pt.property());
        assertEquals("content", pt.text());
    }
    
    @Test
    public void testParse_text_property() {
        PropertyAndText pt = parser.parse(asList("path", "content"));
        assertEquals(FILE_URL, pt.property());
        assertEquals("content", pt.text());
    }

}