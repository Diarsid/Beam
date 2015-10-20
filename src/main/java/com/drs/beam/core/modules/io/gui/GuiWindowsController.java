/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package com.drs.beam.core.modules.io.gui;

import com.drs.beam.core.Beam;

/**
 *
 * @author Diarsid
 */
public class GuiWindowsController {
    // Fields =============================================================================
    
    private boolean exitAfterAllWindowsClosed;
    private int activeWindowsCounter;
    
    // Constructors =======================================================================

    public GuiWindowsController() {
        this.exitAfterAllWindowsClosed = false;
        this.activeWindowsCounter = 0;
    }
    
    // Methods ============================================================================

    void setExitAfterAllWindowsClosed(){
        this.exitAfterAllWindowsClosed = true;
    }
    
    public void plusOneActiveWindow(){
        this.activeWindowsCounter++;
    }
    
    public void minusOneActiveWindow(){
        this.activeWindowsCounter--;
        this.checkForExit();
    }
    
    boolean hasActiveWindows(){
        return (this.activeWindowsCounter != 0);
    }
    
    private void checkForExit(){
        if (this.exitAfterAllWindowsClosed == true 
                && this.activeWindowsCounter == 0){
            Beam.exitServerNow();
        }
    }
}
