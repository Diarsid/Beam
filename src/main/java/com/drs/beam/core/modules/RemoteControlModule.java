/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.drs.beam.core.modules;

import com.drs.beam.external.ExternalIOInterface;
import com.drs.beam.core.modules.Module;

/**
 *
 * @author Diarsid
 */
public interface RemoteControlModule extends Module{
    
    boolean hasExternalIOProcessor();
    void acceptNewIOProcessor(ExternalIOInterface externalIOEngine);
    void useExternalShowTaskMethod();
    void useNativeShowTaskMethod();
    void exitBeamServer();
    void setDefaultIO();    
    
    static String getModuleName(){
        return "Remote Control Module";
    }
}
