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
public class PluginPrefixesRecognizer extends NodeRecognizer {
    
    private final Set<Plugin> plugins;
    
    PluginPrefixesRecognizer() {
        this.plugins = new HashSet<>();
    }
    
    public boolean install(Plugin newPlugin) {
        boolean canBeInstalled = this.isPluginWithThisPrefixNotInstalled(newPlugin);
        if ( canBeInstalled ) {
            this.plugins.add(newPlugin);
        }
        return canBeInstalled;
    }

    private boolean isPluginWithThisPrefixNotInstalled(Plugin newPlugin) {
        return ! this.plugins
                .stream()
                .filter(plugin -> plugin.equalsByPrefix(newPlugin))
                .findFirst()
                .isPresent();
    }
    
    @Override
    public Command assess(Input input) {
        if ( input.hasNotRecognizedArgs() ) {
            Optional<Plugin> foundPlugin = this.plugins
                    .stream()
                    .filter(plugin -> input.currentArg().startsWith(plugin.prefix()))
                    .findFirst();
            if ( foundPlugin.isPresent() ) {
                input.removePrefixFromCurrentArg(foundPlugin.get().prefix());
                return pluginCommandOf(input, foundPlugin.get());
            } else {
                return undefinedCommand();
            }
        } else {
            return undefinedCommand();
        }
    }
    
    private PluginTaskCommand pluginCommandOf(Input input, Plugin plugin) {
        return new PluginTaskCommand(plugin.name(), input.allRemainingArgsString());
    }
}
