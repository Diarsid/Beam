/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package diarsid.beam.core.modules.executor.os;

import java.io.IOException;

import diarsid.beam.core.modules.IoInnerModule;

/**
 *
 * @author Diarsid
 */
class RunnableRuntimeCommandTask implements Runnable {
    
    private final String executableCommand;
    private final IoInnerModule ioEngine;

    RunnableRuntimeCommandTask(IoInnerModule ioEngine, String executableCommand) {
        this.executableCommand = executableCommand;
        this.ioEngine = ioEngine;
    }
    
    @Override
    public void run() {
        try {
            Runtime.getRuntime().exec(executableCommand);
        } catch (IOException e) {
            this.ioEngine.reportException(e, "IOException: open file with program.");
        } catch (IllegalArgumentException argumentException) {
            this.ioEngine.reportError("Unknown target.");
        }
    }
}
