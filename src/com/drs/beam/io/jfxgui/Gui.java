/*
 * project: Beam
 * author: Diarsid
 */
package com.drs.beam.io.jfxgui;

import com.drs.beam.tasks.Task;

/*
 * 'Native' program`s output interface.
 * Is implemented in GuiEngine class using JavaFX based GUI technology.
 */
public interface Gui extends Runnable{
    public void showTask(Task task);
    public void showMessage(String message); 
    public void showException(Exception e);
}
