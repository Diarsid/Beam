/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package com.drs.beam.core.modules.executor.os;

import java.io.IOException;

import com.drs.beam.core.modules.InnerIOModule;

/**
 *
 * @author Diarsid
 */
class RunnableRuntimeCommandTask implements Runnable {
    
    private final String executableCommand;
    private final InnerIOModule ioEngine;

    RunnableRuntimeCommandTask(InnerIOModule ioEngine, String executableCommand) {
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
