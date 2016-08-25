/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package diarsid.beam.core.modules.executor.os.actions;

import diarsid.beam.core.modules.IoInnerModule;

/**
 *
 * @author Diarsid
 */
abstract class AbstractRunnableAction implements Runnable {
    
    private final String executableArgument;
    private final IoInnerModule ioEngine;
    
    protected AbstractRunnableAction(IoInnerModule ioEngine, String executableArgument) {
        this.executableArgument = executableArgument;
        this.ioEngine = ioEngine;
    }
    
    protected IoInnerModule getIo() {
        return this.ioEngine;
    }
    
    protected String getArgument() {
        return this.executableArgument;
    }
}
