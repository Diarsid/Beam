/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package com.drs.beam.core;

import java.util.HashSet;
import java.util.Set;

import com.drs.beam.core.modules.ConfigModule;
import com.drs.beam.core.modules.DataModule;
import com.drs.beam.core.modules.ExecutorModule;
import com.drs.beam.core.modules.IoInnerModule;
import com.drs.beam.core.modules.IoModule;
import com.drs.beam.core.modules.RmiModule;
import com.drs.beam.core.modules.TaskManagerModule;
import com.drs.gem.injector.core.Declaration;
import com.drs.gem.injector.core.ModuleDeclaration;
import com.drs.gem.injector.core.ModuleType;

/**
 *
 * @author Diarsid
 */
class BeamModulesDeclaration implements Declaration {
    
    BeamModulesDeclaration() {
    }
    
    @Override
    public Set<ModuleDeclaration> getDeclaredModules(){
        Set<ModuleDeclaration> modules = new HashSet<>();
        
        modules.add(new ModuleDeclaration(
                ConfigModule.class.getCanonicalName(), 
                "com.drs.beam.core.modules.config.ConfigModuleWorker",
                ModuleType.SINGLETON));
        
        modules.add(new ModuleDeclaration(
                DataModule.class.getCanonicalName(), 
                "com.drs.beam.core.modules.data.DataModuleWorker",
                ModuleType.SINGLETON));
        
        modules.add(new ModuleDeclaration(
                ExecutorModule.class.getCanonicalName(), 
                "com.drs.beam.core.modules.executor.ExecutorModuleWorker",
                ModuleType.SINGLETON));
        
        modules.add(new ModuleDeclaration(
                IoModule.class.getCanonicalName(), 
                "com.drs.beam.core.modules.io.IoModuleWorker",
                ModuleType.SINGLETON));
        
        modules.add(new ModuleDeclaration(
                IoInnerModule.class.getCanonicalName(), 
                "com.drs.beam.core.modules.innerio.IoInnerModuleWorker",
                ModuleType.SINGLETON));
        
        modules.add(new ModuleDeclaration(
                RmiModule.class.getCanonicalName(), 
                "com.drs.beam.core.modules.rmi.RmiModuleWorker",
                ModuleType.SINGLETON));
        
        modules.add(new ModuleDeclaration(
                TaskManagerModule.class.getCanonicalName(), 
                "com.drs.beam.core.modules.tasks.TaskManagerModuleWorker",
                ModuleType.SINGLETON));
        
        return modules;
    }
}
