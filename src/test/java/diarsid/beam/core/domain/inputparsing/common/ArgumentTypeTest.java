/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package diarsid.beam.core.domain.inputparsing.common;

import org.junit.BeforeClass;
import org.junit.Test;

import static org.junit.Assert.*;

import static diarsid.beam.core.domain.inputparsing.common.ArgumentType.DOMAIN_WORD;
import static diarsid.beam.core.domain.inputparsing.common.ArgumentType.TEXT;

/**
 *
 * @author Diarsid
 */
public class ArgumentTypeTest {

    public ArgumentTypeTest() {
    }

    @BeforeClass
    public static void setUpClass() {
    }
    
    @Test
    public void testNAME() {
        assertTrue(DOMAIN_WORD.isAppropriateFor("content"));
        assertTrue(DOMAIN_WORD.isAppropriateFor("content (1)"));
        assertTrue(DOMAIN_WORD.isAppropriateFor("text (1)"));
        assertFalse(DOMAIN_WORD.isAppropriateFor("text"));
        assertFalse(DOMAIN_WORD.isAppropriateFor("name"));
        assertFalse(DOMAIN_WORD.isAppropriateFor("url"));
        assertFalse(DOMAIN_WORD.isAppropriateFor("order"));
        assertFalse(DOMAIN_WORD.isAppropriateFor("path/to"));
        assertFalse(DOMAIN_WORD.isAppropriateFor("panel"));
    }
    
    @Test
    public void testTEXT() {
        assertTrue(TEXT.isAppropriateFor("content"));
        assertTrue(TEXT.isAppropriateFor("content (1)"));
        assertTrue(TEXT.isAppropriateFor("text (1)"));
        assertTrue(TEXT.isAppropriateFor("+3"));
        assertTrue(TEXT.isAppropriateFor("ask: - ?"));
        assertFalse(TEXT.isAppropriateFor("text"));
        assertFalse(TEXT.isAppropriateFor("name"));
        assertFalse(TEXT.isAppropriateFor("url"));
        assertFalse(TEXT.isAppropriateFor("order"));
        assertFalse(TEXT.isAppropriateFor("path/to"));
        assertFalse(TEXT.isAppropriateFor("panel"));
    }
}