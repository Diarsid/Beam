package com.drs.beam.io;

import com.drs.beam.tasks.Task;

/**
 * Org by Diarsid
 * Time: 13:15 - 25.02.15
 * IDE: IntelliJ IDEA 12
 */

public interface InnerIOIF {
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
