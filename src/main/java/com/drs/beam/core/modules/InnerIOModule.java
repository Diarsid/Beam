/*
 * project: Beam
 * author: Diarsid
 */
package com.drs.beam.core.modules;

import java.util.List;

import com.drs.beam.core.entities.Task;

/*
 * Interface defines methods for output within the program.
 * These methods are used by another parts of program to reportInfo 
 * about messages, exceptions, tasks, events and so on.
 * Implementation has to describe concrete ways how exactly 
 * display given output infomation according to it`s 
 * types, settings and other requirements.
 */
public interface InnerIOModule extends Module{  
    
    void showTask(Task task);  
    
    void reportInfo(String... info);
    
    void reportMessage(String... info);
    
    void reportError(String... error);
    void reportErrorAndExitLater(String... error);
    
    void reportException(Exception e, String... description); 
    void reportExceptionAndExitLater(Exception e, String... description); 
    
    int  resolveVariantsWithExternalIO(String message, List<String> variants);
    
    static String getModuleName(){
        return "Inner IO Module";
    }
}
