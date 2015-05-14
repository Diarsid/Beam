/*
 * project: Beam
 * author: Diarsid
 */
package com.drs.beam.io;

import com.drs.beam.tasks.Task;

public interface InnerIOIF {    
    /*
     *
     */
    public void showTask(Task task);

    /*
     *
     */
    public void informAboutError(String error);

    /*
     *
     */
    public void inform(String info);
}
