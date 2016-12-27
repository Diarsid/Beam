/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package diarsid.beam.core;

import java.util.HashSet;
import java.util.Set;

import diarsid.beam.core.modules.ConfigHolderModule;
import diarsid.beam.core.modules.CoreControlModule;
import diarsid.beam.core.modules.CoreRemoteManagerModule;
import diarsid.beam.core.modules.IoModule;

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
    public Set<GemModuleDeclaration> getDeclaredModules() {
        Set<GemModuleDeclaration> modules = new HashSet<>();
        
        modules.add(new GemModuleDeclaration(
                ConfigHolderModule.class.getCanonicalName(), 
                "diarsid.beam.core.modules.config.ConfigHolderModuleWorker",
                GemModuleType.SINGLETON));
        
        modules.add(new GemModuleDeclaration(
                IoModule.class.getCanonicalName(), 
                "diarsid.beam.core.modules.io.IoModuleWorker",
                GemModuleType.SINGLETON));
        
        modules.add(new GemModuleDeclaration(
                CoreControlModule.class.getCanonicalName(), 
                "diarsid.beam.core.modules.corecontrol.CoreControlModuleWorker",
                GemModuleType.SINGLETON));
        
        modules.add(new GemModuleDeclaration(
                CoreRemoteManagerModule.class.getCanonicalName(), 
                "diarsid.beam.core.modules.remotemanager.CoreRemoteManagerModuleWorker",
                GemModuleType.SINGLETON));
        
        return modules;
    }
}
