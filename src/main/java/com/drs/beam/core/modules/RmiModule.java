/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.drs.beam.core.modules;

import com.drs.beam.core.rmi.interfaces.RmiExecutorInterface;
import com.drs.beam.core.rmi.interfaces.RmiRemoteControlInterface;
import com.drs.beam.core.rmi.interfaces.RmiTaskManagerInterface;

/**
 *
 * @author Diarsid
 */
public interface RmiModule extends Module {
    
    RmiExecutorInterface getRmiExecutorInterface();
    
    RmiTaskManagerInterface getRmiTaskManagerInterface();
    
    RmiRemoteControlInterface getRmiRemoteControlInterface();
    
    void exportInterfaces();
    
    static String getModuleName(){
        return "Rmi Module";
    }    
}
