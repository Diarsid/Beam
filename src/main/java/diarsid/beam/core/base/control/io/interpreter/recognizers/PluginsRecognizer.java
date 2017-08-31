/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package diarsid.beam.core.base.control.io.interpreter.recognizers;

import java.util.HashSet;
import java.util.Optional;
import java.util.Set;

import diarsid.beam.core.base.control.io.commands.Command;
import diarsid.beam.core.base.control.io.commands.executor.PluginTaskCommand;
import diarsid.beam.core.base.control.io.interpreter.Input;
import diarsid.beam.core.base.control.io.interpreter.NodeRecognizer;
import diarsid.beam.core.base.control.plugins.Plugin;

import static diarsid.beam.core.base.control.io.commands.EmptyCommand.undefinedCommand;

/**
 *
 * @author Diarsid
 */
public class PluginsRecognizer extends NodeRecognizer {
    
    private final Set<Plugin> plugins;
    
    PluginsRecognizer() {
        this.plugins = new HashSet<>();
    }
    
    public void install(Plugin newPlugin) {
        this.plugins.add(newPlugin);
//        boolean canBeInstalled = this.isPluginWithThisPrefixNotInstalled(newPlugin);
//        if ( canBeInstalled ) {
//            
//        }
//        return canBeInstalled;
    }

//    private boolean isPluginWithThisPrefixNotInstalled(Plugin newPlugin) {
//        return ! this.plugins
//                .stream()
//                .filter(plugin -> plugin.equalsByPrefix(newPlugin))
//                .findFirst()
//                .isPresent();
//    }
    
    @Override
    public Command assess(Input input) {
        if ( input.hasNotRecognizedArgs() ) {
            Optional<Plugin> foundPlugin = this.plugins
                    .stream()
                    .filter(plugin -> this.isPluginApplicableToInput(plugin, input))
                    .findFirst();
            if ( foundPlugin.isPresent() ) {
                return pluginCommandOf(input, foundPlugin.get());
            } else {
                return undefinedCommand();
            }
        } else {
            return undefinedCommand();
        }
    }
    
    private boolean isPluginApplicableToInput(Plugin plugin, Input input) {
        return 
                plugin.isPluginCommandFirstArg(input.currentArg()) ||
                plugin.isPluginCommandArgs(input.allRemainingArgs()) ||
                plugin.isPluginCommand(input.allRemainingArgsString());
    }
    
    private PluginTaskCommand pluginCommandOf(Input input, Plugin plugin) {
        return new PluginTaskCommand(plugin.name(), input.allRemainingArgsString());
    }
}
