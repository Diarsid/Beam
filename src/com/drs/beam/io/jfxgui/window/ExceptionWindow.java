/*
 * project: Beam
 * author: Diarsid
 */

package com.drs.beam.io.jfxgui.window;

/*
 * Window for showing exceptions.
 */
public class ExceptionWindow implements Window{
    // Fields ==================================================================
    private final Exception exc;
    
    // Constructors ============================================================
    public ExceptionWindow(Exception exc){    
        this.exc = exc;        
    }

    // Methods =================================================================
    @Override
    public void run() {
        
    }
}
