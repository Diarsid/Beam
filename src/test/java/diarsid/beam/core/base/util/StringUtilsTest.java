/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package diarsid.beam.core.base.util;

import java.util.List;

import org.junit.Test;

import diarsid.beam.base.BaseTest;

import static java.util.Arrays.asList;

import static org.hamcrest.CoreMatchers.equalTo;
import static org.junit.Assert.assertThat;

import static diarsid.beam.core.base.util.StringUtils.countOccurences;
import static diarsid.beam.core.base.util.StringUtils.removeAllSeparators;
import static diarsid.beam.core.base.util.StringUtils.splitToLines;

/**
 *
 * @author Diarsid
 */
public class StringUtilsTest extends BaseTest {
    
    @Test
    public void splitToLinesTest() {
        String multilines = "line1\nline2\nline3\n";
        List<String> expected = asList("line1", "line2", "line3");
        
        assertThat(splitToLines(multilines), equalTo(expected));
    }
    
    @Test
    public void countOccurencesTest() {
        assertThat(countOccurences("ABcABd", "AB"), equalTo(2));
    }
    
    @Test
    public void removeAllSeparatorsTest() {
        String target = "0_1-2\\3/4_5--6\\\\7//8__9";
        String result = removeAllSeparators(target);
        assertThat(result, equalTo("0123456789"));
    }
}
