/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package diarsid.beam.core.modules.pluginsloader;

import java.util.HashSet;
import java.util.Set;

import diarsid.beam.core.base.control.plugins.Plugin;
import diarsid.beam.core.modules.PluginsLoaderModule;

import static java.util.Arrays.asList;


class PluginsLoaderModuleWorker implements PluginsLoaderModule {
    
    private final Set<Plugin> plugins;

    public PluginsLoaderModuleWorker(Plugin... embeddedPlugins) {
        this.plugins = new HashSet<>();
        this.plugins.addAll(asList(embeddedPlugins));
    }

    @Override
    public Set<Plugin> plugins() {
        return this.plugins;
    }
    
}
