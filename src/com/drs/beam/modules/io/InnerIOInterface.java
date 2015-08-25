/*
 * project: Beam
 * author: Diarsid
 */
package com.drs.beam.modules.io;

import com.drs.beam.modules.tasks.Task;
import java.util.List;

/*
 * Interface defines methods for output within the program.
 * These methods are used by another parts of program to inform 
 * about messages, exceptions, tasks, events and so on.
 * Implementation has to describe concrete ways how exactly 
 * display given output infomation according to it`s 
 * types, settings and other requirements.
 */
public interface InnerIOInterface {    
    void showTask(Task task);    
    void inform(String info);    
    void informAboutError(String error, boolean isCritical);
    void informAboutException(Exception e, boolean isCritical);
    int  resolveVariantsWithExternalIO(String message, List<String> variants);
}
