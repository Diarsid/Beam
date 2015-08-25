/*
 * project: Beam
 * author: Diarsid
 */
package com.drs.beam.modules.io.gui;

import com.drs.beam.modules.io.gui.jfx.GuiJFX;
import com.drs.beam.modules.io.gui.swing.GuiSwing;
import com.drs.beam.modules.tasks.Task;
import com.drs.beam.util.config.ConfigContainer;
import com.drs.beam.util.config.ConfigParam;

/*
 * 'Native' program`s output interface.
 *
 * Used to output messages from program through native output mechanism when external UI 
 * (e.g. console)is unavailable or output configuration was set not to use it.
 *
 * Is implemented with JavaFX or Swing technology which is choosed from config.xml.
 */
public interface Gui{
    public final String IMAGES_LOCATION = ConfigContainer.getParam(ConfigParam.IMAGES_LOCATION);
    
    /*
     * Method used to show user`s tasks.
     */
    public void showTask(Task task);
    
    /*
     * Used to show program`s messages, typically errors, warnings or notifications.
     * Param isCritical defines whether program should be closed after this event.
     */
    public void showMessage(String message, boolean isCritical); 
    
    /*
     * Used to show program`s occured exceptions.
     * Param isCritical defines whether program should be closed after this event.
     */
    public void showException(Exception e, boolean isCritical);
    
    /*
     * Returns Gui object with different implementations to perform GUI output.
     * 
     * Uses ConfigContainer to determine which gui implementation should be used. If it is 
     * impossible to determine gui platform, program should be closed.
     */
    public static Gui getGui(){
        switch(ConfigContainer.getParam(ConfigParam.GUI_PLATFORM).toLowerCase()) {
            case "javafx" : {
                GuiJFX fx = new GuiJFX();
                new Thread(fx, "JavaFX Application Thread").start();
                return fx;
            }
            case "swing" : {
                GuiSwing swing = new GuiSwing();
                return swing;
            }
            default : {
                System.out.println("Invalid GUI Platform settings.");
                System.exit(1);
                return null;
            }
        }        
    }
}
