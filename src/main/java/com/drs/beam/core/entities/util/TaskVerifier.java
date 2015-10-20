/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package com.drs.beam.core.entities.util;

import com.drs.beam.core.entities.Task;

/**
 *
 * @author Diarsid
 */
public class TaskVerifier {

    // Constructors =======================================================================
    public TaskVerifier(){        
    }
    
    // Methods ============================================================================
    
    /*
     * Characters sequence '~}' is used as a delimiter between strings
     * when task`s text in string[] is saved into DB TEXT field.    
     */
    public boolean verifyTaskOnForbiddenChars(String[] text) {
        for (String s : text){
            if (s.contains(Task.DB_TASK_DELIMITER)){
                return false;
            }                                
        }
        return true;
    }
    
    /*
     * Characters sequence '~}' is used as a delimiter between task`s text strings that 
     * was saved into DB TEXT field. 
     */
    public boolean verifyTextOnForbiddenChars(String text) {        
        return (! text.contains(Task.DB_TASK_DELIMITER));
    }
}
