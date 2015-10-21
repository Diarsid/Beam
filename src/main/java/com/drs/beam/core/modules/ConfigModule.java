/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.drs.beam.core.modules;

import com.drs.beam.util.config.ConfigParam;

/**
 *
 * @author Diarsid
 */
public interface ConfigModule extends Module{
    
    void parseStartArgumentsIntoConfiguration(String[] args);
    
    String getParameter(ConfigParam param);
    
    static String getModuleName(){
        return "Config Module";
    }
}
