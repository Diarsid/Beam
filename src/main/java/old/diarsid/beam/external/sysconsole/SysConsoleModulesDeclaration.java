/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package old.diarsid.beam.external.sysconsole;

import java.util.HashSet;
import java.util.Set;

import old.diarsid.beam.external.sysconsole.modules.BeamCoreAccessModule;
import old.diarsid.beam.external.sysconsole.modules.ConsoleDispatcherModule;
import old.diarsid.beam.external.sysconsole.modules.ConsoleListenerModule;
import old.diarsid.beam.external.sysconsole.modules.ConsolePrinterModule;
import old.diarsid.beam.external.sysconsole.modules.ConsoleReaderModule;
import old.diarsid.beam.external.sysconsole.modules.RmiConsoleManagerModule;

import com.drs.gem.injector.core.Declaration;
import com.drs.gem.injector.core.GemModuleDeclaration;
import com.drs.gem.injector.core.GemModuleType;

import diarsid.beam.core.modules.ConfigHolderModule;

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
                ConfigHolderModule.class.getCanonicalName(), 
                "diarsid.beam.shared.modules.config.ConfigModuleWorker",
                GemModuleType.SINGLETON));
        
        modules.add(new GemModuleDeclaration(
                BeamCoreAccessModule.class.getCanonicalName(), 
                "diarsid.beam.external.sysconsole.modules.workers.BeamCoreAccess",
                GemModuleType.SINGLETON));
        
        modules.add(new GemModuleDeclaration(
                ConsolePrinterModule.class.getCanonicalName(), 
                "diarsid.beam.external.sysconsole.modules.workers.ConsolePrinter",
                GemModuleType.SINGLETON));
        
        modules.add(new GemModuleDeclaration(
                ConsoleReaderModule.class.getCanonicalName(), 
                "diarsid.beam.external.sysconsole.modules.workers.ConsoleReader",
                GemModuleType.SINGLETON));
        
        modules.add(new GemModuleDeclaration(
                ConsoleListenerModule.class.getCanonicalName(), 
                "diarsid.beam.external.sysconsole.modules.workers.ConsoleListener",
                GemModuleType.SINGLETON));
        
        modules.add(new GemModuleDeclaration(
                ConsoleDispatcherModule.class.getCanonicalName(), 
                "diarsid.beam.external.sysconsole.modules.workers.ConsoleDispatcher",
                GemModuleType.SINGLETON));
        
        modules.add(new GemModuleDeclaration(
                RmiConsoleManagerModule.class.getCanonicalName(), 
                "diarsid.beam.external.sysconsole.modules.workers.RmiConsoleManager",
                GemModuleType.SINGLETON));
        
        return modules;
    }
}
