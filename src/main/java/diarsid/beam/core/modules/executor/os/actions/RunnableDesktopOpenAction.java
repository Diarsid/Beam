/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package diarsid.beam.core.modules.executor.os.actions;

import java.awt.Desktop;
import java.io.File;
import java.io.IOException;

import old.diarsid.beam.core.modules.IoInnerModule;

import diarsid.beam.core.util.Logs;

import static diarsid.beam.core.util.Logs.logError;

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
            logError(this.getClass(), "Exception during Desktop.open() with " + super.getArgument(), e);
            super.getIo().reportException(e, "Run task with Desktop -> IOException: given path may be invalid.");
        } catch (IllegalArgumentException argumentException) {
            Logs.logError(this.getClass(), super.getArgument(), argumentException);
            super.getIo().reportError("Unknown target");
        }
    }
}
