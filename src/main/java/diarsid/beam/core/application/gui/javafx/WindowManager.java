/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package diarsid.beam.core.application.gui.javafx;


import java.util.PriorityQueue;
import java.util.Queue;

import static diarsid.beam.core.Beam.beamRuntime;

/**
 *
 * @author Diarsid
 */
public class WindowManager {
    
    private final BeamHiddenRoot beamHiddenRootWindow;
    private final GuiJavaFXResources resources;
    private final WindowPositionManager windowPositionManager;
    private final Queue<BeamPopupWindow> popupWindows;
    private final Queue<BeamTaskWindow> taskWindows;
    private final Queue<BeamTaskListWindow> taskListWindows;
    
    private boolean exitAfterAllWindowsClosed;

    WindowManager(
            BeamHiddenRoot beamHiddenRootWindow,
            WindowPositionManager windowPositionManager, 
            GuiJavaFXResources resources) {
        this.beamHiddenRootWindow = beamHiddenRootWindow;
        this.windowPositionManager = windowPositionManager;
        this.resources = resources;
        this.exitAfterAllWindowsClosed = false;
        this.popupWindows = new PriorityQueue<>();
        this.taskWindows = new PriorityQueue<>();
        this.taskListWindows = new PriorityQueue<>();
    }
    
    BeamHiddenRoot hiddenRoot() {
        return this.beamHiddenRootWindow;
    }
    
    WindowPosition getNewWindowPosition() {
        return this.windowPositionManager.getNewWindowPosition();
    }

    void setExitAfterAllWindowsClosed() {
        this.exitAfterAllWindowsClosed = true;
    }
    
    public void windowClosed(BeamWindow beamWindow) {
        this.windowPositionManager.notifyWindowClosed();
        this.accept(beamWindow);
        this.checkForExit();
    }
    
    public void reportLastWindowPosition(double x, double y) {
        this.windowPositionManager.reportLastWindowPosition(x, y);
    }
    
    private void checkForExit() {
        if ( this.exitAfterAllWindowsClosed && this.windowPositionManager.hasNoActiveWindows() ) {
            beamRuntime().exitBeamCoreNow();
        }
    }
    
    BeamPopupWindow getBeamPopupWindow() {
        BeamPopupWindow window;
        synchronized ( this.popupWindows ) {            
            if ( this.popupWindows.isEmpty() ) {
                window = new BeamPopupWindow(this, resources);
            } else {
                window = this.popupWindows.poll();
            }
        }
        return window;
    }
    
    BeamTaskWindow getBeamTaskWindow() {
        BeamTaskWindow window;
        synchronized ( this.taskWindows ) {            
            if ( this.taskWindows.isEmpty() ) {
                window = new BeamTaskWindow(this, resources);
            } else {
                window = this.taskWindows.poll();
            }
        }
        return window;
    }
    
    BeamTaskListWindow getBeamTaskListWindow() {
        BeamTaskListWindow window;
        synchronized ( this.taskListWindows ) {            
            if ( this.taskListWindows.isEmpty() ) {
                window = new BeamTaskListWindow(this, resources);
            } else {
                window = this.taskListWindows.poll();
            }
        }
        return window;
    }
    
    
    public void accept(BeamWindow beamWindow) {
        if ( beamWindow instanceof BeamTaskWindow ) {
            synchronized ( this.taskWindows ) {
                this.taskWindows.offer((BeamTaskWindow) beamWindow);
            }
        } else if ( beamWindow instanceof BeamPopupWindow ) {
            synchronized ( this.popupWindows ) {
                this.popupWindows.offer((BeamPopupWindow) beamWindow);
            }
        } else if ( beamWindow instanceof BeamTaskListWindow ) {
            synchronized ( this.taskListWindows ) {
                this.taskListWindows.offer((BeamTaskListWindow) beamWindow);
            }
        }
    }
}
