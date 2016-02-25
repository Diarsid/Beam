/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package diarsid.beam.core.modules.tasks;

import diarsid.beam.core.modules.tasks.TaskVerifier;

import org.junit.Before;
import org.junit.Test;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

/**
 *
 * @author Diarsid
 */
public class TestTaskVerifier {
    
    TaskVerifier verifier;
    
    @Before
    public void init(){
        verifier = new TaskVerifier();
    }
    
    /*
    @Test
    public void testVerifyTaskOnForbiddenCharsFalse(){
        String forbiddenChars = Task.DB_TASK_DELIMITER;
        String[] task = {"line"+forbiddenChars, "line2", "line3"};
        assertFalse(verifier.verifyTaskOnForbiddenChars(task));
    }
    
    @Test
    public void testVerifyTextOnForbiddenCharsFalse(){
        String forbiddenChars = Task.DB_TASK_DELIMITER;
        String text = "some text " + forbiddenChars + " some text";
        assertFalse(verifier.verifyTextOnForbiddenChars(text));
    }

    @Test
    public void testVerifyTaskOnForbiddenCharsTrue(){
        String[] task = {"line1", "line2", "line3"};
        assertTrue(verifier.verifyTaskOnForbiddenChars(task));
    }
    
    @Test
    public void testVerifyTextOnForbiddenCharsTrue(){
        String text = "some text ";
        assertTrue(verifier.verifyTextOnForbiddenChars(text));
    }
    */
}
