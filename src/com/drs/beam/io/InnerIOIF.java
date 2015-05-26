/*
 * project: Beam
 * author: Diarsid
 */
package com.drs.beam.io;

import com.drs.beam.tasks.Task;

/*
 * Interface defines methods for output within the program.
 * These methods are used by another parts of program to inform 
 * about messages, exceptions, tasks, events and so on.
 * Implementation has to describe concrete ways how exactly 
 * display given output infomation according to it`s 
 * types, settings and other requirements.
 */
public interface InnerIOIF {    
    public void showTask(Task task);    
    public void inform(String info);    
    public void informAboutError(String error, boolean isCritical);
    public void informAboutException(Exception e, boolean isCritical);
}
