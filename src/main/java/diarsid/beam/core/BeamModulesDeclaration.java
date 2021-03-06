/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package diarsid.beam.core;

import java.util.HashSet;
import java.util.Set;

import diarsid.beam.core.modules.BeamEnvironmentModule;
import diarsid.beam.core.modules.ControlModule;
import diarsid.beam.core.modules.DataModule;
import diarsid.beam.core.modules.DomainKeeperModule;
import diarsid.beam.core.modules.ExecutorModule;
import diarsid.beam.core.modules.IoModule;
import diarsid.beam.core.modules.PluginsLoaderModule;
import diarsid.beam.core.modules.RemoteManagerModule;
import diarsid.beam.core.modules.ResponsiveDataModule;
import diarsid.beam.core.modules.TasksWatcherModule;
import diarsid.beam.core.modules.WebModule;
import diarsid.support.configuration.Configuration;

import com.drs.gem.injector.core.Declaration;
import com.drs.gem.injector.core.GemModuleDeclaration;
import com.drs.gem.injector.core.GemModuleType;

/**
 *
 * @author Diarsid
 */
class BeamModulesDeclaration implements Declaration {
    
    private final Configuration configuration;
    
    BeamModulesDeclaration(Configuration configuration) {
        this.configuration = configuration;
    }
    
    @Override
    public Set<GemModuleDeclaration> getDeclaredModules() {
        Set<GemModuleDeclaration> modules = new HashSet<>();
        
        modules.add(new GemModuleDeclaration(
                BeamEnvironmentModule.class.getCanonicalName(), 
                "diarsid.beam.core.modules.beamenvironment.BeamEnvironmentModuleWorker",
                GemModuleType.SINGLETON));
        
        modules.add(new GemModuleDeclaration(
                DataModule.class.getCanonicalName(), 
                "diarsid.beam.core.modules.data.DataModuleWorker",
                GemModuleType.SINGLETON));
        
        modules.add(new GemModuleDeclaration(
                DomainKeeperModule.class.getCanonicalName(), 
                "diarsid.beam.core.modules.domainkeeper.DomainKeeperModuleWorker",
                GemModuleType.SINGLETON));
        
        modules.add(new GemModuleDeclaration(
                TasksWatcherModule.class.getCanonicalName(), 
                "diarsid.beam.core.modules.taskswatcher.TasksWatcherModuleWorker",
                GemModuleType.SINGLETON));
        
        modules.add(new GemModuleDeclaration(
                WebModule.class.getCanonicalName(), 
                "diarsid.beam.core.modules.web.WebModuleWorker",
                GemModuleType.SINGLETON));
        
        modules.add(new GemModuleDeclaration(
                IoModule.class.getCanonicalName(), 
                "diarsid.beam.core.modules.io.IoModuleWorker",
                GemModuleType.SINGLETON));
        
        modules.add(new GemModuleDeclaration(
                ResponsiveDataModule.class.getCanonicalName(), 
                "diarsid.beam.core.modules.responsivedata.ResponsiveDataModuleWorker",
                GemModuleType.SINGLETON));
        
        modules.add(new GemModuleDeclaration(
                ControlModule.class.getCanonicalName(), 
                "diarsid.beam.core.modules.control.ControlModuleWorker",
                GemModuleType.SINGLETON));
        
        if ( this.configuration.asBoolean("rmi.core.active") ) {
            modules.add(new GemModuleDeclaration(
                    RemoteManagerModule.class.getCanonicalName(), 
                    "diarsid.beam.core.modules.remotemanager.RemoteManagerModuleWorker",
                    GemModuleType.SINGLETON));
        }
        
        modules.add(new GemModuleDeclaration(
                ExecutorModule.class.getCanonicalName(), 
                "diarsid.beam.core.modules.executor.ExecutorModuleWorker",
                GemModuleType.SINGLETON));
        
        modules.add(new GemModuleDeclaration(
                PluginsLoaderModule.class.getCanonicalName(), 
                "diarsid.beam.core.modules.pluginsloader.PluginsLoaderModuleWorker",
                GemModuleType.SINGLETON));
        
        return modules;
    }
}
