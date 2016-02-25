/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package diarsid.beam.core.modules.executor.os;

import diarsid.beam.shared.modules.ConfigModule;

import diarsid.beam.core.modules.executor.OS;

import diarsid.beam.core.exceptions.ModuleInitializationException;

import diarsid.beam.core.modules.IoInnerModule;

import diarsid.beam.core.modules.executor.IntelligentResolver;

/**
 *
 * @author Diarsid
 */
public interface OSProvider {
    
    static OS getOS(IoInnerModule io, ConfigModule configModule){
        String systemName = System.getProperty("os.name").toLowerCase();
        if (systemName.contains("win")){
            return new OSWindowsv2(io, configModule);
        } else if (systemName.contains("x")) {
            // Program does not have OSUnix implementation for working under this OS.
            // Terminates program
            io.reportErrorAndExitLater(
                    "Program does not have OS *nix implementation yet.",
                    "Program will be closed.");
            throw new ModuleInitializationException();
        } else {
            // Some error occured or there is unknown OS.
            // Terminates program
            io.reportErrorAndExitLater(
                    "Unsupported or unknown OS.", 
                    "Program will be closed.");
            throw new ModuleInitializationException();
        }     
    }
}
