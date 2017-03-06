/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package diarsid.beam.core.domain.inputparsing.webpages;

import org.junit.BeforeClass;
import org.junit.Test;

import static java.util.Arrays.asList;

import static org.junit.Assert.assertEquals;

import static diarsid.beam.core.domain.entities.WebPlace.BOOKMARKS;
import static diarsid.beam.core.domain.entities.WebPlace.UNDEFINED_PLACE;
import static diarsid.beam.core.domain.entities.WebPlace.WEBPANEL;
import static diarsid.beam.core.domain.entities.metadata.EntityProperty.ORDER;

/**
 *
 * @author Diarsid
 */
public class WebObjectsInputParserTest {
    
    static WebObjectsInputParser parser = new WebObjectsInputParser();
    

    @BeforeClass
    public static void setUpClass() {
    }

    /**
     * Test of parseNameAndPlace method, of class WebObjectsInputParser.
     */
    @Test
    public void testParseNameAndPlace_place_name() {
        WebDirectoryNameAndPlace nameAndPlace = parser.parseNameAndPlace(asList("panel", "common"));
        assertEquals(WEBPANEL, nameAndPlace.place());
        assertEquals("common", nameAndPlace.name());
    }
    
    @Test
    public void testParseNameAndPlace_name_place() {
        WebDirectoryNameAndPlace nameAndPlace = parser.parseNameAndPlace(asList("common", "bookm"));
        assertEquals(BOOKMARKS, nameAndPlace.place());
        assertEquals("common", nameAndPlace.name());
    }
    
    @Test
    public void testParseNameAndPlace_name() {
        WebDirectoryNameAndPlace nameAndPlace = parser.parseNameAndPlace(asList("common"));
        assertEquals(UNDEFINED_PLACE, nameAndPlace.place());
        assertEquals("common", nameAndPlace.name());
    }
    
    @Test
    public void testParseNameAndPlace_place() {
        WebDirectoryNameAndPlace nameAndPlace = parser.parseNameAndPlace(asList("webpanel"));
        assertEquals(WEBPANEL, nameAndPlace.place());
        assertEquals("", nameAndPlace.name());
    }

    /**
     * Test of parseNameUrlAndPlace method, of class WebObjectsInputParser.
     */
    @Test
    public void testParseNameUrlAndPlace_url() {
        WebPageNameUrlAndPlace input = parser.parseNameUrlAndPlace(asList("https://www.google.com.ua/"));
        assertEquals("", input.name());
        assertEquals(UNDEFINED_PLACE, input.place());
        assertEquals("https://www.google.com.ua/", input.url());
    }
    
    @Test
    public void testParseNameUrlAndPlace_name() {
        WebPageNameUrlAndPlace input = parser.parseNameUrlAndPlace(asList("google"));
        assertEquals("google", input.name());
        assertEquals(UNDEFINED_PLACE, input.place());
        assertEquals("", input.url());
    }
    
    @Test
    public void testParseNameUrlAndPlace_place() {
        WebPageNameUrlAndPlace input = parser.parseNameUrlAndPlace(asList("webp"));
        assertEquals("", input.name());
        assertEquals(WEBPANEL, input.place());
        assertEquals("", input.url());
    }
    
    @Test
    public void testParseNameUrlAndPlace_url_name() {
        WebPageNameUrlAndPlace input = parser.parseNameUrlAndPlace(asList("https://www.google.com.ua/", "google"));
        assertEquals("google", input.name());
        assertEquals(UNDEFINED_PLACE, input.place());
        assertEquals("https://www.google.com.ua/", input.url());
    }
    
    @Test
    public void testParseNameUrlAndPlace_name_url() {
        WebPageNameUrlAndPlace input = parser.parseNameUrlAndPlace(asList("google", "https://www.google.com.ua/"));
        assertEquals("google", input.name());
        assertEquals(UNDEFINED_PLACE, input.place());
        assertEquals("https://www.google.com.ua/", input.url());
    }
    
    @Test
    public void testParseNameUrlAndPlace_url_place() {
        WebPageNameUrlAndPlace input = parser.parseNameUrlAndPlace(asList("https://www.google.com.ua/", "bookm"));
        assertEquals("", input.name());
        assertEquals(BOOKMARKS, input.place());
        assertEquals("https://www.google.com.ua/", input.url());
    }
    
    @Test
    public void testParseNameUrlAndPlace_place_url() {
        WebPageNameUrlAndPlace input = parser.parseNameUrlAndPlace(asList("bookm", "https://www.google.com.ua/"));
        assertEquals("", input.name());
        assertEquals(BOOKMARKS, input.place());
        assertEquals("https://www.google.com.ua/", input.url());
    }
    
    @Test
    public void testParseNameUrlAndPlace_name_place() {
        WebPageNameUrlAndPlace input = parser.parseNameUrlAndPlace(asList("google", "panel"));
        assertEquals("google", input.name());
        assertEquals(WEBPANEL, input.place());
        assertEquals("", input.url());
    }
    
    @Test
    public void testParseNameUrlAndPlace_place_name() {
        WebPageNameUrlAndPlace input = parser.parseNameUrlAndPlace(asList("panel", "google"));
        assertEquals("google", input.name());
        assertEquals(WEBPANEL, input.place());
        assertEquals("", input.url());
    }   
    
    @Test
    public void testParseNameUrlAndPlace_url_name_place() {
        WebPageNameUrlAndPlace input = parser.parseNameUrlAndPlace(asList("https://www.google.com.ua/", "google", "panel"));
        assertEquals("google", input.name());
        assertEquals(WEBPANEL, input.place());
        assertEquals("https://www.google.com.ua/", input.url());
    }
    
    @Test
    public void testParseNameUrlAndPlace_name_url_place() {
        WebPageNameUrlAndPlace input = parser.parseNameUrlAndPlace(asList("google", "https://www.google.com.ua/", "panel"));
        assertEquals("google", input.name());
        assertEquals(WEBPANEL, input.place());
        assertEquals("https://www.google.com.ua/", input.url());
    }
    
    @Test
    public void testParseNameUrlAndPlace_url_place_name() {
        WebPageNameUrlAndPlace input = parser.parseNameUrlAndPlace(asList("https://www.google.com.ua/", "panel", "google"));
        assertEquals("google", input.name());
        assertEquals(WEBPANEL, input.place());
        assertEquals("https://www.google.com.ua/", input.url());
    }
    
    @Test
    public void testParseNameUrlAndPlace_place_url_name() {
        WebPageNameUrlAndPlace input = parser.parseNameUrlAndPlace(asList("panel", "https://www.google.com.ua/", "google"));
        assertEquals("google", input.name());
        assertEquals(WEBPANEL, input.place());
        assertEquals("https://www.google.com.ua/", input.url());
    }
    
    @Test
    public void testParseNameUrlAndPlace_name_place_url() {
        WebPageNameUrlAndPlace input = parser.parseNameUrlAndPlace(asList("google", "panel", "https://www.google.com.ua/"));
        assertEquals("google", input.name());
        assertEquals(WEBPANEL, input.place());
        assertEquals("https://www.google.com.ua/", input.url());
    }
    
    @Test
    public void testParseNameUrlAndPlace_place_name_url() {
        WebPageNameUrlAndPlace input = parser.parseNameUrlAndPlace(asList("panel", "google", "https://www.google.com.ua/"));
        assertEquals("google", input.name());
        assertEquals(WEBPANEL, input.place());
        assertEquals("https://www.google.com.ua/", input.url());
    }  
    
    @Test
    public void testParseNamePlaceAndProperty_property() {
        WebDirectoryNamePlaceAndProperty namePlaceProperty = 
                parser.parseNamePlaceAndProperty(asList("order"));
        assertEquals(ORDER, namePlaceProperty.property());
    }
    
    @Test
    public void testParseNamePlaceAndProperty_name_place_property() {
        WebDirectoryNamePlaceAndProperty input = 
                parser.parseNamePlaceAndProperty(asList("common", "webp", "order"));
        assertEquals("common", input.name());
        assertEquals(WEBPANEL, input.place());
        assertEquals(ORDER, input.property());
    }
    
}