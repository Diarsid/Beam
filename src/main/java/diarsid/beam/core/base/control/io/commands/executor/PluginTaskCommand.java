/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package diarsid.beam.core.base.control.io.commands.executor;

import java.util.Objects;

import diarsid.beam.core.base.control.io.commands.Command;
import diarsid.beam.core.base.control.io.commands.CommandType;

import static diarsid.beam.core.base.control.io.commands.CommandType.PLUGIN_TASK;

/**
 *
 * @author Diarsid
 */
public class PluginTaskCommand implements Command {
    
    private final String pluginName;
    private final String argument;
    
    public PluginTaskCommand(String pluginName, String argument) {
        this.pluginName = pluginName;
        this.argument = argument;
    }
    
    public String pluginName() {
        return this.pluginName;
    }
    
    public String argument() {
        return this.argument;
    }

    @Override
    public CommandType type() {
        return PLUGIN_TASK;
    }

    @Override
    public int hashCode() {
        int hash = 3;
        hash = 47 * hash + Objects.hashCode(this.pluginName);
        hash = 47 * hash + Objects.hashCode(this.argument);
        return hash;
    }

    @Override
    public boolean equals(Object obj) {
        if ( this == obj ) {
            return true;
        }
        if ( obj == null ) {
            return false;
        }
        if ( getClass() != obj.getClass() ) {
            return false;
        }
        final PluginTaskCommand other = ( PluginTaskCommand ) obj;
        if ( !Objects.equals(this.pluginName, other.pluginName) ) {
            return false;
        }
        if ( !Objects.equals(this.argument, other.argument) ) {
            return false;
        }
        return true;
    }
}
