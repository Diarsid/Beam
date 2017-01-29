/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package old.diarsid.beam.core.os.actions;

import java.io.IOException;

import old.diarsid.beam.core.modules.IoInnerModule;

/**
 *
 * @author Diarsid
 */
public class RunnableRuntimeCommandAction extends AbstractRunnableAction {
    
    public RunnableRuntimeCommandAction(IoInnerModule ioEngine, String executableCommand) {
        super(ioEngine, executableCommand);
    }
    
    @Override
    public void run() {
        try {
            Runtime.getRuntime().exec(super.getArgument());
        } catch (IOException e) {
            super.getIo().reportException(e, "IOException: open file with program.");
        } catch (IllegalArgumentException argumentException) {
            super.getIo().reportError("Unknown target.");
        }
    }
}
