/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package diarsid.beam.core.base.util;

import org.junit.Test;

import static org.hamcrest.CoreMatchers.equalTo;
import static org.junit.Assert.assertThat;

import static diarsid.beam.core.base.util.StringNumberUtils.removeLeadingDigitsFrom;

/**
 *
 * @author Diarsid
 */
public class StringNumberUtilsTest {
    
    @Test
    public void removeLeadingDigitsFromTest() {
        String target = "123 abc";
        String expected = " abc";
        
        assertThat(removeLeadingDigitsFrom(target), equalTo(expected));
    }
    
    @Test
    public void removeLeadingDigitsFromTest_notChanging() {
        String target = " abc";
        String expected = " abc";
        
        assertThat(removeLeadingDigitsFrom(target), equalTo(expected));
    }
    
    @Test
    public void removeLeadingDigitsFromTest_notChanging_withDigits() {
        String target = " abc 123";
        String expected = " abc 123";
        
        assertThat(removeLeadingDigitsFrom(target), equalTo(expected));
    }
}
