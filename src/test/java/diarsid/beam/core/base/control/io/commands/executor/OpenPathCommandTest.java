/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package diarsid.beam.core.base.control.io.commands.executor;

import org.junit.BeforeClass;
import org.junit.Test;

import diarsid.beam.core.base.control.io.base.interaction.Variant;
import diarsid.beam.core.base.exceptions.RequirementException;

import static org.junit.Assert.*;

/**
 *
 * @author Diarsid
 */
public class OpenPathCommandTest {

    public OpenPathCommandTest() {
    }

    @BeforeClass
    public static void setUpClass() {
    }

    /**
     * Test of location method, of class OpenPathCommand.
     */
    @Test
    public void test_originalArgs() {
        OpenPathCommand c = new OpenPathCommand("proj/netb");
        assertEquals("proj", c.location().original());
        assertEquals("netb", c.target().original());
    }

    /**
     * Test of target method, of class OpenPathCommand.
     */
    @Test(expected = RequirementException.class)
    public void testEmptyConstructor() {
        OpenPathCommand c = new OpenPathCommand("");
        fail();
    }

    /**
     * Test of type method, of class OpenPathCommand.
     */
    @Test
    public void testConstructor_1() {
        OpenPathCommand c = new OpenPathCommand("proj/netb", "");
        assertEquals("proj", c.location().original());
        assertEquals("netb", c.target().original());
        assertFalse(c.location().hasExtended());
        assertFalse(c.target().hasExtended());
    }
    
    @Test
    public void testConstructor_2() {
        OpenPathCommand c = new OpenPathCommand("proj/netb", "projects/netbeans");
        assertEquals("proj", c.location().original());
        assertEquals("netb", c.target().original());
        assertEquals("projects", c.location().extended());
        assertEquals("netbeans", c.target().extended());        
    }

    /**
     * Test of toVariant method, of class OpenPathCommand.
     */
    @Test
    public void testToVariant() {
        OpenPathCommand c = new OpenPathCommand("proj/netb", "projects/netbeans");
        Variant v = c.toVariant(1);
        assertEquals(1, v.index());
        assertEquals("open proj/netb", v.text());
    }

    /**
     * Test of stringify method, of class OpenPathCommand.
     */
    @Test
    public void testStringify() {        
        OpenPathCommand c = new OpenPathCommand("proj/netb");
        assertEquals("open proj/netb", c.stringify());
        
        OpenPathCommand c1 = new OpenPathCommand("proj/netb", "projects/netbeans");
        assertEquals("open proj/netb", c1.stringify());
    }

    /**
     * Test of originalArgument method, of class OpenPathCommand.
     */
    @Test
    public void testStringifyOriginalArgs() {
        OpenPathCommand c = new OpenPathCommand("proj/netb", "projects/netbeans");
        assertEquals("proj/netb", c.originalArgument());
    }

    /**
     * Test of extendedArgument method, of class OpenPathCommand.
     */
    @Test
    public void testStringifyExtendedArgs() {
        OpenPathCommand c = new OpenPathCommand("proj/netb");
        assertEquals("", c.extendedArgument());
        
        OpenPathCommand c1 = new OpenPathCommand("proj/netb", "projects/netbeans");
        assertEquals("projects/netbeans", c1.extendedArgument());
    }
}