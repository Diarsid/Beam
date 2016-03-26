/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package diarsid.beam.core;

import java.util.HashSet;
import java.util.Set;

import diarsid.beam.core.modules.DataModule;
import diarsid.beam.core.modules.ExecutorModule;
import diarsid.beam.core.modules.HandlerManagerModule;
import diarsid.beam.core.modules.IoInnerModule;
import diarsid.beam.core.modules.IoModule;
import diarsid.beam.core.modules.RmiModule;
import diarsid.beam.core.modules.TaskManagerModule;
import diarsid.beam.core.modules.WebModule;
import diarsid.beam.shared.modules.ConfigModule;

import com.drs.gem.injector.core.Declaration;
import com.drs.gem.injector.core.GemModuleDeclaration;
import com.drs.gem.injector.core.GemModuleType;

/**
 *
 * @author Diarsid
 */
class BeamModulesDeclaration implements Declaration {
    
    BeamModulesDeclaration() {
    }
    
    @Override
    public Set<GemModuleDeclaration> getDeclaredModules(){
        Set<GemModuleDeclaration> modules = new HashSet<>();
        
        modules.add(new GemModuleDeclaration(
                ConfigModule.class.getCanonicalName(), 
                "diarsid.beam.shared.modules.config.ConfigModuleWorker",
                GemModuleType.SINGLETON));
        
        modules.add(new GemModuleDeclaration(
                DataModule.class.getCanonicalName(), 
                "diarsid.beam.core.modules.data.DataModuleWorker",
                GemModuleType.SINGLETON));
        
        modules.add(new GemModuleDeclaration(
                ExecutorModule.class.getCanonicalName(), 
                "diarsid.beam.core.modules.executor.ExecutorModuleWorker",
                GemModuleType.SINGLETON));
        
        modules.add(new GemModuleDeclaration(
                IoModule.class.getCanonicalName(), 
                "diarsid.beam.core.modules.io.IoModuleWorker",
                GemModuleType.SINGLETON));
        
        modules.add(new GemModuleDeclaration(
                IoInnerModule.class.getCanonicalName(), 
                "diarsid.beam.core.modules.innerio.IoInnerModuleWorker",
                GemModuleType.SINGLETON));
        
        modules.add(new GemModuleDeclaration(
                RmiModule.class.getCanonicalName(), 
                "diarsid.beam.core.modules.rmi.RmiModuleWorker",
                GemModuleType.SINGLETON));
        
        modules.add(new GemModuleDeclaration(
                TaskManagerModule.class.getCanonicalName(), 
                "diarsid.beam.core.modules.tasks.TaskManagerModuleWorker",
                GemModuleType.SINGLETON));
        
        modules.add(new GemModuleDeclaration(
                WebModule.class.getCanonicalName(), 
                "diarsid.beam.core.modules.web.WebModuleWorker", 
                GemModuleType.SINGLETON));
        
        modules.add(new GemModuleDeclaration(
                HandlerManagerModule.class.getCanonicalName(), 
                "diarsid.beam.core.modules.handlers.HandlerManager", 
                GemModuleType.SINGLETON));
        
        return modules;
    }
}
