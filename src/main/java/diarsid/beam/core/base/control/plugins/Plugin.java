/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package diarsid.beam.core.base.control.plugins;

import java.util.List;

import diarsid.beam.core.base.control.io.base.actors.Initiator;
import diarsid.beam.core.base.control.io.base.actors.InnerIoEngine;
import diarsid.beam.core.base.control.io.commands.executor.PluginTaskCommand;

/**
 *
 * @author Diarsid
 */
public abstract class Plugin {
    
    private final String name;
    private final InnerIoEngine ioEngine;
    
    protected Plugin(InnerIoEngine ioEngine) {
        this.name = this.name();
        this.ioEngine = ioEngine;
    }
    
    public final InnerIoEngine ioEngine() {
        return this.ioEngine;
    }
    
    public abstract String name();
    
    public abstract void process(Initiator initiator, PluginTaskCommand command);
    
    public boolean isPluginCommand(String command) {
        return false;
    }
    
    public boolean isPluginCommandFirstArg(String arg) {
        return false;
    }
    
    public boolean isPluginCommandArgs(List<String> commandArgs) {
        return false;
    }
}
