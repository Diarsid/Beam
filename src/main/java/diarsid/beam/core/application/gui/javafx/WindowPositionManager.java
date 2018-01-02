/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package diarsid.beam.core.application.gui.javafx;

import static diarsid.beam.core.base.util.JavaFXUtil.screenHeight;
import static diarsid.beam.core.base.util.JavaFXUtil.screenWidth;

/**
 *
 * @author Diarsid
 */
class WindowPositionManager {    
   
    private final double xShift;
    private final double yShift;
    private final double xLimit;
    private final double yLimit;
    
    private final Object windowCounterLock;
    private int activeWindowsCounter;
    
    private double lastWindowX;
    private double lastWindowY;
    
    WindowPositionManager() {
        this.activeWindowsCounter = 0;  
        this.windowCounterLock = new Object();
        double screenHeight = screenHeight();
        double screenWidth = screenWidth();
        this.xShift = screenWidth / 36;
        this.yShift = screenHeight / 36;
        this.xLimit = screenWidth * 0.8;
        this.yLimit = screenHeight * 0.8;
    }
    
    public WindowPosition getNewWindowPosition() {
        synchronized ( this.windowCounterLock ) {
            this.activeWindowsCounter++;
            return this.determineNewWindowPosition();
        }
    }
    
    private WindowPosition determineNewWindowPosition() {
        // If this is the first window, it should be placed in the screen center.
        // Position 0,0 means that window using receiving this position will not 
        // actually use this coordinates and will be placed in the center by default.
        if ( this.activeWindowsCounter == 1 ) {
            return new WindowPosition(0, 0); 
        } else { 
            double newX = this.lastWindowX + this.xShift;
            double newY = this.lastWindowY + this.yShift;
            // if x or y coordinates of new window are to close to the  
            // right bottom screen corner, new windows begin appearing in the 
            // top left corner in point 100:100.
            if ( (newX > this.xLimit) || (newY > this.yLimit) ) {
                newX = 100.0;
                newY = 100.0;
            }
            return new WindowPosition(newX, newY);
        }
    }
    
    boolean hasNoActiveWindows() {
        synchronized ( this.windowCounterLock ) {
            return this.activeWindowsCounter == 0;
        }
    }
    
    public void reportLastWindowPosition(double x, double y){
        synchronized ( this.windowCounterLock ) {
            this.lastWindowX = x;
            this.lastWindowY = y;
        }        
    }
    
    public void notifyWindowClosed() {
        synchronized ( this.windowCounterLock ) {
            this.activeWindowsCounter--;
            this.checkIfClearLastPosition();
        }
    }
    
    private void checkIfClearLastPosition() {
        if ( this.activeWindowsCounter == 0 ) {
            this.lastWindowX = 0;
            this.lastWindowY = 0;
        } 
    }
}
