/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package diarsid.beam.core.base.util;

import org.junit.Test;

import static org.hamcrest.CoreMatchers.equalTo;
import static org.junit.Assert.assertThat;

import static diarsid.beam.core.base.util.MutableString.mutableString;

/**
 *
 * @author Diarsid
 */
public class MutableStringTest {
    
    public MutableStringTest() {
    }
    
    @Test
    public void testGetAndEmpty() {
        MutableString mString = mutableString("string");
        String string = mString.getAndEmpty();
        
        assertThat(string, equalTo("string"));
        assertThat(mString.isEmpty(), equalTo(true));
    }
    
}
