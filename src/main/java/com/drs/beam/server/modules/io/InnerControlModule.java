/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.drs.beam.server.modules.io;

/**
 *
 * @author Diarsid
 */
public interface InnerControlModule {
    
    void exitAfterAllNotifications();
    
    static String getModuleName(){
        return "Inner Control Module";
    }
}
