/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package diarsid.beam.core.base.control.io.commands.executor;

import org.junit.BeforeClass;
import org.junit.Test;

import diarsid.beam.core.base.analyze.variantsweight.Variant;
import diarsid.beam.core.base.exceptions.RequirementException;

import static org.junit.Assert.*;

import static diarsid.beam.core.base.control.io.commands.executor.InvocationCommandLifePhase.STORED;
import static diarsid.beam.core.base.control.io.commands.executor.InvocationCommandTargetState.TARGET_FOUND;

/**
 *
 * @author Diarsid
 */
public class OpenLocationTargetCommandTest {

    public OpenLocationTargetCommandTest() {
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
        assertEquals("proj", c.originalLocation());
        assertEquals("netb", c.originalTarget());
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
        OpenLocationTargetCommand c = new OpenLocationTargetCommand("proj/netb");
        assertEquals("proj", c.originalLocation());
        assertEquals("netb", c.originalTarget());
        assertFalse(c.argument().isExtended());
    }
    
    @Test
    public void testConstructor_2() {
        OpenLocationTargetCommand c = new OpenLocationTargetCommand("proj/netb", "projects/netbeans", STORED, TARGET_FOUND);
        assertEquals("proj", c.originalLocation());
        assertEquals("netb", c.originalTarget());
        assertEquals("projects", c.extendedLocation());
        assertEquals("netbeans", c.extendedTarget());        
    }

    /**
     * Test of toVariant method, of class OpenLocationTargetCommand.
     */
    @Test
    public void testToVariant() {
        OpenLocationTargetCommand c = new OpenLocationTargetCommand("proj/netb", "projects/netbeans", STORED, TARGET_FOUND);
        Variant v = c.toVariant(1);
        assertEquals(1, v.index());
        assertEquals("open projects/netbeans", v.name());
        assertEquals("projects/netbeans", v.text());
    }

    /**
     * Test of stringify method, of class OpenLocationTargetCommand.
     */
    @Test
    public void testStringify() {        
        OpenLocationTargetCommand c = new OpenLocationTargetCommand("proj/netb");
        assertEquals("open proj/netb", c.stringify());
        
        OpenLocationTargetCommand c1 = new OpenLocationTargetCommand("proj/netb", "projects/netbeans", STORED, TARGET_FOUND);
        assertEquals("open projects/netbeans", c1.stringify());
    }

    /**
     * Test of originalArgument method, of class OpenLocationTargetCommand.
     */
    @Test
    public void testStringifyOriginalArgs() {
        OpenLocationTargetCommand c = new OpenLocationTargetCommand("proj/netb", "projects/netbeans", STORED, TARGET_FOUND);
        assertEquals("proj/netb", c.originalArgument());
    }

    /**
     * Test of extendedArgument method, of class OpenLocationTargetCommand.
     */
    @Test
    public void testStringifyExtendedArgs() {
        OpenLocationTargetCommand c = new OpenLocationTargetCommand("proj/netb");
        assertEquals("", c.extendedArgument());
        
        OpenLocationTargetCommand c1 = new OpenLocationTargetCommand("proj/netb", "projects/netbeans", STORED, TARGET_FOUND);
        assertEquals("projects/netbeans", c1.extendedArgument());
    }
}