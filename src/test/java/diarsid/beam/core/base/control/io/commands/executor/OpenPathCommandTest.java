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
     * Test of location method, of class OpenLocationTargetCommand.
     */
    @Test
    public void test_originalArgs() {
        OpenLocationTargetCommand c = new OpenLocationTargetCommand("proj/netb");
        assertEquals("proj", c.location().original());
        assertEquals("netb", c.target().original());
    }

    /**
     * Test of target method, of class OpenLocationTargetCommand.
     */
    @Test(expected = RequirementException.class)
    public void testEmptyConstructor() {
        OpenLocationTargetCommand c = new OpenLocationTargetCommand("");
        fail();
    }

    /**
     * Test of type method, of class OpenLocationTargetCommand.
     */
    @Test
    public void testConstructor_1() {
        OpenLocationTargetCommand c = new OpenLocationTargetCommand("proj/netb", "");
        assertEquals("proj", c.location().original());
        assertEquals("netb", c.target().original());
        assertFalse(c.location().hasExtended());
        assertFalse(c.target().hasExtended());
    }
    
    @Test
    public void testConstructor_2() {
        OpenLocationTargetCommand c = new OpenLocationTargetCommand("proj/netb", "projects/netbeans");
        assertEquals("proj", c.location().original());
        assertEquals("netb", c.target().original());
        assertEquals("projects", c.location().extended());
        assertEquals("netbeans", c.target().extended());        
    }

    /**
     * Test of toVariant method, of class OpenLocationTargetCommand.
     */
    @Test
    public void testToVariant() {
        OpenLocationTargetCommand c = new OpenLocationTargetCommand("proj/netb", "projects/netbeans");
        Variant v = c.toVariant(1);
        assertEquals(1, v.index());
        assertEquals("open proj/netb", v.text());
    }

    /**
     * Test of stringify method, of class OpenLocationTargetCommand.
     */
    @Test
    public void testStringify() {        
        OpenLocationTargetCommand c = new OpenLocationTargetCommand("proj/netb");
        assertEquals("open proj/netb", c.stringify());
        
        OpenLocationTargetCommand c1 = new OpenLocationTargetCommand("proj/netb", "projects/netbeans");
        assertEquals("open proj/netb", c1.stringify());
    }

    /**
     * Test of originalArgument method, of class OpenLocationTargetCommand.
     */
    @Test
    public void testStringifyOriginalArgs() {
        OpenLocationTargetCommand c = new OpenLocationTargetCommand("proj/netb", "projects/netbeans");
        assertEquals("proj/netb", c.originalArgument());
    }

    /**
     * Test of extendedArgument method, of class OpenLocationTargetCommand.
     */
    @Test
    public void testStringifyExtendedArgs() {
        OpenLocationTargetCommand c = new OpenLocationTargetCommand("proj/netb");
        assertEquals("", c.extendedArgument());
        
        OpenLocationTargetCommand c1 = new OpenLocationTargetCommand("proj/netb", "projects/netbeans");
        assertEquals("projects/netbeans", c1.extendedArgument());
    }
}