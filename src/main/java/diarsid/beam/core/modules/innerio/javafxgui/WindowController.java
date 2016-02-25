/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package diarsid.beam.core.modules.innerio.javafxgui;

import java.util.Deque;
import java.util.LinkedList;

import javafx.geometry.Rectangle2D;
import javafx.stage.Screen;

import diarsid.beam.core.Beam;

/**
 *
 * @author Diarsid
 */
public class WindowController {    
   
    private final double xCenter;
    private final double yCenter;
    private final double xShift;
    private final double yShift;
    private final double xLimit;
    private final double yLimit;
    
    private final Object windowCounterLock;
    
    private final Deque<WindowPosition> positionStack;
    
    private boolean exitAfterAllWindowsClosed;
    private int activeWindowsCounter;
    
    private double lastWindowX;
    private double lastWindowY;
    
    WindowController() {
        this.exitAfterAllWindowsClosed = false;
        this.activeWindowsCounter = 0;
        this.positionStack = new LinkedList<>();        
        this.windowCounterLock = new Object();
        Rectangle2D screen = Screen.getPrimary().getVisualBounds();
        this.xCenter = screen.getWidth() / 2;
        this.yCenter = screen.getHeight() / 2;
        this.xShift = screen.getWidth() / 36;
        this.yShift = screen.getHeight() / 36;
        this.xLimit = screen.getWidth() * 0.8;
        this.yLimit = screen.getHeight() * 0.8;
    }

    void setExitAfterAllWindowsClosed() {
        this.exitAfterAllWindowsClosed = true;
    }
    
    public WindowPosition getNewWindowPosition() {
        synchronized (windowCounterLock) {
            this.activeWindowsCounter++;
            return determineNewWindowPosition();
        }
    }
    
    private WindowPosition determineNewWindowPosition() {
        // If this is the first window, it should be placed in the screen center.
        // Position 0,0 means that window using receiving this position will not 
        // actually use this coordinates and will be placed in the center by default.
        if ( this.activeWindowsCounter == 1 ) {
            return new WindowPosition(0, 0); 
        } else { 
            double newX = lastWindowX + xShift;
            double newY = lastWindowY + yShift;
            // if x or y coordinates of new window are to close to the  
            // right bottom screen corner, new windows begin appearing in the 
            // top left corner in point 100:100.
            if ( (newX > xLimit) || (newY > yLimit) ) {
                newX = 100.0;
                newY = 100.0;
            }
            return new WindowPosition(newX, newY);
        }
    }
    
    public void windowClosed() {
        synchronized (windowCounterLock) {
            this.activeWindowsCounter--;
            this.checkForExit();
            this.checkIfClearLastPosition();
        }
    }
    
    private void checkForExit() {
        if ( this.exitAfterAllWindowsClosed == true 
                && this.activeWindowsCounter == 0 ) {
            Beam.exitBeamCoreNow();
        }
    }
    
    private void checkIfClearLastPosition() {
        if ( this.activeWindowsCounter == 0 ) {
            lastWindowX = 0;
            lastWindowY = 0;
        } 
    }
    
    public void reportLastWindowPosition(double x, double y){
        synchronized (windowCounterLock) {
            this.lastWindowX = x;
            this.lastWindowY = y;
        }        
    }
}
