/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package diarsid.beam.core.modules;

import java.util.Set;

import diarsid.beam.core.base.control.plugins.Plugin;

import com.drs.gem.injector.module.GemModule;

/**
 *
 * @author Diarsid
 */
public interface PluginsLoaderModule extends GemModule {
    
    Set<Plugin> plugins();
}
