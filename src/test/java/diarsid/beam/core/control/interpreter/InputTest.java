/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package diarsid.beam.core.control.interpreter;

import org.junit.BeforeClass;
import org.junit.Test;

import static org.junit.Assert.*;

/**
 *
 * @author Diarsid
 */
public class InputTest {

    public InputTest() {
    }

    @BeforeClass
    public static void setUpClass() {
    }

    /**
     * Test of toNextArg method, of class Input.
     */
    @Test
    public void testInputNextArgsBehavior() {
        String inputString = "call arg_1 arg_2";
        Input input = new Input(inputString);
        
        assertTrue(input.hasNotRecognizedArgs());
        assertEquals("call", input.currentArg());
        
        input.toNextArg();
        
        assertTrue(input.hasNotRecognizedArgs());
        assertEquals("arg_1", input.currentArg());
        
        input.toNextArg();
        
        assertTrue(input.hasNotRecognizedArgs());
        assertEquals("arg_2", input.currentArg());
        
        input.toNextArg();
        
        assertFalse(input.hasNotRecognizedArgs());
        assertEquals("arg_2", input.currentArg()); // last arg always remain as 'to recognize'
        
        input.toNextArg();
        assertEquals("arg_2", input.currentArg()); 
    }

}