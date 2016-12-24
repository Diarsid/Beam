/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package diarsid.beam.core;

import java.util.HashSet;
import java.util.Set;

import old.diarsid.beam.core.modules.DataModule;
import old.diarsid.beam.core.modules.ExecutorModule;
import old.diarsid.beam.core.modules.IoInnerModule;
import old.diarsid.beam.core.modules.RmiModule;
import old.diarsid.beam.core.modules.TaskManagerModule;
import old.diarsid.beam.core.modules.WebModule;

import diarsid.beam.core.modules.ConfigModule;

import com.drs.gem.injector.core.Declaration;
import com.drs.gem.injector.core.GemModuleDeclaration;
import com.drs.gem.injector.core.GemModuleType;

import old.diarsid.beam.core.modules.OldIoModule;

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
                OldIoModule.class.getCanonicalName(), 
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
        
        return modules;
    }
}
