/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package diarsid.beam.core.modules.tasks;

/**
 *
 * @author Diarsid
 */
class TaskVerifier {
    
    TaskVerifier(){        
    }
        
    /*
     * Characters sequence '~}' is used as a delimiter between strings
     * when task`s text in string[] is saved into DB TEXT field.    
    
    boolean verifyTaskOnForbiddenChars(String[] text) {
        for (String s : text){
            if (s.contains(Task.DB_TASK_DELIMITER)){
                return false;
            }                                
        }
        return true;
    }
     */
    
    /*
     * Characters sequence '~}' is used as a delimiter between task`s text strings that 
     * was saved into DB TEXT field. 
    
    boolean verifyTextOnForbiddenChars(String text) {        
        return (! text.contains(Task.DB_TASK_DELIMITER));
    }
    */
}
