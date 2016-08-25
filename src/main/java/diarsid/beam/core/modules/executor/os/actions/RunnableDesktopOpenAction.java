/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package diarsid.beam.core.modules.executor.os.actions;

import java.awt.Desktop;
import java.io.File;
import java.io.IOException;

import diarsid.beam.core.modules.IoInnerModule;

/**
 *
 * @author Diarsid
 */
public class RunnableDesktopOpenAction extends AbstractRunnableAction {

    public RunnableDesktopOpenAction(IoInnerModule ioEngine, String target) {
        super(ioEngine, target);
    }
    
    @Override
    public void run() {
        try {
            Desktop.getDesktop().open(new File(super.getArgument()));            
        } catch (IOException e) {
            super.getIo().reportException(e, "Run task with Desktop -> IOException: given path may be invalid.");
        } catch (IllegalArgumentException argumentException) {
            super.getIo().reportError("Unknown target");
        }
    }
}
