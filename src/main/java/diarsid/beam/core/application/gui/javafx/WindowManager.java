/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package diarsid.beam.core.application.gui.javafx;


import java.util.PriorityQueue;
import java.util.Queue;

import static diarsid.beam.core.Beam.exitBeamCoreNow;
import static diarsid.beam.core.base.util.Logs.debug;

/**
 *
 * @author Diarsid
 */
public class WindowManager {
    
    private final BeamHiddenRootWindow beamHiddenRootWindow;
    private final BeamControlWindow beamControlWindow;
    private final GuiJavaFXResources resources;
    private final WindowPositionManager windowPositionManager;
    private final Queue<BeamPopupWindow> popupWindows;
    private final Queue<BeamTaskWindow> taskWindows;
    private final Queue<BeamTaskListWindow> taskListWindows;
    
    private boolean exitAfterAllWindowsClosed;

    WindowManager(WindowPositionManager windowPositionManager, GuiJavaFXResources resources) {
        this.beamHiddenRootWindow = new BeamHiddenRootWindow();
        this.beamControlWindow = new BeamControlWindow(this.beamHiddenRootWindow, resources);
        this.windowPositionManager = windowPositionManager;
        this.resources = resources;
        this.exitAfterAllWindowsClosed = false;
        this.popupWindows = new PriorityQueue<>();
        this.taskWindows = new PriorityQueue<>();
        this.taskListWindows = new PriorityQueue<>();
    }
    
    BeamHiddenRootWindow hiddenRootWindow() {
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
            exitBeamCoreNow();
        }
    }
    
    BeamPopupWindow getBeamPopupWindow() {
        BeamPopupWindow window;
        synchronized ( this.popupWindows ) {            
            if ( this.popupWindows.isEmpty() ) {
                debug("[GUI] [POPUP WINDOWS] new!");
                window = new BeamPopupWindow(this, resources);
            } else {
                debug("[GUI] [POPUP WINDOWS] reuse!");
                window = this.popupWindows.poll();
            }
        }
        return window;
    }
    
    BeamTaskWindow getBeamTaskWindow() {
        BeamTaskWindow window;
        synchronized ( this.taskWindows ) {            
            if ( this.taskWindows.isEmpty() ) {
                debug("[GUI] [TASK WINDOWS] new!");
                window = new BeamTaskWindow(this, resources);
            } else {
                debug("[GUI] [TASK WINDOWS] reuse!");
                window = this.taskWindows.poll();
            }
        }
        return window;
    }
    
    BeamTaskListWindow getBeamTaskListWindow() {
        BeamTaskListWindow window;
        synchronized ( this.taskListWindows ) {            
            if ( this.taskListWindows.isEmpty() ) {
                debug("[GUI] [TASK LIST WINDOWS] new!");
                window = new BeamTaskListWindow(this, resources);
            } else {
                debug("[GUI] [TASK LIST WINDOWS] reuse!");
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
