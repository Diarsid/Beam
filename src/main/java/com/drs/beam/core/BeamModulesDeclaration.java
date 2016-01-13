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
                "com.drs.beam.core.modules.config.ConfigModuleWorker",
                GemModuleType.SINGLETON));
        
        modules.add(new GemModuleDeclaration(
                DataModule.class.getCanonicalName(), 
                "com.drs.beam.core.modules.data.DataModuleWorker",
                GemModuleType.SINGLETON));
        
        modules.add(new GemModuleDeclaration(
                ExecutorModule.class.getCanonicalName(), 
                "com.drs.beam.core.modules.executor.ExecutorModuleWorker",
                GemModuleType.SINGLETON));
        
        modules.add(new GemModuleDeclaration(
                IoModule.class.getCanonicalName(), 
                "com.drs.beam.core.modules.io.IoModuleWorker",
                GemModuleType.SINGLETON));
        
        modules.add(new GemModuleDeclaration(
                IoInnerModule.class.getCanonicalName(), 
                "com.drs.beam.core.modules.innerio.IoInnerModuleWorker",
                GemModuleType.SINGLETON));
        
        modules.add(new GemModuleDeclaration(
                RmiModule.class.getCanonicalName(), 
                "com.drs.beam.core.modules.rmi.RmiModuleWorker",
                GemModuleType.SINGLETON));
        
        modules.add(new GemModuleDeclaration(
                TaskManagerModule.class.getCanonicalName(), 
                "com.drs.beam.core.modules.tasks.TaskManagerModuleWorker",
                GemModuleType.SINGLETON));
        
        return modules;
    }
}
