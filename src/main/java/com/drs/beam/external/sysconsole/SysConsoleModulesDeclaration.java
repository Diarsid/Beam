/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package com.drs.beam.external.sysconsole;

import java.util.HashSet;
import java.util.Set;

import com.drs.beam.external.sysconsole.modules.BeamCoreAccessModule;
import com.drs.beam.external.sysconsole.modules.ConsoleDispatcherModule;
import com.drs.beam.external.sysconsole.modules.ConsoleListenerModule;
import com.drs.beam.external.sysconsole.modules.ConsolePrinterModule;
import com.drs.beam.external.sysconsole.modules.ConsoleReaderModule;
import com.drs.beam.external.sysconsole.modules.RmiConsoleManagerModule;
import com.drs.beam.shared.modules.ConfigModule;
import com.drs.gem.injector.core.Declaration;
import com.drs.gem.injector.core.GemModuleDeclaration;
import com.drs.gem.injector.core.GemModuleType;

/**
 *
 * @author Diarsid
 */
class SysConsoleModulesDeclaration implements Declaration {
    
    SysConsoleModulesDeclaration() {
    }
    
    @Override
    public Set<GemModuleDeclaration> getDeclaredModules() {
        
        Set<GemModuleDeclaration> modules = new HashSet<>();
        
        modules.add(new GemModuleDeclaration(
                ConfigModule.class.getCanonicalName(), 
                "com.drs.beam.shared.modules.config.ConfigModuleWorker",
                GemModuleType.SINGLETON));
        
        modules.add(new GemModuleDeclaration(
                BeamCoreAccessModule.class.getCanonicalName(), 
                "com.drs.beam.external.sysconsole.modules.workers.BeamCoreAccess",
                GemModuleType.SINGLETON));
        
        modules.add(new GemModuleDeclaration(
                ConsolePrinterModule.class.getCanonicalName(), 
                "com.drs.beam.external.sysconsole.modules.workers.ConsolePrinter",
                GemModuleType.SINGLETON));
        
        modules.add(new GemModuleDeclaration(
                ConsoleReaderModule.class.getCanonicalName(), 
                "com.drs.beam.external.sysconsole.modules.workers.ConsoleReader",
                GemModuleType.SINGLETON));
        
        modules.add(new GemModuleDeclaration(
                ConsoleListenerModule.class.getCanonicalName(), 
                "com.drs.beam.external.sysconsole.modules.workers.ConsoleListener",
                GemModuleType.SINGLETON));
        
        modules.add(new GemModuleDeclaration(
                ConsoleDispatcherModule.class.getCanonicalName(), 
                "com.drs.beam.external.sysconsole.modules.workers.ConsoleDispatcher",
                GemModuleType.SINGLETON));
        
        modules.add(new GemModuleDeclaration(
                RmiConsoleManagerModule.class.getCanonicalName(), 
                "com.drs.beam.external.sysconsole.modules.workers.RmiConsoleManager",
                GemModuleType.SINGLETON));
        
        return modules;
    }
}
